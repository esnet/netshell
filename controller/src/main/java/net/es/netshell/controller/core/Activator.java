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

        Controller controller = new Controller();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        System.out.println("NetShell Generic Controller and API: stopped");
    }
}
