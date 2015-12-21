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

package net.es.netshell.shell;

import jline.console.ConsoleReader;
import net.es.netshell.api.FileUtils;
import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.security.FileACL;
import net.es.netshell.kernel.users.UserProfile;
import net.es.netshell.kernel.users.Users;
import net.es.netshell.shell.annotations.ShellCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UserShellCommands {
    @ShellCommand(name = "adduser",
    shortHelp = "Add a user to the system",
    longHelp = "Required arguments are a username, an initial password, a user class, a name, an organization name, and an email.\n" +
            "The user class should be either \"root\" or \"user\".",
    privNeeded = true)
    public static void addUser(String[] args, InputStream in, OutputStream out, OutputStream err) {
        Logger logger = LoggerFactory.getLogger(UserShellCommands.class);
        logger.info("adduser with {} arguments", args.length);

        CommandResponse cmd;
        PrintStream o = new PrintStream(out);

        // Argument checking
        if (args.length != 7) {
            o.println("Usage:  adduser <username> <password> <userclass> <name> <organization> <email>");
            return;
        }

        UserProfile newProfile = new UserProfile(args[1], args[2], args[3], args[4], args[5], args[6]);

        cmd = Users.getUsers().createUser(newProfile);

        //if (cmd.isSuccess()) {
        //    o.print("New User created!");
        //} else {
        //    o.print("Unable to create new user");
        //}

        o.print(cmd.getMessage());

    }

    @ShellCommand(name = "passwd",
            shortHelp = "Change user password",
            longHelp = "No arguments are required; this command will prompt for them interactively.\n")
    public static void passwd(String[] args, InputStream in, OutputStream out, OutputStream err) {
        Logger logger = LoggerFactory.getLogger(UserShellCommands.class);
        logger.info("passwd with {} arguments", args.length);

        PrintStream o = new PrintStream(out);

        ConsoleReader consoleReader = null;
        try {
            consoleReader = new ConsoleReader(in, out, new NetShellTerminal());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        // in = new ShellInputStream(in, consoleReader);

        try {
            // Get our current username.
            String thisUserName = KernelThread.currentKernelThread().getUser().getName();

            // If this thread is privileged, then ask for a username (because we can set anybody's passwd).
            // If not privileged, require password verification to change our own passwd.
            String userName, oldPassword;
            if (KernelThread.currentKernelThread().isPrivileged()) {
                userName = consoleReader.readLine("Username (default = " + thisUserName + "): ");
                if (userName.isEmpty()) {
                    userName = thisUserName;
                }
            }
            else {
                userName = KernelThread.currentKernelThread().getUser().getName();
                oldPassword = consoleReader.readLine("Old password: ", '*');

                // Password check to fail early here
                // TODO:  Figure out why this fails.
                // ^User is not privileged, authUser requires access to passwd file.
                if (! Users.getUsers().authUser(thisUserName, oldPassword)) {
                    o.println("Old password is incorrect");
                    return;
                }
            }

            o.println("Changing password for " + userName);
            String newPassword = consoleReader.readLine("New password: ", '*');
            String new2Password = consoleReader.readLine("New password (confirm): ", '*');
            if (! newPassword.equals(new2Password)) {
                o.println("Error: Passwords do not match");
                return;
            }

            boolean p = Users.getUsers().setPassword(userName, newPassword);
            if (p) {
                o.println("Password change successful!");
            }
            else {
                o.println("Password change failed...");
            }

        } catch (IOException e) {
            return;
        }
    }


    @ShellCommand(name = "removeuser",
            shortHelp = "Remove a user from the system",
            longHelp = "No arguments are required. \n")
    public static void removeUser(String[] args, InputStream in, OutputStream out, OutputStream err) {
        Logger logger = LoggerFactory.getLogger(UserShellCommands.class);
        logger.info("removeuser with {} arguments", args.length);

        PrintStream o = new PrintStream(out);

        if (args.length != 1) {
            o.print("removeuser does not take any parameters");
	        return;
        }

        ConsoleReader consoleReader = null;
        try {
            consoleReader = new ConsoleReader(in, out, new NetShellTerminal());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            // Get current username.
            String thisUserName = KernelThread.currentKernelThread().getUser().getName();

            // If this thread is privileged, then ask for a username (because we can remove any user).
            // If not privileged, require password verification to remove user.
            String userName, password;
            if (KernelThread.currentKernelThread().isPrivileged()) {
                userName = consoleReader.readLine("Username (default = " + thisUserName + "): ");
                if (userName.isEmpty()) {
                    userName = thisUserName;
                }
            }
            else {
                userName = KernelThread.currentKernelThread().getUser().getName();
                password = consoleReader.readLine("Password: ", '*');

                if (! Users.getUsers().authUser(thisUserName, password)) {
                    o.println("Password is incorrect");
                    return;
                }
            }

	        // Confirm removal of user account
            o.println("Are you sure you wish to remove this user account?");
            String confirmRemove = consoleReader.readLine("Y/N: ");
            if (confirmRemove.equals("Y")) {
                boolean r = Users.getUsers().removeuser(userName);
                if (r) {
                    o.print("Removed User!");
                } else {
                    o.print("Unable to remove user.");
                }
            } else {
	            o.print("Canceling operation.");
            }

        } catch (IOException e) {
	        return;
        }
    }


	@ShellCommand(name = "ls",
			shortHelp = "List all files in current directory",
			longHelp = "ls <directory (defaults to current directory)>")
	public static void ls(String[] args, InputStream in, OutputStream out, OutputStream err) {
		Logger logger = LoggerFactory.getLogger(UserShellCommands.class);
		logger.info("ls with {} arguments", args.length);

		PrintStream o = new PrintStream(out);
        String userPath;
		// Do argument number check. If no extra args, displays files in current directory;
		// If 1 extra arg, displays files in directory specified by the arg.
		if (args.length == 1 ) {
            userPath = KernelThread.currentKernelThread().getCurrentDirectory();
		} else if (args.length == 2) {
			userPath = FileUtils.normalize(args[1]);
		} else {
			o.println("Incorrect number of args");
			return;
		}
        if (userPath == null) {
            return;
        }
        Path dirPath = FileUtils.toRealPath(userPath);
        if ((dirPath == null) ||
            (dirPath.toFile() == null) ||
            (dirPath.toFile().list() == null)) {
            o.println(args[1] + " does not exist");
            return;
        }
		for (String file : dirPath.toFile().list()) {
			o.println(file);
		}
	}

	@ShellCommand(name = "mkdir",
			shortHelp = "Create a directory in current directory",
			longHelp = "Creates a directory>")
	public static void mkdir (String[] args, InputStream in, OutputStream out, OutputStream err) {
		Logger logger = LoggerFactory.getLogger(UserShellCommands.class);
		logger.info("mkdir with {} arguments", args.length);

		PrintStream o = new PrintStream(out);
		Path dirPath = null;

		// Argument checking
		if (args.length != 2) {
			o.println("Usage mkdir <directory>");
			return;
		}

        // Get canonical path
		dirPath = FileUtils.toRealPath(args[1]) ;
		File newDir = new File (dirPath.toString());
		// Create new directory and write ACLs
		try {
			boolean r = Users.getUsers().mkdir(newDir);
			if (r) {
				o.println(args[1] + " has been created.");
			} else {
				o.println("mkdir failed.");
			}
		} catch (SecurityException e) {
			o.println("Invalid permissions to access this directory");
		}
        return;
	}


	@ShellCommand(name = "cd",
	             shortHelp = "Change current directory",
	             longHelp = "cd <directory>")
	public static void cd(String[] args, InputStream in, OutputStream out, OutputStream err) {
		Logger logger = LoggerFactory.getLogger(UserShellCommands.class);
		logger.info("cd with {} arguments", args.length);

		PrintStream o = new PrintStream(out);

		// Argument checking
		if (args.length > 2 ) {
			o.println("Incorrect number of args");
			return;
		}
        if (args.length == 1) {
            // Go to the home directory
            String userPath = KernelThread.currentKernelThread().getUser().getHomePath().toString();
            KernelThread.currentKernelThread().setCurrentDirectory(userPath);
        } else {
            KernelThread.currentKernelThread().setCurrentDirectory(args[1]);
        }
        o.println("changed to: " + KernelThread.currentKernelThread().getCurrentDirectory());

	}


	@ShellCommand(name = "cat",
			shortHelp = "Display contents of a file",
			longHelp = "cat <directory>")
	public static void cat(String[] args, InputStream in, OutputStream out, OutputStream err) {
		Logger logger = LoggerFactory.getLogger(UserShellCommands.class);
		logger.info("cat with {} arguments", args.length);

		PrintStream o = new PrintStream(out);

		// Argument checking
		if (args.length != 2 ) {
			o.println("Incorrect number of args");
			return;
		}

		Path dirPath = null;
		dirPath = FileUtils.toRealPath(args[1]) ;
		File catDir = new File (dirPath.toString());

		// Make sure user has permission to read this directory. If not, outputs error message.
		try {
			try {
				BufferedReader read = new BufferedReader(new FileReader(Paths.get(catDir.toString()).toString()));
				String print;
				while((print = read.readLine()) != null) {
					o.println(print);
				}
				read.close();
			} catch (FileNotFoundException e) {
				o.println("File not found");
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (SecurityException e) {
			o.println("Invalid permissions to read this file");
			return;
		}
	}


	@ShellCommand(name = "rm",
			shortHelp = "Removes specified file",
			longHelp = "rm <file>")
	public static void rm(String[] args, InputStream in, OutputStream out, OutputStream err) {
		Logger logger = LoggerFactory.getLogger(UserShellCommands.class);
		logger.info("rm with {} arguments", args.length);

		PrintStream o = new PrintStream(out);

		Path dirPath = null;
		dirPath = FileUtils.toRealPath(args[1]) ;
		File rmFile = new File (dirPath.toString());

		try {
			// Make sure user has permission to write in this directory. If not, outputs error message.
			rmFile.canWrite();
			if (rmFile.exists()) {
				rmFile.delete();
				// Delete acl file associated with file if it exists
				File aclDelete = new File(Paths.get(dirPath.getParent().toString(), ".acl", args[1]).toString());
				aclDelete.delete();
				logger.debug("rm success");
			} else {
				o.println("File not found");
			}

		} catch (SecurityException e) {
			o.println("Invalid permissions to write in this directory");
		}
	}


	@ShellCommand(name = "pwd",
			shortHelp = "Displays current directory",
			longHelp = "Displays current directory (no arguments)")
	public static void pwd(String[] args, InputStream in, OutputStream out, OutputStream err) {
		Logger logger = LoggerFactory.getLogger(UserShellCommands.class);
		logger.info("pwd", args.length);

		PrintStream o = new PrintStream(out);

		String userPath = KernelThread.currentKernelThread().getCurrentDirectory();
		o.println(userPath);
	}


    static private void displayACL(String fileName, InputStream in, OutputStream out, OutputStream err) {
        PrintStream o = new PrintStream(out);

        FileACL acl = new FileACL(fileName);
        String[] users;
        users = acl.getCanRead();
        o.println("Read Access:");
        if ((users == null) || (users.length == 0)) {
            o.println("    None");
        } else {
            o.print("    ");
            for (String user : users) {
                o.print(user + ",");
            }
            o.println("\n");
        }
        users = acl.getCanWrite();
        o.println("Write Access:");
        if ((users == null) || (users.length == 0)) {
            o.println("    None");
        } else {
            o.print("    ");
            for (String user : users) {
                o.print(user + ",");
            }
            o.println("\n");
        }
    }

    @ShellCommand(name = "acl",
            shortHelp = "Manages file and directories Access Control List",
            longHelp = "acl <file path> : show ACL\n" +
                       "acl <allow|deny> <user> <read|write> <file path>")
    static public void acl(String[] args, InputStream in, OutputStream out, OutputStream err) {

        if (args.length == 1) {
            UserShellCommands.displayACL(".", in, out, err);
            return;
        }

        if (args.length == 2) {
            UserShellCommands.displayACL(args[1],in,out,err);
            return;
        }
        PrintStream o = new PrintStream(out);
        o.println("Not implemented yet");
    }

}
