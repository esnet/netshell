package net.es.netshell.vm;

import java.io.*;
import java.util.Properties;
import com.jcraft.jsch.*;
import net.es.netshell.vm.*;
import java.lang.StringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.es.netshell.vm.LibvirtVirtualMachine;

/**
 * created by amercian on 06/15/2015
 */

public class LibvirtSSHVirtualMachine {
    /**
     * Class for SSH functionality into virtual machines
     * Functions: SSH into VM, package installations
     */
     private String remoteVMpassword;
     private String prvKeyFilename;

     // to log output
     private static final Logger LOGGER = LoggerFactory.getLogger(LibvirtSSHVirtualMachine.class);

     /** 
     * Executing SSH command into the remote VM and passing command, this function describes commands such as ls, pwd
     * @param ip Remote VM IP addres
     * @param command Command to execute
     * @return Returns the message from the host machine
     * @throws JSchException SSH Exception, 
     * @throws IOException Reading remote results
     */ 
     public String commandExecute(String ip, String command) throws JSchException, IOException {
	
	JSch jsch=new JSch();
	
	Session session=jsch.getSession("root", ip, 22);
	session.setPassword(remoteVMpassword);
	Properties config = new Properties();
	config.put("StrictHostKeyChecking", "no");
	session.setConfig(config);
	session.connect();

	ChannelExec channel=(ChannelExec) session.openChannel("exec");
	
	BufferedReader in=new BufferedReader(new InputStreamReader(channel.getInputStream()));
	channel.setCommand(command);
	//command = "ls -l"
	//command = "mkdir dir"
	channel.setErrStream(System.err);
	channel.connect();

	// Capturing the message
	String msg=null;
	StringBuilder stringBuilder = new StringBuilder();
	while((msg=in.readLine())!=null){
	  //System.out.println(msg);
	  stringBuilder.append(msg);
	}

	channel.disconnect();
	session.disconnect();
	System.out.println("Exit code: " + channel.getExitStatus());
	
	return stringBuilder.toString();
     }

     /**
      * Executing SCP function using Secure FTP tunnel
      * @param name name of file to copy
      * @throws JSchException SSH exception
      * @throws IOException I/O exception
      * @throws SftpException FTP exception
      */
     public void remoteCopy(String ip, String name) throws JSchException, IOException, SftpException {
	JSch jsch = new JSch();

	Session session = jsch.getSession("root", ip, 22);
	session.setPassword(remoteVMpassword);
	Properties config = new Properties();
	config.put("StrictHostKeyChecking", "no");
	session.setConfig(config);
	session.connect();

	ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");

	channel.connect();

	channel.put(String.format("/root/%s",name),String.format("%s",name));

	channel.disconnect();
	session.disconnect(); 
     }

     /**
      * Exclusive function to delete a session
      */
     public void disconnectSession(Session session){
	session.disconnect();
     }

     /**
      * create a session using private key
      * @param ip IP address
      * @throws JSchException SSH exception
      * @throws FileNotFoundException
      * @throws IOException
      */
     public Session createSessionAuth(String ip, String keyPairType, LibvirtVirtualMachine vm) throws JSchException, FileNotFoundException, IOException {
	JSch jsch=new JSch();
	//Need to generate this only once
	KeyPair kpair = generateKeyPair(jsch, keyPairType, vm);
	jsch.addIdentity(getPrivateKeyFileName());
	Session session=jsch.getSession("root",ip,22);
	Properties config=new Properties();
	config.put("StrictHostKeyChecking","no");
	session.setConfig(config);
	//currently disposing key pair to avoid multiple key pairs
	kpair.dispose();
	return session;
     }

     /**
      * Function to generate a Key Pair
      * @param keyPairType RSA or DSA 
      * @param jsch the SSH session
      * @return KeyPair generated
      * @throws JSchException KeyGeneration exception
      * @throws FileNotFoundException for writing key-gen file
      * @throws IOException 
      */
     public KeyPair generateKeyPair(JSch jsch, String keyPairType, LibvirtVirtualMachine vm) throws JSchException, FileNotFoundException, IOException{
	int type = 0;
	//Determine how to obtain these
	String filename = generateFileName(vm.getName(), keyPairType);
	String comment = "";
	String passphrase = "";
	
	if(keyPairType.equals("rsa")){type=KeyPair.RSA;}
        else if(keyPairType.equals("dsa")){type=KeyPair.DSA;}
	else{type=KeyPair.RSA;} //default

	KeyPair kpair=KeyPair.genKeyPair(jsch, type);
	kpair.setPassphrase(passphrase);
	kpair.writePrivateKey(filename);
	setPrivateKeyFileName(filename);
	kpair.writePublicKey(filename+".pub", comment);
	System.out.println("Finger print: "+kpair.getFingerPrint());
	//kpair.dispose();
	return kpair;
     }

     private String generateFileName(String name, String keyPairType) {
	String filename;
	filename = String.format("~/.ssh/%s/",name) + String.format("id_%s",keyPairType);
	return filename;
     }

    private void setPrivateKeyFileName(String prvKeyFilename) {
	this.prvKeyFilename = prvKeyFilename;
    }

    private String getPrivateKeyFileName() {
	return this.prvKeyFilename;
    }

     /**
      * Setting the root password for VM
      * @param remoteVMpassword password of VM
      */
     public void setPassword(String remoteVMpassword){
	this.remoteVMpassword = remoteVMpassword;
     }

     /**
      * Getting the root password for VM
      * @return the password of VM
      */
     public String getPassword(String remoteVMpassword){
	return this.remoteVMpassword;
     }
}
