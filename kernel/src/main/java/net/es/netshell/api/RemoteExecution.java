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
package net.es.netshell.api;

import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
//import org.apache.sshd.common.util.TeePipedOutputStream;


import org.apache.sshd.common.util.NoCloseOutputStream;
import org.apache.sshd.common.util.NoCloseInputStream;
import org.apache.sshd.client.channel.ChannelExec;

import java.io.*;
import java.lang.InterruptedException;

public class RemoteExecution {

	public static void sshExec(String host, int port, String login, String password, String command) throws InterruptedException, IOException{
        SshClient client = SshClient.setUpDefaultClient();
    	client.start();

        try {
            ClientSession session = client.connect(login,host, port).await().getSession();
            int response = ClientSession.WAIT_AUTH;
            while ((response & ClientSession.WAIT_AUTH) != 0) {
                session.authPassword(login, password);
                response = session.waitFor(ClientSession.WAIT_AUTH | ClientSession.CLOSED | ClientSession.AUTHED, 0);
            }

            ChannelExec channel = session.createExecChannel(command);
            channel.setOut(new NoCloseOutputStream(System.out));
            channel.setErr(new NoCloseOutputStream(System.err));
            channel.open();

            channel.waitFor(ClientChannel.CLOSED, 120000);
            channel.close(true);
            session.close(true);
            client.stop();
    } finally {
        client.stop();
    }

}
	

}
