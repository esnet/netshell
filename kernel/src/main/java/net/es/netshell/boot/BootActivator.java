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

import net.es.netshell.api.NetShellException;
import net.es.netshell.api.PropertyKeys;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * OSGi activator for NetShell.
 * Runs the old BootStrap main function in a thread.
 */
public class BootActivator implements BundleActivator, Runnable {
    private BundleContext context;

    /**
     * OSGi start function
     * Starts up a new thread, inside which we run the old Bootstrap main.
     * @param bc
     */
    @Override
    public void start(BundleContext bc) {
        System.out.println("NetShell is starting");

        // save bundle context to pass to BootStrap
        context = bc;

        // If nobody specified a location for the configuration file, set one.
        if (System.getProperty(PropertyKeys.NETSHELL_CONFIGURATION) == null) {
            System.setProperty(PropertyKeys.NETSHELL_CONFIGURATION, "./netshell.json");
        }

        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void stop(BundleContext bc) {
        BootStrap.getBootStrap().shutdown();
        System.out.println("Goodbye NetShell");
    }

    @Override
    public void run() {
        try {
            String[] argv = null;
            BootStrap.main(argv, this.context);
        }
        catch (NetShellException e) {
            e.printStackTrace();
        }
    }
}
