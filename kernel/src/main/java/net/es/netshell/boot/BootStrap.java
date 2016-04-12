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
package net.es.netshell.boot;

import net.es.netshell.api.DefaultValues;
import net.es.netshell.api.NetShellException;
import net.es.netshell.configuration.NetShellConfiguration;
import net.es.netshell.configuration.GlobalConfiguration;
import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.api.DataBase;
import net.es.netshell.mongodb.MongoDBProvider;

import net.es.netshell.kernel.security.AllowedSysCalls;
import net.es.netshell.kernel.security.KernelSecurityManager;
import net.es.netshell.kernel.exec.annotations.SysCall;
import net.es.netshell.rabbitmq.RMQShellCommands;
import net.es.netshell.shell.*;
import net.es.netshell.sshd.SShd;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.Thread;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created by lomax on 2/20/14.
 */
public final class BootStrap implements Runnable {
    private static BootStrap bootStrap = null;
    private String[] args = null;
    private SShd sshd = null;
    private static Thread thread;
    private BundleContext bundleContext;
    private DataBase dbClient = null;

    // We need to be sure the global configuration gets instantiated before the security manager,
    // because the former controls the initialization actions of the latter.
    private static final GlobalConfiguration masterConfiguration = NetShellConfiguration.getInstance().getGlobal();
    private static KernelSecurityManager securityManager = null;

    public final static Path rootPath = BootStrap.toRootRealPath();

    final private Logger logger = LoggerFactory.getLogger(BootStrap.class);

    static public Path toRootRealPath() {

        Path realPathName;
        try {
            if (masterConfiguration.getRootDirectory() != null) {
                realPathName = Paths.get(
                        new File(masterConfiguration.getRootDirectory()).getCanonicalFile().toString());
            } else {
                realPathName = Paths.get(new File(DefaultValues.NETSHELL_DEFAULT_ROOTDIR).getCanonicalFile().toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return realPathName;
    }

    public Shell getShell() {
        return shell;
    }

    private Shell shell = null;

    public SShd getSshd() {
        return sshd;
    }

    public String[] getArgs() {
        return args;
    }

    public KernelSecurityManager getSecurityManager() {
        return securityManager;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public DataBase getDataBase() {
        return this.dbClient;
    }

    public void init() {

        BootStrap.securityManager = new KernelSecurityManager();
        BootStrap.thread = new Thread(BootStrap.getBootStrap().getSecurityManager().getNetShellRootThreadGroup(),
                                      this,
                                      "NetShell Bootstrap");
        logger.info("Starting BootStrap thread");
        logger.info("Current directory: {}", System.getProperty("user.dir"));
        BootStrap.thread.start();

    }
    public static void main(String[] args, BundleContext bundleContext) throws NetShellException {

        final Logger logger = LoggerFactory.getLogger(BootStrap.class);

        // Set default logging level.
        // TODO:  This doesn't work.  It appears that setting the default logging level has no effect, possibly because all the various loggers have already been created?
        String defaultLogLevel = NetShellConfiguration.getInstance().getGlobal().getDefaultLogLevel();
        Logger rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        if (rootLogger instanceof ch.qos.logback.classic.Logger) {
            ((ch.qos.logback.classic.Logger) rootLogger).setLevel(ch.qos.logback.classic.Level.toLevel(defaultLogLevel));
        }


        // Make sure the root directory exists and that we can write to it.
        File root = new File(BootStrap.rootPath.toString());
        if (root.isDirectory()) {
            if (root.canWrite()) {
                logger.info("Starting NetShell root= " + BootStrap.rootPath.toString());
            }
            else {
                logger.error("NetShell root directory " + BootStrap.rootPath + " not writable");
                throw new NetShellException("NetShell root directory " + BootStrap.rootPath + " not writable");
            }
        }
        else {
            logger.error("NetShell root directory " + BootStrap.rootPath + " not found");
            throw new NetShellException("NetShell root directory " + BootStrap.rootPath + " not found");
        }

        NetShellConfiguration netShellConfiguration = NetShellConfiguration.getInstance();


        BootStrap.bootStrap = new BootStrap(args, bundleContext);
        BootStrap.bootStrap.init();
        BootStrap.bootStrap.postInitialization();

        logger.info("Bootstrap thread exits");
    }

    private BootStrap (String[] args, BundleContext bundleContext) {
        this.args = args;
        this.bundleContext = bundleContext;
    }


    public static BootStrap getBootStrap() {
        return BootStrap.bootStrap;
    }

    public void startServices() {

        // Start sshd if it's not disabled.
        int sshDisabled = NetShellConfiguration.getInstance().getGlobal().getSshDisabled();
        if (sshDisabled == 0) {
            this.sshd = SShd.getSshd();
            try {
                this.sshd.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (NetShellConfiguration.getInstance().getGlobal().useDB()) {
            // Create the Mongo Database client.
            String dbHost = NetShellConfiguration.getInstance().getGlobal().getDbHost();
            int dbPort = NetShellConfiguration.getInstance().getGlobal().getDbPort();
            String dbUser = NetShellConfiguration.getInstance().getGlobal().getDbUser();
            String dbPassword = NetShellConfiguration.getInstance().getGlobal().getDbUserPassword();
            String dbName = NetShellConfiguration.getInstance().getGlobal().getDbName();
            this.dbClient = new MongoDBProvider(dbHost,dbPort,dbName,dbUser,dbPassword);
        }
        // Add Shell Modules
        addShellModules();
        // Initialize SystemCalls
        KernelThread.initSysCalls(AllowedSysCalls.getAllowedClasses());

    }

    private void addShellModules() {
        ShellCommandsFactory.registerShellModule(ShellBuiltinCommands.class);
        ShellCommandsFactory.registerShellModule(UserShellCommands.class);
	    ShellCommandsFactory.registerShellModule(RMQShellCommands.class);
        ShellCommandsFactory.registerShellModule(NetworkingShellCommands.class);
	    ShellCommandsFactory.registerShellModule(AccessShellCommands.class);
        ShellCommandsFactory.registerShellModule(OpenvswitchShellCommands.class);
    }


    public void stop() {
        synchronized (this) {
            notify();
        }
    }
    private void postInitialization() {
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        logger.info("Starting services");
        this.startServices();
    }

    // Syscall to shut down the kernel, etc. gracefully.
    public boolean shutdown() {
        Method method = null;
        try {
            method = KernelThread.getSysCallMethod(this.getClass(), "do_shutdown");
            KernelThread.doSysCall(this, method);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }
    @SysCall(name = "do_shutdown")
    public void do_shutdown() {
        logger.info("do_shutdown entry");

            // Stop SSHD
            int sshDisabled = NetShellConfiguration.getInstance().getGlobal().getSshDisabled();
            if (sshDisabled == 0) {
                this.sshd = SShd.getSshd();
                    this.sshd.stop();
            }
            BootStrap.thread.interrupt();
    }
}
