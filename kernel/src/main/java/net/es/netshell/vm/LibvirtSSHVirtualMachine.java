package net.es.netshell.vm;

import java.io.*;
import java.util.Properties;
import com.jcraft.jsch.*;
import net.es.netshell.vm.*;
import java.lang.StringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * created by amercian on 06/15/2015
 */

public class LibvirtSSHVirtualMachine {
    /**
     * Class for SSH functionality into virtual machines
     * Functions: SSH into VM, package installations
     */
     private String remoteVMpassword;
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
