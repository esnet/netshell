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

import net.es.netshell.boot.BootStrap;
import net.es.netshell.osgi.OsgiBundlesClassLoader;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.jgrapht.graph.DefaultListenableGraph;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lomax on 5/21/14.
 */
public class TopologyFactory extends PersistentObject {
    public final static String FACTORY_DIR = "/topologies";
    public final static String FACTORY_CONFIGFILE = "factory";
    public final static String LOCAL_LAYER1 = "localLayer1";
    public final static String LOCAL_LAYER2 = "localLayer2";

    private List<TopologyProviderDescriptor> providers; // List of class name implementing topology providers
    @JsonIgnore
    private HashMap <String, TopologyProvider> topologyProviders = new HashMap<String,TopologyProvider>();
    @JsonIgnore
    private static TopologyFactory  instance;
    @JsonIgnore
    private static Object instanceLock = new Object();

    private OsgiBundlesClassLoader loader = null;

    public TopologyFactory() throws IOException {
        // Need to set up a bundle class loader so we can find topology providers in other bundles.
        BundleContext bc = BootStrap.getBootStrap().getBundleContext();
        Bundle[] bundles = bc.getBundles();

        this.loader = new OsgiBundlesClassLoader(bundles, TopologyFactory.class.getClassLoader());
    }

    private void startProviders() {
        // Initialize the topologies
        if (this.providers != null) {
            for (TopologyProviderDescriptor provider : this.providers) {
                this.startProvider(provider);
            }
        }
    }

    private void startProvider(TopologyProviderDescriptor provider) {
        try {
            TopologyProvider topologyProvider =
                    (TopologyProvider) this.loader.findClass(provider.getClassName()).newInstance();

            this.topologyProviders.put(provider.getType(),topologyProvider);

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public TopologyProvider retrieveTopologyProvider (String type) {
        return this.topologyProviders.get(type);
    }

    public DefaultListenableGraph retrieveTopology (String type) throws IOException {
        // Assume graph of the current topology (i.e. as in "right now" and Traffic Engineering metrics
        return this.topologyProviders.get(type).getGraph(TopologyProvider.WeightType.TrafficEngineering);
    }

    public static TopologyFactory instance() {
        synchronized (TopologyFactory.instanceLock) {
            if (TopologyFactory.instance == null) {
                try {
                    TopologyFactory.instance =
                            (TopologyFactory) PersistentObject.newObjectFromFile(Paths.get(FACTORY_DIR, FACTORY_CONFIGFILE).toString());
                    TopologyFactory.instance().startProviders();
                } catch (InstantiationException e) {
                    throw new RuntimeException (e);
                }
            }

        }
        return TopologyFactory.instance;
    }

    public List<TopologyProviderDescriptor> getProviders() {
        return providers;
    }

    public void setProviders(List<TopologyProviderDescriptor> providers) {
        this.providers = providers;
    }

    public synchronized void registerTopologyProvider(String className, String type) throws IOException {
        if (this.providers == null) {
            this.providers = new ArrayList<TopologyProviderDescriptor>();
        }
        // Checks if there is already a provider for this type
        if (this.topologyProviders.containsKey(type)) {
            // Remove existing provider
            TopologyProvider provider = this.topologyProviders.get(type);
            this.topologyProviders.remove(provider);
            ArrayList<TopologyProviderDescriptor> toRemove = new ArrayList<TopologyProviderDescriptor>();
            for (TopologyProviderDescriptor desc : this.providers) {
                if (desc.getType().equals(type)) {
                    // Add it to the list of descriptors to be removed
                    toRemove.add(desc);
                }
            }
            for (TopologyProviderDescriptor desc : toRemove) {
                this.providers.remove(desc);
            }
        }
        TopologyProviderDescriptor provider = new TopologyProviderDescriptor(className,type);
        this.providers.add(provider);
        this.saveToFile(Paths.get(FACTORY_DIR, FACTORY_CONFIGFILE).toString());
        this.startProvider(provider);
    }
}
