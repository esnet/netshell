package net.es.netshell.mongodb;

import java.util.ArrayList;

import net.es.netshell.api.DataBase;
import net.es.netshell.api.PersistentObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.ServerAddress;
import com.mongodb.MongoCredential;



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
    public void store(String collection, PersistentObject obj) {

    }

    @Override
    public PersistentObject load(String collection, String name) {
        return null;
    }

    @Override
    public void createCollection(String name) {

    }

    @Override
    public void deleteCollection(String name) {

    }

    @Override
    public Iterable<PersistentObject> find(String collection, String query) {
        return null;
    }
}
