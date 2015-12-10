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

import jline.console.ConsoleReader;
import net.es.netshell.api.NetShellException;
import net.es.netshell.boot.BootStrap;
import net.es.netshell.shell.annotations.ShellCommand;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class ShellBuiltinCommands {
    /**
     * Dummy command method for shell exit command.
     *
     * This functionality is really handled internally within the shell itself, but
     * we make a command object for it here to allow internal commands to get
     * command completion and help in a consistent way.  This method should never
     * get called.
     *
     * @param args unused
     * @param in unused
     * @param out unused
     * @param err unused
     * @throws net.es.netshell.api.NetShellException
     */
    @ShellCommand(name = "exit",
    shortHelp = "Exit login shell")
    public static void exitCommand(String[] args, InputStream in, OutputStream out, OutputStream err) throws NetShellException {
        throw new NetShellException("Built-in command not handled by shell");
    }

    @ShellCommand(name = "help",
    shortHelp = "Print command information and help",
    longHelp = "With no arguments, print the complete list of commands and abbreviated help.\n" +
            "With one argument, print detailed help on a given command.")
    public static void helpCommand(String[] args, InputStream in, OutputStream out, OutputStream err) throws NetShellException {
        throw new NetShellException("Built-in command not handled by shell");
    }

    @ShellCommand(name = "shutdown",
            shortHelp = "Cleanly shutdown the system.",
            longHelp = "", privNeeded = true)
    public static void shutdownCommand(String[] args, InputStream in, OutputStream out, OutputStream err) throws NetShellException {

        // Confirm shutdown
        ConsoleReader consoleReader = null;
        try {
            consoleReader = new ConsoleReader(in, out, new NetShellTerminal());
            PrintStream o = new PrintStream(out);
            String yorn = consoleReader.readLine("Really exit?");
            if (yorn != null && yorn.equalsIgnoreCase(new String("yes"))) {
                BootStrap.getBootStrap().shutdown();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
