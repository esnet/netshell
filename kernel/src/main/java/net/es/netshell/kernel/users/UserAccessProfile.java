package net.es.netshell.kernel.users;

import net.es.netshell.api.FileUtils;
import net.es.netshell.kernel.security.FileACL;
import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.users.UserProfile;
import net.es.netshell.kernel.users.Users;
import net.es.netshell.kernel.users.User;
import net.es.netshell.shell.annotations.ShellCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by amercian on 7/7/15.
 */

/**
 * Class to identify the different User Access Profiles based on Netshell Application Management
 * Currently support Network, User and VM Management profiles
 */

public class UserAccessProfile {
    public static final String NETWORK = "network";
    public static final String USER = "user";
    public static final String VM = "vm";

    private String access;
    private String username;

    /**
     * Constructor types
     */
    public UserAccessProfile(){    }

    public UserAccessProfile(String username, String access){
	this.username = username;
	this.access = access;
    }

    public UserAccessProfile(String line) {
        String[] elements = line.split(":");
        if (elements.length != 2) {
            // Incorrect format. Ignore
            return;
        }
        username = elements[0];
        access = elements[1];
    }

    @Override
    public String toString() {
        String line = "";
        line += username + ":";
        line += access;
        return line;
    }

    public void setAccess(String access){
	this.access = access;
    }

    public String getAccess(){
	return this.access;
    }

    public void setName(String username){
	this.username = username;
    }

    public String getName(){
	return this.username;
    } 
}
