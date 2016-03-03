package net.es.netshell.api;

import java.io.IOException;
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
    public void store (String collection, PersistentObject obj) throws IOException;

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
    public List<String> find(String collection, Map<String, Object> query) throws InstantiationException;
}
