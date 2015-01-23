/*
 * ENOS, Copyright (c) $today.date, The Regents of the University of California, through Lawrence Berkeley National Laboratory (subject to receipt of any required approvals from the U.S. Dept. of Energy).  All rights reserved.
 *
 * If you have questions about your rights to use or distribute this software, please contact Berkeley Lab's Technology Transfer Department at TTD@lbl.gov.
 *
 * NOTICE.  This software is owned by the U.S. Department of Energy.  As such, the U.S. Government has been granted for itself and others acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software to reproduce, prepare derivative works, and perform publicly and display publicly.  Beginning five (5) years after the date permission to assert copyright is obtained from the U.S. Department of Energy, and subject to any subsequent five (5) year renewals, the U.S. Government is granted for itself and others acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software to reproduce, prepare derivative works, distribute copies to the public, perform publicly and display publicly, and to permit others to do so.
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

    BundleContext context;

    /**
     * OSGi start function
     * Starts up a new thread, inside which we run the old Bootstrap main.
     * @param b
     */
    public void start(BundleContext b) {
        System.out.println("NetShell is starting");

        // If nobody specified a location for the configuration file, set one.
        if (System.getProperty(PropertyKeys.NETSHELL_CONFIGURATION) == null) {
            System.setProperty(PropertyKeys.NETSHELL_CONFIGURATION, "./netshell.json");
        }
        context = b;    // save bundle context to pass to BootStrap
        Thread t = new Thread(this);
        t.start();
    }

    public void stop(BundleContext b) {

        System.out.println("Goodbye NetShell");
        BootStrap.getBootStrap().shutdown();
    }

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
