/*
 * ENOS, Copyright (c) $today.date, The Regents of the University of California, through Lawrence Berkeley National Laboratory (subject to receipt of any required approvals from the U.S. Dept. of Energy).  All rights reserved.
 *
 * If you have questions about your rights to use or distribute this software, please contact Berkeley Lab's Technology Transfer Department at TTD@lbl.gov.
 *
 * NOTICE.  This software is owned by the U.S. Department of Energy.  As such, the U.S. Government has been granted for itself and others acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software to reproduce, prepare derivative works, and perform publicly and display publicly.  Beginning five (5) years after the date permission to assert copyright is obtained from the U.S. Department of Energy, and subject to any subsequent five (5) year renewals, the U.S. Government is granted for itself and others acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software to reproduce, prepare derivative works, distribute copies to the public, perform publicly and display publicly, and to permit others to do so.
 */

package net.es.netshell.python;

import net.es.netshell.shell.ShellCommandsFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Set;

/**
 * Created by bmah on 12/4/14.
 */
public class PythonActivator implements BundleActivator{

    BundleContext bundleContext;

    public void start(BundleContext b) {
        System.out.println("Hello Python");

        bundleContext = b;

        ShellCommandsFactory.registerShellModule(PythonShell.class);
    }
    public void stop(BundleContext b) {
        ShellCommandsFactory.unregisterShellModule(PythonShell.class);
        System.out.println("Goodbye Python");
    }
}
