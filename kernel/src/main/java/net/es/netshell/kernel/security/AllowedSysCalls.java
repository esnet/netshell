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
package net.es.netshell.kernel.security;

/**
 * Created by lomax on 2/28/14.
 */

import net.es.netshell.kernel.exec.KernelThread;

import java.util.LinkedList;
import java.util.List;

/**
 * List of classes that are allowed to change the privileged status of an
 * application thread.
 */
public final class AllowedSysCalls {
    static private List<Class> allowedSysCallClasses = new LinkedList<Class>();

    static {
        allowedSysCallClasses.add(net.es.netshell.kernel.exec.KernelThread.class);
        allowedSysCallClasses.add(net.es.netshell.kernel.security.FileACL.class);
        allowedSysCallClasses.add(net.es.netshell.kernel.users.Users.class);
        allowedSysCallClasses.add(net.es.netshell.kernel.acl.UserAccess.class);
        allowedSysCallClasses.add(net.es.netshell.kernel.container.Container.class);
        allowedSysCallClasses.add(net.es.netshell.kernel.container.Containers.class);
        allowedSysCallClasses.add(net.es.netshell.boot.BootStrap.class);
        allowedSysCallClasses.add(net.es.netshell.kernel.networking.NetworkInterfaces.class);
        allowedSysCallClasses.add(net.es.netshell.kernel.perfsonar.Bwctl.class);
        allowedSysCallClasses.add(net.es.netshell.kernel.ovs.Openvswitch.class);
    }

    public static List<Class> getAllowedClasses() {
        LinkedList<Class> ret = new LinkedList<Class>(AllowedSysCalls.allowedSysCallClasses);
        ret.add(KernelThread.class);
        return ret;
    }

    public static boolean isAllowed (Class c) {
        return AllowedSysCalls.allowedSysCallClasses.contains(c);
    }
}
