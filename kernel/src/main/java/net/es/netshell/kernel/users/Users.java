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

package net.es.netshell.kernel.users;

import net.es.netshell.api.*;
import net.es.netshell.boot.BootStrap;
import net.es.netshell.configuration.NetShellConfiguration;
import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.exec.annotations.SysCall;
import net.es.netshell.kernel.security.FileACL;
import net.es.netshell.kernel.acl.UserAccess;
import net.es.netshell.shell.CommandResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.apache.commons.codec.digest.Crypt.crypt;

/**
 * Manages Users.
 * This class implements the user management. It is responsible for providing the hooks to AA(A),
 * persistent user information/state/permission. While implementation of those services may vary
 * upon deployments, the Users class is not intended to be extended. It is a singleton.
 *
 * TODO: this class is currently implemented very poorly. It is just intended so other part of NetShell can be worked on.
 * IMPORTANT: this class is not intended for production.
 */
public final class Users {

    private final static Users users = new Users();

    /* uSERS directory */
    public final static String USERS_DIR="users";

    private final static String ADMIN_USERNAME = "admin";
    private final static String ADMIN_PASSWORD = "netshell";

    private final static String ROOT = "root";
    private final static String USER = "user";
    List<String> privArray = Arrays.asList(ROOT, USER); //List of privs to check when creating user

    private Path passwordFilePath;
    private Path NetShellRootPath;
    private HashMap<String,UserProfile> passwords = new HashMap<String, UserProfile>();
    private final Logger logger = LoggerFactory.getLogger(Users.class);

    public Users() {
        // Figure out the NetShell root directory.
        String NetShellRootDir = NetShellConfiguration.getInstance().getGlobal().getRootDirectory();
        this.NetShellRootPath = Paths.get(NetShellRootDir).normalize();

        if (!BootStrap.getBootStrap().isStandAlone()) {
            this.passwordFilePath = FileUtils.toRealPath("/etc/netshell.users");

            // Read user file or create it if necessary
            try {
                this.readUserFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static Users getUsers() {
        return users;
    }

    /**
     * Return true if a user exists, false otherwise
     * @param user user to check
     * @return true if user exists
     */
    public boolean userExists(String user) {
        if(BootStrap.getBootStrap().isStandAlone()) {
            String currentUser = System.getProperty("user.name");
            return currentUser.equals(user);
        } else {
            return Users.getUsers().passwords.containsKey(user);
        }
    }

    public boolean authUser (String userName, String password) {
        if (BootStrap.getBootStrap().isStandAlone()) {
            // Forbid password auth in standalone
            return false;
        }
        Method method = null;
        try {
            method = KernelThread.getSysCallMethod(this.getClass(), "do_authUser");

            KernelThread.doSysCall(this,
                    method,
                    userName,
                    password);
        } catch (NonExistentUserException e) {
            return false;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @SysCall(
            name="do_authUser"
    )
    public void  do_authUser (String user, String password) throws NonExistentUserException, UserException,UserClassException {
        logger.info("do_authUser entry");
        if (BootStrap.getBootStrap().isStandAlone()) {
            throw new SecurityException("not allowed");
        }
        // Read file.
        try {
            this.readUserFile();
        } catch (IOException e) {
            logger.error("Cannot read password file");
        }

        if (this.passwords.isEmpty()) {
            // No user has been created. Will accept "admin","netshell".
            // TODO: default admin user should be configured in a safer way.
            if (Users.ADMIN_USERNAME.equals(user) && Users.ADMIN_PASSWORD.equals(password)) {
                // Create the initial configuration file
                try {

                    // Set up the default admin user.  This takes basically two steps.
                    // First we need to create the user's entry in the user file.
                    // Then we need to get a User object from that, so we can be running
                    // as that initial user.  Finally this allows us to create a container.
                    UserProfile adminProfile = new UserProfile(Users.ADMIN_USERNAME,
                                                               Users.ADMIN_PASSWORD,
                                                               Users.ROOT,
                                                               "Admin",
                                                               "Admin",
                                                               "admin@localhost");
                    this.do_createUser(adminProfile, false);

                } catch (UserAlreadyExistException e) {
                    // Since this code is executed only when the configuration file is empty, this should never happen.
                    logger.error("User {} already exists in empty configuration file", Users.ADMIN_USERNAME);
                } catch (IOException e) {
                    // This shouldn't happen either...it means we couldn't create the initial password file.
                    logger.error("Cannot create initial password file");
                }
            }
        }

        logger.warn("looking for key for {}", user);
        if (!Users.getUsers().passwords.containsKey(user)) {
            logger.warn("{} is unknown", user);
        }
        UserProfile userProfile = Users.getUsers().passwords.get(user);
        if (userProfile.getPassword().equals("*")) {
            // Disable password
            throw new UserException("not authorized");
        }

        // Local password verification here.  Check an encrypted version of the user's password
        // against what was stored in password file, a la UNIX password authentication.
        if (userProfile.getPassword().equals(crypt(password, userProfile.getPassword()))) {
            logger.warn("{} has entered correct password", user);

        } else {
            logger.warn("{} has entered incorrect password", user);
            throw new UserException (user);
        }
    }


    public boolean setPassword (String userName, String newPassword) {
        Method method = null;
        try {
            method = KernelThread.getSysCallMethod(this.getClass(), "do_setPassword");

            KernelThread kt = KernelThread.currentKernelThread();
            String currentUserName = kt.getUser().getName();

            logger.info("current user {}", currentUserName);

            if ((currentUserName.equals(userName)) ||
                    isPrivileged(currentUserName)) {
                logger.info("OK to change");

                KernelThread.doSysCall(this,
                                       method,
                                       userName,
                                       newPassword);
            }
        } catch (NonExistentUserException e) {
            return false;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }catch (UserClassException e){
            e.printStackTrace();
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    @SysCall(
            name="do_setPassword"
    )
    public void do_setPassword(String userName, String newPassword) throws NonExistentUserException, IOException {
        if (BootStrap.getBootStrap().isStandAlone()) {
            throw new SecurityException("not allowed");
        }
        logger.info("do_setPassword entry");

	    try {
		    this.readUserFile();
	    } catch (IOException e) {
		    logger.error("Cannot read password file");
	    }

	    // Make sure the user exists.
        if (!this.passwords.containsKey(userName)) {
            throw new NonExistentUserException(userName);
        }

        UserProfile userProfile = Users.getUsers().passwords.get(userName);

        // Encrypt new password and write out the users file
        userProfile.setPassword(crypt(newPassword));
        this.writeUserFile();
    }


    public CommandResponse createUser(UserProfile newUser) {
        if (BootStrap.getBootStrap().isStandAlone()) {
            throw new SecurityException("not allowed");
        }
        Method method = null;
        CommandResponse commandResponse;
        String resMessage = null;
        boolean resCode = false;
        try {
            method = KernelThread.getSysCallMethod(this.getClass(), "do_createUser");

	    // Access per Application
	    UserAccess currentUserAccess = UserAccess.getUsers();
	    KernelThread kt = KernelThread.currentKernelThread();
            String currentUserName = kt.getUser().getName();

            // Check if user is authorized to create users
            if (KernelThread.currentKernelThread().isPrivileged()
                    || currentUserAccess.isAccessPrivileged(currentUserName, "user:create")) {
                KernelThread.doSysCall(this, method, newUser, true);
                resCode = true;
                resMessage = "User added";
            } else {

                resCode = false;
                resMessage = "Operation Not Permitted";
            }

        } catch (UserAlreadyExistException e) {
            e.printStackTrace();
	    resCode = false;
            resMessage = "User already exists";
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            resCode = false;
            resMessage = "Method not implemented";
        }catch(UserClassException e){
            e.printStackTrace();
            resCode = false;
            resMessage = "User class must be root or user";
        }catch (Exception e) {
            e.printStackTrace();
            resCode = false;
            resMessage = "Error in operation";
        }

        commandResponse = new CommandResponse(resMessage,resCode);
        return commandResponse;
    }


    @SysCall(
            name="do_createUser"
    )
    public void do_createUser (UserProfile newUser, boolean createContainer) throws UserAlreadyExistException, UserException, UserClassException, IOException {
        logger.info("do_createUser entry");
        if (BootStrap.getBootStrap().isStandAlone()) {
            throw new SecurityException("not allowed");
        }
        String username = newUser.getName();
        String password = newUser.getPassword();
        String privilege = newUser.getPrivilege();
        String name = newUser.getRealName();
        String organization = newUser.getorganization();
        String email = newUser.getemail();


        // Check if fields entered contain valid characters (and don't contain colons)
        /* lomax@es.net: disabling this test in order to allow users to be the name of containers, or email address.
         * not sure if the test is really needed anyway.
         * todo Revisit: dhua@es.net->lomax@es.net: if we want to be safe, we should still check if any field contains a colon (since that's how our password file fields are delimited)
         * Username check was needed because some symbols may be invalid on certain file systems (colon, semicolon, pipe, comma, slash, etc --esp in Windows), and the directory name is created from username
         *
        if (! username.matches("[a-zA-Z0-9_/]+") || name.contains(":")
                || organization.contains(":") || email.contains(":")
                || ! email.contains("@") || ! email.contains(".")) {
            throw new UserException(username);
        }
        **/

        // Make sure privilege value entered is valid
        if (!privArray.contains(privilege)) {
            throw new UserClassException(privilege);
        }

        // Checks if the user already exists
	    try {
		    this.readUserFile();
	    } catch (IOException e) {
		    logger.error("Cannot read password file");
	    }
        if (this.passwords.containsKey(username)) {
            throw new UserAlreadyExistException(username);
        }

        // Construct the new Profile. A null, empty or * password means that password is disabled for the user.
        UserProfile userProfile = new UserProfile(
                username,
                (password != null) && (!password.equals("") && (!password.equals("*"))) ?
                        crypt(password) : // Let the Crypt library pick a suitable algorithm and a random salt
                        "*",
                privilege,
                name,
                organization,
                email);
        this.passwords.put(username,userProfile);

        // Create home directory
        File homeDir = new File (Paths.get(this.getHomePath().toString(), username).toString());

        homeDir.mkdirs();
        // Create proper access right
        FileACL fileACL = new FileACL(homeDir.toPath());
        fileACL.allowUserRead(username);
        fileACL.allowUserWrite(username);

        // Commit ACL's
        fileACL.store();

        // Update NetShell user file
        this.writeUserFile();
    }

    public boolean removeuser (String userName) {
        Method method = null;
        KernelThread kt = KernelThread.currentKernelThread();
        String currentUserName = kt.getUser().getName();

        try {
            method = KernelThread.getSysCallMethod(this.getClass(), "do_removeUser");

	    // Access per Application
	    UserAccess currentUserAccess = UserAccess.getUsers();

            if ((currentUserName.equals(userName))  ||
                    (isPrivileged(currentUserName)) || 
		    (currentUserAccess.isAccessPrivileged(currentUserName, "user:delete"))) {
                logger.info("OK to remove");
                KernelThread.doSysCall(this,
                        method,
                        userName);
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

        if (! isPrivileged(currentUserName) || currentUserName.equals(userName)) {
            // End user session if not privileged account (unless root removed own account)
            kt.getThread().interrupt();
        }
        return true;
    }


    @SysCall(
            name="do_removeUser"
    )
    public void do_removeUser(String userName) throws NonExistentUserException, IOException {
        logger.info("do_removeUser entry");

        // Make sure the user exists.
	    try {
		    this.readUserFile();
	    } catch (IOException e) {
		    logger.error("Cannot read password file");
	    }

        if (!this.passwords.containsKey(userName)) {
            throw new NonExistentUserException(userName);
        }
        User user = new User (userName);
        UserProfile userProfile = Users.getUsers().passwords.get(userName);
        // Set name to null so writeUserFile will skip this UserProfile
        userProfile.setName(null);

        File userDir = new File (Paths.get(this.getHomePath().toString(), userName).toString());

        //Delete user directory
        this.deleteUserDir(userDir);

	    // Delete .acl file associated with this user account
	    File aclDelete = new File (Paths.get(Users.getUsers().getHomePath().toString(), ".acl", userName).toString());
	    aclDelete.delete();

        // Save User File with removed user
        this.writeUserFile();
    }


	public boolean mkdir (File homeDir) throws SecurityException{
		try {
			KernelThread kt = KernelThread.currentKernelThread();
			String username = kt.getUser().getName();

			// Check if directory entered contain valid characters only
			if (! homeDir.getName().matches("[a-zA-Z0-9_]+")) {
				throw new UserException(username);
			}

			// Make sure directory doesn't already exist.
			if (! homeDir.exists()) {
				// Will throw exception if user does not have proper permissions to write in directory.
				homeDir.mkdirs();
			} else {
				throw new IOException();
			}

			// Create proper access rights in new directory
			FileACL fileACL = new FileACL(homeDir.toPath());
            // First empty the permissions
            for (String prop : fileACL.stringPropertyNames()) {
                fileACL.remove(prop);
            }
            // Set user permissions
			fileACL.allowUserRead(username);
			fileACL.allowUserWrite(username);
			// Commit ACL's
			fileACL.store();

		} catch (UserException | IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}


    public static boolean isPrivileged (String username) {

        if (Users.getUsers().passwords.isEmpty()  && Users.ADMIN_USERNAME.equals(username)) {
            // Initial configuration. Add admin user and create configuration file.
            return true;
        }
        UserProfile userProfile = Users.getUsers().passwords.get(username);

        if (userProfile == null) {
            // Not a user
            return false;
        }

        return Users.ROOT.equals(userProfile.getPrivilege());

    }

    private synchronized void readUserFile() throws IOException {
        File passwordFile = new File(this.passwordFilePath.toString());
        passwordFile.getParentFile().mkdirs();
        if (!passwordFile.exists()) {
            // File does not exist yet, create it.
            if (!passwordFile.createNewFile()) {
                // File could not be created, return a RuntimeError
                throw new RuntimeException("Cannot create " + this.passwordFilePath.toString());
            }
        }
        BufferedReader reader = new BufferedReader(new FileReader(passwordFile));
        String line = null;

        // Reset the cache
        this.passwords.clear();

        while ((line = reader.readLine()) != null) {
            UserProfile p = new UserProfile(line);
            if (p.getName() != null) {
                this.passwords.put(p.getName(), p);
            }
            else {
                logger.error("Malformed user entry:  {}", line);
            }
        }
    }


    private synchronized void writeUserFile() throws IOException {
        File passwordFile = new File(this.passwordFilePath.toString());
        BufferedWriter writer = new BufferedWriter(new FileWriter(passwordFile));
        for (UserProfile p : this.passwords.values() ) {
            if (p.getName() != null) {
                writer.write(p.toString());
                writer.newLine();
            }
        }
        writer.flush();
        writer.close();
    }


    private synchronized void deleteUserDir(File userDir) throws IOException {

        // Remove all files from directory before deleting directory.
        if (userDir.list().length == 0) {
            userDir.delete();
	        logger.debug("Directory deleted");
        } else {
            for (File userFile : userDir.listFiles()) {
                userFile.delete();
            }
	        // Make sure all files have been deleted from the directory.
            if (userDir.list().length == 0) {
                userDir.delete();
	            logger.debug("Directory deleted");
            }
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
