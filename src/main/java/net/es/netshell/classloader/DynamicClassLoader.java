package net.es.netshell.classloader;

import net.es.netshell.api.PersistentObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xeustechnologies.jcl.JarClassLoader;

import java.io.IOException;
import java.lang.ClassLoader;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;


/**
 * Created by lomax on 10/10/14.
 */
public class DynamicClassLoader extends JarClassLoader {

    final private Logger logger = LoggerFactory.getLogger(DynamicClassLoader.class);

    public DynamicClassLoader(Object[] sources) {
        super(sources);
        this.init();
    }

    public DynamicClassLoader(List sources) {
        super(sources);
        this.init();
    }

    public DynamicClassLoader() {
        super();
        this.init();
    }


    private void init() {

    }

    public  void initDefault () {
        DefaultJars defaultJars = DefaultJars.instance();
        if (defaultJars == null) {
            // Configuration file does not yet exist. Creates it.
            try {
                defaultJars = (DefaultJars) PersistentObject.newObject(DefaultJars.class);
                try {
                    defaultJars.save(DefaultJars.CONFIG_FILE);
                } catch (IOException e) {
                    logger.error("Cannot create DefaultJars object: " + e.getMessage());
                }
            } catch (InstantiationException e) {
                logger.error("Cannot create DefaultJars object: " + e.getMessage());
            }
        } else {
            if (defaultJars.getJars() != null) {
                for (String jar : defaultJars.getJars()) {
                    logger.info("Loads jar: " + jar);
                }
            }
        }

    }

    @Override
    public Class loadClass(String className) throws ClassNotFoundException {
        return super.loadClass(className);
    }

    @Override
    public Class loadClass(String className, boolean resolveIt) throws ClassNotFoundException {
        return super.loadClass(className, resolveIt);
    }


}
