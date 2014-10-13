package net.es.netshell.classloader;

import net.es.netshell.api.PersistentObject;

import java.util.List;

/**
 * Created by lomax on 10/13/14.
 */
public class DefaultJars extends PersistentObject {
    public final static String CONFIG_FILE = "/etc/default-jars";

    private List<String> jars;

    public static final DefaultJars instance() {
        try {
            PersistentObject obj = PersistentObject.newObject(CONFIG_FILE);
            return (DefaultJars) obj;
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getJars() {
        return jars;
    }

    public void setJars(List<String> jars) {
        this.jars = jars;
    }
}
