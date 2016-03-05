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
package net.es.netshell.api;

import net.es.netshell.kernel.acl.UserAccess;
import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.exec.annotations.SysCall;
import net.es.netshell.kernel.users.User;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Created by lomax on 3/3/16.
 */
public class ACL extends PersistentObject {

    public ACL() {
        super();
    }

    public void checkAcces(User currentUser, Resource resource, HashMap<String, Object> accessQuery) {
        throw new SecurityException("Not authorized");
    }

}
