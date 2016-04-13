package net.es.netshell.mongodb;

import com.mongodb.Block;
import com.mongodb.Function;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.UpdateOptions;
import net.es.netshell.api.Node;
import net.es.netshell.api.PersistentObject;
import net.es.netshell.api.Resource;
import net.es.netshell.boot.BootStrap;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by lomax on 4/12/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class MongoDBProviderTest {

    @Mock
    MongoDatabase database;
    @Mock
    MongoCollection collection;

    BootStrap bootStrap;
    MongoDBProvider provider;


    @Before
    public void setUp() {
        provider = new MongoDBProvider(database);
        when(database.getName()).thenReturn("test_db");
        when(database.getCollection("admin_collection")).thenReturn(collection);
        when(database.getCollection("admin_none")).thenReturn(null);
        BootStrap.setSingleton(new BootStrap());
        BootStrap.getBootStrap().setDataBase(provider);
    }

    @After
    public void tearDown() {
        database = null;
        collection = null;
        provider = null;
    }

    @Test
    public void testGetClient() throws Exception {

    }

    @Test
    public void testGetDatabase() throws Exception {
        MongoDatabase db = provider.getDatabase();
        assertEquals(db.getName(), "test_db");
    }

    @Test
    public void testSimpleStore() throws Exception {
        Resource resource = new Resource("test");
        provider.store("admin","collection",resource);
        // Check db saved it.
        verify(collection,atLeastOnce()).replaceOne(any(Document.class), any(Document.class), any(UpdateOptions.class));
    }

    @Test
    public void testDupResourceStore() throws Exception {
        Resource resource1 = new Resource("test");
        Resource resource2 = new Resource("test");
        provider.store("admin","collection",resource1);
    }

    @Test
    public void testStore1() throws Exception {

    }

    @Test
    public void testDelete() throws Exception {

    }

    @Test
    public void testDelete1() throws Exception {

    }

    @Test
    public void testCreateCollection() throws Exception {

    }

    @Test
    public void testDeleteCollection() throws Exception {

    }

    @Test
    public void testFind() throws Exception {

    }

}