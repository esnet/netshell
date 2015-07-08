/*
 * Copyright (c) 2014, Regents of the University of California  All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.es.netshell.kernel.users;

import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.security.FileACL;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by amercian on 7/8/15.
 */
public class UserAccessACL extends FileACL {
    public static final String CAN_EXECUTE = "execute";

    public UserAccessACL(Path file) {
        super(file);
    }

    public UserAccessACL(String fileName) { super(fileName);}

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

    public synchronized void denyUserExecute(String username) {
        if (!this.canExecute(username)) {
            // is already denied
            return;
        }
        // Remove user from the list
        String[] users = FileACL.removeUser(this.getCanExecute(),username);
        if (users.length == 0) {
            this.remove(UserAccessACL
                    .CAN_EXECUTE);
        }  else {
            this.setProperty(UserAccessACL.CAN_EXECUTE, FileACL.makeString(users));
        }
    }


    public synchronized void allowUserExecute(String username) {
        if (this.canExecute(username)) {
            // is already allowed
            return;
        }
        // Add user to the list
        this.setProperty(UserAccessACL.CAN_EXECUTE,
                FileACL.makeString(FileACL.addUser(this.getCanExecute(),username)));

    }
    public String[] getCanExecute() {
        String users = this.getProperty(UserAccessACL.CAN_EXECUTE);
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
        } else {
            super.changeACL(user,cmd,aclType);
            return;
        }
        // Save the ACL
        this.store();
    }
}
