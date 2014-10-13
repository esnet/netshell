package net.es.netshell.classloader;

import org.xeustechnologies.jcl.JarClassLoader;

import java.lang.ClassLoader;
import java.util.List;


/**
 * Created by lomax on 10/10/14.
 */
public class DynamicClassLoader extends JarClassLoader {
    private ClassLoader parent;

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

    private void init () {
        this.parent = this.getClass().getClassLoader();
    }



}
