package net.es.netshell.kernel.users;

import net.es.netshell.api.*;
import net.es.netshell.configuration.NetShellConfiguration;
import net.es.netshell.kernel.security.FileACL;
import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.exec.annotations.SysCall;
import net.es.netshell.kernel.users.UserAccessProfile;
import net.es.netshell.kernel.users.UserAccessACL;
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
     * Creates a folder /netshell-root/access/network/.acl/username
     * Creates the table of users and privilege profiles in /netshell-root/etc/netshell.user.access
    */

    private final static UserAccess users = new UserAccess();

    List<String> privArray = Arrays.asList(UserAccessProfile.NETWORK, UserAccessProfile.USER, UserAccessProfile.VM);
  
    public final static String USERS_DIR = "access";

    private Path aclFilePath; // Useful for checking if acl exists or not
    private Path NetShellRootPath;
    // Hash Table: key = user, value = accesses
    // To include duplicate key but no duplicate values using SetMultimap
    private SetMultimap<String, String> userAccessList = HashMultimap.create();
    private final Logger logger = LoggerFactory.getLogger(UserAccess.class);


    public UserAccess() {
        // Figure out the NetShell root directory.
        String NetShellRootDir = NetShellConfiguration.getInstance().getGlobal().getRootDirectory();
        this.NetShellRootPath = Paths.get(NetShellRootDir).normalize();
	this.aclFilePath = FileUtils.toRealPath("/etc/netshell.user.access");

        // Read acl user file or create it if necessary
        UserAccessProfile user = new UserAccessProfile();
        try {
            this.readUserFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
	String access = newUser.getAccess();

        // Make sure privilege value entered 
        if (!privArray.contains(access)) {
            throw new UserClassException(access);
        }

         // Checks if the user access already exists
	    try {
		    this.readUserFile();
	    } catch (IOException e) {
		    logger.error("Cannot read access file");
	    }

	// Create hash table with list of user/access and write to /etc/netshell.user.access
	// Including functionality to have duplicates in (hash) table
	// user1 = network, user
        this.userAccessList.put(username, access);
	this.writeUserFile(); 

	// Create home directory
        File homeDir = new File (Paths.get(this.getHomePath().toString(), access, username).toString());

	/* In case directory is required */
        //homeDir.mkdirs();
       
        // Create access only to specific application with path = netshell-root/access/network/.acl/<username>
        UserAccessACL acl = new UserAccessACL(homeDir.toPath());
        acl.allowUserRead(username);
        acl.allowUserWrite(username);
	acl.allowUserExecute(username);
        // Commit ACL's
        acl.store();

    }

    public boolean removeaccess (UserAccessProfile user) {
        Method method = null;
        KernelThread kt = KernelThread.currentKernelThread();
        String currentUserName = kt.getUser().getName();
	String userName = user.getName();
	String access = user.getAccess();

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

        // Make sure the user exists.
	    try {
		    this.readUserFile();
	    } catch (IOException e) {
		    logger.error("Cannot read access file");
	    }

	String userName = user.getName();
	String access = user.getAccess();

	// Remove entry from the multimap
	this.userAccessList.remove(userName, access);

        // Delete .acl file associated with this user account
	File aclDelete = new File (Paths.get(this.getHomePath().toString(), access, ".acl", userName).toString());
	aclDelete.delete();

        // Save entry in the /etc/netshell.user.access
        this.writeUserFile();

    }

    /**
     * Function to check if username has access privilege. 
     * Can be called from command with respective application name
     * @param username accessing user
     * @param access accessing application
     * @return TRUE or FALSE
     */
    public static boolean isAccessPrivileged (String username, String access) {

	String accessInList;
        if (UserAccess.getUsers().userAccessList.isEmpty()  && username.equals("admin")) {
            // Initial configuration. Add admin user and create configuration file.
            return true;
        }
	
        Collection<String> accesslist = UserAccess.getUsers().userAccessList.get(username);

	if (username.equals(null)) {
            // Not a user
            return false;
        } else if (accesslist.contains(access) || Users.isPrivileged(username)) {
	    return true;
	} else {
	    // Any other case
	    return false;
	}
    }

    private synchronized void readUserFile() throws IOException {
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
            if (p.getName() != null) {
                this.userAccessList.put(p.getName(), p.getAccess()); 
            }
            else {
                logger.error("Malformed user entry:  {}", line);
            }
        }
    }

    private synchronized void writeUserFile() throws IOException {
        File aclFile = new File(this.aclFilePath.toString());
	aclFile.delete();
        BufferedWriter writer = new BufferedWriter(new FileWriter(aclFile));
        for (Map.Entry p : this.userAccessList.entries() ) {
            if (p.getKey() != null) {
		UserAccessProfile newEntry = new UserAccessProfile(p.getKey().toString(), p.getValue().toString());
		System.out.println("Key: " + p.getKey().toString() + "\t Value: " + p.getValue().toString() + "\n");
                writer.write(newEntry.toString());
                writer.newLine();
            }
        }
        writer.flush();
        writer.close();
    }

    public Path getNetShellRootPath() { return NetShellRootPath; }

    public Path getHomePath() {
        return NetShellRootPath.resolve(USERS_DIR);
    }

    public Path getHomePath(String username) {
        return getHomePath().resolve(username);
    }
  

} 
