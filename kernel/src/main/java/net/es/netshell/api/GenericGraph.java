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
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.DirectedMultigraph;

/**
 * GenericGraph is the class that is used to clone any Graph<Node,Link>. It is a
 * DirectedMultigraph. It also supports get/setEdgeWeight so it can be used with Graph functions
 * that requires a weighted graph.
 */
public class GenericGraph extends DefaultListenableGraph<Node, Link> implements DirectedGraph<Node, Link> {
    public GenericGraph(Class<? extends Link> edgeClass) {
        super(new DirectedMultigraph<Node, Link>(edgeClass));
    }

    public GenericGraph() {
        super(new DirectedMultigraph<Node, Link>(Link.class));
    }

    @JsonIgnore
    @Override
    public double getEdgeWeight(Link link) {
        return link.getWeight();
    }

    @JsonIgnore
    @Override
    public void setEdgeWeight(Link link, double weight) {
        link.setWeight(weight);
    }

    @JsonIgnore
    public GenericNode getGenericNode(String name) {
        for (Object obj : this.vertexSet().toArray()) {
            GenericNode node = (GenericNode) obj;
            if (node.getResourceName().equals(name)) {
                return (GenericNode) node;
            }
        }
        return null;
    }
}

