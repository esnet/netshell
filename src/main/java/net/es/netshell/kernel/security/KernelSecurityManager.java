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

	public KernelSecurityManager() {

		// See if SecurityManager should be disabled.  We need to be very conservative here in terms
		// of letting admins turn this off.
		if (NetShellConfiguration.getInstance().getGlobal().getSecurityManagerDisabled() != 0) {
			logger.warn("NetShell SecurityManager is currently disabled.  No security checks will be run.  MUST NOT BE USED IN PRODUCTION.");
			return;
		}

		this.preloadClasses();
		this.initializePreAuthorized();

		System.setSecurityManager(this);

		// Figure out the NetShell root directory.
		String rootdir = NetShellConfiguration.getInstance().getGlobal().getRootDirectory();
		this.rootPath = Paths.get(rootdir).normalize();
	}

	@Override
	public void checkAccess(Thread t) throws SecurityException {
		// System.out.println("checkAccess(Thread current= " + Thread.currentThread().getName() + " t = " + t.getName());
		// Threads that are not part of NetShell ThreadGroup are authorized
		Thread currentThread = Thread.currentThread();
		if (this.isPrivileged()) {
			return;
		}

		if ((currentThread.getThreadGroup() == null) ||
				(KernelThread.currentKernelThread().isPrivileged()) ||
				( !this.NetShellRootThreadGroup.parentOf(currentThread.getThreadGroup()))) {
			return;
		}
		if (Thread.currentThread().getThreadGroup().parentOf(t.getThreadGroup())) {
			// A thread can do whatever it wants on thread of the same user
			return;
		}
		if ( ! this.NetShellRootThreadGroup.parentOf(t.getThreadGroup())) {
			// This is a non NetShell Thread. Allow since the only non NetShell thread that can be referenced to are
			// from java library classes. This is safe.
			return;
		}

		throw new SecurityException("Illegal Thread access from " + Thread.currentThread().getName() + " onto " +
				t.getName());
	}

	@Override
	public void checkPackageAccess(String p) throws SecurityException {

		// TODO: lomax@es.net the restriction on es.net classes sounds like a neat idea, but might not be
		// neither realistic nor usefull. To revisit.
	}

	@Override
	public void checkPermission(Permission perm) throws SecurityException {
        if (perm.getName().contains("exitVM")) {
            throw new SecurityException("exit is denied");
        }
	}


	@Override
	public void checkWrite(String file) throws SecurityException {
		logger.debug("checkWrite " + file );
		for (Map.Entry<String, Boolean> s : KernelSecurityManager.writeAccess.entrySet()) {
			if (s.getValue() && file.startsWith(s.getKey())) {
				// Allowed by predefined access
				logger.debug("Allowing write access by predefined access to " + file);
				return;
			} else if (!s.getValue() && file.equals(s.getKey())) {
				// Request strict pathname
				logger.debug("Allowing write access by predefined access to " + file);
				return;
			}
		}
		if (this.isPrivileged()) {
			logger.debug("checkWrite allows " + file + " because thread is privileged");
			return;
		}
		try {
			if (this.rootPath == null ||
					(!file.startsWith(this.rootPath.toFile().toString()) &&
							!file.startsWith(this.rootPath.toFile().getCanonicalPath()))) {
				// If the file is not within NetShell root dir, reject.
				// TODO: this should be sufficient but perhaps needs to be revisited
				logger.debug("reject write file " + file + " because the file is not an NetShell file");

				throw new SecurityException("Cannot write file " + file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

        FileACL acl = new FileACL(Paths.get(file));

        if (acl.canWrite(KernelThread.currentKernelThread().getUser().getName())) {
            logger.debug("checkWrite allows " + file + " because NetShell User ACL for the user allows it.");
            return;
        }

		logger.debug("checkWrite rejects " + file);

		throw new SecurityException("Not authorized to write file " + file);
	}

	public void checkWrite(FileDescriptor file) throws SecurityException {
		// System.out.println("checkWrite fd ");
		// throw new SecurityException();
	}

	@Override
	public void checkRead(String file) {
		logger.debug("checkRead starts " + file);
		if (this.rootPath == null || !file.startsWith(this.rootPath.toFile().getAbsolutePath())) {
				// If the file is not within NetShell root dir, allow and rely on system permissions for read.
				// TODO: this should be sufficient but perhaps needs to be revisited
				logger.debug("checkRead ok " + file + " not an NetShell file. Rely on system access");
				return;
			}
		if (this.isPrivileged()) {
			logger.debug("checkRead ok " + file + " because thread is privileged");
			return;
		}

        FileACL acl = new FileACL(Paths.get(file));

        if (acl.canRead()) {
            logger.debug("checkRead ok " + file + " because user NetShell ACL allows it.");
            return;
        }

		logger.debug("checkRead reject  " + file + " because thread is user, file is in NetShell rootdir and user ACL does not allows");
		throw new SecurityException(Thread.currentThread().getName() + "Not authorized to read file " + file);
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
        return super.checkTopLevelWindow(window);
    }

    @Override
    public void checkAwtEventQueueAccess() {
        super.checkAwtEventQueueAccess();
    }

    @Override
    public void checkExit(int status) {
        super.checkExit(status);
        throw new SecurityException("Cannot quit NetShell");
    }

    @Override
    public void checkExec(String cmd) {
        if (KernelThread.currentKernelThread().isPrivileged()) {
            return;
        }
        throw new ExitSecurityException("Cannot execute UNIX processes");
	}
}