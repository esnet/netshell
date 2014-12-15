/*
 * Copyright (c) 2014, Regents of the University of California  All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.es.netshell.kernel.security;

import net.es.netshell.api.NetShellException;
import net.es.netshell.boot.BootStrap;
import net.es.netshell.configuration.NetShellConfiguration;
import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.exec.annotations.SysCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Permission;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements the core NetShell Security Manager. It implements SecurityManager and is set as the System
 * SecurityManager. It is, therefore, critical to the overall security if the system.
 */
public class KernelSecurityManager extends SecurityManager {
	private ThreadGroup NetShellRootThreadGroup = new ThreadGroup("NetShell Root ThreadGroup");
	private Path rootPath;
	private final Logger logger = LoggerFactory.getLogger(KernelSecurityManager.class);
	private static HashMap<String,Boolean> writeAccess = new HashMap<String,Boolean>();
    private boolean isDebug = false;

	public KernelSecurityManager() {

		this.preloadClasses();
		this.initializePreAuthorized();

		// Figure out the NetShell root directory.
		String rootdir = NetShellConfiguration.getInstance().getGlobal().getRootDirectory();
		this.rootPath = Paths.get(rootdir).normalize();

        if (NetShellConfiguration.getInstance().getGlobal().getSecurityManagerDisabled() != 0) {
            logger.warn("NetShell SecurityManager is currently disabled.  No security checks will be run.  MUST NOT BE USED IN PRODUCTION.");
            return;
        } else {
            System.setSecurityManager(this);
        }
	}

	@Override
	public void checkAccess(Thread t) throws SecurityException {

		// Threads that are not part of NetShell ThreadGroup are authorized
		Thread currentThread = Thread.currentThread();
		if (this.isPrivileged()) {
            logger.debug("checkAccess(Thread " + t.getName() + " id= " + t.getId() + ") is privileged - OK");
			return;
		}

		if ((currentThread.getThreadGroup() == null) ||
				(KernelThread.currentKernelThread().isPrivileged()) ||
				( !this.NetShellRootThreadGroup.parentOf(currentThread.getThreadGroup()))) {
            logger.debug("checkAccess(Thread " + t.getName() + " id= " + t.getId() + ") not NetShell thread / privileged calling thread - OK");
			return;
		}
		if (Thread.currentThread().getThreadGroup().parentOf(t.getThreadGroup())) {
			// A thread can do whatever it wants on thread of the same user
            logger.debug("checkAccess(Thread " + t.getName() + " id= " + t.getId() + ") same user - OK");
			return;
		}
		if ( ! this.NetShellRootThreadGroup.parentOf(t.getThreadGroup())) {
			// This is a non NetShell Thread. Allow since the only non NetShell thread that can be referenced to are
			// from java library classes. This is safe.
            logger.debug("checkAccess(Thread " + t.getName() + " id= " + t.getId() + ") not NetShell thread - OK");
			return;
		}

        logger.debug("checkAccess(Thread " + t.getName() + " id= " + t.getId() + ") Illegal Thread access from " + Thread.currentThread().getName() + " onto " +
                t.getName());
		throw new SecurityException("checkAccess(Thread " + t.getName() + " id= " + t.getId() + ") Illegal Thread access from " + Thread.currentThread().getName() + " onto " +
				t.getName());
	}

	@Override
	public void checkPackageAccess(String p) throws SecurityException {

		// TODO: lomax@es.net the restriction on es.net classes sounds like a neat idea, but might not be
		// neither realistic nor usefull. To revisit.
        if (false) {
            logger.debug("checkPackageAccess(String " + p + ") Throw SecurityException");
            throw new SecurityException("checkPackageAccess(String " + p + ")");
        }
        logger.debug("checkPackageAccess(String " + p + " OK");
	}

	@Override
	public void checkPermission(Permission perm) throws SecurityException {

        if (perm.getName().contains("exitVM")) {
            logger.debug("checkPermission(Permission " + perm.getName() + " DENIED");
            throw new SecurityException("exit is denied");
        }
        logger.debug("checkPermission(Permission " + perm.getName() + " OK");
	}


	@Override
	public void checkWrite(String file) throws SecurityException {
		for (Map.Entry<String, Boolean> s : KernelSecurityManager.writeAccess.entrySet()) {
			if (s.getValue() && file.startsWith(s.getKey())) {
				// Allowed by predefined access
                logger.debug("checkWrite(String " + file + ") Allowing write access by predefined access");
				return;
			} else if (!s.getValue() && file.equals(s.getKey())) {
				// Request strict pathname
                logger.debug("checkWrite(String " + file + ") Allowing write access by predefined access to ");
				return;
			}
		}
		if (this.isPrivileged()) {
            logger.debug("checkWrite(String " + file + ") checkWrite allows because thread is privileged");
			return;
		}
		try {
			if (this.rootPath == null ||
					(!file.startsWith(this.rootPath.toFile().toString()) &&
							!file.startsWith(this.rootPath.toFile().getCanonicalPath()))) {
				// If the file is not within NetShell root dir, reject.
				// TODO: this should be sufficient but perhaps needs to be revisited
                logger.debug("checkWrite(String " + file + ") reject write file because the file is not an NetShell file");

				throw new SecurityException("Cannot write file " + file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

        FileACL acl = new FileACL(Paths.get(file));

        if (acl.canWrite(KernelThread.currentKernelThread().getUser().getName())) {
            logger.debug("checkWrite(String " + file + ") checkWrite allows because NetShell User ACL for the user allows it.");
            return;
        }

        logger.debug("checkWrite(String " + file + ") checkWrite rejects");

		throw new SecurityException("checkWrite(String " + file + ") Not authorized to write file");
	}

	public void checkWrite(FileDescriptor file) throws SecurityException {
		// System.out.println("checkWrite fd ");
		// throw new SecurityException();
	}

	@Override
	public void checkRead(String file) {
		if (this.rootPath == null || !file.startsWith(this.rootPath.toFile().getAbsolutePath())) {
				// If the file is not within NetShell root dir, allow and rely on system permissions for read.
				// TODO: this should be sufficient but perhaps needs to be revisited
				logger.debug("checkRead(String " + file + " ) not an NetShell file. Rely on system access OK");
				return;
			}
		if (this.isPrivileged()) {
            logger.debug("checkRead(String " + file + " ) OK because thread is privileged");
			return;
		}

        FileACL acl = new FileACL(Paths.get(file));

        if (acl.canRead()) {
            logger.debug("checkRead(String " + file + " )  OK because user NetShell ACL allows it.");
            return;
        }

        logger.debug("checkRead(String " + file + " ) DENIED because thread is user, file is in NetShell rootdir and user ACL does not allows");
		throw new SecurityException(Thread.currentThread().getName() + "Not authorized to read file " + file);
	}

    /**
     * Throws a <code>SecurityException</code> if the
     * calling thread is not allowed to create a new class loader.
     * <p/>
     * This method calls <code>checkPermission</code> with the
     * <code>RuntimePermission("createClassLoader")</code>
     * permission.
     * <p/>
     * If you override this method, then you should make a call to
     * <code>super.checkCreateClassLoader</code>
     * at the point the overridden method would normally throw an
     * exception.
     *
     * @throws SecurityException if the calling thread does not
     *                           have permission
     *                           to create a new class loader.
     * @see ClassLoader#ClassLoader()
     * @see #checkPermission(java.security.Permission) checkPermission
     */
    @Override
    public void checkCreateClassLoader() {
        logger.debug("checkCreateClassLoader() invoke superclass");
        if (isDebug) { if (false) return; };
        super.checkCreateClassLoader();
    }

	@Override
	public ThreadGroup getThreadGroup() {
		// return this.NetShellRootThreadGroup;
		return null;
	}

	/**
	 * All NetShell threads are part of a ThreadGroup that share a same, root, ThreadGroup.
	 * getNetShellRootThreadGroup returns that ThreadGroup.
	 * @return NetShell root ThreadGroup
	 */
	public ThreadGroup getNetShellRootThreadGroup() {
		return this.NetShellRootThreadGroup;
	}

	private boolean isPrivileged() {

		Thread t = Thread.currentThread();
		ThreadGroup NetShellRootThreadGroup = null;
		// BootStrap may be null when running within an IDE: the SecurityManager is changed by NetShell.
		if ((BootStrap.getBootStrap() == null) || (BootStrap.getBootStrap().getSecurityManager() == null)) {
			// Still bootstrapping
			return true;
		}

		NetShellRootThreadGroup = BootStrap.getBootStrap().getSecurityManager().getNetShellRootThreadGroup();

		if (t.getThreadGroup() == null) {
			// Not created yet, this is still bootstraping
			return true;
		} else if (!NetShellRootThreadGroup.parentOf(t.getThreadGroup())) {
			// This thread has no group: not an NetShell thread
			return true;

		} else {
			// This is an NetShell thread.
			return KernelThread.currentKernelThread().isPrivileged();
		}
	}

	/**
	 * Classes that the KernelSecurityManager need to be preloaded so there is not a cyclic dependency
	 */
	private void preloadClasses () {
		Class c = KernelThread.class;
		c = SysCall.class;
		c = NetShellException.class;
		c = net.es.netshell.api.DefaultValues.class;
        c = java.security.Permission.class;
        c = net.es.netshell.kernel.container.Containers.class;

	}

	private void initializePreAuthorized() {
		String classPath =  System.getProperty("java.class.path");
		if ((classPath != null) && (classPath.split(" ").length > 0)) {
			// More than one element in the class path means that NetShell is running within its ONEJAR. Therefore
			// we need to allow write access to the jyphon cache. TODO: this sounds dangerous
			//KernelSecurityManager.writeAccess.put(System.getProperty("java.class.path") + "!", new Boolean(true));
		}
    }

    @Override
    public boolean checkTopLevelWindow(Object window) {
        if (isDebug) { return true; };
        boolean res = super.checkTopLevelWindow(window);
        logger.debug("checkCreateClassLoader() privileged thread invoker super class result= " + res);
        return res;
    }

    @Override
    public void checkAwtEventQueueAccess() {
        logger.debug("checkAwtEventQueueAccess() invoke superclass");
        if (isDebug) { if (false) return; };
        super.checkAwtEventQueueAccess();
    }

    @Override
    public void checkExit(int status) {
        logger.debug("checkExit (int " + status + ") cannot exit NetShell");
        if (isDebug) { if (false) return; };
        if (isPrivileged()) return;
        throw new SecurityException("checkExit (int " + status + ") Cannot exit NetShell");
    }

    @Override
    public void checkExec(String cmd) {
        logger.debug("checkExec (String " + cmd + ") cannot execute host processes");
        throw new ExitSecurityException("Cannot execute host processes");
	}

    /**
     * Creates an object that encapsulates the current execution
     * environment. The result of this method is used, for example, by the
     * three-argument <code>checkConnect</code> method and by the
     * two-argument <code>checkRead</code> method.
     * These methods are needed because a trusted method may be called
     * on to read a file or open a socket on behalf of another method.
     * The trusted method needs to determine if the other (possibly
     * untrusted) method would be allowed to perform the operation on its
     * own.
     * <p> The default implementation of this method is to return
     * an <code>AccessControlContext</code> object.
     *
     * @return an implementation-dependent object that encapsulates
     * sufficient information about the current execution environment
     * to perform some security checks later.
     * @see SecurityManager#checkConnect(String, int,
     * Object) checkConnect
     * @see SecurityManager#checkRead(String,
     * Object) checkRead
     * @see java.security.AccessControlContext AccessControlContext
     */
    @Override
    public Object getSecurityContext() {
        Object res = super.getSecurityContext();
        logger.debug("getSecurityContext() invoke superclass OK result= " + res);
        return res;
    }

    /**
     * Throws a <code>SecurityException</code> if the
     * specified security context is denied access to the resource
     * specified by the given permission.
     * The context must be a security
     * context returned by a previous call to
     * <code>getSecurityContext</code> and the access control
     * decision is based upon the configured security policy for
     * that security context.
     * <p/>
     * If <code>context</code> is an instance of
     * <code>AccessControlContext</code> then the
     * <code>AccessControlContext.checkPermission</code> method is
     * invoked with the specified permission.
     * <p/>
     * If <code>context</code> is not an instance of
     * <code>AccessControlContext</code> then a
     * <code>SecurityException</code> is thrown.
     *
     * @param perm    the specified permission
     * @param context a system-dependent security context.
     * @throws SecurityException    if the specified security context
     *                              is not an instance of <code>AccessControlContext</code>
     *                              (e.g., is <code>null</code>), or is denied access to the
     *                              resource specified by the given permission.
     * @throws NullPointerException if the permission argument is
     *                              <code>null</code>.
     * @see SecurityManager#getSecurityContext()
     * @see java.security.AccessControlContext#checkPermission(java.security.Permission)
     * @since 1.2
     */
    @Override
    public void checkPermission(Permission perm, Object context) {
        logger.debug("checkPermission(Permission " + perm + ", Object " + context + " invoke superclass");
        // There might be a better solution, but OSGI Felix requires allPerm.
        if ((perm.getClass().getCanonicalName().startsWith("org.apache.felix") ||
                (perm.getClass().getCanonicalName().startsWith("org.osgi")))) {
            return;
        }

        if (isDebug) {
            try {
              super.checkPermission(perm, context);
            } catch (Exception e) {
                logger.debug("checkPermission(Permission " + perm + ", Object " + context + " invoke superclass DENIED " + e);
            }
        } else {
            super.checkPermission(perm, context);
        }
    }


    /**
     * Throws a <code>SecurityException</code> if the
     * calling thread is not allowed to modify the thread group argument.
     * <p/>
     * This method is invoked for the current security manager when a
     * new child thread or child thread group is created, and by the
     * <code>setDaemon</code>, <code>setMaxPriority</code>,
     * <code>stop</code>, <code>suspend</code>, <code>resume</code>, and
     * <code>destroy</code> methods of class <code>ThreadGroup</code>.
     * <p/>
     * If the thread group argument is the system thread group (
     * has a <code>null</code> parent) then
     * this method calls <code>checkPermission</code> with the
     * <code>RuntimePermission("modifyThreadGroup")</code> permission.
     * If the thread group argument is <i>not</i> the system thread group,
     * this method just returns silently.
     * <p/>
     *
     * @param g the thread group to be checked.
     * @throws SecurityException    if the calling thread does not have
     *                              permission to modify the thread group.
     * @throws NullPointerException if the thread group argument is
     *                              <code>null</code>.
     * @see ThreadGroup#destroy() destroy
     * @see ThreadGroup#resume() resume
     * @see ThreadGroup#setDaemon(boolean) setDaemon
     * @see ThreadGroup#setMaxPriority(int) setMaxPriority
     * @see ThreadGroup#stop() stop
     * @see ThreadGroup#suspend() suspend
     * @see #checkPermission(java.security.Permission) checkPermission
     */
    @Override
    public void checkAccess(ThreadGroup g) {
        logger.debug("checkAccess(ThreadGroup " + g + " ) invoke superclass");
        if (isDebug) { if (false) return; };
        super.checkAccess(g);
    }


    /**
     * Throws a <code>SecurityException</code> if the
     * calling thread is not allowed to dynamic link the library code
     * specified by the string argument file. The argument is either a
     * simple library name or a complete filename.
     * <p/>
     * This method is invoked for the current security manager by
     * methods <code>load</code> and <code>loadLibrary</code> of class
     * <code>Runtime</code>.
     * <p/>
     * This method calls <code>checkPermission</code> with the
     * <code>RuntimePermission("loadLibrary."+lib)</code> permission.
     * <p/>
     * If you override this method, then you should make a call to
     * <code>super.checkLink</code>
     * at the point the overridden method would normally throw an
     * exception.
     *
     * @param lib the name of the library.
     * @throws SecurityException    if the calling thread does not have
     *                              permission to dynamically link the library.
     * @throws NullPointerException if the <code>lib</code> argument is
     *                              <code>null</code>.
     * @see Runtime#load(String)
     * @see Runtime#loadLibrary(String)
     * @see #checkPermission(java.security.Permission) checkPermission
     */
    @Override
    public void checkLink(String lib) {
        if (this.isPrivileged()) {
            logger.debug("checkLink(String " + lib + " ) thread is privileged, invoke superclass");
            if (isDebug) { if (false) return; };
            super.checkLink(lib);
            return;
        }
        logger.debug("checkLink(String " + lib + " DENIED");
        throw new SecurityException("checkLink(String \" + lib + \" ) DENIED");
    }

    /**
     * Throws a <code>SecurityException</code> if the
     * calling thread is not allowed to read from the specified file
     * descriptor.
     * <p/>
     * This method calls <code>checkPermission</code> with the
     * <code>RuntimePermission("readFileDescriptor")</code>
     * permission.
     * <p/>
     * If you override this method, then you should make a call to
     * <code>super.checkRead</code>
     * at the point the overridden method would normally throw an
     * exception.
     *
     * @param fd the system-dependent file descriptor.
     * @throws SecurityException    if the calling thread does not have
     *                              permission to access the specified file descriptor.
     * @throws NullPointerException if the file descriptor argument is
     *                              <code>null</code>.
     * @see java.io.FileDescriptor
     * @see #checkPermission(java.security.Permission) checkPermission
     */
    @Override
    public void checkRead(FileDescriptor fd) {
        logger.debug("checkRead(FileDescriptor " + fd + " ) invoke superclass");
        if (isDebug) { if (false) return; };
        super.checkRead(fd);
    }

    /**
     * Throws a <code>SecurityException</code> if the
     * specified security context is not allowed to read the file
     * specified by the string argument. The context must be a security
     * context returned by a previous call to
     * <code>getSecurityContext</code>.
     * <p> If <code>context</code> is an instance of
     * <code>AccessControlContext</code> then the
     * <code>AccessControlContext.checkPermission</code> method will
     * be invoked with the <code>FilePermission(file,"read")</code> permission.
     * <p> If <code>context</code> is not an instance of
     * <code>AccessControlContext</code> then a
     * <code>SecurityException</code> is thrown.
     * <p/>
     * If you override this method, then you should make a call to
     * <code>super.checkRead</code>
     * at the point the overridden method would normally throw an
     * exception.
     *
     * @param file    the system-dependent filename.
     * @param context a system-dependent security context.
     * @throws SecurityException    if the specified security context
     *                              is not an instance of <code>AccessControlContext</code>
     *                              (e.g., is <code>null</code>), or does not have permission
     *                              to read the specified file.
     * @throws NullPointerException if the <code>file</code> argument is
     *                              <code>null</code>.
     * @see SecurityManager#getSecurityContext()
     * @see java.security.AccessControlContext#checkPermission(java.security.Permission)
     */
    @Override
    public void checkRead(String file, Object context) {
        logger.debug("checkRead(String file= " + file + ", Object context= " + context + " ) invoke superclass");
        if (isDebug) { if (false) return; };
        super.checkRead(file, context);
    }

    /**
     * Throws a <code>SecurityException</code> if the
     * calling thread is not allowed to delete the specified file.
     * <p/>
     * This method is invoked for the current security manager by the
     * <code>delete</code> method of class <code>File</code>.
     * <p/>
     * This method calls <code>checkPermission</code> with the
     * <code>FilePermission(file,"delete")</code> permission.
     * <p/>
     * If you override this method, then you should make a call to
     * <code>super.checkDelete</code>
     * at the point the overridden method would normally throw an
     * exception.
     *
     * @param file the system-dependent filename.
     * @throws SecurityException    if the calling thread does not
     *                              have permission to delete the file.
     * @throws NullPointerException if the <code>file</code> argument is
     *                              <code>null</code>.
     * @see java.io.File#delete()
     * @see #checkPermission(java.security.Permission) checkPermission
     */
    @Override
    public void checkDelete(String file) {
        logger.debug("checkDelete(String file= " + file + " ) invoke superclass");
        if (isDebug) { if (false) return; };
        super.checkDelete(file);
    }

    /**
     * Throws a <code>SecurityException</code> if the
     * calling thread is not allowed to open a socket connection to the
     * specified host and port number.
     * <p/>
     * A port number of <code>-1</code> indicates that the calling
     * method is attempting to determine the IP address of the specified
     * host name.
     * <p/>
     * This method calls <code>checkPermission</code> with the
     * <code>SocketPermission(host+":"+port,"connect")</code> permission if
     * the port is not equal to -1. If the port is equal to -1, then
     * it calls <code>checkPermission</code> with the
     * <code>SocketPermission(host,"resolve")</code> permission.
     * <p/>
     * If you override this method, then you should make a call to
     * <code>super.checkConnect</code>
     * at the point the overridden method would normally throw an
     * exception.
     *
     * @param host the host name port to connect to.
     * @param port the protocol port to connect to.
     * @throws SecurityException    if the calling thread does not have
     *                              permission to open a socket connection to the specified
     *                              <code>host</code> and <code>port</code>.
     * @throws NullPointerException if the <code>host</code> argument is
     *                              <code>null</code>.
     * @see #checkPermission(java.security.Permission) checkPermission
     */
    @Override
    public void checkConnect(String host, int port) {
        logger.debug("checkConnect(String host= " + host + " port= " + port + " invoke superclass");
        if (isDebug) { if (false) return; };
        super.checkConnect(host, port);
    }

    /**
     * Throws a <code>SecurityException</code> if the
     * specified security context is not allowed to open a socket
     * connection to the specified host and port number.
     * <p/>
     * A port number of <code>-1</code> indicates that the calling
     * method is attempting to determine the IP address of the specified
     * host name.
     * <p> If <code>context</code> is not an instance of
     * <code>AccessControlContext</code> then a
     * <code>SecurityException</code> is thrown.
     * <p/>
     * Otherwise, the port number is checked. If it is not equal
     * to -1, the <code>context</code>'s <code>checkPermission</code>
     * method is called with a
     * <code>SocketPermission(host+":"+port,"connect")</code> permission.
     * If the port is equal to -1, then
     * the <code>context</code>'s <code>checkPermission</code> method
     * is called with a
     * <code>SocketPermission(host,"resolve")</code> permission.
     * <p/>
     * If you override this method, then you should make a call to
     * <code>super.checkConnect</code>
     * at the point the overridden method would normally throw an
     * exception.
     *
     * @param host    the host name port to connect to.
     * @param port    the protocol port to connect to.
     * @param context a system-dependent security context.
     * @throws SecurityException    if the specified security context
     *                              is not an instance of <code>AccessControlContext</code>
     *                              (e.g., is <code>null</code>), or does not have permission
     *                              to open a socket connection to the specified
     *                              <code>host</code> and <code>port</code>.
     * @throws NullPointerException if the <code>host</code> argument is
     *                              <code>null</code>.
     * @see SecurityManager#getSecurityContext()
     * @see java.security.AccessControlContext#checkPermission(java.security.Permission)
     */
    @Override
    public void checkConnect(String host, int port, Object context) {
        logger.debug("checkConnect(String host= " + host + " port= " + port + " context= " + context + " invoke superclass");
        if (isDebug) { if (false) return; };
        super.checkConnect(host, port, context);
    }

    /**
     * Throws a <code>SecurityException</code> if the
     * calling thread is not allowed to wait for a connection request on
     * the specified local port number.
     * <p/>
     * If port is not 0, this method calls
     * <code>checkPermission</code> with the
     * <code>SocketPermission("localhost:"+port,"listen")</code>.
     * If port is zero, this method calls <code>checkPermission</code>
     * with <code>SocketPermission("localhost:1024-","listen").</code>
     * <p/>
     * If you override this method, then you should make a call to
     * <code>super.checkListen</code>
     * at the point the overridden method would normally throw an
     * exception.
     *
     * @param port the local port.
     * @throws SecurityException if the calling thread does not have
     *                           permission to listen on the specified port.
     * @see #checkPermission(java.security.Permission) checkPermission
     */
    @Override
    public void checkListen(int port) {
        logger.debug("checkListen (port=  " + port + ") invoke superclass");
        { if (false) return; };
        super.checkListen(port);
    }

    /**
     * Throws a <code>SecurityException</code> if the
     * calling thread is not permitted to accept a socket connection from
     * the specified host and port number.
     * <p/>
     * This method is invoked for the current security manager by the
     * <code>accept</code> method of class <code>ServerSocket</code>.
     * <p/>
     * This method calls <code>checkPermission</code> with the
     * <code>SocketPermission(host+":"+port,"accept")</code> permission.
     * <p/>
     * If you override this method, then you should make a call to
     * <code>super.checkAccept</code>
     * at the point the overridden method would normally throw an
     * exception.
     *
     * @param host the host name of the socket connection.
     * @param port the port number of the socket connection.
     * @throws SecurityException    if the calling thread does not have
     *                              permission to accept the connection.
     * @throws NullPointerException if the <code>host</code> argument is
     *                              <code>null</code>.
     * @see java.net.ServerSocket#accept()
     * @see #checkPermission(java.security.Permission) checkPermission
     */
    @Override
    public void checkAccept(String host, int port) {
        logger.debug("checkAccept (host= " + host + " port=  " + port + ") invoke superclass");
        if (isDebug) { if (false) return; };
        super.checkAccept(host, port);
    }

    /**
     * Throws a <code>SecurityException</code> if the
     * calling thread is not allowed to use
     * (join/leave/send/receive) IP multicast.
     * <p/>
     * This method calls <code>checkPermission</code> with the
     * <code>java.net.SocketPermission(maddr.getHostAddress(),
     * "accept,connect")</code> permission.
     * <p/>
     * If you override this method, then you should make a call to
     * <code>super.checkMulticast</code>
     * at the point the overridden method would normally throw an
     * exception.
     *
     * @param maddr Internet group address to be used.
     * @throws SecurityException    if the calling thread is not allowed to
     *                              use (join/leave/send/receive) IP multicast.
     * @throws NullPointerException if the address argument is
     *                              <code>null</code>.
     * @see #checkPermission(java.security.Permission) checkPermission
     * @since JDK1.1
     */
    @Override
    public void checkMulticast(InetAddress maddr) {
        logger.debug("checkMulticast(InetAddress " + maddr.toString() + " invoke superclass");
        if (isDebug) { if (false) return; };
        super.checkMulticast(maddr);
    }

    /**
     * Throws a <code>SecurityException</code> if the
     * calling thread is not allowed to access or modify the system
     * properties.
     * <p/>
     * This method is used by the <code>getProperties</code> and
     * <code>setProperties</code> methods of class <code>System</code>.
     * <p/>
     * This method calls <code>checkPermission</code> with the
     * <code>PropertyPermission("*", "read,write")</code> permission.
     * <p/>
     * If you override this method, then you should make a call to
     * <code>super.checkPropertiesAccess</code>
     * at the point the overridden method would normally throw an
     * exception.
     * <p/>
     *
     * @throws SecurityException if the calling thread does not have
     *                           permission to access or modify the system properties.
     * @see System#getProperties()
     * @see System#setProperties(java.util.Properties)
     * @see #checkPermission(java.security.Permission) checkPermission
     */
    @Override
    public void checkPropertiesAccess() {
        logger.debug("checkPropertiesAccess() invoke superclass");
        if (isDebug) { if (false) return; };
        super.checkPropertiesAccess();
    }

    /**
     * Throws a <code>SecurityException</code> if the
     * calling thread is not allowed to access the system property with
     * the specified <code>key</code> name.
     * <p/>
     * This method is used by the <code>getProperty</code> method of
     * class <code>System</code>.
     * <p/>
     * This method calls <code>checkPermission</code> with the
     * <code>PropertyPermission(key, "read")</code> permission.
     * <p/>
     * <p/>
     * If you override this method, then you should make a call to
     * <code>super.checkPropertyAccess</code>
     * at the point the overridden method would normally throw an
     * exception.
     *
     * @param key a system property key.
     * @throws SecurityException        if the calling thread does not have
     *                                  permission to access the specified system property.
     * @throws NullPointerException     if the <code>key</code> argument is
     *                                  <code>null</code>.
     * @throws IllegalArgumentException if <code>key</code> is empty.
     * @see System#getProperty(String)
     * @see #checkPermission(java.security.Permission) checkPermission
     */
    @Override
    public void checkPropertyAccess(String key) {
        logger.debug("checkPropertiesAccess(String key= " + key + " ) invoke superclass");
        if (isDebug) { if (false) return; };
        super.checkPropertyAccess(key);
    }

    /**
     * Throws a <code>SecurityException</code> if the
     * calling thread is not allowed to initiate a print job request.
     * <p/>
     * This method calls
     * <code>checkPermission</code> with the
     * <code>RuntimePermission("queuePrintJob")</code> permission.
     * <p/>
     * If you override this method, then you should make a call to
     * <code>super.checkPrintJobAccess</code>
     * at the point the overridden method would normally throw an
     * exception.
     * <p/>
     *
     * @throws SecurityException if the calling thread does not have
     *                           permission to initiate a print job request.
     * @see #checkPermission(java.security.Permission) checkPermission
     * @since JDK1.1
     */
    @Override
    public void checkPrintJobAccess() {
        logger.debug("checkPrintJobAccess() invoke superclass");
        if (isDebug) { if (false) return; };;
        super.checkPrintJobAccess();
    }

    /**
     * Throws a <code>SecurityException</code> if the
     * calling thread is not allowed to access the system clipboard.
     * <p/>
     * This method calls <code>checkPermission</code> with the
     * <code>AWTPermission("accessClipboard")</code>
     * permission.
     * <p/>
     * If you override this method, then you should make a call to
     * <code>super.checkSystemClipboardAccess</code>
     * at the point the overridden method would normally throw an
     * exception.
     *
     * @throws SecurityException if the calling thread does not have
     *                           permission to access the system clipboard.
     * @see #checkPermission(java.security.Permission) checkPermission
     * @since JDK1.1
     */
    @Override
    public void checkSystemClipboardAccess() {
        logger.debug("checkSystemClipboardAccess() invoke superclass");
        if (isDebug) { if (false) return; };
        super.checkSystemClipboardAccess();
    }

    /**
     * Throws a <code>SecurityException</code> if the
     * calling thread is not allowed to define classes in the package
     * specified by the argument.
     * <p/>
     * This method is used by the <code>loadClass</code> method of some
     * class loaders.
     * <p/>
     * This method first gets a list of restricted packages by
     * obtaining a comma-separated list from a call to
     * <code>java.security.Security.getProperty("package.definition")</code>,
     * and checks to see if <code>pkg</code> starts with or equals
     * any of the restricted packages. If it does, then
     * <code>checkPermission</code> gets called with the
     * <code>RuntimePermission("defineClassInPackage."+pkg)</code>
     * permission.
     * <p/>
     * If this method is overridden, then
     * <code>super.checkPackageDefinition</code> should be called
     * as the first line in the overridden method.
     *
     * @param pkg the package name.
     * @throws SecurityException if the calling thread does not have
     *                           permission to define classes in the specified package.
     * @see ClassLoader#loadClass(String, boolean)
     * @see java.security.Security#getProperty getProperty
     * @see #checkPermission(java.security.Permission) checkPermission
     */
    @Override
    public void checkPackageDefinition(String pkg) {
        logger.debug("checkPackageDefinition(String pkg= " + pkg + " invoke superclass");
        if (isDebug) { if (false) return; };
        super.checkPackageDefinition(pkg);
    }

    /**
     * Throws a <code>SecurityException</code> if the
     * calling thread is not allowed to set the socket factory used by
     * <code>ServerSocket</code> or <code>Socket</code>, or the stream
     * handler factory used by <code>URL</code>.
     * <p/>
     * This method calls <code>checkPermission</code> with the
     * <code>RuntimePermission("setFactory")</code> permission.
     * <p/>
     * If you override this method, then you should make a call to
     * <code>super.checkSetFactory</code>
     * at the point the overridden method would normally throw an
     * exception.
     * <p/>
     *
     * @throws SecurityException if the calling thread does not have
     *                           permission to specify a socket factory or a stream
     *                           handler factory.
     * @see java.net.ServerSocket#setSocketFactory(java.net.SocketImplFactory) setSocketFactory
     * @see java.net.Socket#setSocketImplFactory(java.net.SocketImplFactory) setSocketImplFactory
     * @see java.net.URL#setURLStreamHandlerFactory(java.net.URLStreamHandlerFactory) setURLStreamHandlerFactory
     * @see #checkPermission(java.security.Permission) checkPermission
     */
    @Override
    public void checkSetFactory() {
        logger.debug("checkSetFactory() invoke superclass");
        if (isDebug) { if (false) return; };
        super.checkSetFactory();
    }

    /**
     * Throws a <code>SecurityException</code> if the
     * calling thread is not allowed to access members.
     * <p/>
     * The default policy is to allow access to PUBLIC members, as well
     * as access to classes that have the same class loader as the caller.
     * In all other cases, this method calls <code>checkPermission</code>
     * with the <code>RuntimePermission("accessDeclaredMembers")
     * </code> permission.
     * <p/>
     * If this method is overridden, then a call to
     * <code>super.checkMemberAccess</code> cannot be made,
     * as the default implementation of <code>checkMemberAccess</code>
     * relies on the code being checked being at a stack depth of
     * 4.
     *
     * @param clazz the class that reflection is to be performed on.
     * @param which type of access, PUBLIC or DECLARED.
     * @throws SecurityException    if the caller does not have
     *                              permission to access members.
     * @throws NullPointerException if the <code>clazz</code> argument is
     *                              <code>null</code>.
     * @see java.lang.reflect.Member
     * @see #checkPermission(java.security.Permission) checkPermission
     * @since JDK1.1
     */
    @Override
    public void checkMemberAccess(Class<?> clazz, int which) {
        logger.debug("checkMemberAccess(Class class= " + clazz + " int which= " + which + " invoke superclass");
        if (isDebug) { if (false) return; };
        super.checkMemberAccess(clazz, which);
    }

    /**
     * Determines whether the permission with the specified permission target
     * name should be granted or denied.
     * <p/>
     * <p> If the requested permission is allowed, this method returns
     * quietly. If denied, a SecurityException is raised.
     * <p/>
     * <p> This method creates a <code>SecurityPermission</code> object for
     * the given permission target name and calls <code>checkPermission</code>
     * with it.
     * <p/>
     * <p> See the documentation for
     * <code>{@link java.security.SecurityPermission}</code> for
     * a list of possible permission target names.
     * <p/>
     * <p> If you override this method, then you should make a call to
     * <code>super.checkSecurityAccess</code>
     * at the point the overridden method would normally throw an
     * exception.
     *
     * @param target the target name of the <code>SecurityPermission</code>.
     * @throws SecurityException        if the calling thread does not have
     *                                  permission for the requested access.
     * @throws NullPointerException     if <code>target</code> is null.
     * @throws IllegalArgumentException if <code>target</code> is empty.
     * @see #checkPermission(java.security.Permission) checkPermission
     * @since JDK1.1
     */
    @Override
    public void checkSecurityAccess(String target) {
        if (this.isPrivileged()) {
            logger.debug("checkSecurityAccess(String target= " + target + " ) thread is privileged invoke superclass");
            if (isDebug) { if (false) return; };
            super.checkSecurityAccess(target);
            return;
        }
        logger.debug("checkSecurityAccess(String target= " + target + " DENIED");
        throw new SecurityException("checkSecurityAccess(String target= " + target + " DENIED");
    }

    @Override
    public boolean getInCheck() {
        return super.getInCheck();
    }

    @Override
    protected Class[] getClassContext() {
        logger.debug("getClassContext() invoke superclass");
        Class[] res = super.getClassContext();
        logger.debug("getClassContext() invoke superclass returns= " + res);
        return res;
    }

    @Override
    protected ClassLoader currentClassLoader() {
        return super.currentClassLoader();
    }

    @Override
    protected Class<?> currentLoadedClass() {
        return super.currentLoadedClass();
    }

    @Override
    protected int classDepth(String name) {
        return super.classDepth(name);
    }

    @Override
    protected int classLoaderDepth() {
        return super.classLoaderDepth();
    }

    @Override
    protected boolean inClass(String name) {
        return super.inClass(name);
    }

    @Override
    protected boolean inClassLoader() {
        return super.inClassLoader();
    }

    @Override
    public void checkMulticast(InetAddress maddr, byte ttl) {
        super.checkMulticast(maddr, ttl);
    }
}