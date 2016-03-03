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

import net.es.netshell.boot.BootStrap;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class implements the base class of any NetShell resources that need to be persistent. It originally
 * was designe to use a local file system for storing the serialized objects, including a translation in the
 * host file system name space.
 *
 * 3/2016 lomax@es.net There is an ongoing effort to add or replace the backend store with a database, MongoDB. It is expected that
 * this class will change in the coming weeks.
 */
public class PersistentObject implements Serializable {

    private String resourceClassName = this.getClass().getCanonicalName();
    private String eid = this.eid = UUID.randomUUID().toString();

    public String getResourceClassName() {
        return resourceClassName;
    }

    public String getEid() {
        return this.eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public void setResourceClassName(String resourceClassName) {
        this.resourceClassName = resourceClassName;
    }

    /**
     * Builds the correct pathname of a file, taking into account the NETSHELL_ROOT and the NetShell user
     * current directory
     */
    public static File buildFile(String filename) {
        File file = null;
        filename = ResourceUtils.normalizeResourceName(filename);
        if (BootStrap.rootPath == null) {
            // Not yet initialized. Assume non NetShell path
            file = new File(filename);
        } else {
            file = new File(FileUtils.toRealPath(filename).toString());
        }
        return file;
    }

    /**
     * Returns the object in JSON format.
     * @return
     * @throws IOException
     */
    public String toJSON (OutputStream output) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(output, this);
        String res = output.toString();
        output.flush();
        output.close();
        return res;
    }

    /**
     * Save the resource in a file specified by the provided file name. NetShell root is added
     * to the file name if the filename is absolute.
     * @param filename
     * @throws java.io.IOException
     */
    public void saveToFile(String filename) throws IOException {
        File file = PersistentObject.buildFile(ResourceUtils.normalizeResourceName(filename));
        /* Make sure all directories exist */
        file.getParentFile().mkdirs();
        FileOutputStream output = new FileOutputStream(file);
        this.toJSON(output);
        output.flush();
        output.close();
    }

    /**
     * Save the resource in a database collection.
     * @param collection
     * @throws java.io.IOException
     */
    public void saveToDatabase(String collection) throws IOException {
        DataBase db = BootStrap.getBootStrap().getDataBase();
        db.store(collection, this);
    }

    /**
     * Save the resource to string containing the JSON representation of the object.
     * @throws java.io.IOException
     */
    public String saveToJSON() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        this.toJSON(output);
        output.flush();
        output.close();
        return output.toString();
    }

    private static final String getClassName (String filename) throws IOException {
        // Without loading the file, retrieve the className
        File file = PersistentObject.buildFile(filename);
        if (! file.exists()) {
            return null;
        }
        BufferedReader br = null;

        br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            String[] items = line.split(",");
            for (String item : items) {
                int pos = item.indexOf("resourceClassName");
                if (pos > 0) {
                    String[] values = item.split(":");
                    br.close();
                    return values[1].substring(1,values[1].length() -1);
                }
            }
        }
        br.close();
        return null;
    }

    public static final PersistentObject newObjectFromJSON (String json) throws InstantiationException {

        try {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(json, JsonNode.class);
            String className = node.get("resourceClassName").asText();
            PersistentObject obj = null;
            mapper = new ObjectMapper();
            obj = (PersistentObject) mapper.readValue(json, Class.forName(className));
            return obj;
        } catch (ClassNotFoundException e) {
            throw new InstantiationException(e.toString());
        } catch (IOException e) {
            throw new InstantiationException(e.toString());
        }
    }

    public static final List<PersistentObject> findFromDatabase (String collection, Map<String,Object> query) throws InstantiationException {

        try {
            DataBase db = BootStrap.getBootStrap().getDataBase();
            List<String> res = db.find(collection, query);
            ArrayList<PersistentObject> objects = new ArrayList<PersistentObject>();
            for (String json : res) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readValue(json, JsonNode.class);
                String className = node.get("resourceClassName").asText();
                PersistentObject obj = null;
                mapper = new ObjectMapper();
                obj = (PersistentObject) mapper.readValue(json, Class.forName(className));
                objects.add(obj);
            }
            return objects;
        } catch (ClassNotFoundException e) {
            throw new InstantiationException(e.toString());
        } catch (IOException e) {
            throw new InstantiationException(e.toString());
        }
    }
    public static final PersistentObject newObjectFromFile (String fileName) throws InstantiationException {
        try {
            String className = PersistentObject.getClassName(fileName);
            return PersistentObject.newObjectFromFile(fileName,Class.forName(className));
        } catch (IOException e) {
            throw new InstantiationException(e.toString());
        }  catch (ClassNotFoundException e) {
            throw new InstantiationException(e.toString());
        }
    }
    public static final PersistentObject newObjectFromFile (String fileName, Class c) throws InstantiationException {
        System.out.println("E1 " + fileName);
        File file = PersistentObject.buildFile(fileName);
        if ( ! file.exists() ) {
            throw new InstantiationException(fileName + "file does not exist.");
        }
        try {
            System.out.println("E4");
            ObjectMapper mapper = new ObjectMapper();
            System.out.println("E5");
            FileInputStream input = new FileInputStream(file);
            PersistentObject obj = null;
            System.out.println("E6");
            obj = (PersistentObject) mapper.readValue(input, c);
            System.out.println("E7");
            return obj;
        } catch (IOException e) {
            throw new InstantiationException(e.toString());
        }
    }

}
