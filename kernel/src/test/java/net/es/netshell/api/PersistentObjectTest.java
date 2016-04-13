package net.es.netshell.api;

import net.es.netshell.boot.BootStrap;
import net.es.netshell.kernel.exec.KernelThread;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by lomax on 4/12/16.
 */
public class PersistentObjectTest {

    @Test
    public void testGetResourceClassName() throws Exception {
        PersistentObject obj = new PersistentObject();
        assertTrue(obj.getResourceClassName() != null);
        assertTrue(obj.getResourceClassName().equals("net.es.netshell.api.PersistentObject"));
    }

    @Test
    public void testGetEid() throws Exception {
        PersistentObject obj = new PersistentObject();
        assertTrue(obj.getEid() != null);
    }

    @Test
    public void testSetEid() throws Exception {
        PersistentObject obj = new PersistentObject();
        obj.setEid("test");
        assertTrue(obj.getEid() != null);
        assertEquals("test",obj.getEid());
    }

    @Test
    public void testSetResourceClassName() throws Exception {

    }


    @Test
    public void testToJSON() throws Exception {

    }

    @Test
    public void testSaveToFile() throws Exception {

    }

    @Test
    public void testSave() throws Exception {

    }

    @Test
    public void testSave1() throws Exception {

    }

    @Test
    public void testSaveToJSON() throws Exception {

    }

    @Test
    public void testDelete() throws Exception {

    }

    @Test
    public void testDelete1() throws Exception {

    }

    @Test
    public void testNewObjectFromJSON() throws Exception {

    }

    @Test
    public void testFind() throws Exception {

    }

    @Test
    public void testFind1() throws Exception {

    }

}