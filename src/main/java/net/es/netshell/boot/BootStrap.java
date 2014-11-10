/*
 * Copyright (c) 2014, Regents of the University of California  All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.es.netshell.boot;

import net.es.netshell.api.DefaultValues;
import net.es.netshell.api.NetShellException;
import net.es.netshell.classloader.DynamicClassLoader;
import net.es.netshell.configuration.NetShellConfiguration;
import net.es.netshell.configuration.GlobalConfiguration;
import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.security.AllowedSysCalls;
import net.es.netshell.kernel.security.KernelSecurityManager;
import net.es.netshell.osgi.HostActivator;
import net.es.netshell.osgi.OsgiCommands;
import net.es.netshell.osgi.ServiceMediator;
import net.es.netshell.python.PythonShell;
import net.es.netshell.rabbitmq.RMQShellCommands;
import net.es.netshell.shell.*;
import net.es.netshell.sshd.SShd;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.apache.felix.main.AutoProcessor;

import javax.xml.ws.Service;

/**
 * Created by lomax on 2/20/14.
 */
public final class BootStrap implements Runnable {
    private static BootStrap bootStrap = null;
    private String[] args = null;
    private SShd sshd = null;
    private static Thread thread;
    private Framework fr = null;
    private ServiceMediator mediator = null;
    private HostActivator activator = null;

    // We need to be sure the global configuration gets instantiated before the security manager,
    // because the former controls the initialization actions of the latter.
    private static final GlobalConfiguration masterConfiguration = NetShellConfiguration.getInstance().getGlobal();
    private static final KernelSecurityManager securityManager = new KernelSecurityManager();

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

    public void init() {


        BootStrap.thread = new Thread(BootStrap.getBootStrap().getSecurityManager().getNetShellRootThreadGroup(),
                                      this,
                                      "NetShell Bootstrap");
        logger.info("Starting BootStrap thread");
        logger.info("Current directory: {}", System.getProperty("user.dir"));
        BootStrap.thread.start();

    }
    public static void main(String[] args) throws NetShellException {

        final Logger logger = LoggerFactory.getLogger(BootStrap.class);

        // Set default logging level.
        // TODO:  This doesn't work.  It appears that setting the default logging level has no effect, possibly because all the various loggers have already been created?
        String defaultLogLevel = NetShellConfiguration.getInstance().getGlobal().getDefaultLogLevel();

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

        System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, defaultLogLevel);


        BootStrap.bootStrap = new BootStrap(args);
        BootStrap.bootStrap.init();
        BootStrap.bootStrap.postInitialization();

        logger.info("Bootstrap thread exits");
    }

    private BootStrap (String[] args) {
        this.args = args;
    }


    public static BootStrap getBootStrap() {
        return BootStrap.bootStrap;
    }

    public ServiceMediator getMediator() { return this.mediator; }

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

        // Add Shell Modules
        addShellModules();

        // Initialize SystemCalls
        KernelThread.initSysCalls(AllowedSysCalls.getAllowedClasses());

        setupOsgi();
    }

    private void addShellModules() {
        ShellCommandsFactory.registerShellModule(ShellBuiltinCommands.class);
        ShellCommandsFactory.registerShellModule(PythonShell.class);
        ShellCommandsFactory.registerShellModule(UserShellCommands.class);
        ShellCommandsFactory.registerShellModule(ContainerShellCommands.class);
	    ShellCommandsFactory.registerShellModule(RMQShellCommands.class);
        ShellCommandsFactory.registerShellModule(OsgiCommands.class);
    }


    /**
     * Set up OSGi framework
     *
     * As a side effect, sets this.fr to be the OSGi framework object.
     */
    private void setupOsgi() {

        // TODO:  Do this the right way, don't hard-code Felix stuff here.
        try {
            logger.info("Configuring OSGI framework");

            // Initialize properties for bundle auto-loading to work.  This
            // depends on using the Felix framework.
            Map configProps = new HashMap();
            configProps.put(AutoProcessor.AUTO_DEPLOY_DIR_PROPERY, "bundle");
            configProps.put(AutoProcessor.AUTO_DEPLOY_ACTION_PROPERY, AutoProcessor.AUTO_DEPLOY_INSTALL_VALUE + "," + AutoProcessor.AUTO_DEPLOY_START_VALUE);

            // By default, gogo shell puts up an interactive shell on the
            // default console.  We don't really want this; we want people to
            // authenticate to a remote login shell, and then get a gogo shell
            // from there.  Mostly this is to prevent someone on the console
            // from accidentally doing something stupid.  It also lets us
            // run enos in background.
            //
            // Uncomment this line to bring back that gogo shell.
            configProps.put("gosh.args", "--nointeractive");

            // Do we need to export anything here?
            // configProps.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
            //        "");

            // Create host activator
            activator = new HostActivator();
            List list = new ArrayList();
            list.add(activator);
            configProps.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, list);

            // Get framework factory.  This is ideally specified in
            // META-INF/services/org.osgi.framework.launch.FrameworkFactory
            // and says the class name of the framework factory in the implementation
            // of the OSGi framework
            // to use (such as org.apache.felix.framework.FrameworkFactory).
            //
            // If we don't don't find this, then default to Felix.
            //
            // The intent here is to try to allow for some pluggability of different
            // OSGi modules, although there are still some Felix-isms (in particular
            // the AutoProcessor), so we can't be completely framework agnostic, yet.
            ServiceLoader<org.osgi.framework.launch.FrameworkFactory> ffLoader =
                    ServiceLoader.load(org.osgi.framework.launch.FrameworkFactory.class);
            FrameworkFactory ff = ffLoader.iterator().next();
            if (ff == null) {
                logger.warn("No framework defined, using Felix by default.");
                ff = new org.apache.felix.framework.FrameworkFactory();
            }

            // Instantiate a new framework and start setting it up.
            fr = ff.newFramework(configProps);
            fr.init();
            AutoProcessor.process(configProps, activator.getBundleContext());

            Runtime.getRuntime().addShutdownHook(new Thread("OSGi Shutdown Hook") {
                public void run() {
                    try {
                        if (fr != null) {
                            fr.stop();
                            fr.waitForStop(0);
                        }
                    } catch (Exception e3) {
                        System.err.println("Error stopping OSGi framework: " + e3);
                    }
                }
            });


            logger.info("Starting OSGi framework");
            fr.start();

            // Track certain services that we (the kernel) are interested in.
            this.mediator = new ServiceMediator(activator.getBundleContext());

            // Dump out a display of all the loaded bundles.
            Bundle[] bundles;
            bundles = activator.getBundleContext().getBundles();
            logger.info ("Bundles loaded: " + bundles.length);
            int i;
            for (i = 0; i < bundles.length; i++) {
                Bundle b = bundles[i];
                logger.info("  " + b.getLocation() + ": " + b.getState());
            }

            // Hang out here until the framework stops.  Note that in the background
            // (and even after we get here), the main part of ENOS can continue serving
            // SSH requests.
            //
            // TODO:  This is weird and needs to be fixed.
            // The framework should only go down when the application quits.  We really
            // need to have some sort of "shutdown" command in the ENOS shell that shuts
            // ENOS down gracefully, then the OSGi shutdown will happen via the
            // shutdown hook.
            try {
                fr.waitForStop(0);
            }
            catch (Exception e2) {
                e2.printStackTrace();
            }
//            System.exit(0);

        }
        catch (BundleException e) {
            System.err.println("Could not create OSGi framework: " + e);
            e.printStackTrace();
        }

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
        Thread.currentThread().setContextClassLoader(new DynamicClassLoader());
        this.startServices();
    }

    public boolean test (String tt) {
       return false;
    }
}
