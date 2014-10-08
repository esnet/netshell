/*
 * Copyright (c) 2014, Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
