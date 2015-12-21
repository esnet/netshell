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

package net.es.netshell.shell;

import net.es.netshell.kernel.container.Container;
import net.es.netshell.kernel.container.ContainerACL;
import net.es.netshell.kernel.container.Containers;
import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.shell.annotations.ShellCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by lomax on 7/28/14.
 */
public class ContainerShellCommands {

    public static void mkContainer (String[] args, InputStream in, OutputStream out, OutputStream err) {
        Logger logger = LoggerFactory.getLogger(ContainerShellCommands.class);
        logger.info("mkcontainer with {} arguments", args.length);

        PrintStream o = new PrintStream(out);

        // Argument checking
        if (args.length < 2) {
            o.println("Usage mkcontainer <name>");
            return;
        }
        String name = args[2];
        try {
            Containers.createContainer(name);
        } catch (SecurityException e) {
            o.println("Not authorized to create " + e.getMessage());
            return;
        } catch (InvocationTargetException e) {
            o.println("failed with " + e.getTargetException().getMessage());
            return;
        } catch (Exception e) {
            o.println("Failed with " + e.toString());
        }
        o.println("container " + name + " is created");
    }

    @ShellCommand(name = "join",
            shortHelp = "Joins a container",
            longHelp = "join <name>")
    public static void mkJoin (String[] args, InputStream in, OutputStream out, OutputStream err) {
        Logger logger = LoggerFactory.getLogger(ContainerShellCommands.class);
        logger.info("join with {} arguments", args.length);

        PrintStream o = new PrintStream(out);

        // Argument checking
        if (args.length < 2) {
            o.println("Usage join <name>");
            return;
        }
        String name = args[1];
        try {
            KernelThread.currentKernelThread().joinContainer(name);
        } catch (SecurityException e) {
            o.println("Failed: " + e.getMessage());
            return;
        }
        o.println("container " + name + " is now the current container");
    }

    @ShellCommand(name = "leave",
            shortHelp = "Leaves the current container and re-join, if any, the previously joined container.",
            longHelp = "leave")
    public static void leave (String[] args, InputStream in, OutputStream out, OutputStream err) {
        Logger logger = LoggerFactory.getLogger(ContainerShellCommands.class);
        logger.info("leave with {} arguments", args.length);

        PrintStream o = new PrintStream(out);
        KernelThread.currentKernelThread().leaveContainer();

    }

    @ShellCommand(name = "container",
            shortHelp = "administrate a container.",
            longHelp = "container create <container name> : creates a container in the current container\n" +
                       "container list : list the sub containers of the current container\n" +
                       "container acl <container name>  : show access control list\n" +
                       "container <allow|deny> <user> <access|exec|admin> <container name> \n")
    public static void container (String[] args, InputStream in, OutputStream out, OutputStream err) {
        Logger logger = LoggerFactory.getLogger(ContainerShellCommands.class);
        logger.info("container with {} arguments", args.length);


        PrintStream o = new PrintStream(out);
        if (args.length == 1) {
            // Needs at list one option
            o.println("needs at least one option. try help container");
            return;
        }

        if (args[1].equals("create")) {
            ContainerShellCommands.mkContainer(args,in,out,err);
            return;
        } else if (args[1].equals("acl")) {
            ContainerShellCommands.showACL(args,in,out,err);
            return;
        } else if (args[1].equals("allow") || (args[1].equals("deny"))) {
            ContainerShellCommands.changeACL(args,in,out,err);
        }

    }

    public static void showACL(String[] args, InputStream in, OutputStream out, OutputStream err) {
        PrintStream o = new PrintStream(out);
        if (args.length != 3) {
            o.println("container name is missing");
            return;
        }
        Container container = new Container(args[2]);
        ContainerACL acl = container.getACL();
        String[] users;
        users = acl.getCanRead();
        o.println("Read Access:");
        if ((users == null) || (users.length == 0)) {
            o.println("    None");
        } else {
            o.print("    ");
            for (String user : users) {
                o.print(user + ",");
            }
            o.println("\n");
        }
        users = acl.getCanWrite();
        o.println("Write Access:");
        if ((users == null) || (users.length == 0)) {
            o.println("    None");
        } else {
            o.print("    ");
            for (String user : users) {
                o.print(user + ",");
            }
            o.println("\n");

        }
        users = acl.getCanAdmin();
        o.println("Administrative Access:");
        if ((users == null) || (users.length == 0)) {
            o.println("    None");
        } else {
            o.print("    ");
            for (String user : users) {
                o.print(user + ",");
            }
            o.println("\n");
        }
        users = acl.getCanExecute();
        o.println("Execution Access:");
        if ((users == null) || (users.length == 0)) {
            o.println("    None");
        } else {
            o.print("    ");
            for (String user : users) {
                o.print(user + ",");
            }
            o.println("\n");
        }


    }

    public static void changeACL(String[] args, InputStream in, OutputStream out, OutputStream err) {
        PrintStream o = new PrintStream(out);
        if (args.length != 5) {
            o.println("syntax help, please try help container");
            return;
        }
        String cmd = args[1];
        String user = args[2];
        String aclType = args[3];
        String containerName = args[4];

        try {
            Container container = new Container(containerName);
            ContainerACL acl = container.getACL();
            acl.changeACL(user,cmd,aclType);
        } catch (Exception e) {
            o.print("failed: can not change ACL: " + e.getMessage());
            return;
        }
        String[] newArgs  = {"container","acl",containerName};
        showACL(newArgs,in,out,err);
    }

}
