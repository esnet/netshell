package net.es.netshell.api;

import java.util.List;
import java.util.Map;

/**
 * This interface must be implemented by all ENOS Databases
 */
public interface DataBase {
    /**
     * Stores a PersistentObject into a collection
     * @param collection is the name of the collection where to store the object
     * @param obj is the persistent object
     */
    public void store (String collection, PersistentObject obj);

    /**
     * Load a PersistentObject from a collection
     * @param collection is the name of the collection from where to load the object
     * @param name is the name of the object
     * @return the persistent object
     */
    public PersistentObject load (String collection, String name);

    /**
     * Create a collection into the database. Implementation of this method must be idempotent.
     * @param name of the collection to create.
     */
    public void createCollection(String name);

    /**
     * Delete a collection.
     * @param name name of the collection to delete
     */
    public void deleteCollection(String name);

    /**
     * Finds docunments within a collection. The query is expressed in JSON. If query is null, then all documents of the collection are returned.
     * The query language is identical to MongoDB queries.
     * @param collection
     * @param query a Map of key/value pairs.
     * @return
     */
    public List<PersistentObject> find (String collection, Map<String,Object> query);
}
