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
package net.es.netshell.api;

import net.es.netshell.kernel.users.User;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This interface must be implemented by all ENOS Databases
 */
public interface DataBase {

    /**
     * Create a collection into the database. Implementation of this method must be idempotent.
     * @param user user owning the collection to create
     * @param collectionName of the collection to create.
     */
    void createCollection(String user, String collectionName);

    /**
     * Delete a collection.
     * @param user user owning the collection to delete
     * @param collectionName name of the collection to delete
     */
    void deleteCollection(String user, String collectionName);

    boolean collectionExists(String user, String collectionName);

    /**
     * Stores a PersistentObject into a collection
     * @param user user owning the collection to create
     * @param collectionName of the collection to create.
     * @throws IOException
     */
    void store(String user, String collectionName, PersistentObject obj) throws IOException;


    /**
     * Finds docunments within a collection. The query is expressed in JSON. If query is null, then all documents of the collection are returned.
     * The query language is identical to MongoDB queries.
     * @param user user owning the collection
     * @param collectionName name of the collection where to perform the search
     * @param query a Map of key/value pairs.
     * @throws InstantiationException
     * @return
     */
    List<String> find(String user,
                      String collectionName,
                      Map<String, Object> query) throws InstantiationException;


    /**
     * Delete a PersistentObject from a collection.
     * Attemting to delete a non-existant object will silently fail
     * @param user user owning the collection
     * @param collectionName name of the collection where the object to delete is.
     * @param obj name of the object to delete
     * @throws InstantiationException
     */
    void delete(String user, String collectionName, PersistentObject obj) throws InstantiationException;


}
