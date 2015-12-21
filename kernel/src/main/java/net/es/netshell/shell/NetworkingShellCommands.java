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

import net.es.netshell.kernel.networking.NetworkInterfaces;
import net.es.netshell.shell.annotations.ShellCommand;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Author: sowmya
 * Date: 4/27/15
 * Time: 3:37 PM
 */
public class NetworkingShellCommands {

    @ShellCommand(name = "ipconfig",
            shortHelp = "Configure/List interface parameters",
            longHelp = "Configure/List interface address \n" +
                    "ipconfig interface-name address <address>\n"+
                    "ipconfig interface-name mtu <mtu>\n",
            privNeeded = true)

    static public void ipconfig(String[] args, InputStream in, OutputStream out, OutputStream err) {


        PrintStream o = new PrintStream(out);

        if(args.length==3){
            o.println("Insufficent arguments. Please specify interface name and ip address/mtu in the correct format");
            return;
        }

        NetworkInterfaces interfaces = NetworkInterfaces.getInstance();

        String interfaceName = args[1];

        if(args.length>2) {


            if (args[2].equals("address")) {
                try {
                    InetAddress address = InetAddress.getByName(args[3]);
                    if (interfaces.ipconfig(interfaceName, address)) {
                        o.println("Executed ipconfig");
                    } else {
                        o.println("No permission to execute");
                    }
                } catch (UnknownHostException e) {
                    o.println("Please check the ip address");
                    o.println(e);
                }

            } else if (args[2].equals("mtu")) {
                int mtusize = Integer.parseInt(args[3]);
                if (interfaces.ipconfig(interfaceName, mtusize)) {
                    o.println("Executed ipconfig");
                } else {
                    o.println("No permission to execute");
                }


            }
        }else if(args.length==2){
            boolean result = interfaces.ipconfig(interfaceName, out, err);
            if (result) {
                o.println("Executed ipconfig");
            }else{
                o.println("Error executing ipconfig");
            }

        }

    }

    @ShellCommand(name = "vconfig",
            shortHelp = "VLAN Tagging",
            longHelp = "Tag vlan \n" +
                    "vconfig interface-name vlan-id\n",
            privNeeded = true)

    static public void vconfig(String[] args, InputStream in, OutputStream out, OutputStream err) {


        PrintStream o = new PrintStream(out);

        if(args.length < 3){
            o.println("Please specify interface name and vlan id");
            return;
        }
        String interface_name = args[1];
        String vlanId = args[2];

        int vid = Integer.parseInt(vlanId);
        NetworkInterfaces interfaces = NetworkInterfaces.getInstance();

	if(interfaces.vconfig(interface_name,vid)) {
	     o.println("Executed vconfig");
	} else {
	     o.println("No permission to execute");
	}
   }
}
