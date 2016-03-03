package net.es.netshell.mongodb;

import java.io.IOException;
import java.util.*;


import com.mongodb.client.FindIterable;
import net.es.netshell.api.DataBase;
import net.es.netshell.api.PersistentObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.ServerAddress;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import javax.management.RuntimeErrorException;


/**
 * This class implement support for MongoDB.
 */
public class MongoDBProvider implements DataBase {
    private MongoDatabase db;
    private MongoClient client;
    private String user;
    private String password;
    private ServerAddress serverAddress;

    public MongoDBProvider(String host, int port, String dbName, String user, String password) {

        System.out.println("host= " + host + " port=" + port + "user= " + user + " password= " + password + "db= " + dbName);
        this.serverAddress = new ServerAddress(host,port);
        ArrayList<MongoCredential> creds = new ArrayList<MongoCredential>();
        MongoCredential enosCred = MongoCredential.createCredential(user,dbName,password.toCharArray());
        creds.add(enosCred);
        this.client = new MongoClient(this.serverAddress,creds);
        this.db = client.getDatabase(dbName);
        System.out.println("Connected to MongoDB.");
    }

    public MongoClient getClient() {
        return this.client;
    }

    public MongoDatabase getDatabase() {
        return this.db;
    }

    @Override
    public void store (String collectionName, PersistentObject obj) throws IOException {
        MongoCollection collection = this.db.getCollection(collectionName);
        if (collection == null) {
            throw new RuntimeErrorException(new Error("Could not store into collection " + collectionName));
        }
        Document doc = Document.parse(obj.saveToJSON());
        Document query = new Document("eid",obj.getEid());
        FindIterable<Document> res = collection.find(query);
        for (Document item : res) {
            // The object already exists. Replace it.
            collection.findOneAndReplace(query, doc);
            return;
        }
        collection.insertOne(doc);
    }

    @Override
    public void createCollection(String name) {
        this.db.createCollection(name);
        MongoCollection collection = this.db.getCollection(name);
        if (collection == null) {
            throw new RuntimeErrorException(new Error("Could not create collection " + name));
        }
    }

    @Override
    public void deleteCollection(String name) {
        MongoCollection collection = this.db.getCollection(name);
        if (collection == null) {
            throw new RuntimeErrorException(new Error("Could not delete collection " + name));
        }
        collection.drop();
    }

    @Override
    public List<String> find(String collectionName, Map<String, Object> query) throws InstantiationException {
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
}
