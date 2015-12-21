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

import net.es.netshell.kernel.ovs.Openvswitch;
import net.es.netshell.shell.annotations.ShellCommand;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Author: sowmya
 * Date: 10/30/15
 * Time: 2:35 PM
 */
public class OpenvswitchShellCommands {

    @ShellCommand(name = "ovs_addbridge",
            shortHelp = "Create ovs bridge",
            longHelp = "Create ovs beidge \n" +
                    "ovs_addbridge <bridge_name> \n",
            privNeeded = true)

    static public void ovs_addbridge(String[] args, InputStream in, OutputStream out, OutputStream err) {


        PrintStream o = new PrintStream(out);

        if (args.length < 2) {
            o.println("Please specify bridge name");
            return;
        }
        String bridgeName = args[1];
        Openvswitch ovs = Openvswitch.getInstance();

        if (ovs.ovs_addbridge(bridgeName)) {
            o.println("Created ovs bridge");
        } else {
            o.println("No permission to create");
        }
    }

    @ShellCommand(name = "ovs_removebridge",
            shortHelp = "Delete ovs bridge",
            longHelp = "Delete ovs beidge \n" +
                    "ovs_removebridge <bridge_name> \n",
            privNeeded = true)

    static public void ovs_removebridge(String[] args, InputStream in, OutputStream out, OutputStream err) {


        PrintStream o = new PrintStream(out);

        if (args.length < 2) {
            o.println("Please specify bridge name");
            return;
        }
        String bridgeName = args[1];
        Openvswitch ovs = Openvswitch.getInstance();

        if (ovs.ovs_removebridge(bridgeName)) {
            o.println("Removed ovs bridge");
        } else {
            o.println("No permission to create");
        }
    }


    @ShellCommand(name = "ovs_addport",
            shortHelp = "Add interface/port to ovs bridge",
            longHelp = "Add interface/port to ovs bridge \n" +
                    "ovs_addport <bridge_name> <interface_name> <vlan-tag> \n" +
                    "bridge_name and interface_name are mandatory. vlan-tag is optional",
            privNeeded = true)

    static public void ovs_addport(String[] args, InputStream in, OutputStream out, OutputStream err) {


        PrintStream o = new PrintStream(out);

        if (args.length < 3) {
            o.println("Insufficient arguments. Please specify bridge name and interface name");
            return;
        }
        String bridgeName = args[1];
        String portName = args[2];
        String vlanTag = "";
        if (args.length == 4) {
            vlanTag = args[4];
        }
        Openvswitch ovs = Openvswitch.getInstance();

        if (ovs.ovs_addport(bridgeName, portName, vlanTag)) {
            o.println("Added ovs port to bridge");
        } else {
            o.println("No permission to create");
        }
    }


    @ShellCommand(name = "ovs_removeport",
            shortHelp = "Remove interface/port to ovs bridge",
            longHelp = "Remove interface/port to ovs bridge \n" +
                    "ovs_removeport <bridge_name> <interface_name> \n" +
                    "bridge_name and interface_name are mandatory. vlan-tag is optional",
            privNeeded = true)

    static public void ovs_removeport(String[] args, InputStream in, OutputStream out, OutputStream err) {


        PrintStream o = new PrintStream(out);

        if (args.length < 3) {
            o.println("Insufficient arguments. Please specify bridge name and interface name");
            return;
        }
        String bridgeName = args[1];
        String portName = args[2];

        Openvswitch ovs = Openvswitch.getInstance();

        if (ovs.ovs_removeport(bridgeName, portName)) {
            o.println("Removed ovs port from bridge");
        } else {
            o.println("No permission to create");
        }
    }


}
