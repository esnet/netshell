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
import net.es.netshell.osgi.OsgiBundlesClassLoader;
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

    /**
     * Modified ClassLoader used to give Jython access to classes made available via OSGi.
     */
    public static class JythonClassLoader extends ClassLoader {
        public JythonClassLoader(ClassLoader parent) {
            super(parent);
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
                if (KernelThread.currentKernelThread().getUser() != null) {
                    python.exec("sys.path = sys.path + ['" + KernelThread.currentKernelThread().getUser().getHomePath()
                            + "']");
                }
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
                if (KernelThread.currentKernelThread().getUser() != null) {
                    logger.info("Executes file " + args[1] + " for user " + KernelThread.currentKernelThread().getUser().getName());
                }
                else {
                    logger.info("Executes file " + args[1] + " (no user defined)");
                }
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
                if (in instanceof ShellInputStream) {
                    ((ShellInputStream) in).setEofHack(true);
                }
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
        if (user != null) {
            Path homeDir = user.getHomePath();
            File profile = Paths.get(homeDir.toString(), "profile.py").toFile();
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
        BundleContext bc = BootStrap.getBootStrap().getBundleContext();
        Bundle [] bundles = bc.getBundles();

        sys.setClassLoader(new OsgiBundlesClassLoader(bundles, PythonShell.class.getClassLoader()));
        logger.debug("Jython class loader now: " + sys.getClassLoader().getClass().getName());
        logger.debug("Jython class loader parent now: " + sys.getClassLoader().getParent().getClass().getName());

        // Let the Jython package manager know about Java classes
        // visible via OSGi.  There are a couple of ways to do this.
        // For example we can do this with all packages that are exported
        // by certain other bundles (saveExportedPackageNames), or
        // we can do this with all the packages we import
        // (saveImportedPackageNames).
        String [] packages = saveExportedPackageNames(bundles);
        for(String p : packages) {
             sys.add_package(p);
            logger.info("Add package {}", p);
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
     * Break apart a list of package names and add to an ongoing array of names
     * We also have to strip out some optional OSGi attributes that are added to the name
     * of each package.
     */
    static private void parsePackageNames(String packages, ArrayList<String> names) {
//        System.out.println("parsePackageNames(" + packages + ")");

        // Use a couple of regex substitutions to get down to just the package names.
        // First, some of the optional attributes can be quoted, and inside the quotes
        // can be commas (which outside the quotes are actually the delimiters between
        // the package names).  So we first get rid of anything that's quoted, noting
        // that we really don't need any of these attributes anyway.
        String p = packages.replaceAll("\"[^\"]*\"", "");

        // Once that's done, we know that anything between a semicolon and a comma is
        // attribute and something that we can throw away.
        p = p.replaceAll(";[^,]*", "");
//        System.out.println("p = " + p);

        // Now, everything that's left is really a comma-separated list of package
        // names so we can split on the commas.
        String[] ps = p.split(",");
        for (String s : ps) {

            // But we also need to trim some gunk away.
            names.add(s.trim());
        }
    }


}
