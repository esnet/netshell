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
import net.es.netshell.api.DataBase;
import net.es.netshell.api.PersistentObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.ServerAddress;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoCollection;
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
    private String user;
    private String password;
    private ServerAddress serverAddress;

    public MongoDBProvider(String host, int port, String dbName, String user, String password) {
        this.serverAddress = new ServerAddress(host,port);
        ArrayList<MongoCredential> creds = new ArrayList<MongoCredential>();
        MongoCredential enosCred = MongoCredential.createCredential(user,dbName,password.toCharArray());
        creds.add(enosCred);
        this.client = new MongoClient(this.serverAddress,creds);
        this.db = client.getDatabase(dbName);
        System.out.println("Connected to MongoDB.");
    }

    public final MongoClient getClient()
    {
        if (!KernelThread.currentKernelThread().getUser().isPrivileged()) {
            throw new SecurityException("not authorized");
        }
        return this.client;
    }

    public final MongoDatabase getDatabase() {
        if (!KernelThread.currentKernelThread().getUser().isPrivileged()) {
            throw new SecurityException("not authorized");
        }
        return this.db;
    }

    @Override
    public final void store(String user, String collection, PersistentObject obj) throws IOException {
        String collectionName = user + "_" + collection;
        if (!KernelThread.currentKernelThread().getUser().isPrivileged()) {
            throw new SecurityException("not authorized");
        }
        MongoCollection mongoCollection = this.db.getCollection(collectionName);
        if (mongoCollection == null) {
            throw new RuntimeErrorException(new Error("Could not store into collection " + collectionName));
        }
        Document doc = Document.parse(obj.saveToJSON());
        Document query = new Document("eid",obj.getEid());
        FindIterable<Document> res = mongoCollection.find(query);

        for (Document item : res) {
            // The object already exists. Replace it.
            mongoCollection.findOneAndReplace(query, doc);
            return;
        }
        mongoCollection.insertOne(doc);
    }

    @Override
    public void delete(String user, String name, PersistentObject obj) throws InstantiationException {
        String collectionName = user + "_" + name;
        if (!KernelThread.currentKernelThread().getUser().isPrivileged()) {
            throw new SecurityException("not authorized");
        }
        MongoCollection mongoCollection = this.db.getCollection(collectionName);
        if (mongoCollection == null) {
            throw new RuntimeErrorException(new Error("Could not store into collection " + collectionName));
        }
        Document query = new Document("resourceName",obj.getResourceClassName());
        mongoCollection.deleteMany(query);
    }

    @Override
    public final void createCollection(String user, String name) {
        String collectionName = user + "_" + name;
        if (!KernelThread.currentKernelThread().getUser().isPrivileged()) {
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
        if (!KernelThread.currentKernelThread().getUser().isPrivileged()) {
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
