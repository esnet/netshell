package net.es.netshell.rexec;

/**
 * Author: sowmya
 * Date: 1/15/16
 * Time: 2:32 PM
 */

import org.apache.sshd.SshClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.File;

import java.lang.InterruptedException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.sshd.common.util.SecurityUtils;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.future.AuthFuture;

public class SSHRemoteExecution implements RemoteExecution {

    private String host;
    private int port;
    private String username;
    private String password;
    private String command;
    private String keyFile;
    private int timeout;


    private InputStream in;
    private OutputStream out;

    private PipedInputStream accessRemoteOutputStream;
    private PipedOutputStream connectionOutputStream;

    private SshClient client;
    private List<KeyPair> keyPair;

    public String getHost() {

        return host;
    }

    public void setHost(String host) {

        this.host = host;
    }

    public int getPort() {

        return port;
    }

    public void setPort(int port) {

        this.port = port;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

    public String getCommand() {

        return command;
    }

    public void setCommand(String command) {

        this.command = command;
    }

    public String getKeyFile() {

        return keyFile;
    }

    public int getTimeout() {

        return timeout;
    }

    public void setTimeout(int timeout) {

        this.timeout = timeout;
    }

    //return true if key was successfully loaded from the directory
    public boolean setKeyDirectory(String keyDirectory) {

        this.keyFile = keyDirectory;
        return loadkeys();

    }

    private boolean loadkeys() {

        List<String> files = new ArrayList<String>();
        File f = new File(this.keyFile);
        if (f.exists() && f.isFile() && f.canRead()) {
            files.add(f.getAbsolutePath());
        }
        if (files.size() > 0) {
            try {

                //need bouncy-provider and bouncy-pkix-openssl jar file for this line to work.
                if (SecurityUtils.isBouncyCastleRegistered()) {
                    FileKeyPairProvider fileKeyPairProvider = new FileKeyPairProvider(files.toArray(new String[files.size()]));
                    Iterable<KeyPair> keys = fileKeyPairProvider.loadKeys();
                    KeyPair key = null;
                    while (keys.iterator().hasNext()) {
                        key = keys.iterator().next();
                        keyPair.add(key);

                    }

                } else {
                    System.out.println("Bouncy castle check failed");
                }
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(1);
            }
        }

        return false;
    }

    public InputStream getIn() {

        return in;
    }

    public void setIn(InputStream in) {

        this.in = in;
    }

    public OutputStream getOut() {

        return out;
    }

    public void setOut(OutputStream out) {

        this.out = out;
    }

    public PipedInputStream getAccessRemoteOutputStream() {

        return accessRemoteOutputStream;
    }

    //Constructors
    public SSHRemoteExecution() {

        this("localhost", 8000);

    }

    public SSHRemoteExecution(String host, int port) {

        this.host = host;
        this.port = port;

    }

    public SSHRemoteExecution(String host, int port, String username, String keyFile) {

        this.host = host;
        this.port = port;
        this.username = username;
        this.timeout = 60000;

        this.setKeyDirectory(keyFile);

    }

    @Override
    public synchronized void exec() {

        client.start();
        ClientSession session=null;

        try{
            client.connect(username, host, port).await().getSession();
        }catch(IOException e){
            try {
                writeToOutputStream("Cannot connect to host/port with the username");
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }catch(InterruptedException ie){
            try {
                writeToOutputStream("Interrupted Exception. Cannot connect to host/port, username");
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

        if(session == null){
            try {
                writeToOutputStream("Error creating session. Exiting");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }


        //add ssh key to session
        if (keyPair != null && keyPair.size() > 0) {
            session.addPublicKeyIdentity(keyPair.get(0));

        } else {
            try {
                writeToOutputStream("Error adding key. Cannot connect without ssh keys");
            } catch (IOException e) {
                e.printStackTrace();//just print to standard out
            }

        }

        //try auth
        try {
            AuthFuture auth = session.auth();
            auth.verify();

        } catch (IOException e) {
            try {
                writeToOutputStream("Auth failed");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        int ret = ClientSession.WAIT_AUTH;
        while ((ret & ClientSession.WAIT_AUTH) != 0) {
            ret = session.waitFor(ClientSession.WAIT_AUTH | ClientSession.CLOSED | ClientSession.AUTHED, 0);
        }
        if ((ret & ClientSession.CLOSED) != 0) {
            try {
                writeToOutputStream("SSH connection error: Error authenticating. Please check your ssh keys");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //send command

        try {

            writeToOutputStream("Connecting input and output streams");
            connectionOutputStream = new PipedOutputStream();

            accessRemoteOutputStream = new PipedInputStream();
            accessRemoteOutputStream.connect(connectionOutputStream);

            ChannelExec execChannel = session.createExecChannel(command);
            execChannel.open();
            execChannel.setOut(connectionOutputStream);

            int response = ClientSession.CLOSED;
            response = execChannel.waitFor(ChannelExec.CLOSED, 0);

            execChannel.waitFor(ChannelExec.CLOSED, 0);

            execChannel.close(true);
            session.close(true);
            client.close(true);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void writeToOutputStream(String message) throws IOException {

        byte[] byteMessage = message.getBytes();
        out.write(byteMessage);

    }
}
