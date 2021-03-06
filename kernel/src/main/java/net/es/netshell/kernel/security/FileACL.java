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

package net.es.netshell.kernel.security;

import net.es.netshell.api.FileUtils;
import net.es.netshell.configuration.NetShellConfiguration;
import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.exec.annotations.SysCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;
/**
 * NetShell cannot rely on the native file system to provide user access control, since the JVM runs with a
 * single user. NetShell implements File ACL by storing metadata associated  to the file in a separated
 * file which contains ACL.
 */

public class FileACL extends Properties {

    public static final String ACLDIR = ".acl"; // prefix of the acl file
    public static final String CAN_READ = "read";
    public static final String CAN_WRITE = "write";

    private Path aclPath;
    private Path filePath;
    private static Path rootPath;
    private final Logger logger = LoggerFactory.getLogger(FileACL.class);

    static {
        // Figure out the NetShell root directory.
        String rootdir = NetShellConfiguration.getInstance().getGlobal().getRootDirectory();
        rootPath = Paths.get(rootdir).normalize();
    }

    public FileACL (Path file) {
        super ();
        if (file == null) {
            // root of the file system, no parent
            return;
        }

        if (file.toString().equals(File.separator)) {
            // Root does not have ACL's
            return;
        }
        this.filePath = FileUtils.toRealPath(file.toString());

        this.aclPath = Paths.get(this.filePath.getParent().toString(),
                                    FileACL.ACLDIR,
                                    file.getFileName().toString());
        this.aclPath = FileUtils.toRealPath(this.aclPath.toString());
        this.loadACL();
    }

    public FileACL (String fileName) {
        this(Paths.get(FileUtils.normalize(fileName)));
    }


    /**
     * Loads the ACL rules from the ACL file. This will require privilege access and be
     * implemented by a SysCall, do_loadACL.
     * @throws IOException
     */
    public void loadACL() {

        Method method;
        try {
            method = KernelThread.getSysCallMethod(FileACL.class, "do_loadACL");

            KernelThread.doSysCall(this, method);

        } catch (Exception e) {
            // Nothing particular to do.
            e.printStackTrace();
        }
    }

    /**
     * SysCall implementing the privileged access to the ACL file. If the file does not exist, do_loadACL will
     * attempt to retrieve ACL from parent directories until reaching NetShell root.
     * @throws IOException
     */
    @SysCall(
            name="do_loadACL"
    )
    public void do_loadACL ()  {
        try {
            File aclFile = new File(this.aclPath.toString());
            if (!aclFile.exists()) {

                if (!this.filePath.startsWith(FileACL.rootPath.toString())) {
                    // The file is not part of the NetShell file system. Cannot inherit permission from parent
                    return;
                }
                // It is ok for a file to not have an ACL file: it then inherits its parents.
                this.inheritParent();
                return;
            }
            logger.debug("loads file");
            this.load(new FileInputStream(aclFile));
        } catch (IOException e) {
            // File or directory does not exist. Return an empty ACL
            return;
        }
    }

    /**
     * Loads the parent ACL.
     * @throws IOException
     */
    private void inheritParent() throws IOException {
	    if ( !this.filePath.toString().startsWith(FileACL.rootPath.toString())){
		    // Not NetShell file system
            return;
        }
        FileACL parentACL = this.getParentFileACL();
        if (parentACL != null) {
            this.putAll(parentACL);
        }
    }

    /**
     * Attempts to read the parent ACL.
     * @return
     * @throws IOException
     */
    private FileACL getParentFileACL() throws IOException {

        if (this.filePath.getParent().equals(FileACL.rootPath)) {
            // The parent is the NetShell root directory. Cannot inherit. No ACL
            return null;
        }
        FileACL parentACL;
        parentACL = new FileACL(this.filePath.getParent());
        return parentACL;
    }

    /**
     * Stores the ACL into ACL file.
     * @throws IOException
     */
    public void store() throws IOException {
        // By default inherit parent's ACL
        this.inheritParent();
        // Create the file and save it
        this.aclPath.getParent().toFile().mkdirs();
        this.aclPath.toFile().createNewFile();
        this.store(new FileOutputStream(this.aclPath.toString()),"NetShell File ACL");
    }

    /**
     * Checks if the ACL allows the user of the current thread to read the file.
     * @return true if the thread can read the file, false otherwise.
     */
    public boolean canRead() {
        return this.canRead(KernelThread.currentKernelThread().getUser().getName());
    }

    /**
     * Checks if the ACL allows the user of the current thread to read the file.
     * @param username
     * @return
     */
    public boolean canRead(String username) {
        String[] users = this.getCanRead();
        for (String user : users) {
            if (user.equals("*") || user.equals(username)) {
                return true;
            }
        }
        return false;
    }

    public boolean canWrite(String username) {
        String[] users = this.getCanWrite();
        for (String user : users) {
            if (user.equals("*") || user.equals(username)) {
                return true;
            }
        }
        return false;
    }

    public String[] getCanRead() {
        String users = this.getProperty(FileACL.CAN_READ);
        if (users == null) {
            return new String[0];
        }
        return users.split(",");
    }

    public String[] getCanWrite() {
        String users = this.getProperty(FileACL.CAN_WRITE);
        if (users == null) {
            return new String[0];
        }
        return users.split(",");
    }


    protected static String[] addUser(String[] users, String username) {
        String[] newUsers = new String[users.length + 1];
        int index = 0;
        for (String user : users) {
            newUsers[index] = user;
            ++index;
        }
        newUsers[index] = username;
        return newUsers;
    }

    protected static String makeString(String [] strings) {
        String result = "";
        for (String u : strings) {
            result += u + ",";
        }
        // Remove trailing ,
        if (result.length() > 1) {
            result = result.substring(0,result.length() - 1);
        } else if (result.length() == 1) {
            result = "";
        }
        return result;
    }

    public static String[] removeUser(String[] users, String username) {
        ArrayList<String> newUsers = new ArrayList<String>();
        for (String user : users) {

            if (! user.equals(username)) {
                // Keep user in the list
                newUsers.add(user);
            }
        }
        return newUsers.toArray(new String[newUsers.size()]);
    }

    public synchronized void allowUserRead(String username) {
        if (this.canRead(username)) {
            // is already allowed
            return;
        }
        // Add user to the list
        this.setProperty(FileACL.CAN_READ,
                FileACL.makeString(FileACL.addUser(this.getCanRead(),username)));

    }
    public synchronized void allowUserWrite(String username) {
        if (this.canWrite(username)) {
            // is already allowed
            return;
        }
        // Add user to the list
        this.setProperty(FileACL.CAN_WRITE,
                FileACL.makeString(FileACL.addUser(this.getCanWrite(),username)));

    }

    public synchronized void denyUserRead(String username) {
        if (!this.canRead(username)) {
            // is already denied
            return;
        }
        // Remove user from the list
        String[] users = FileACL.removeUser(this.getCanRead(),username);
        if (users.length == 0) {
            this.remove(FileACL.CAN_READ);
        }  else {
            this.setProperty(FileACL.CAN_READ,FileACL.makeString(users));
        }

    }


    public synchronized void denyUserWrite(String username) {
        if (!this.canWrite(username)) {
            // is already denied
            return;
        }
        // Remove user from the list
        String[] users = FileACL.removeUser(this.getCanRead(),username);
        if (users.length == 0) {
            this.remove(FileACL.CAN_WRITE);
        }  else {
            this.setProperty(FileACL.CAN_WRITE,FileACL.makeString(users));
        }

    }


    public void changeACL(String user, String cmd, String aclType) throws IOException {
        if (aclType.equals("read")) {
            if (cmd.equals("allow")) {
                this.allowUserRead(user);
            } else if (cmd.equals("deny")) {
                this.denyUserRead(user);
            }
        } else if (aclType.equals("write")) {
            if (cmd.equals("allow")) {
                this.allowUserWrite(user);
            } else if (cmd.equals("deny")) {
                this.denyUserWrite(user);
            }
        } else {
            // Invalid type do nothing
            return;
        }
        this.store();
    }

}







