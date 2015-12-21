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
package net.es.netshell.vm;

import java.io.*;
import java.io.File;
import java.nio.channels.FileChannel;
import java.lang.System;
import java.lang.StringBuilder;
import java.util.Properties;
import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.es.netshell.vm.*;
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
     public void createAuth(String ip, String keyPairType, LibvirtVirtualMachine vm) throws JSchException, FileNotFoundException, IOException {
	JSch jsch=new JSch();
	//Need to generate this only once
	KeyPair kpair = generateKeyPair(jsch, keyPairType);
	//TODO
	//Unable to do the copying here because couldn't assume root access
     }

     /**
      * SSH into a VM using key-gen authentication
      * Assuming that key-gen has already been generated
      * @param ip 
      * @return session
      * @throws JSchException
      */
     public Session createSessionAuth(String ip) throws JSchException {
	JSch jsch = new JSch();
	jsch.addIdentity(getPrivateKeyFileName());
   	Session session=jsch.getSession("root",ip,22);
	Properties config=new Properties();
	config.put("StrictHostKeyChecking","no");
	session.setConfig(config);
 	session.connect();
	return session;
     }

     /**
      * Function to copies files from one folder to another
      * @param sourceFile src file
      * @param destFile dst file
      */
     public static void copyFile(File sourceFile, File destFile) throws IOException {
	if(!destFile.exists()) {
	      destFile.createNewFile();
	}

	FileChannel source = null;
	FileChannel destination = null;
	try {
	      source = new RandomAccessFile(sourceFile,"rw").getChannel();
	      destination = new RandomAccessFile(destFile,"rw").getChannel();

	      long position = 0;
	      long count    = source.size();

	      source.transferTo(position, count, destination);
	}
        finally {
              if(source != null) {
     	         source.close();
              }
      	      if(destination != null) {
       	        destination.close();
              }
        }
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
     public KeyPair generateKeyPair(JSch jsch, String keyPairType) throws JSchException, FileNotFoundException, IOException{
	int type = 0;
	//Determine how to obtain these
	String filename = generateFileName(keyPairType);
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

     private String generateFileName(String keyPairType) {
	String filename;
	final String user = System.getProperty("user.home");
	filename = String.format("%s/.ssh/",user) + String.format("id_%s",keyPairType);
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
