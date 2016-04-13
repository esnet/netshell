/*
 * ESnet Network Operating System (ENOS) Copyright (c) 2015, The Regents
 * of the University of California, through Lawrence Berkeley National
 * Laboratory (subject to receipt of any required approvals from the
 * U.S. Dept. of Energy).  All rights reserved.
 *
 * If you have questions about your rights to use or distribute this
 * software, please contact Berkeley Lab's Innovation & Partnerships
 * Office at IPO@lbl.gov.
 *
 * NOTICE.  This Software was developed under funding from the
 * U.S. Department of Energy and the U.S. Government consequently retains
 * certain rights. As such, the U.S. Government has been granted for
 * itself and others acting on its behalf a paid-up, nonexclusive,
 * irrevocable, worldwide license in the Software to reproduce,
 * distribute copies to the public, prepare derivative works, and perform
 * publicly and display publicly, and to permit other to do so.
 */
package net.es.netshell.mongodb;

import java.io.IOException;
import java.util.*;


import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.*;
import net.es.netshell.api.DataBase;
import net.es.netshell.api.PersistentObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.ServerAddress;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoCollection;
import net.es.netshell.api.Resource;
import net.es.netshell.api.ResourceAnchor;
import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.users.User;
import org.bson.Document;

import javax.management.RuntimeErrorException;


/**
 * This class implement support for MongoDB.
 */
public final class MongoDBProvider implements DataBase {
    private MongoDatabase db;
    private MongoClient client;
    private String mongoUser;
    private String password;
    private ServerAddress serverAddress;
    private HashMap<String,MongoCollection>  collections = new HashMap<String,MongoCollection>();

    public MongoDBProvider(MongoDatabase db) {
        this.db = db;
    }

    public MongoDBProvider(String host, int port, String dbName, String mongoUser, String password) {
        this.serverAddress = new ServerAddress(host,port);
        ArrayList<MongoCredential> creds = new ArrayList<MongoCredential>();
        MongoCredential enosCred = MongoCredential.createCredential(mongoUser,dbName,password.toCharArray());
        creds.add(enosCred);
        this.client = new MongoClient(this.serverAddress,creds);
        this.db = client.getDatabase(dbName);
        System.out.println("Connected to MongoDB.");
    }

    public final MongoClient getClient()
    {
        if (!KernelThread.currentKernelThread().isPrivileged()) {
            throw new SecurityException("not authorized");
        }
        return this.client;
    }

    private synchronized MongoCollection getCollection(String user, String collection) {
        String collectionName = user + "_" + collection;
        if (this.collections.containsKey(collectionName)) {
            return this.collections.get(collectionName);
        }
        MongoCollection mongoCollection = this.db.getCollection(collectionName);
        if (mongoCollection != null) {
            this.collections.put(collectionName,mongoCollection);
        }
        return mongoCollection;
    }

    public final MongoDatabase getDatabase() {
        if (!KernelThread.currentKernelThread().isPrivileged()) {
            throw new SecurityException("not authorized");
        }
        return this.db;
    }

    @Override
    public final void store(List<ResourceAnchor> anchors) throws IOException {
        if (!KernelThread.currentKernelThread().isPrivileged()) {
            throw new SecurityException("not authorized");
        }
        HashMap<String,ArrayList<WriteModel>>  collectionRequests = new HashMap<String,ArrayList<WriteModel>>();
        // Build the bulk requests per collection
        for (ResourceAnchor anchor : anchors) {
            String user = anchor.getContainerOwner();
            String collection = anchor.getContainerName();
            String collectionName = user + "_" + collection;
            ArrayList<WriteModel> requests;
            if (!collectionRequests.containsKey(collectionName)) {
                requests = new ArrayList<WriteModel>();
                collectionRequests.put(collectionName,requests);
            } else {
                requests = collectionRequests.get(collectionName);
            }
            try {
                // Likely to be in the Resource cache. Otherwise replace by itself.
                Resource resource = Resource.findByName(user, collection, anchor.getResourceName());
                Document doc = Document.parse(resource.saveToJSON());
                Document query = new Document("eid",resource.getEid());
;
                ReplaceOneModel<Document> request = new ReplaceOneModel<Document>(query,doc);
                request.getOptions().upsert(true);
                requests.add(request);
            } catch (InstantiationException e) {
                throw new IOException(e);
            }
        }
        // Bulk write the collection's request
        for (Map.Entry<String,ArrayList<WriteModel>> entry : collectionRequests.entrySet()) {
            String[] name = entry.getKey().split("_");
            ArrayList<WriteModel> requests = entry.getValue();
            MongoCollection mongoCollection = this.getCollection(name[0], name[1]);
            if (mongoCollection == null) {
                throw new RuntimeErrorException(new Error("Could not store into collection " + entry.getKey()));
            }
            BulkWriteOptions options = new BulkWriteOptions();
            options.ordered(false);
            mongoCollection.bulkWrite(requests,options);
        }
    }

    @Override
    public final void store(String user, String collection, PersistentObject obj) throws IOException {
        String collectionName = user + "_" + collection;
        if (!KernelThread.currentKernelThread().isPrivileged()) {
            throw new SecurityException("not authorized");
        }
        MongoCollection mongoCollection = this.db.getCollection(collectionName);
        if (mongoCollection == null) {
            throw new RuntimeErrorException(new Error("Could not store into collection " + collectionName));
        }
        Document doc = Document.parse(obj.saveToJSON());
        Document query = new Document("eid",obj.getEid());
        UpdateOptions options = new UpdateOptions();
        options.upsert(true);
        mongoCollection.replaceOne(query, doc, options);
    }

    @Override
    public void delete(String user, String name, PersistentObject obj) throws InstantiationException {
        String collectionName = user + "_" + name;
        if (!KernelThread.currentKernelThread().isPrivileged()) {
            throw new SecurityException("not authorized");
        }
        MongoCollection mongoCollection = this.db.getCollection(collectionName);
        if (mongoCollection == null) {
            throw new RuntimeErrorException(new Error("Could not store into collection " + collectionName));
        }
        Document query = new Document("eid",obj.getEid());
        mongoCollection.deleteMany(query);
    }

    @Override
    public final void delete(List<ResourceAnchor> anchors) throws IOException {
        if (!KernelThread.currentKernelThread().isPrivileged()) {
            throw new SecurityException("not authorized");
        }
        HashMap<String,ArrayList<WriteModel>>  collectionRequests = new HashMap<String,ArrayList<WriteModel>>();
        // Build the bulk requests per collection
        for (ResourceAnchor anchor : anchors) {
            String user = anchor.getContainerOwner();
            String collection = anchor.getContainerName();
            String collectionName = user + "_" + collection;
            ArrayList<WriteModel> requests;
            if (!collectionRequests.containsKey(collectionName)) {
                requests = new ArrayList<WriteModel>();
                collectionRequests.put(collectionName,requests);
            } else {
                requests = collectionRequests.get(collectionName);
            }
            Document query = new Document("eid",anchor.getEid());
            DeleteOneModel request = new DeleteOneModel(query);;
            requests.add(request);
        }
        // Bulk delete the collection's request
        for (Map.Entry<String,ArrayList<WriteModel>> entry : collectionRequests.entrySet()) {
            String[] name = entry.getKey().split("_");
            ArrayList<WriteModel> requests = entry.getValue();
            MongoCollection mongoCollection = this.getCollection(name[0], name[1]);
            if (mongoCollection == null) {
                throw new RuntimeErrorException(new Error("Could not delete into collection " + entry.getKey()));
            }
            BulkWriteOptions options = new BulkWriteOptions();
            options.ordered(false);
            mongoCollection.bulkWrite(requests,options);
        }
    }


    @Override
    public final void createCollection(String user, String name) {
        String collectionName = user + "_" + name;
        if (!KernelThread.currentKernelThread().isPrivileged()) {
            throw new SecurityException("not authorized");
        }
        this.db.createCollection(collectionName);
        MongoCollection collection = this.db.getCollection(collectionName);
        if (collection == null) {
            throw new RuntimeErrorException(new Error("Could not create collection " + name));
        }
    }

    @Override
    public final void deleteCollection(String user,String name) {
        String collectionName = user + "_" + name;
        if (!KernelThread.currentKernelThread().isPrivileged()) {
            throw new SecurityException("not authorized");
        }
        MongoCollection collection = this.db.getCollection(collectionName);
        if (collection == null) {
            throw new RuntimeErrorException(new Error("Could not delete collection " + name));
        }
        collection.drop();
    }

    @Override
    public final List<String> find(String user,
                                   String name,
                                   Map<String, Object> query) throws InstantiationException {
        String collectionName = user + "_" + name;
        MongoCollection collection = this.db.getCollection(collectionName);
        if (collection == null) {
            throw new RuntimeErrorException(new Error("Could not create collection " + collectionName));
        }
        ArrayList<String> res = new ArrayList<String>();
        FindIterable<Document> results = null;

        if (query == null) {
            results = collection.find();
        } else {
            Document queryDocument = new Document(query);
            results = collection.find(queryDocument);
            for (Document doc : results) {
                doc.remove("_id");
                res.add(doc.toJson());
            }
        }
        return res;
    }

    @Override
    public final boolean collectionExists(String user, String name) {
        String collectionName = user + "_" + name;
        MongoIterable<String> names = db.listCollectionNames();
        for (String n : names) {
            if (n.equals(collectionName)) {
                return true;
            }
        }
        return false;
    }

}
