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
package net.es.netshell.configuration;

import net.es.netshell.api.DefaultValues;
import net.es.netshell.sshd.SshdIoServiceFactory;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * NetShell global configuration object. It is intended to be used as a read-only singleton.
 * Defines the behavior of the NetShell main daemon.
 */
public class GlobalConfiguration {

    private String defaultLogLevel = "info";
    private String rootDirectory = DefaultValues.NETSHELL_DEFAULT_ROOTDIR;
    private int sshDisabled = 0;
    private int sshPort = 8000;
    private int sshIdleTimeout = 3600000;
    private int sshNbWorkerThreads = SshdIoServiceFactory.DEFAULT_NB_WORKER_THREADS;
    private int securityManagerDisabled = 0;
    private int useDB = 0;
    private String dbHost = "localhost";
    private int dbPort = 27017;
    private String dbUser = "enos";
    private String dbUserPassword =  "enos";
    private String dbName = "enos";
    @JsonIgnore
    private boolean canSet = true;

    public String getDefaultLogLevel() {
        return defaultLogLevel;
    }

    public String getRootDirectory() {
        return rootDirectory;
    }

    public int getSshDisabled() {
        return sshDisabled;
    }

    public int getSshPort() {
        return sshPort;
    }

    public int getSshIdleTimeout() {
        return sshIdleTimeout;
    }

    public int getSecurityManagerDisabled() {
        return securityManagerDisabled;
    }

    public int getUseDB() { return this.useDB;}

    public boolean useDB () {
        return this.useDB == 1;
    }

    public String getDbUserPassword() {
        return dbUserPassword;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbHost() {
        return dbHost;
    }

    public int getDbPort() {
        return dbPort;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDefaultLogLevel(String defaultLogLevel) {
        if (!this.canSet) {
            // Silently fail
            return;
        }
        this.defaultLogLevel = defaultLogLevel;
    }

    public void setRootDirectory(String rootDirectory) {
        if (!this.canSet) {
            // Silently fail
            return;
        }
        this.rootDirectory = rootDirectory;
    }

    public void setSshDisabled(int sshDisabled) {
        if (!this.canSet) {
            // Silently fail
            return;
        }
        this.sshDisabled = sshDisabled;
    }

    public void setUseDB(int useDB) {
        if (!this.canSet) {
            // Silently fail
            return;
        }
        this.useDB = useDB;
    }

    public void setDbHost(String dbHost) {
        if (!this.canSet) {
            // Silently fail
            return;
        }
        this.dbHost = dbHost;
    }

    public void setDbPort(int dbPort) {
        if (!this.canSet) {
            // Silently fail
            return;
        }
        this.dbPort = dbPort;
    }

    public void setDbName(String dbName) {
        if (!this.canSet) {
            // Silently fail
            return;
        }
        this.dbName = dbName;
    }

    public void setDbUser(String dbUser) {
        if (!this.canSet) {
            // Silently fail
            return;
        }
        this.dbUser = dbUser;
    }

    public void setDbUserPassword(String dbUserPassword) {
        if (!this.canSet) {
            // Silently fail
            return;
        }
        this.dbUserPassword = dbUserPassword;
    }

    public void setSshPort(int sshPort) {
        if (!this.canSet) {
            // Silently fail
            return;
        }
        this.sshPort = sshPort;
    }

    public void setSshIdleTimeout(int sshIdleTimeout) {
        if (!this.canSet) {
            // Silently fail
            return;
        }
        this.sshIdleTimeout = sshIdleTimeout;
    }

    public void setSecurityManagerDisabled(int securityManagerDisabled) {
        if (!this.canSet) {
            // Silently fail
            return;
        }
        this.securityManagerDisabled = securityManagerDisabled;
    }

    public int getSshNbWorkerThreads() {
        return sshNbWorkerThreads;
    }

    public void setSshNbWorkerThreads(int sshNbWorkerThreads) {
        this.sshNbWorkerThreads = sshNbWorkerThreads;
    }

    public GlobalConfiguration() {
    }

    public void readOnly() {
        this.canSet = false;
    }
}
