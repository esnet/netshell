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

            channel.waitFor(ClientChannel.CLOSED, 3000);
            channel.close(true);
            session.close(true);
            client.stop();
    } finally {
        client.stop();
    }

}
	

}