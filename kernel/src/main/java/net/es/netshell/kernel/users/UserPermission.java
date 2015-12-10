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

package net.es.netshell.kernel.users;

import java.security.Permission;

/**
 * Created by lomax on 2/25/14.
 */
public class UserPermission extends Permission {
    /**
     * Constructs a permission with the specified name.
     *
     * @param name name of the Permission object being created.
     */
    public UserPermission(String name) {
        super(name);
    }

    @Override
    public boolean implies(Permission permission) {
        if (! (permission instanceof UserPermission)) {
            // Probably due to a bug in the caller. Returns false
            return false;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String getActions() {
        return null;
    }
}
