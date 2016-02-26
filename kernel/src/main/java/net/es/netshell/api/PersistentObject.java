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
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lomax on 6/24/14.
 */
public class PersistentObject implements Serializable {

    @JsonIgnore
    private boolean isNewInstance = true;
    @JsonIgnore
    private File file;
    private String resourceClassName;

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

    public boolean exists() {
        if (this.file == null) {
            return false;
        }
        return this.file.exists();
    }

    public static boolean exists(String name) {
        File f = buildFile(name);
        if (f == null) {
            return false;
        }
        return f.exists();
    }

    /**
     * Returns the object in JSON format.
     * @return
     * @throws IOException
     */
    public String toJSON () throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
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
    public final void save(String filename) throws IOException {
        this.file = PersistentObject.buildFile(ResourceUtils.normalizeResourceName(filename));
        this.save(file);
    }

    /**
     * Saves the resource into the provided File
     * @param file
     * @throws IOException
     */
    private void save(File file) throws IOException {
        this.file = file;
        // Set the classname.
        this.resourceClassName = this.getClass().getCanonicalName();
        /* Make sure all directories exist */
        file.getParentFile().mkdirs();
        /* Write JSON */
        ObjectMapper mapper = new ObjectMapper();
        FileOutputStream output = new FileOutputStream(file);
        mapper.writeValue(output, this);
        output.flush();
        output.close();
        // No longer a new resource.
        this.isNewInstance = false;
    }

    public void delete() {
        if (this.file != null) {
            file.delete();
        }
    }
    /**
     * Creates a resource from a file specified by the provided file name. NetShell root is added
     * to the file name if the filename is absolute.
     * @param c
     * @param filename
     * @return
     * @throws IOException
     * @throws InstantiationException
     */
    public static final PersistentObject newObject (Class c, String filename) throws IOException, InstantiationException {
        File file = PersistentObject.buildFile(filename);
        if ( ! file.exists() ) {
            // This is a new resource.
            PersistentObject obj = PersistentObject.newObject(c);
            obj.isNewInstance = true;
            return obj;
        } else {
            ObjectMapper mapper = new ObjectMapper();
            FileInputStream input = new FileInputStream(file);
            PersistentObject obj = (PersistentObject) mapper.readValue(input, c);
            obj.isNewInstance = false;
            obj.file = file;
            return obj;
        }
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

    /**
     * Returns true if the resource did not have persistent store file. False if the resource was loaded from
     * an existing file.
     * @return whether the resource was loaded or not from a file.
     */
    @JsonIgnore
    public boolean isNewInstance() {
        return isNewInstance;
    }

    public static final PersistentObject newObject (Class c) throws InstantiationException {
        PersistentObject obj = null;
        try {
            obj = (PersistentObject) Class.forName(c.getName()).newInstance();
            obj.isNewInstance = true;
            return obj;
        } catch (IllegalAccessException e) {
            throw new InstantiationException(e.toString());
        } catch (ClassNotFoundException e) {
            throw new InstantiationException(e.toString());
        }
    }

    public static final PersistentObject newObject (String fileName) throws InstantiationException {
        PersistentObject obj = null;
        try {
            String className = PersistentObject.getClassName(fileName);
            if (className == null) {
                // File does not exist.
                return null;
            }
            return PersistentObject.newObject(Class.forName(className),fileName);

        } catch (ClassNotFoundException e) {
            throw new InstantiationException(e.toString());
        } catch (IOException e) {
            throw new InstantiationException(e.toString());
        }
    }

    public void setNewInstance(boolean isNewInstance) {
        this.isNewInstance = isNewInstance;
    }

    public String getResourceClassName() {
        return resourceClassName;
    }

    public void setResourceClassName(String resourceClassName) {
        this.resourceClassName = resourceClassName;
    }

    @JsonIgnore
    public File getFile() {
        return this.file;
    }

    @JsonIgnore
    public String getFileName() {
        if (this.file != null) {
            return this.file.getName();
        } else {
            return null;
        }
    }

    @JsonIgnore
    public  List<PersistentObject> getObjects(String directory, Class filteredClass) throws IOException {
        File directoryFile = PersistentObject.buildFile(directory);
        if ( ! directoryFile.exists() || ! directoryFile.isDirectory()) {
            return null;
        }
        ArrayList<PersistentObject> objects = new ArrayList<PersistentObject>();
        for (File file : directoryFile.listFiles()) {
            if (PersistentObject.getClassName(file.getPath()).equals(filteredClass.getCanonicalName())) {
                try {
                    objects.add(PersistentObject.newObject(filteredClass, file.getPath()));
                } catch (InstantiationException e) {
                    continue;
                }
            }
        }
        return objects;
    }
}
