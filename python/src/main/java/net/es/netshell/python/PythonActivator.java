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
package net.es.netshell.python;

import net.es.netshell.shell.PythonShellService;
import net.es.netshell.shell.ShellCommandsFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Hashtable;

/**
 * Created by bmah on 12/4/14.
 */
public class PythonActivator implements BundleActivator{

    static BundleContext bundleContext;

    public void start(BundleContext b) {
        System.out.println("Hello Python");

        bundleContext = b;

        ShellCommandsFactory.registerShellModule(PythonShell.class);

        // Register us as a service with OSGi, so the Shell class in the main
        // netshell-kernel module can find us.
        Hashtable<String, String> props = new Hashtable<String, String>();
        // props.put("foo", "bar");
        ServiceRegistration s =
                bundleContext.registerService(PythonShellService.class.getName(), new PythonShellServiceImpl(), props);
        // The python shell bundle requires to load OSGi and Karaf jar files when it first execute a python
        // command or script. This can be done only by a privileged thread.
        // Therefore, the following line forces the python shell execution at initialization time which is
        // performed by a privileged thread.
        // lomax@es.net TODO
        PythonShell.runInit();
    }
    public void stop(BundleContext b) {
        ShellCommandsFactory.unregisterShellModule(PythonShell.class);
        System.out.println("Goodbye Python");
    }

    static public BundleContext getBundleContext() {
        return bundleContext;
    }
}
