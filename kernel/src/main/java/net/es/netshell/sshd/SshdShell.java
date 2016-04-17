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

package net.es.netshell.sshd;

import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.users.User;
import net.es.netshell.shell.Shell;
import org.apache.sshd.common.file.FileSystemAware;
import org.apache.sshd.common.file.FileSystemView;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.command.ScpCommand;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SshdShell extends Shell implements Command, SessionAware, FileSystemAware, Runnable {

    private OutputStream err;
    private ExitCallback callback;
    private Environment environment;
    private Thread thread;
    private ServerSession session;
    private ScpCommand scpCommand;
    private final Logger logger = LoggerFactory.getLogger(SshdShell.class);
    public SshdShell() throws IOException {
        // Constructor for ssh
        super(null, null, null);
        logger.debug("Accepted new SSH connection");
    }

    public SshdShell(String[] command) throws IOException {
        // Constructor for scp (or ssh with a command)
	    super(null, null, command);

	    if (command[0].startsWith("scp")) {
		    scpCommand = new ScpCommand(command[0]);
		    logger.debug("Accepted new SCP connection " + command[0]);
	    } else {
		    logger.debug("Accepted new SSH connection");
	    }
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setInputStream(InputStream in) {
        super.setIn (in);
        if (this.scpCommand != null) {
            this.scpCommand.setInputStream(in);
        }
    }

    public void setOutputStream(OutputStream out) {
        super.setOut(out);
        if (this.scpCommand != null) {
            this.scpCommand.setOutputStream(out);
        }
    }

    public void setErrorStream(OutputStream err) {
        this.err = err;
        if (this.scpCommand != null) {
            this.scpCommand.setErrorStream(err);
        }
    }

    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
        if (this.scpCommand != null) {
            this.scpCommand.setExitCallback(callback);
        }
    }

    public void start(Environment env) throws IOException {
        this.environment = env;

        // Retrieve user
        SShd.TokenId tokenId = this.session.getAttribute(SShd.TOKEN_ID);
        if (tokenId == null) {
            // Should not happen
            throw new RuntimeException("Trying to start an SSH session without users");
        }

        if (!tokenId.accepted) {
            // Not authenticated
            return;
        }
        User user = User.getUser(tokenId.username);
        if (user == null ) {
            // First login from this user
            user = new User(tokenId.username);
        }
        if (this.scpCommand != null) {
            logger.info("Accepted new SCP user=" + user.getName() + " command=" + scpCommand.toString());
        } else {
            logger.info("Accepted new SSH user=" + user.getName());
        }
        // Create a new Thread.
        this.thread = new Thread(user.getThreadGroup(),
                                 this,
                                 "NetShell User= " + user.getName() );
        Thread currentThread = Thread.currentThread();
        KernelThread kt = KernelThread.getKernelThread(this.thread);
        kt.setUser(user);
        this.thread.start();
    }

    public void run() {

        if (this.scpCommand == null) {
            // SSH
            this.startShell();
        } else {
            // SCP
            try {
                scpCommand.start(this.environment);
            } catch (IOException e) {
                e.printStackTrace();
                logger.warn("Exception while scp " + this.scpCommand.toString());
            }

        }
    }

    @Override
    public void destroy() {
        this.callback.onExit(0);
    }


    @Override
    public void setSession(ServerSession serverSession) {
        this.session = serverSession;
    }

    @Override
    public void setFileSystemView(FileSystemView view) {
        if (scpCommand != null) {
            scpCommand.setFileSystemView(view);
        }
    }
}
