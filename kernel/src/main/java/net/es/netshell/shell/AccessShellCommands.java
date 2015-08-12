package net.es.netshell.shell;

import jline.console.ConsoleReader;
import net.es.netshell.api.FileUtils;
import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.security.FileACL;
import net.es.netshell.kernel.acl.UserAccessProfile;
import net.es.netshell.kernel.acl.UserAccess;
import net.es.netshell.shell.annotations.ShellCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * created by amercian on 07/07/2015
 */

public class AccessShellCommands {
    @ShellCommand(name = "addaccess",
    shortHelp = "Add access to a particular user",
    longHelp = "Required arguments are a username, map.\n" +
	    "addaccess user network:ipconfig:interface:eth0:vlan:10,11 " +
            "The access class should start with either \"network\" or \"user\" or \"vm\"." +
	    "map includes the second layer granuality of access control" ,
    privNeeded = true)
    public static void addAccess(String[] args, InputStream in, OutputStream out, OutputStream err) {
        Logger logger = LoggerFactory.getLogger(AccessShellCommands.class);
        logger.info("addaccess with {} arguments", args.length);

        CommandResponse cmd;
        PrintStream o = new PrintStream(out);

        // Argument checking
        if (args.length != 3) {
            o.println("Usage:  addaccess <username> <map> \n addaccess <username> <network:ipconfig:interface:<eth0>:vlan:<10>> \n addaccess <username> <user:create> \n addaccess <username> <vm:root:10>");
	    /**
             * addaccess user1 network:ipconfig:interface:eth0:vlan:1,2,3
             * addaccess user1 network:vconfig:vlan:10,11,12
             * addaccess user1 user:root  //user can add users with root access
             * addaccess user1 vm:10  //user can add upto 10vms
             * addaccess user1 network:all
             * if map is all, then all types of network functions can be done
             */
	    // the map can be modified for granuality
            return;
        }

        UserAccessProfile newProfile = new UserAccessProfile(args[1], args[2]);

        cmd = UserAccess.getUsers().createAccess(newProfile);

        //if (cmd.isSuccess()) {
        //    o.print("New Access created!");
        //} else {
        //    o.print("Unable to create new Access");
        //}

        o.print(cmd.getMessage());

    }


    @ShellCommand(name = "removeaccess",
            shortHelp = "Remove access from a user",
            longHelp = "Arguments include username, type, map/all. \n")
    public static void removeAccess(String[] args, InputStream in, OutputStream out, OutputStream err) {
        Logger logger = LoggerFactory.getLogger(AccessShellCommands.class);
        logger.info("removeaccess with {} arguments", args.length);

        PrintStream o = new PrintStream(out);

        if (args.length != 3) {
            o.print("Usage: removeaccess <username> <map>");
	    /*
             * <map> can be all to remove all access
             */
	    return;
        }

	UserAccessProfile userProfile = new UserAccessProfile(args[1], args[2]);

             ConsoleReader consoleReader = null;
        try {
            consoleReader = new ConsoleReader(in, out, new NetShellTerminal());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } 

	try {
           // Confirm removal of user account
           o.println("Are you sure you wish to remove access to this user account?");
           String confirmRemove = consoleReader.readLine("Y/N: ");
           if (confirmRemove.equals("Y")) {
                boolean r = UserAccess.getUsers().removeaccess(userProfile);
                if (r) {
                    o.print("Removed User Access!");
                } else {
                    o.print("Unable to remove user.");
                }
           } else {
	            o.print("Canceling operation.");
           }
	} catch (IOException e) {
	        return;
        }
	
    }

/*
    @ShellCommand(name = "acl",
            shortHelp = "Manages file and directories Access Control List",
            longHelp = "acl <file path> : show ACL\n" +
                       "acl <allow|deny> <user> <read|write> <file path>")
    static public void acl(String[] args, InputStream in, OutputStream out, OutputStream err) {

        if (args.length == 1) {
            UserShellCommands.displayACL(".", in, out, err);
            return;
        }

        if (args.length == 2) {
            UserShellCommands.displayACL(args[1],in,out,err);
            return;
        }
        PrintStream o = new PrintStream(out);
        o.println("Not implemented yet");
    }
*/
}
