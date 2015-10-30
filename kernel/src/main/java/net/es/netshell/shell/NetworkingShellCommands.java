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
                    "ipconfig interface-name address <address>\n"+
                    "ipconfig interface-name mtu <mtu>\n",
            privNeeded = true)

    static public void ipconfig(String[] args, InputStream in, OutputStream out, OutputStream err) {


        PrintStream o = new PrintStream(out);

        if(args.length < 4){
            o.println("Insufficent arguments. Please specify interface name and ip address/mtu in the correct format");
            return;
        }

        NetworkInterfaces interfaces = NetworkInterfaces.getInstance();

        String interfaceName = args[1];
        if(args[2].equals("address")){
            try {
                InetAddress address = InetAddress.getByName(args[3]);
                if (interfaces.ipconfig(interfaceName,address)){
                    o.println("Executed ipconfig");
                } else {
                    o.println("No permission to execute");
                }
            } catch (UnknownHostException e) {
            o.println("Please check the ip address");
            o.println(e);
        }

        }else if(args[2].equals("mtu")){
            int mtusize = Integer.parseInt(args[3]);
            if (interfaces.ipconfig(interfaceName,mtusize)){
                o.println("Executed ipconfig");
            } else {
                o.println("No permission to execute");
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
