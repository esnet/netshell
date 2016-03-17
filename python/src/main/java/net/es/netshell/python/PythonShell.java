/*
 * ESnet Network Operating System (ENOS) Copyright (c) 2016, The Regents
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
 *
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
import org.python.core.PyFile;
import org.python.core.Options;
import org.python.util.PythonInterpreter;
import org.python.util.InteractiveConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class PythonShell {

    public static class ActiveLocals {
        public PyDictionary currentLocals;
        public PyDictionary inputStreamLocals;
        public ActiveLocals(PyDictionary inputStreamLocals) {
            this.inputStreamLocals = inputStreamLocals;
            this.currentLocals = inputStreamLocals;
        }
    }
    private static final Logger logger = LoggerFactory.getLogger(PythonShell.class);
    private static HashMap<InputStream,ActiveLocals> locals = new HashMap<InputStream, ActiveLocals>();
    private static HashMap<String,HashMap<String,PyDictionary>> userLocals = new HashMap<String,HashMap<String,PyDictionary>>();
    private static HashMap<String,PyDictionary> userCurrentLocals = new HashMap<String,PyDictionary>();
    public static String INIT_SCRIPT = "/etc/init.py";
    public static String PROFILE_SCRIPT = "/etc/profile.py";

    /**
     * Modified ClassLoader used to give Jython access to classes made available via OSGi.
     */
    public static class JythonClassLoader extends ClassLoader {
        public JythonClassLoader(ClassLoader parent) {
            super(parent);
        }
    }

    public static void runInit() {
        String args[] = new String[2];
        args[1] = PythonShell.INIT_SCRIPT;

        PythonShell.startPython(args, System.in, System.out, System.err);
    }

    static private PyDictionary getSessionEnv (InputStream in, OutputStream out, OutputStream err) {
        if (in instanceof ShellInputStream) {
            if (((ShellInputStream) in).getSourceInputStream() instanceof TabFilteringInputStream) {
                ((TabFilteringInputStream) ((ShellInputStream) in).getSourceInputStream()).setFilters(true);
            }
        }
        PyDictionary sessionLocals;
        boolean isFirstSession = true;
        // Find or create locals
        if (out == null) {
            out = System.out;
        }
        if (err == null) {
            err = out;
        }
        synchronized (PythonShell.locals) {
            if (PythonShell.locals.containsKey(in)) {
                // Already has a locals created for this session, re-use
                sessionLocals = PythonShell.locals.get(in).currentLocals;
                if (sessionLocals == null) {
                    sessionLocals = PythonShell.locals.get(in).inputStreamLocals;
                }
                isFirstSession = false;
            } else {
                // First python for this session. Create locals
                sessionLocals = new PyDictionary();
                // TODO: this creates a memory leak since the environment is not removed after the SSH session is closed.
                PythonShell.locals.put(in,new ActiveLocals(sessionLocals));
                // Don't try to import site.py.  The move from Jython 2.5.2 to 2.7beta4 seems to
                // require this, because it appears that we can't find site.py and blow up.
                org.python.core.Options.importSite = false;

                if (KernelThread.currentKernelThread().getUser() != null) {
                    User user =KernelThread.currentKernelThread().getUser();
                    if (!PythonShell.userLocals.containsKey(user.getName())) {
                        // First session for this user.
                        HashMap<String,PyDictionary> userEnv = new HashMap<String,PyDictionary>();
                        PythonShell.userLocals.put(user.getName(),userEnv);
                    }
                    HashMap<String,PyDictionary> userEnv = PythonShell.userLocals.get(user.getName());
                    sessionLocals.put("_user_locals",userEnv);
                    // If the user is privileged, add all user locals
                    if (user.isPrivileged()) {
                        sessionLocals.put("_current_locals",PythonShell.userCurrentLocals);
                        sessionLocals.put("_ssh_locals",PythonShell.locals);
                    }
                }
                sessionLocals.put("_all_user_locals",PythonShell.userLocals);
                try {
                    err.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Execute the global profile.py
                String filePath = BootStrap.rootPath.toString() + PythonShell.PROFILE_SCRIPT;
                if (! new File(filePath).exists()) {
                    try {
                        err.write((PythonShell.PROFILE_SCRIPT + " not found.\n").getBytes());
                        err.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        PythonInterpreter python = PythonShell.getPythonInterpreter(in,out,err,sessionLocals);
                        python.execfile(filePath);
                    } catch (Exception e) {
                        try {
                            err.write(e.toString().getBytes());
                        } catch (Exception e2) {
                            // Can't recover from it.
                            e2.printStackTrace();
                        }
                    }
                }
            }
        }
        return sessionLocals;
    }

    private static PythonInterpreter getPythonInterpreter(InputStream in, OutputStream out, OutputStream err, PyDictionary sessionLocals) {

        PySystemState systemState = null;
        PythonInterpreter python;

        systemState = (PySystemState) sessionLocals.get("_sessionsys");
        if (systemState == null) {
            systemState = new PySystemState();
            sessionLocals.put("_sessionsys",systemState);
        }
        String mode = Options.unbuffered ? "b" : "";
        int buffering = Options.unbuffered ? 0 : 1;
        systemState.stdin = new PyFile(in, "<stdin>", "r" + mode, buffering, false);
        systemState.stdout = new PyFile(out, "<stdout>", "w" + mode, buffering, false);
        systemState.stderr = new PyFile(err, "<stderr>", "w" + mode, 0, false);
        python = new PythonInterpreter(sessionLocals,systemState);
        //python.setIn(in);
        //python.setOut(out);
        //python.setErr(err);
        osgiSetup(python.getSystemState());
        return python;
    }

    @ShellCommand(
            name="pyenv",
            forwardLines=false,
            shortHelp="Manage the python session environment",
            longHelp="Save, load or delete a python environment\n"  +
                    "\tpyenv save <env_name> [user_name] saves the current environment. If a user name is provided\n" +
                    "\t\tthe environment is saved into the user's environments. This requires privileged access." +
                    "\tpyenv load <env_name> [user_name] next python session will be loaded with the environment.\n" +
                    "\t\tIf a user name is provided tthe environment is saved into the user's environments." +
                    "\t\tThis requires privileged access." +
                    "\tpyenv list <pattern|'all'>[user_name] lists environments"
    )
    public static void pyenv (String[] args, InputStream in, OutputStream out, OutputStream err) {
        User user = KernelThread.currentKernelThread().getUser();
        if (user == null) {
            // No user, no user locals
            return;
        }
        String userName = user.getName();
        if (args.length < 3) {
            // Syntax error. Should do better reporting
            return;
        }
        HashMap<String,PyDictionary> userEnvs = PythonShell.userLocals.get(user.getName());
        String cmd = args[1];
        String env = args[2];
        String optUser = null;
        if (args.length == 4) {
            optUser = args[3];
            if (user.isPrivileged()) {
                userEnvs = PythonShell.userLocals.get(optUser);
            }
        }
        // Makes sure the python has been initialized at least once.
        if (cmd.equalsIgnoreCase("save")) {
            userEnvs.put(env,PythonShell.locals.get(in).currentLocals);
        } else if (cmd.equalsIgnoreCase("load")) {
            if (userEnvs.containsKey(env)) {
                PythonShell.locals.get(in).currentLocals = userEnvs.get(env);
            }
        } else if (cmd.equals("delete")) {
            if (userEnvs.containsKey(env)) {
                userEnvs.remove(env);
            }
        } else if (cmd.equals("list")) {
            for (String envName : userEnvs.keySet()) {
                if (env.equalsIgnoreCase("all") || envName.contains(env)) {
                    try {
                        out.write((envName + "\n").getBytes());
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
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
        if (in instanceof ShellInputStream) {
            if (((ShellInputStream) in).getSourceInputStream() instanceof TabFilteringInputStream) {
                ((TabFilteringInputStream) ((ShellInputStream) in).getSourceInputStream()).setFilters(true);
            }
        }
        boolean isFirstSession = true;
        // Find or create locals
        if (out == null) {
            out = System.out;
        }
        if (err == null) {
            err = out;
        }
        PyDictionary sessionLocals = PythonShell.getSessionEnv(in, out, err);
        logger.debug("Starting Python");
        // XXX There is a possibly-minor bug here.  isFirstSession will always be true at this point,
        // so we'll always run the profile.py file despite the fact that it's inside a conditional.
        // PythonShell.getSessionEnv also contains a local isFirstSession variable, and appears to set
        // it correctly, but does not make that value available to its caller, specifically the invocation
        // a few lines above this.
        if (isFirstSession) {
            // Run profile
            PythonShell.execProfile(sessionLocals,in,out,err);
        }
        try {
            if ((args != null) && (args.length > 1)) {
                // A program is provided. Add the arguments into the python environment as command_args variable
                PythonInterpreter python = PythonShell.getPythonInterpreter(in,out,err,sessionLocals);
                if (KernelThread.currentKernelThread().getUser() != null) {
                    User user = KernelThread.currentKernelThread().getUser();
                    python.exec("sys.path = sys.path + ['" + user.getHomePath() + "']");
                }
                if (KernelThread.currentKernelThread().getUser() != null) {
                    logger.info("Executes file " + args[1] + " for user " + KernelThread.currentKernelThread().getUser().getName());
                } else {
                    logger.info("Executes file " + args[1] + " (no user defined)");
                }
                String filePath;

                String argv = "import sys\nsys.argv=[";
                boolean skipFirst = true;
                for (String a :  args) {
                    if (skipFirst) {
                        skipFirst = false;
                        continue;
                    }
                    argv += "'" + a + "',";
                }
                argv += "]";
                python.exec(argv);
                if (args[1].startsWith(BootStrap.rootPath.toString())) {
                    // The file path already contains the NetShell Root.
                    filePath = args[1];
                } else {
                    // Need to prefix the file path with NetShell Root.
                    filePath = BootStrap.rootPath.toString() + args[1];
                }
                if (! new File(filePath).exists()) {
                    err.write((args[1] + " not found.\n").getBytes());
                } else {
                    python.execfile(filePath);
                }

            } else {
                // This is an interactive session
                if (!sessionLocals.containsKey("command_args"))  {
                    // Make sure that the variable exists
                    sessionLocals.put("command_args", new String[] {"python"});
                }
                PySystemState systemState = (PySystemState) sessionLocals.get("_sessionsys");
                InteractiveConsole console = new InteractiveConsole(sessionLocals);
                console.setOut(out);
                console.setErr(err);
                console.setIn(in);
                console.getSystemState().path = systemState.path;
                if (System.getProperty("python.home") == null) {
                    System.setProperty("python.home", "");
                }
                InteractiveConsole.initialize(System.getProperties(),
                        null, new String[0]);

                osgiSetup(console.getSystemState());
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
            PythonInterpreter python = PythonShell.getPythonInterpreter(in,out,err,locals);
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
     * @param sys SystemState
     */
    static void osgiSetup(PySystemState sys) {

        // Set the class loader to know it has a parent class loader.
        BundleContext bc = BootStrap.getBootStrap().getBundleContext();
        Bundle [] bundles = bc.getBundles();

        if (sys == null) {
            System.out.println("SYS IS NULL");
        }

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
            logger.debug("Add package {}", p);
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
    static private void parsePackageNames(String packages, ArrayList<String> names) {;

        // Use a couple of regex substitutions to get down to just the package names.
        // First, some of the optional attributes can be quoted, and inside the quotes
        // can be commas (which outside the quotes are actually the delimiters between
        // the package names).  So we first get rid of anything that's quoted, noting
        // that we really don't need any of these attributes anyway.
        String p = packages.replaceAll("\"[^\"]*\"", "");

        // Once that's done, we know that anything between a semicolon and a comma is
        // attribute and something that we can throw away.
        p = p.replaceAll(";[^,]*", "");

        // Now, everything that's left is really a comma-separated list of package
        // names so we can split on the commas.
        String[] ps = p.split(",");
        for (String s : ps) {

            // But we also need to trim some gunk away.
            names.add(s.trim());
        }
    }


}
