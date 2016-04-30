/*
 * ESnet Network Operating System (ENOS) Copyright (c) 2016, The Regents
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
 *
 */

package net.es.netshell.shell;

import net.es.netshell.shell.annotations.ShellCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by lomax on 2/21/14.
 */
public class ShellCommandsFactory {

    private static HashMap<String, Method> shellCommands = new HashMap<String, Method>();

    public static void registerShellModule (Class shellModule) {
        final Logger logger = LoggerFactory.getLogger(ShellCommandsFactory.class);

        Method[] methods = shellModule.getMethods();

        for (Method method : methods) {

            ShellCommand command = method.getAnnotation(ShellCommand.class);
            if (command != null) {
                // This method is command.
                ShellCommandsFactory.shellCommands.put(command.name(),method);
            }
        }
    }

    public static void unregisterShellModule (Class shellModule) {
        final Logger logger = LoggerFactory.getLogger(ShellCommandsFactory.class);

        Method[] methods = shellModule.getMethods();

        for (Method method : methods) {

            ShellCommand command = method.getAnnotation(ShellCommand.class);
            if (command != null) {
                ShellCommandsFactory.shellCommands.remove(command.name());
            }
        }

    }

    public static Method getCommandMethod (String command) {
        return ShellCommandsFactory.shellCommands.get(command);
    }

    public static Set<String> getCommandNames() {
        return shellCommands.keySet();
    }
}
