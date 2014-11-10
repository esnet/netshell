/*
 * ENOS, Copyright (c) $today.date, The Regents of the University of California, through Lawrence Berkeley National Laboratory (subject to receipt of any required approvals from the U.S. Dept. of Energy).  All rights reserved.
 *
 * If you have questions about your rights to use or distribute this software, please contact Berkeley Lab's Technology Transfer Department at TTD@lbl.gov.
 *
 * NOTICE.  This software is owned by the U.S. Department of Energy.  As such, the U.S. Government has been granted for itself and others acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software to reproduce, prepare derivative works, and perform publicly and display publicly.  Beginning five (5) years after the date permission to assert copyright is obtained from the U.S. Department of Energy, and subject to any subsequent five (5) year renewals, the U.S. Government is granted for itself and others acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software to reproduce, prepare derivative works, distribute copies to the public, perform publicly and display publicly, and to permit others to do so.
 */

package net.es.netshell.osgi;

import net.es.netshell.boot.BootStrap;
import net.es.netshell.shell.ShellInputStream;
import net.es.netshell.shell.TabFilteringInputStream;
import net.es.netshell.shell.annotations.ShellCommand;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

import org.apache.felix.gogo.runtime.CommandProcessorImpl;
import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.CommandProcessor;

/**
 * Created by bmah on 10/23/14.
 */
public class OsgiCommands {

    @ShellCommand(name = "gogo",
            forwardLines = false,
            shortHelp = "Access OSGi gogo shell",
            longHelp = "Send EOF on the shell to exit the gogo shell and return to the top-level\n" +
                    "ENOS shell.")
    public static void gogo(String[] args, InputStream in, OutputStream out, OutputStream err) {
        Object obj = BootStrap.getBootStrap().getMediator().getCommandProcessor(ServiceMediator.NO_WAIT);
        if (obj != null) {

            PrintStream os = new PrintStream(out);
            PrintStream es = new PrintStream(err);

            try {
                if (in instanceof ShellInputStream) {
                    if (((ShellInputStream) in).getSourceInputStream() instanceof TabFilteringInputStream) {
                        ((TabFilteringInputStream) ((ShellInputStream) in).getSourceInputStream()).setFilters(true);
                    }
                    ((ShellInputStream) in).setDoEcho(true);
                    ((ShellInputStream) in).setEchoOut(out);
                    ((ShellInputStream) in).setEofHack(false);
                }

                // Try to do something like this:
                //  CommandProcessor cp = (CommandProcessor) obj;
                //  CommandSession cs = cp.createSession(in, os, es);
                //  try {
                //      cs.execute("gosh --login --noshutdown");
                //  }
                //  catch (Exception e) {
                //      e.printStackTrace();
                // }
                // This is really hard because we aren't able to import the correct packages from
                // the bundle (different class loader).  If we try, we aren't able to execute the
                // cast above correctly.  So instead, we use reflection to muck around in the
                // objects' classes (without knowing what class they really are) to find the
                // methods and invoke them.

                // Call createSession to set up a new command session with the right arguments.
                Method m1 = obj.getClass().getMethod("createSession", InputStream.class, PrintStream.class, PrintStream.class);
                Object cs = m1.invoke(obj, in, os, es);

                Method m2 =  cs.getClass().getMethod("execute", CharSequence.class);
                Object retval = m2.invoke(cs, "gosh --login --noshutdown");

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (in instanceof ShellInputStream) {
                    if (((ShellInputStream) in).getSourceInputStream() instanceof TabFilteringInputStream) {
                        ((TabFilteringInputStream) ((ShellInputStream) in).getSourceInputStream()).setFilters(false);
                    }
                    ((ShellInputStream) in).setDoEcho(false);
                    ((ShellInputStream) in).setEofHack(true);
                }
            }
        }
        else {
            PrintStream o  = new PrintStream(out);
            o.print("Can't find gogo shell command processor!");
        }
    }

}
