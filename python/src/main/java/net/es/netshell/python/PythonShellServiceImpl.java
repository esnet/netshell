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
package net.es.netshell.python;

import net.es.netshell.shell.PythonShellService;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * OSGi service interface to the PythonShell class.
 * This class isn't really OSGi specific, but it exists so that we can
 * instantiate an object that can be passed to the OSGi service registry.
 * Its only purpose is to invoke static methods in the PythonShell class.
 * The (already existing) shell command APIs to PythonShell more or less
 * dictate that that class has largely static methods, else we could
 * make the implementation of PythonShell a singleton.
 */
public class PythonShellServiceImpl implements PythonShellService {
    @Override
    public void startPython(String[] args, InputStream in, OutputStream out, OutputStream err) {
        PythonShell.startPython(args, in, out, err);
    }
    @Override
    public String getProgramPath(String cmd) {
        return PythonShell.getProgramPath(cmd);
    }

    @Override
    public void startPython(InputStream cmdInputStream, String[] args, InputStream in, OutputStream out, OutputStream err) {
        PythonShell.startPython(cmdInputStream,args,in,out,err);
    }
}
