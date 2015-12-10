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

import net.es.netshell.configuration.NetShellConfiguration;

import java.nio.file.Paths;
import java.io.FilePermission;
import java.util.LinkedList;

/**
 * This class sets the file permission that KernelSecurityManager will use when doing checkRead/checkWrite.
 */
public final class Authorized {
    private static LinkedList<FilePermission> filePermissions;

    private static void init() {
        if (filePermissions != null) {
            return;
        }
        filePermissions = new LinkedList<FilePermission>();

        // Figure out the NetShell root directory.
        String rootdir = NetShellConfiguration.getInstance().getGlobal().getRootDirectory();
        filePermissions.add(new FilePermission(Paths.get(rootdir).normalize().toString() + "/-",
                            "read,write"));

    }

    /**
     * Checks if the provided FilePermission is implied by any of the authorized FilePermissions
     * @param filePermission  is Permission that is requested
     * @return true if authorized
     */
    public static boolean isAuthorized (FilePermission filePermission) {
        // System.out.println ("isAuthorized ");
        init();
        for (FilePermission perm : filePermissions) {
            if (perm.implies(filePermission)) {
                // System.out.println("Authorized ");
                // TODO: log
                return true;
            }
        }
        return false;
    }
}
