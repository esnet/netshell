/*
 * Copyright (c) 2014, Regents of the University of California  All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.es.netshell.python;

import net.es.netshell.boot.BootStrap;
import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.security.ExitSecurityException;
import net.es.netshell.kernel.users.User;
import net.es.netshell.shell.ShellInputStream;;
import net.es.netshell.shell.TabFilteringInputStream;
import net.es.netshell.shell.annotations.ShellCommand;


import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.python.core.PyDictionary;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;
import org.python.util.InteractiveConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Created by lomax on 2/20/14.
 */
public class PythonShell {
    private static final Logger logger = LoggerFactory.getLogger(PythonShell.class);
    private static HashMap<InputStream,PyDictionary> locals = new HashMap<InputStream, PyDictionary>();

    Bundle[] bundles;
    private String[] packageNames;

    /**
     * Modified ClassLoader used to give Jython access to classes made available via OSGi.
     */
    public static class JythonClassLoader extends ClassLoader {
        public JythonClassLoader(ClassLoader parent) {
            super(parent);
        }
    }

    /**
     * Modified ClassLoader that looks in a bunch of bundles for a class if it's not locally findable.
     */
    public static class JythonBundlesClassLoader extends ClassLoader {
        private Bundle [] bundles;

        public JythonBundlesClassLoader(Bundle [] b, ClassLoader parent) {
            super(parent);
            bundles = b;
        }

        protected Class findClass(String className) throws ClassNotFoundException {
            try {
                // First try the default class loader
                return super.findClass(className);
            }
            catch (ClassNotFoundException e) {
                // Look for the class from the bundles.
                // XXX Security issue here?
                for (Bundle b : bundles) {
                    try {
                        return b.loadClass(className);
                    }
                    catch (ClassNotFoundException e2) {
                        // ignore
                    }
                }
                // Can't find the class, re-throw the exception
                throw e;
            }
        }
    }

    @ShellCommand(
            name="python",
            forwardLines=false,
            shortHelp="Invoke interactive Python shell",
            longHelp="EOF in the shell exits the shell and returns control to the top-level\n" +
                    "NetShell."
    )
    public static void startPython (String[] args, InputStream in, OutputStream out, OutputStream err) {

        final Logger logger = LoggerFactory.getLogger(PythonShell.class);
        if (in instanceof ShellInputStream) {
            if (((ShellInputStream) in).getSourceInputStream() instanceof TabFilteringInputStream) {
                ((TabFilteringInputStream) ((ShellInputStream) in).getSourceInputStream()).setFilters(true);
            }
        }
        PyDictionary sessionLocals;
        boolean isFirstSession = true;
        // Find or create locals
        synchronized (PythonShell.locals) {
            if (PythonShell.locals.containsKey(in)) {
                // Already has a locals created for this session, re-use
                sessionLocals = PythonShell.locals.get(in);
                isFirstSession = false;
            } else {
                // First python for this session. Create locals
                sessionLocals = new PyDictionary();
                PythonShell.locals.put(in,sessionLocals);
                // Sets the default search path
                PythonInterpreter python = new PythonInterpreter(sessionLocals);
                python.setIn(in);
                python.setOut(out);
                python.setErr(err);
                osgiSetup(python);
                python.exec("import sys");
                python.exec("sys.path = sys.path + ['" + BootStrap.rootPath.resolve("bin/") + "']");
                python.exec("sys.path = sys.path + ['" + KernelThread.currentKernelThread().getUser().getHomePath()
                            + "']");
                try {
                    err.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        logger.debug("Starting Python");
        if (isFirstSession) {
            // Run profile
            PythonShell.execProfile(sessionLocals,in,out,err);
        }
        try {
            if ((args != null) && (args.length > 1)) {
                // A program is provided. Add the arguments into the python environment as command_args variable
                sessionLocals.put("command_args", args);
                PythonInterpreter python = new PythonInterpreter(sessionLocals);
                python.setIn(in);
                python.setOut(out);
                python.setErr(err);
                osgiSetup(python);
                logger.info("Executes file " + args[1] + " for user " + KernelThread.currentKernelThread().getUser().getName());
                String filePath;
                if (args[1].startsWith(BootStrap.rootPath.toString())) {
                    // The file path already contains the NetShell Root.
                    filePath = args[1];
                } else {
                    // Need to prefix the file path with NetShell Root.
                    filePath = BootStrap.rootPath.toString() + args[1];
                }
                python.execfile(filePath);

            } else {
                // This is an interactive session
                if (!sessionLocals.containsKey("command_args"))  {
                    // Make sure that the variable exists
                    sessionLocals.put("command_args", new String[] {"python"});
                }
                InteractiveConsole console = new InteractiveConsole(sessionLocals);
                if (System.getProperty("python.home") == null) {
                    System.setProperty("python.home", "");
                }
                InteractiveConsole.initialize(System.getProperties(),
                        null, new String[0]);

                console.setOut(out);
                console.setErr(err);
                console.setIn(in);
                osgiSetup(console);
                // Start the interactive session
                ((ShellInputStream) in).setEofHack(true);
                console.interact();
                ((ShellInputStream) in).setEofHack(false);
            }
        } catch (Exception e) {
            if ((e instanceof ExitSecurityException) || (e.toString().contains("SystemExit"))) {
                // This is simply due to sys.exit(). Ignore.
            } else {
                try {
                    err.write(e.toString().getBytes());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }
        try {
            err.flush();
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (in instanceof ShellInputStream) {
            if (((ShellInputStream) in).getSourceInputStream() instanceof TabFilteringInputStream) {
                ((TabFilteringInputStream) ((ShellInputStream) in).getSourceInputStream()).setFilters(false);
            }
        }

        logger.debug("Exiting Python");
    }

    private static void execProfile(PyDictionary locals, InputStream in, OutputStream out, OutputStream err) {
        User user = KernelThread.currentKernelThread().getUser();
        Path homeDir = user.getHomePath();
        File profile = Paths.get(homeDir.toString(),"profile.py").toFile();
        if (!profile.exists()) {
            // No profile, nothing to do
            return;
        }
        // Execute the profile
        PythonInterpreter python = new PythonInterpreter(locals);
        python.setIn(in);
        python.setOut(out);
        python.setErr(err);
        logger.info("Executes file " + profile.toString() + " for user " + KernelThread.currentKernelThread().getUser().getName());
        python.execfile(profile.toString());
    }

    public static String getProgramPath(String cmd) {
        File path = null;
        // Make sure that the extension .py is in the name of the command.
        String command;
        if (cmd.endsWith(".py")) {
            command = cmd;
        } else {
            command = cmd + ".py";
        }
        // Retrieve the python search path
	    try {
		    path = new File(BootStrap.rootPath.toString() + KernelThread.currentKernelThread().getCurrentDirectory() + "/" + command);
		    if (path.exists()) {
			    return path.toString();
		    }
		    path = new File(BootStrap.rootPath.resolve("bin").resolve(command).toString());
		    if (path.exists()) {
			    return path.toString();
		    }
	    } catch (SecurityException e) {
	    }

	    try {
		    Path normalized = KernelThread.currentKernelThread().getUser().getHomePath().normalize().toRealPath();
		    path = new File(normalized.resolve(command).toString());
		    if (path.exists()) {
			    return path.toString();
		    }
		    return null;
	    } catch (SecurityException|IOException e) {
		    return null;
	    }
    }

    /**
     * Do the OSGi setup for a Python interpreter.
     * This includes setting up the ClassLoader for the interpreter
     * and telling the Jython package manager that it can use the Java
     * classes made available by the OSGi framework.
     * @param py
     */
    static void osgiSetup(PythonInterpreter py) {
        PySystemState sys = py.getSystemState();

        // Set the class loader to know it has a parent class loader.
        sys.setClassLoader(new JythonClassLoader(PythonShell.class.getClassLoader()));
        // sys.setClassLoader(PythonShell.class.getClassLoader());
        // sys.setClassLoader(new JythonBundlesClassLoader(BootStrap.getBootStrap()));
        try {
            System.out.println("PySystemState ClassLoader: " + sys.getClass().getClassLoader().getClass().getName());
            System.out.println("Thread ClassLoader: " + Thread.currentThread().getContextClassLoader().getClass().getName());
            System.out.println("PythonShell ClassLoader: " + PythonShell.class.getClassLoader().getClass().getName());
            System.out.println("PythonShell ClassLoader parent: " + PythonShell.class.getClassLoader().getParent().getClass().getName());
        }
        catch (NullPointerException npe) {
            System.out.println("Something was null");
        }
        System.out.println("Jython class loader now: " + sys.getClassLoader().getClass().getName());
        System.out.println("Jython class loader parent now: " + sys.getClassLoader().getParent().getClass().getName());

        // Let the Jython package manager know about Java classes
        // visible via OSGi.  There are a couple of ways to do this.
        // For example we can do this with all the packages we import.
        // Another approach is to do this with all the packages that are
        // exported by certain other bundles.
        //
        // TODO:  For now we're just working with all packages we import.
        /* String[] packages = saveImportedPackageNames(); */

        String[] packages = { "net.es.netshell.api", "net.es.netshell.kernel", "net.es.netshell.kernel.exec", "net.es.netshell.kernel.security", "net.es.netshell.kernel.users", "net.es.netshell.shell"};
        for(String p : packages) {
            sys.add_package(p);
        }

        for (String p : packages) {
            System.out.println("  " + p);
        }


    }

    /**
     * Grab the package names that are provided / exported by a set of bundles.
     * @param bundles
     * @return Array of package names
     */
    static private String[] saveExportedPackageNames(Bundle[] bundles) {
        ArrayList<String> names = new ArrayList<String>();
        for (Bundle b : bundles) {
            Dictionary headers = b.getHeaders();
            String packages = (String) headers.get("Provide-Package");
            if (packages != null) {
                parsePackageNames(packages, names);
            }
            packages = (String) headers.get("Export-Package");
            if (packages != null) {
                parsePackageNames(packages, names);
            }
        }
        return names.toArray(new String[names.size()]);
    }

    /**
     * Grab all package names that are imported into this bundle.
     * @return Array of package names
     */
    static private String[] saveImportedPackageNames() {
        ArrayList<String> names = new ArrayList<String>();
        Bundle b = PythonActivator.getBundleContext().getBundle();
        Dictionary headers = b.getHeaders();
        String packages = (String) headers.get("Import-Package");
        if (packages != null) {
            parsePackageNames(packages, names);
        }
        return names.toArray(new String[names.size()]);
    }

    /**
     * Break apart a comma-separated list of package names and add to an ongoing array of names
     * We also have to strip out some optional OSGi attributes that are added to the name
     * of each package.
     */
    static private void parsePackageNames(String packages, ArrayList<String> names) {
        System.out.println("parsePackageNames(" + packages + ")");
        String p = packages;
        p.replaceAll(";[^;]*[,$]", "");
        System.out.println("p = " + p);
        String[] ps = p.split(",");
        for (String s : ps) {
            names.add(s.trim());
        }
    }


}
