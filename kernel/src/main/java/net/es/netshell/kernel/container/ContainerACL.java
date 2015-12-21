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
package net.es.netshell.kernel.container;

import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.security.FileACL;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by lomax on 7/21/14.
 */
public class ContainerACL extends FileACL {
    public static final String CAN_EXECUTE = "execute";
    public static final String CAN_ADMIN = "admin";

    public ContainerACL(Path file) {
        super(file);
    }

    public ContainerACL(String fileName) { super(fileName);}

    public synchronized void allowAdmin(String username) {
        if (this.canAdmin(username)) {
            // is already allowed
            return;
        }
        // Add user to the list
        this.setProperty(CAN_ADMIN,
                FileACL.makeString(FileACL.addUser(this.getCanAdmin(),username)));

    }



    public String[] getCanAdmin() {
        String users = this.getProperty(CAN_ADMIN);
        if (users == null) {
            return new String[0];
        }
        return users.split(",");
    }

    public boolean canAdmin(String username) {
        String[] users = this.getCanAdmin();
        for (String user : users) {
            if (user.equals("*") || user.equals(username)) {
                return true;
            }
        }
        return false;
    }

    public boolean canAdmin() {
        String username = KernelThread.currentKernelThread().getUser().getName();
        String[] users = this.getCanAdmin();
        for (String user : users) {
            if (user.equals("*") || user.equals(username)) {
                return true;
            }
        }
        return false;
    }

    public boolean canExec() {
        String username = KernelThread.currentKernelThread().getUser().getName();
        String[] users = this.getCanExecute();
        for (String user : users) {
            if (user.equals("*") || user.equals(username)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void allowUserAdmin(String username) {
        if (this.canAdmin(username)) {
            // is already allowed
            return;
        }
        // Add user to the list
        this.setProperty(ContainerACL.CAN_ADMIN,
                FileACL.makeString(FileACL.addUser(this.getCanAdmin(),username)));
    }

    public synchronized void denyUserAdmin(String username) {
        if (!this.canAdmin(username)) {
            // is already denied
            return;
        }
        // Remove user from the list
        String[] users = FileACL.removeUser(this.getCanAdmin(),username);
        if (users.length == 0) {
            this.remove(ContainerACL.CAN_ADMIN);
        }  else {
            this.setProperty(ContainerACL.CAN_ADMIN,FileACL.makeString(users));
        }
    }

    public synchronized void denyUserExecute(String username) {
        if (!this.canExecute(username)) {
            // is already denied
            return;
        }
        // Remove user from the list
        String[] users = FileACL.removeUser(this.getCanExecute(),username);
        if (users.length == 0) {
            this.remove(ContainerACL
                    .CAN_EXECUTE);
        }  else {
            this.setProperty(ContainerACL.CAN_EXECUTE, FileACL.makeString(users));
        }
    }


    public synchronized void allowUserExecute(String username) {
        if (this.canExecute(username)) {
            // is already allowed
            return;
        }
        // Add user to the list
        this.setProperty(ContainerACL.CAN_EXECUTE,
                FileACL.makeString(FileACL.addUser(this.getCanExecute(),username)));

    }
    public String[] getCanExecute() {
        String users = this.getProperty(ContainerACL.CAN_EXECUTE);
        if (users == null) {
            return new String[0];
        }
        return users.split(",");
    }

    public boolean canExecute(String username) {
        String[] users = this.getCanExecute();
        for (String user : users) {
            if (user.equals("*") || user.equals(username)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void changeACL(String user, String cmd, String aclType) throws IOException {
        if (aclType.equals("exec")) {
            if (cmd.equals("allow")) {
                this.allowUserExecute(user);
            } else if (cmd.equals("deny")) {
                this.denyUserExecute(user);
            }
        } else if (aclType.equals("admin")) {
            if (cmd.equals("allow")) {
                this.allowUserAdmin(user);
            } else if (cmd.equals("deny")) {
                this.denyUserAdmin(user);
            }
        } else {
            super.changeACL(user,cmd,aclType);
            return;
        }
        // Save the ACL
        this.store();
    }
}
