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
                    "ovs int_addbridge <bridge_name> \n",
            privNeeded = true)

    static public void ovs_addbridge(String[] args, InputStream in, OutputStream out, OutputStream err) {


        PrintStream o = new PrintStream(out);

        if(args.length < 2){
            o.println("Please specify bridge name");
            return;
        }
        String bridgeName = args[1];
        Openvswitch ovs = Openvswitch.getInstance();

        if(ovs.ovs_addbridge(bridgeName)) {
            o.println("Created ovs bridge");
        } else {
            o.println("No permission to create");
        }
    }

}
