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

/**
 * lomax@es.net: This class is a clone of JLINE's UnixTerminal, but the TerminalLineSettings is disabled since it required to
 * execute a UNIX shell. Perhaps, in the future, terminal settings might be implemented by retrieving the x/y values
 * from the SSHD session.
 */

package net.es.netshell.shell;


import jline.TerminalSupport;
// import jline.internal.Log;


/**
 * NetShell Terminal emulation
 */
public class NetShellTerminal extends TerminalSupport {

    public NetShellTerminal() throws Exception {
        super(true);
    }


    @Override
    public void init() throws Exception {
        super.init();
        setAnsiSupported(true);
        setEchoEnabled(false);
    }


    @Override
    public void restore() throws Exception {
        super.restore();
    }

    @Override
    public int getWidth() {
        // TODO: should be a option of the constructor
        return 200;
    }

    /**
     * Returns the value of <tt>stty rows>/tt> param.
     */
    @Override
    public int getHeight() {
        // TODO: should be a option of the constructor
        return 25;
    }

    @Override
    public synchronized void setEchoEnabled(final boolean enabled) {
        try {
            super.setEchoEnabled(enabled);
        }
        catch (Exception e) {
//            Log.error("Failed to ", (enabled ? "enable" : "disable"), " echo", e);
            e.printStackTrace();
        }
    }

    public void disableInterruptCharacter() {
        // TODO to be implemented
    }

    public void enableInterruptCharacter() {
        // TODO to be implemented
    }
}
