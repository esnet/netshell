package net.es.netshell.api;
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

import net.es.netshell.kernel.exec.KernelThread;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ResourceType(
        type=ResourceTypes.TOPOLOGY
)
public class Topology extends Container {


    @JsonIgnore
    public synchronized List<Resource> getNodes() throws IOException, InstantiationException {
        HashMap<String, Object> query = new HashMap<String,Object>();
        /**
         * new Document("$or", asList(new Document("cuisine", "Italian"),
         new Document("address.zipcode", "10075"))));
         */
        ArrayList<HashMap<String,Object>> q = new ArrayList<HashMap<String,Object>>();
        HashMap<String, Object> q1 = new HashMap<String,Object>();
        q1.put("resourceType",ResourceTypes.NODE);
        q.add(q1);
        HashMap<String, Object> q2 = new HashMap<String,Object>();
        q2.put("resourceType", ResourceTypes.HOST);
        q.add(q2);
        query.put("$or", q);
        List<Resource> nodes = this.loadResources(query);
        return nodes;
    }

    @JsonIgnore
    public synchronized List<Resource> getLinks() throws IOException, InstantiationException {
        HashMap<String, Object> query = new HashMap<String,Object>();
        query.put("resourceType",ResourceTypes.LINK);
        List<Resource> links = this.findResources(this,query);
        return links;
    }


    public static Topology createTopology(String name)
            throws IOException, IllegalAccessException, InstantiationException {
        String owner = KernelThread.currentKernelThread().getUser().getName();
        return Topology.createTopology(owner, name);
    }

    public static Topology createTopology(String name,Class topologyClass)
            throws IllegalAccessException, IOException, InstantiationException {
        return Topology.createTopology(
                KernelThread.currentKernelThread().getUser().getName(),
                name,
                topologyClass
                );
    }
    public static Topology createTopology(String owner, String name)
            throws IllegalAccessException, IOException, InstantiationException {
        return Topology.createTopology(owner,name,Topology.class);
    }

    public static Topology createTopology(String owner, String name, Class topologyClass)
            throws IOException, InstantiationException, IllegalAccessException {
        Container container = Container.createContainer(owner, name, topologyClass);
        return (Topology) container;
    }

    public static Topology getTopology(String name) throws InstantiationException {
        return Topology.getTopology(KernelThread.currentKernelThread().getUser().getName(), name);
    }

    public static Topology getTopology(String name, Class topologyClass) throws InstantiationException {
        return Topology.getTopology(KernelThread.currentKernelThread().getUser().getName(), name, topologyClass);
    }

    public static Topology getTopology(String owner, String name) throws InstantiationException {
        return Topology.getTopology(owner, name, Topology.class);
    }

    public static Topology getTopology(String owner, String name, Class topologyClass)
            throws InstantiationException {
        return (Topology) Container.findByName(owner,name,name,topologyClass);
    }

}
