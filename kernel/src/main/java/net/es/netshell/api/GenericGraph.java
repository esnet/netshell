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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.DirectedMultigraph;

import java.io.IOException;
import java.util.HashMap;

/**
 * GenericGraph is the class that is used to clone any Graph<Node,Link>. It is a
 * DirectedMultigraph. It also supports get/setEdgeWeight so it can be used with Graph functions
 * that requires a weighted graph.
 */
public class GenericGraph extends DefaultListenableGraph<Node, Link> implements DirectedGraph<Node, Link> {

    @JsonIgnore
    private HashMap<String,Node> nodeIndex = new HashMap<String,Node>();
    @JsonIgnore
    private HashMap<String,Link> linkIndex = new HashMap<String,Link>();

    public GenericGraph(Class<? extends Link> edgeClass) {
        super(new DefaultDirectedWeightedGraph<Node, Link>(edgeClass));
    }

    public GenericGraph() {
        super(new DefaultDirectedWeightedGraph<Node, Link>(Link.class));
    }

    public GenericGraph(Topology topology) throws IOException, InstantiationException {
        super(new DefaultDirectedWeightedGraph<Node, Link>(Link.class));
        // Add vertices
        for (Resource n : topology.getNodes()) {
            if (n instanceof Node) {
                Node node = (Node) n;
                this.nodeIndex.put(node.getResourceName(), node);
                this.addVertex(node);
            }
        }
        // Add links
        for (Resource l : topology.getLinks()) {
            if (l instanceof Link) {
                Link link = (Link) l;
                this.linkIndex.put(link.getResourceName(), link);
                String srcNodeName = Link.nameToSrcNode(link.getResourceName());
                String dstNodeName = Link.nameToSrcNode(link.getResourceName());
                Node srcNode = this.nodeIndex.get(srcNodeName);
                Node dstNode = this.nodeIndex.get(dstNodeName);
                this.addEdge(srcNode,dstNode,link);
            }
        }

    }

    @Override
    public double getEdgeWeight(Link link) {
        return link.getWeight();
    }

    @Override
    public void setEdgeWeight(Link link, double weight) {
        link.setWeight(weight);
    }
}

