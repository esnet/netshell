/*
 * Copyright (c) 2014, Regents of the University of California  All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.es.netshell.configuration;

import net.es.netshell.api.PersistentObject;
import net.es.netshell.api.PropertyKeys;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by lomax on 6/23/14.
 */
public class NetShellConfiguration extends PersistentObject {
    @JsonIgnore
    private static NetShellConfiguration instance;
    @JsonIgnore
    public static String DEFAULT_FILENAME = "netshell.json.default";

    @JsonIgnore
    private boolean canSet = false;
    @JsonIgnore
    private final static Logger logger = LoggerFactory.getLogger(NetShellConfiguration.class);


    private GlobalConfiguration global;


    public static NetShellConfiguration getInstance() {
        if (instance == null) {
            instance = NetShellConfiguration.loadConfiguration();
        }
        return instance;
    }

    /**
     * Special constructor that is intended to create a instance of the
     * NetShellConfiguration that can be set
     * @param canSet
     */
    public NetShellConfiguration(boolean canSet){
        this.canSet = true;
    }

    public NetShellConfiguration() {

    }

    public GlobalConfiguration getGlobal() {
        return global;
    }

    public final synchronized void setGlobal(GlobalConfiguration global) {
        if (this.global == null) {
            // Can only set once. Fail silently otherwise
            this.global = global;
            // Make it read-only
            this.getGlobal().readOnly();
        }
    }
    /**
     * Loads from the configuration file. If the file does not exist, the configuration is
     * "settable". As soon as it is writen onto the file, the configuration is set to not settable.
     * @return the singleton NetShellConfiguration.
     */
    private static NetShellConfiguration loadConfiguration () {

        String configurationFilePath = System.getProperty(PropertyKeys.NETSHELL_CONFIGURATION);

        if (configurationFilePath == null) {
            logger.info("No configuration file property!");
            configurationFilePath = DEFAULT_FILENAME;
        }
        NetShellConfiguration netShellConfiguration = null;

        // Read the configuration
        try {
            netShellConfiguration = (NetShellConfiguration) PersistentObject.newObject(NetShellConfiguration.class,
                                                                       configurationFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        if (netShellConfiguration.isNewInstance()) {
            // This is a new instance. Can set
            netShellConfiguration.canSet = true;
            // Allocate a default GlobalConfiguration
            netShellConfiguration.global = new GlobalConfiguration();
        }
        logger.info("Master configuration file is {}", new File(configurationFilePath).getAbsolutePath());
        return netShellConfiguration;
    }

    public void save(File file) throws IOException {
        super.save(file.getPath());
        // Can no longer be modified within NetShell
        this.canSet = false;
        this.global.readOnly();
    }

}
