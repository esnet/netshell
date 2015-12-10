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
package net.es.netshell.kernel.acl;

import net.es.netshell.api.*;
import net.es.netshell.configuration.NetShellConfiguration;
import net.es.netshell.kernel.security.FileACL;
import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.exec.annotations.SysCall;
import net.es.netshell.kernel.acl.UserAccessProfile;
import net.es.netshell.kernel.acl.UserAccessACL;
import net.es.netshell.kernel.acl.NetworkManageProfile;
import net.es.netshell.kernel.acl.UserManageProfile;
import net.es.netshell.kernel.acl.VMManageProfile;
import net.es.netshell.kernel.users.Users;
import net.es.netshell.kernel.users.User;
import net.es.netshell.shell.annotations.ShellCommand;
import net.es.netshell.shell.CommandResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
/**
 * Created by amercian on 7/1/15.
 */

public final class UserAccess {
   /**
    * Extends Access Shell application
     * Creates a folder /netshell-root/etc/acl/network/vconfig/acl/username
     * Creates the table of users and privilege profiles in /netshell-root/etc/netshell.user.access
    */

    private final static UserAccess users = new UserAccess();

    List<String> privArray = Arrays.asList(UserAccessProfile.NETWORK, UserAccessProfile.USER, UserAccessProfile.VM);
  
    public final static String USERS_DIR = "acl";

    private Path aclFilePath; // Useful for checking if acl exists or not
    private Path NetShellRootPath;
    // Hash Table: key = user, value = accesses
    // To include duplicate key but no duplicate values using SetMultimap
    private SetMultimap<String, String> NetworkAccessList = HashMultimap.create();
    private SetMultimap<String, String> UserAccessList = HashMultimap.create();
    private SetMultimap<String, String> VMAccessList = HashMultimap.create();
    private final Logger logger = LoggerFactory.getLogger(UserAccess.class);


    public UserAccess() {
        // Figure out the NetShell root directory.
        String NetShellRootDir = NetShellConfiguration.getInstance().getGlobal().getRootDirectory();
        this.NetShellRootPath = Paths.get(NetShellRootDir).normalize();
	this.aclFilePath = FileUtils.toRealPath("/etc/acl");

        // Read acl user file or create it if necessary
        UserAccessProfile user = new UserAccessProfile();
        /*String username = user.getName();
	String map = user.getMap();
	String[] access = map.split(":");
        try {
            this.readUserFile(username, access[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    public static UserAccess getUsers() {
        return users;
    }

    public CommandResponse createAccess(UserAccessProfile user) {
        Method method = null;
        CommandResponse commandResponse;
        String resMessage = null;
        boolean resCode = false;
        try {
            method = KernelThread.getSysCallMethod(this.getClass(), "do_createAccess");

            // Only ROOT user can perform this function
            if (KernelThread.currentKernelThread().isPrivileged()) {
                KernelThread.doSysCall(this, method, user); //removed "true"
                resCode = true;
                resMessage = "User access added";
            } else {

                resCode = false;
                resMessage = "Operation Not Permitted";
            }

        } catch (UserAlreadyExistException e) {
	    e.printStackTrace();
            resCode = false;
            resMessage = "User access already exists";
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            resCode = false;
            resMessage = "Method not implemented";
        }catch(UserClassException e){
       	    e.printStackTrace();
     	    resCode = false;
            resMessage = "User access must be network or user or vm";
        }catch (Exception e) {
	    e.printStackTrace();
            resCode = false;
            resMessage = "Error in operation";
        }

        commandResponse = new CommandResponse(resMessage,resCode);
        return commandResponse;
    }

    @SysCall(
            name="do_createAccess"
    )
    public void do_createAccess (UserAccessProfile newUser) throws UserAlreadyExistException, UserException, UserClassException, IOException {
        logger.info("do_createAccess entry");
        String username = newUser.getName();
	String map = newUser.getMap();
	String[] access = map.split(":");

        // Make sure application acl is available 
        if (!privArray.contains(access[0])) {
            throw new UserClassException(access[0]);
        }

         // Checks if the user access already exists
	    try {
		    this.readUserFile(username, access[0]);
	    } catch (IOException e) {
		    logger.error("Cannot read access file");
	    }

	// check which access profile should be performed and lists are created
	if(access[0].equals("network")){
            this.NetworkAccessList.put(username, map);
	    NetworkManageProfile user = new NetworkManageProfile(username, map);
	}
	else if(access[0].equals("user")){
            this.UserAccessList.put(username, map);
	    UserManageProfile user = new UserManageProfile(username, map);
	}
	else if(access[0].equals("vm")){
            this.VMAccessList.put(username, map);
	    VMManageProfile user = new VMManageProfile(username, map);
	}
	
	// Create hash table with list of user/access and write to /etc/netshell.user.access
	// Including functionality to have duplicates in (hash) table
	// Remove dependency on map TODO
	this.writeUserFile(username, access[0]); 

	// Create home directory
        File homeDir = new File (Paths.get(this.getHomePath().toString(), access[0], username).toString());

	/* In case directory is required */
        //homeDir.mkdirs();
       
        // Create access only to specific application with path = netshell-root/acl/network/.acl/<username>
        UserAccessACL acl = new UserAccessACL(homeDir.toPath());
        acl.allowUserRead(username);
        acl.allowUserWrite(username);
	acl.allowUserExecute(username);
        // Commit ACL's
        //acl.store();
    }

    public boolean removeaccess (UserAccessProfile user) {
        Method method = null;
        KernelThread kt = KernelThread.currentKernelThread();
        String currentUserName = kt.getUser().getName();
	String userName = user.getName();
	String map = user.getMap();

        try {
            method = KernelThread.getSysCallMethod(this.getClass(), "do_removeAccess");

            if ((currentUserName.equals(userName)) ||
                    Users.isPrivileged(currentUserName)) {
                logger.info("OK to remove");
                KernelThread.doSysCall(this,
                        method,
                        user);
            }
        } catch (NonExistentUserException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (! Users.isPrivileged(currentUserName) || currentUserName.equals(userName)) {
            // End user session if not privileged account (unless root removed own account)
            kt.getThread().interrupt();
        }
        return true;
    }


    @SysCall(
            name="do_removeAccess"
    )
    public void do_removeAccess(UserAccessProfile user) throws NonExistentUserException, IOException {
        logger.info("do_removeAccess entry");
        String username = user.getName();
	String map = user.getMap();
	String[] access = map.split(":");

        // Make sure the user exists.
	    try {
		    this.readUserFile(username, access[0]);
	    } catch (IOException e) {
		    logger.error("Cannot read access file");
	    }

	// Remove entry from the multimap
	if(access[0].equals("network")){
	    this.NetworkAccessList.remove(username, map);
	} else if(access[0].equals("user")){
	    this.UserAccessList.remove(username, map);
	} else if(access[0].equals("vm")){
	    this.VMAccessList.remove(username, map);
	}   

        // Delete .acl file associated with this user account
	//File aclDelete = new File (Paths.get(this.getHomePath().toString(), access[0], ".acl", username).toString());
	//aclDelete.delete();

        // Save entry in the /etc/netshell.user.access
        this.writeUserFile(username, access[0]);

    }

    /**
     * Function to check if username has access privilege. 
     * Can be called from command with respective application name
     * @param username accessing user
     * @param map accessing application
     * @return TRUE or FALSE
     */
    public static boolean isAccessPrivileged (String username, String map) throws IOException{

	// Access will depend on application
	// map should be decoded for each application
	String access;
	String[] accessInList;
	if(map.contains(":")){
	   accessInList = map.split(":");
	   access = accessInList[0];
	} else {
	   access = map;
	}
	
        if ( access.equals("network") && UserAccess.getUsers().NetworkAccessList.isEmpty() && username.equals("admin")) {
            // Initial configuration. Add admin user and create configuration file.
            return true;
        } else if (access.equals("user") && UserAccess.getUsers().UserAccessList.isEmpty() && username.equals("admin")) {
	    return true;
	} else if (access.equals("vm") && UserAccess.getUsers().VMAccessList.isEmpty() && username.equals("admin")) {
	    return true;
	}
	
	if (username.equals(null)) {
            // Not a user
            return false;
        } else if (/*accesslist.contains(access) ||*/ Users.isPrivileged(username)) {
	    return true;
	} else if (UserAccess.getUsers().NetworkAccessList.containsKey(username) && access.equals("network") && NetworkManageProfile.isPrivileged(username, map)) {
	    return true;
	} else if (UserAccess.getUsers().UserAccessList.containsKey(username) && access.equals("user") && UserManageProfile.isPrivileged(username, map)) {
	    return true;
	} else if (UserAccess.getUsers().VMAccessList.containsKey(username) && access.equals("vm") && VMManageProfile.isPrivileged(username, map)) {
	    return true;
	} else {
	    // Any other case
	    return false;
	}
    }

    private synchronized void readUserFile(String username, String access) throws IOException {
       	this.aclFilePath = FileUtils.toRealPath(String.format("/etc/acl/%s",access));
        File aclFile = new File(this.aclFilePath.toString());
        aclFile.getParentFile().mkdirs();
        if (!aclFile.exists()) {
            // File does not exist yet, create it.
            if (!aclFile.createNewFile()) {
                // File could not be created, return a RuntimeError
                throw new RuntimeException("Cannot create " + this.aclFilePath.toString());
            }
        }
        BufferedReader reader = new BufferedReader(new FileReader(aclFile));
        String line = null;

        while ((line = reader.readLine()) != null) {
            UserAccessProfile p = new UserAccessProfile(line);
            if (p.getName() != null && access.equals("network")) {
                this.NetworkAccessList.put(p.getName(), p.getMap()); 
            }
            else if (p.getName() != null && access.equals("user")) {
                this.UserAccessList.put(p.getName(), p.getMap()); 
            }
	    if (p.getName() != null && access.equals("vm")) {
                this.VMAccessList.put(p.getName(), p.getMap()); 
            }
	    else {
                logger.error("Malformed user entry:  {}", line);
            }
        }
    }

    private synchronized void writeUserFile(String username, String access) throws IOException {
	this.aclFilePath = FileUtils.toRealPath(String.format("/etc/acl/%s",access));
        File aclFile = new File(this.aclFilePath.toString());
	aclFile.delete();
        BufferedWriter writer = new BufferedWriter(new FileWriter(aclFile));
	if(access.equals("network")){
            for (Map.Entry p : this.NetworkAccessList.entries() ) {
                if (p.getKey() != null) {
	  	    UserAccessProfile newEntry = new UserAccessProfile(p.getKey().toString(), p.getValue().toString());
		    //System.out.println("Key: " + p.getKey().toString() + "\t Value: " + p.getValue().toString() + "\n");
                    writer.write(newEntry.toString());
                    writer.newLine();
                }
            }
            writer.flush();
            writer.close();
	} else	if(access.equals("user")){
            for (Map.Entry p : this.UserAccessList.entries() ) {
                if (p.getKey() != null) {
	  	    UserAccessProfile newEntry = new UserAccessProfile(p.getKey().toString(), p.getValue().toString());
                    writer.write(newEntry.toString());
                    writer.newLine();
                }
            }
            writer.flush();
            writer.close();
	} else if(access.equals("vm")){
            for (Map.Entry p : this.VMAccessList.entries() ) {
                if (p.getKey() != null) {
	  	    UserAccessProfile newEntry = new UserAccessProfile(p.getKey().toString(), p.getValue().toString());
                    writer.write(newEntry.toString());
                    writer.newLine();
                }
            }
            writer.flush();
            writer.close();
	}
    }

    public Path getNetShellRootPath() { return NetShellRootPath; }

    public Path getHomePath() {
        return NetShellRootPath.resolve(USERS_DIR);
    }

    public Path getHomePath(String username) {
        return getHomePath().resolve(username);
    }
  

} 
