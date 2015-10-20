/*
 * ENOS, Copyright (c) $today.date, The Regents of the University of California, through Lawrence Berkeley National Laboratory (subject to receipt of any required approvals from the U.S. Dept. of Energy).  All rights reserved.
 *
 * If you have questions about your rights to use or distribute this software, please contact Berkeley Lab's Technology Transfer Department at TTD@lbl.gov.
 *
 * NOTICE.  This software is owned by the U.S. Department of Energy.  As such, the U.S. Government has been granted for itself and others acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software to reproduce, prepare derivative works, and perform publicly and display publicly.  Beginning five (5) years after the date permission to assert copyright is obtained from the U.S. Department of Energy, and subject to any subsequent five (5) year renewals, the U.S. Government is granted for itself and others acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software to reproduce, prepare derivative works, distribute copies to the public, perform publicly and display publicly, and to permit others to do so.
 */

package net.es.netshell.controller.core;

import java.util.Hashtable;


import net.es.netshell.api.NetShellException;
import net.es.netshell.api.PropertyKeys;
import net.es.netshell.boot.BootStrap;
import net.es.netshell.odlcorsa.impl.OdlCorsaImpl;
import net.es.netshell.odlmdsal.impl.OdlMdsalImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Activator class for the API and Generic SDN controller support
 */
public class Activator implements BundleActivator {

    BundleContext bundleContext;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        this.bundleContext = bundleContext;
        System.out.println("NetShell Generic Controller and API: started");

        // XXX Find OdlCorsaImpl and OdlMdsalImpl objects.
        // Create Controller object passing these as constructor arguments.
        OdlMdsalImpl omi = OdlMdsalImpl.getInstance();
        OdlCorsaImpl oci = OdlCorsaImpl.getInstance();
        Controller controller = new Controller(omi, oci);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        System.out.println("NetShell Generic Controller and API: stopped");
    }
}