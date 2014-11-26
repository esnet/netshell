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

import net.es.netshell.kernel.exec.annotations.SysCall;
import net.es.netshell.kernel.security.AllowedSysCalls;
import net.es.netshell.kernel.security.KernelSecurityManager;
import net.es.netshell.osgi.HostActivator;
import net.es.netshell.osgi.OsgiCommands;
import net.es.netshell.osgi.ServiceMediator;
import net.es.netshell.python.PythonShell;
import net.es.netshell.rabbitmq.RMQShellCommands;
import net.es.netshell.shell.*;
import net.es.netshell.sshd.SShd;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;


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

    }

    private void addShellModules() {
        ShellCommandsFactory.registerShellModule(ShellBuiltinCommands.class);
        ShellCommandsFactory.registerShellModule(PythonShell.class);
        ShellCommandsFactory.registerShellModule(UserShellCommands.class);
        ShellCommandsFactory.registerShellModule(ContainerShellCommands.class);
	    ShellCommandsFactory.registerShellModule(RMQShellCommands.class);
        ShellCommandsFactory.registerShellModule(OsgiCommands.class);
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

        try {
            fr.stop();
        }
        catch (BundleException e) {
            e.printStackTrace();
        }

        // Not sure if this is really going to work right.
        System.exit(0);

    }

    public boolean test (String tt) {
       return false;
    }
}
