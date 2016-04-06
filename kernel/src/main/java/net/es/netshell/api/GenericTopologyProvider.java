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

import org.jgrapht.graph.DefaultListenableGraph;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lomax on 3/6/15.
 */
public class GenericTopologyProvider extends TopologyProvider {


    public final Logger logger = LoggerFactory.getLogger(GenericTopologyProvider.class);

    private HashMap<String, Node> nodes = new HashMap<String, Node>();
    private HashMap<String,Node> nodeByLink = new HashMap<String, Node>();
    private HashMap<String,Port> portByLink = new HashMap<String, Port>();
    private HashMap<String, List<Link>> internalLinks = new HashMap<String, List<Link>>();
    private HashMap<String, List<Link>> siteLinks = new HashMap<String, List<Link>>();
    private HashMap<String, List<Link>> domainLinks = new HashMap<String, List<Link>>();
    private HashMap<String, Link> links = new HashMap<String, Link>();
    private HashMap<String, Node> nodeByPort = new HashMap<String, Node>();


    /**
     * Instantiate a GenericTopologyProvider from a Container.
     * @param topologyContainer
     */
    public GenericTopologyProvider(Container topologyContainer) {

    }

    public GenericTopologyProvider() {

    }
    /**
     * Returns a HashMap of List of Links that connects ESnet internal node to each other. The map is indexed by
     * the name of the node as found in the topology.
     * @return returns the indexed Map.
     */
    public HashMap<String, List<Link>> getInternalLinks() { return internalLinks; }

    public HashMap<String, Link> getLinks() {
        return links;
    }

    public HashMap<String, List<Link>> getDomainLinks() {
        return domainLinks;
    }

    public HashMap<String, List<Link>> getSiteLinks() {
        return siteLinks;
    }

    public void addNode (Node node) {
        this.nodes.put(node.getResourceName(), node);
    }

    public void addPort (GenericNode node, GenericPort port) {
        // Create port name
        node.getPorts().add(port);
        this.nodeByPort.put(port.getResourceName(),node);
    }

    public void addLink (GenericLink link) {
        GenericNode srcNode = link.getSrcNode();
        GenericNode dstNode = link.getDstNode();

        this.links.put(link.getResourceName(), link);
        this.nodeByLink.put(link.getResourceName(), srcNode);
        this.portByLink.put(link.getResourceName(),link.getSrcPort());
    }

    /**
     * This method returns a Listenable, Directed and Weighted graph of ESnet topology. While ESnet 5 links are
     * to be assumed to be bidirectional, the generic API does not. Therefore, each links are in fact two identical
     * links, but in reverse directions. The weight is directly taken off the traffic engineering metrics as
     * stated in the topology wire format.
     *
     * @return
     */
    @Override
    public DefaultListenableGraph<Node, Link> getGraph (DateTime start,
                                                        DateTime end,
                                                        WeightType weightType) throws IOException {

        GenericGraph graph = new GenericGraph();

        // Add vertices
        for (Node node : this.nodes.values()) {
            graph.addVertex(node);
        }
        // Add links
        for (Link l : this.links.values()) {
            GenericLink link = (GenericLink) l;
            graph.addEdge(link.getSrcNode(),link.getDstNode(), link);
        }

        return graph;
    }

    @Override
    public Node getNode(String name) {
        if (name == null) {
            return null;
        }
        // Construct the URN
        String[] tmp = name.split("@");
        if (tmp.length != 2) {
            // Incorrect format.
        }
        String hostname = tmp[0];
        String domain = tmp[1];
        String urn = "urn:ogf:network:" + domain + ":" + hostname;

        return this.nodes.get(urn);
    }


    @Override
    public HashMap<String, Node> getNodes() {
        return nodes;
    }

    public HashMap<String, Node> getNodeByLink() {
        return nodeByLink;
    }

    public HashMap<String, Port> getPortByLink() {
        return portByLink;
    }

    public HashMap<String, Node> getNodeByPort() {
        return nodeByPort;
    }

    public GenericGraphViewer getGraphViewer(WeightType weightType) throws IOException {
        GenericGraphViewer viewer = new GenericGraphViewer(this.getGraph(weightType));
        return viewer;
    }
}
