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


import org.joda.time.DateTime;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.jgrapht.graph.DefaultListenableGraph;

/**
 * Created by lomax on 5/21/14.
 */
public abstract class TopologyProvider {

    public static enum WeightType {
                                   TrafficEngineering, // Path cost as defined by ESnet NESG
                                   MaxBandwidth,       // Maximum reservable bandwidth by OSCARS
    }

    /**
     * Convenience method computing the path start now and ending one minute later, meaning, right now.
     * @param weight
     * @return
     * @throws IOException
     */
    public DefaultListenableGraph<Node, Link> getGraph(WeightType weight) throws IOException {


        DateTime start = DateTime.now();
        DateTime end = start.plusMinutes(1);
        return this.getGraph(start, end,weight);
    }

    public DefaultListenableGraph<Node, Link> getGraph(DateTime start,
                                                       DateTime end,
                                                       WeightType weightType) throws IOException {
        return null;
    }

    public HashMap<String, Node> getNodes() {
        return null;
    }

    /**
     * Returns a HashMap of List of Links that connects two Nodes of this topology. The map is indexed by
     * the name of the node as found in the topology.
     *
     * @return returns the indexed Map.
     */
    public HashMap<String, List<Link>> getInternalLinks() {
        return null;
    }

    /**
     * Returns the Node indexed by Link. When links are directional, the source Node is indexed.
     *
     * @return a Node or null if not found
     * @param linkId
     */
    @JsonIgnore
    public Node getNodeByLink(String linkId) {
        return null;
    }

    /**
     * Returns the Ports indexed by Link. When links are directional, the source Port is indexed.
     *
     * @return a port or null if not found.
     * @param linkId
     */
    @JsonIgnore
    public Port getPortByLink(String linkId) {
        return null;
    }

    /**
     * Retrieve from the topology the Node object referenced by its name. The format of the name is as follow:
     * host@domain. For instance, lbl-mr2@es.net. Note that the implementation of the topology may have a
     * different format to identify the nodes: the Node id, as retrieved with Node.getId() is an opaque.
     *
     * @param name is the abstract name of the node, formatted as hostname@domain.
     * @return the node object if any, of the node identified by name.
     */
    public Node getNode(String name) {
        return null;
    }
}
