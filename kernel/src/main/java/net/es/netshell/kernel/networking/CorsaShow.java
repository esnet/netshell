/*
 * ENOS, Copyright (c) 2015, The Regents of the University of California,
 * through Lawrence Berkeley National Laboratory (subject to receipt of any
 * required approvals from the U.S. Dept. of Energy).  All rights reserved.
 *
 * If you have questions about your rights to use or distribute this software,
 * please contact Berkeley Lab's Technology Transfer Department at TTD@lbl.gov.
 *
 * NOTICE.  This software is owned by the U.S. Department of Energy.  As such,
 * the U.S. Government has been granted for itself and others acting on its
 * behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software
 * to reproduce, prepare derivative works, and perform publicly and display
 * publicly.  Beginning five (5) years after the date permission to assert
 * copyright is obtained from the U.S. Department of Energy, and subject to
 * any subsequent five (5) year renewals, the U.S. Government is granted for
 * itself and others acting on its behalf a paid-up, nonexclusive, irrevocable,
 * worldwide license in the Software to reproduce, prepare derivative works,
 * distribute copies to the public, perform publicly and display publicly, and
 * to permit others to do so.
 */

package net.es.netshell.kernel.networking;

import net.es.netshell.kernel.acl.UserAccess;
import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.exec.annotations.SysCall;
import net.es.netshell.kernel.users.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Created by bmah on 12/9/15.
 */
public class CorsaShow {

    private final Logger logger = LoggerFactory.getLogger(CorsaShow.class);

    private static CorsaShow instance;

    private CorsaShow() { }

    public static CorsaShow getInstance() {
        if (instance != null) {
            return instance;
        } else {
            createInstance();
            return instance;
        }
    }

    public static synchronized void createInstance() {
        if (instance == null) {
            instance = new CorsaShow();
        }
    }

    public boolean corsaShow(String sw, String subcommand) {
        Method method = null;
        try {
            method = KernelThread.getSysCallMethod(this.getClass(), "do_corsaExec");

            KernelThread kt = KernelThread.currentKernelThread();
            String currentUserName = kt.getUser().getName();

            logger.info("current user {}", currentUserName);
            Users currentUsers = Users.getUsers();
            // Access per Application
            UserAccess currentUserAccess = UserAccess.getUsers();
            if (currentUsers.isPrivileged(currentUserName) || currentUserAccess.isAccessPrivileged(currentUserName, "fubar")) {
                logger.info("Authorized to execute\n");
                System.out.println("Authorized to execute\n");

                KernelThread.doSysCall(this,
                        method, sw, String.format("show %s", subcommand));
                return true;
            } else {
                return false;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Try to execute ae remote command on a switch
     * @param sw            switch
     * @param command    command
     */
    @SysCall(
            name="do_corsaExec"
    )
    public void do_corsaExec(String sw, String command) {
        logger.info("On " + sw + " executing " + command);
    }

}
