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

import net.es.netshell.boot.BootStrap;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Created by lomax on 2/25/14.
 */


public class User {

    private static HashMap<String,WeakReference<User>> users = new HashMap<String, WeakReference<User>>();
    private static HashMap<String,WeakReference<User>> usersByGroup = new HashMap<String, WeakReference<User>>();

    private String name;
    private ThreadGroup threadGroup;
    private Path homePath;
    private boolean privileged = false;

    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    public User (String name) {
        this.name = name;

        // Set home directory.
        this.homePath = Paths.get(Users.getUsers().getHomePath().toString(),
                                  name);

        // TODO: lomax@es.net this creates a very little memory leak. Will need to have a background
        // thread to clean that up.
        User.users.put(this.name, new WeakReference<User>(this));
        this.privileged = Users.getUsers().isPrivileged(name);
        // Create the user ThreadGroup
        this.threadGroup = new ThreadGroup(BootStrap.getBootStrap().getSecurityManager().getNetShellRootThreadGroup(),
                "NetShell User " + name + " ThreadGroup");
        User.usersByGroup.put(this.threadGroup.getName(), new WeakReference<User>(this));
    }

    public User (String name, String home) {
        this.name = name;
        // Set home directory.
        this.homePath = Paths.get(home);
        User.users.put(this.name, new WeakReference<User>(this));
        this.privileged = true;
        // Create the user ThreadGroup
        this.threadGroup = Thread.currentThread().getThreadGroup();
        User.usersByGroup.put(this.threadGroup.getName(), new WeakReference<User>(this));
    }

    public String getName() {
        return name;
    }

    public static User getUser(String username) {
        synchronized (User.users) {
            WeakReference weakRef = User.users.get(username);
            if (weakRef != null) {
                return (User) weakRef.get();
            }
            // User has not login yet. If NetShell is running as a standalone, users are the host users.
            // Only allow the host's user who is running NetShell.
            if (BootStrap.getBootStrap().isStandAlone()) {
                // Netshell users are the host users. Only allow the host's user who is running NetShell.
                String currentUser = System.getProperty("user.name");
                if (currentUser.equals(username)) {
                    String home = System.getProperty("user.home");
                    User user = new User(username,home);
                    return user;
                }
            }
            return null;
        }
    }

    public static User getUser(ThreadGroup group) {
        synchronized (User.users) {
            WeakReference weakRef = User.usersByGroup.get(group.getName());
            if (weakRef != null) {
                return (User) weakRef.get();
            }
            return null;
        }
    }

    public boolean isPrivileged() {
        return this.privileged;
    }

    public Path getHomePath() {
        return this.homePath;
    }

	// Set the homepath to simulate changing the working directory.
	public void setHomePath(Path newPath) { this.homePath = newPath; }
}
