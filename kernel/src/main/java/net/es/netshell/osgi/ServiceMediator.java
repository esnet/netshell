/*
 * ENOS, Copyright (c) $today.date, The Regents of the University of California, through Lawrence Berkeley National Laboratory (subject to receipt of any required approvals from the U.S. Dept. of Energy).  All rights reserved.
 *
 * If you have questions about your rights to use or distribute this software, please contact Berkeley Lab's Technology Transfer Department at TTD@lbl.gov.
 *
 * NOTICE.  This software is owned by the U.S. Department of Energy.  As such, the U.S. Government has been granted for itself and others acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software to reproduce, prepare derivative works, and perform publicly and display publicly.  Beginning five (5) years after the date permission to assert copyright is obtained from the U.S. Department of Energy, and subject to any subsequent five (5) year renewals, the U.S. Government is granted for itself and others acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software to reproduce, prepare derivative works, distribute copies to the public, perform publicly and display publicly, and to permit others to do so.
 */

package net.es.netshell.osgi;

import org.apache.felix.service.command.CommandProcessor;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Created by bmah on 10/22/14.
 */
public class ServiceMediator {

    private BundleContext context;
    private long bundleId;
    private String bundleName;

    private ServiceTracker cpTracker;

    public ServiceMediator(BundleContext c) {
        context = c;

        bundleName = (context.getBundle().getSymbolicName() == null) ?
                context.getBundle().getLocation() :
                context.getBundle().getSymbolicName();
        bundleId = context.getBundle().getBundleId();

//        cpTracker = new ServiceTracker(context, "org.osgi.service.command.CommandProcessor", null);
        cpTracker = new ServiceTracker(context, CommandProcessor.class.getName(), null);
        cpTracker.open();
    }

    public Object getCommandProcessor(long wait) {
        Object service = null;
        try {
            if (wait < 0) {
                service = cpTracker.getService();
            }
            else {
                service = cpTracker.waitForService(wait);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
        return service;
    }

    public void deactivate() {
        if (cpTracker != null) {
            cpTracker.close();
        }
    }

    public static long WAIT_UNLIMITED = 0;
    public static long NO_WAIT = -1;
}
