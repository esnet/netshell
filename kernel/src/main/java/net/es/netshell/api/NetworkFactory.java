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
package net.es.netshell.api;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lomax on 6/6/14.
 */
public class NetworkFactory extends Resource {
    public final static String FACTORY_DIR = "/networks";
    public final static String FACTORY_CONFIGFILE = "factory";
    public final static String LOCAL_LAYER1 = "localLayer1";
    public final static String LOCAL_LAYER2 = "localLayer2";

    private List<NetworkProviderDescriptor> providers; // List of class name implementing network providers
    private HashMap<String, NetworkProvider> networkProviders = new HashMap<String,NetworkProvider>();
    private static NetworkFactory  instance;
    private static Object instanceLock = new Object();

    public NetworkFactory() throws IOException {

    }

    private void startProviders() {
        // Initialize the networks
        if (this.providers != null) {
            for (NetworkProviderDescriptor provider : this.providers) {
                this.startProvider(provider);
            }
        }
    }
    private void startProvider(NetworkProviderDescriptor provider) {
        try {
            NetworkProvider networkProvider =
                    (NetworkProvider) Class.forName(provider.getClassName()).newInstance();

            this.networkProviders.put(provider.getType(),networkProvider);

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public NetworkProvider retrieveNetworkProvider (String type) {
        return this.networkProviders.get(type);
    }

    public static NetworkFactory instance() {
        synchronized (NetworkFactory.instanceLock) {
            if (NetworkFactory.instance == null) {
                try {
                    NetworkFactory.instance = (NetworkFactory) Resource.newObjectFromFile(
                            Paths.get(FACTORY_DIR,FACTORY_CONFIGFILE).toString());
                    NetworkFactory.instance().startProviders();
                } catch (InstantiationException e) {
                    throw new RuntimeException (e);
                }
            }
        }
        return NetworkFactory.instance;
    }

    public List<NetworkProviderDescriptor> getProviders() {
        return providers;
    }

    public void setProviders(List<NetworkProviderDescriptor> providers) {
        this.providers = providers;
    }

    public synchronized void registerNetworkProvider(String className, String type) throws IOException {
        if (this.providers == null) {
            this.providers = new ArrayList<NetworkProviderDescriptor>();
        }
        // Checks if there is already a provider for this type
        if (this.networkProviders.containsKey(type)) {
            // Remove existing provider
            NetworkProvider provider = this.networkProviders.get(type);
            this.networkProviders.remove(provider);
            ArrayList<NetworkProviderDescriptor> toRemove = new ArrayList<NetworkProviderDescriptor>();
            for (NetworkProviderDescriptor desc : this.providers) {
                if (desc.getType().equals(type)) {
                    // Add it to the list of descriptors to be removed
                    toRemove.add(desc);
                }
            }
            for (NetworkProviderDescriptor desc : toRemove) {
                this.providers.remove(desc);
            }
        }
        NetworkProviderDescriptor provider = new NetworkProviderDescriptor(className,type);
        this.providers.add(provider);
        this.saveToFile(Paths.get(FACTORY_DIR,FACTORY_CONFIGFILE).toString());
        this.startProvider(provider);
    }
}

