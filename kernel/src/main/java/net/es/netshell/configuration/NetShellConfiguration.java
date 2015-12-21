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
