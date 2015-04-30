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
            shortHelp = "Configure interface",
            longHelp = "Configure interface address \n" +
                    "ipconfig interface-name address\n",
            privNeeded = true)

    static public void ipconfig(String[] args, InputStream in, OutputStream out, OutputStream err) {


        PrintStream o = new PrintStream(out);

        if(args.length < 3){
            o.println("Please specify interface name and ip address");
            return;
        }
        String interfaceName = args[1];
        String ipAddress = args[2];
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            NetworkInterfaces interfaces = NetworkInterfaces.getInstance();

            interfaces.ipconfig(interfaceName,address);
        } catch (UnknownHostException e) {
            o.println("Please check the ip address");
            o.println(e);
        }

        o.println("Executed ipconfig");
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

        interfaces.vconfig(interface_name,vid);


        o.println("Executed vconfig");
    }

}
