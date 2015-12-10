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
import org.jgrapht.Graph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.ConnectivityInspector;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * This class is a Resource representation of a topology graph.
 */
public class GraphResource extends Resource {
    private String className;
    private List<NodeDesc> nodeDescs;
    private List<LinkDesc> linkDescs;
    @JsonIgnore
    private Graph<Node,Link> cachedGraph = null;

    static public class NodeDesc {
        private String id;
        private Node node;

        @JsonIgnore
        public NodeDesc(String id, Node node) {
            this.id = id;
            this.node = node;
        }

        public NodeDesc() { }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Node getNode() {
            return node;
        }

        public void setNode(Node node) {
            this.node = node;
        }
    }

    static public class LinkDesc {
        private String srcNodeId;
        private String dstNodeId;
        private Link link;


        public LinkDesc() { }

        @JsonIgnore
        public LinkDesc(Link link, String srcId, String dstId) {
            this.link = link;
            this.srcNodeId = srcId;
            this.dstNodeId = dstId;
        }

        public String getSrcNodeId() {
            return srcNodeId;
        }

        public void setSrcNodeId(String srcNodeId) {
            this.srcNodeId = srcNodeId;
        }

        public String getDstNodeId() {
            return dstNodeId;
        }

        public void setDstNodeId(String dstNodeId) {
            this.dstNodeId = dstNodeId;
        }

        public Link getLink() {
            return link;
        }

        public void setLink(Link link) {
            this.link = link;
        }

    }


    public List<NodeDesc> getNodeDescs() {
        return nodeDescs;
    }

    public void setNodeDescs(List<NodeDesc> nodeDescs) {
        this.nodeDescs = nodeDescs;
    }

    public List<LinkDesc> getLinkDescs() {
        return linkDescs;
    }

    public void setLinkDescs(List<LinkDesc> linkDescs) {
        this.linkDescs = linkDescs;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public GraphResource() {

    }

    /**
     * Creates a GraphSecuredResource cloning the provided Graph. Each Node and
     * Links are cloned, abstracting the original graph.
     * @param graph
     */
    public GraphResource(Graph graph) {

        this.cachedGraph = graph;

        Set<Node> vertices = graph.vertexSet();
        this.nodeDescs = new ArrayList<NodeDesc>();
        this.linkDescs = new ArrayList<LinkDesc>();
        this.className = graph.getClass().getCanonicalName();
        // Index the nodes
        HashMap<String,NodeDesc> map = new HashMap<String,NodeDesc>();
        int id = 0;
        for (Node node : vertices) {
            Node n = new Node(node);
            NodeDesc desc = new NodeDesc(Integer.toString(++id),n);
            map.put(n.getResourceName(),desc);
            this.nodeDescs.add(desc);
        }
        Set<Link> links = graph.edgeSet();
        for (Link link : links) {
            String srcId = map.get(((Node) graph.getEdgeSource(link)).getResourceName()).getId();
            String dstId = map.get(((Node) graph.getEdgeTarget(link)).getResourceName()).getId();
            double weight = graph.getEdgeWeight(link);
            LinkDesc desc = new LinkDesc(new Link(link),srcId,dstId);
            linkDescs.add(desc);
        }
    }


    /**
     * Creates a Graph from GraphSecuredResource.
     * @return
     * @throws InstantiationException
     * @throws ClassNotFoundException
     * @throws java.lang.reflect.InvocationTargetException
     * @throws IllegalAccessException
     */
    public Graph<Node,Link> toGraph() throws InstantiationException,
            ClassNotFoundException,
            InvocationTargetException,
            IllegalAccessException {

        if (this.cachedGraph != null) {
            return this.cachedGraph;
        }

        GenericGraph graph = new GenericGraph();

        // create a map of the Nodes and add them to the graph
        HashMap<String, Node> map = new HashMap<String,Node>();
        for (NodeDesc nodeDesc : this.nodeDescs) {
            map.put(nodeDesc.getId(),nodeDesc.getNode());
            graph.addVertex(nodeDesc.getNode());
        }
        // Add links
        for (LinkDesc linkDesc : this.linkDescs) {
            Link link = linkDesc.getLink();
            Node srcNode = map.get(linkDesc.getSrcNodeId());
            Node dstNode = map.get(linkDesc.getDstNodeId());
            graph.addEdge(srcNode,dstNode,link);
            if (graph instanceof WeightedGraph) {
                ((WeightedGraph) graph).setEdgeWeight(link,link.getWeight());
            }
        }
        this.cachedGraph = graph;
        return graph;
    }

    /**
     * Creates a GraphSecuredResource made of a full mesh topology
     * between the provided list of Node
     * @param nodes
     * @return
     */
    static public GraphResource getFullMesh(List<Node> nodes) {

        GraphResource topoGraph = new GraphResource();
        topoGraph.nodeDescs = new ArrayList<NodeDesc>();
        topoGraph.linkDescs = new ArrayList<LinkDesc>();

        for (Node node : nodes) {
            NodeDesc nodeDesc = new NodeDesc();
            nodeDesc.setNode(node);
            nodeDesc.setId(node.getResourceName());
            topoGraph.nodeDescs.add(nodeDesc);
        }
        // Create full mesh (Directed)
        for (Node srcNode : nodes) {
            for(Node dstNode : nodes) {
                if (srcNode.equals(dstNode)) {
                    // Already added
                    continue;
                }
                LinkDesc linkDesc = new LinkDesc();
                Link link = new Link();
                link.setWeight(1);
                linkDesc.setLink(link);
                linkDesc.setSrcNodeId(srcNode.getResourceName());
                linkDesc.setDstNodeId(dstNode.getResourceName());
                topoGraph.linkDescs.add(linkDesc);
            }
        }
        return topoGraph;
    }

    /**
     * Checks if a Graph<Node,Link> is a super set of this graph.
     * A graph G1<Node,Link> is a super set of graph G2<Node,Link> if each of the links of G2 are
     * included in G1.
     * @param targetGraph the superset graph
     * @return true if targetGraph is a superset of this graph.
     */
    public boolean isSubGraphOf (Graph<Node,Link> targetGraph) {
        Graph<Node,Link> thisGraph;
        try {
            thisGraph = this.toGraph();

        } catch (InstantiationException e) {
            return false;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (InvocationTargetException e) {
            return false;
        } catch (IllegalAccessException e) {
            return false;
        }

        for (Link link : thisGraph.edgeSet()) {
            if ( ! targetGraph.containsEdge(link)) {
                // This link does not exist in targetGraph. targetGraph is not a super set of this graph
                return false;
            }
        }
        // All links of this graph are contained in targetGraph. It is a super set of this graph
        return true;
    }
    /**
     * Checks if a Graph<Node,Link> is a super set of this graph.
     * A graph G1<Node,Link> is a super set of graph G2<Node,Link> if each of the links of G2 are
     * included in G1. This method invokes isSubGraphOf (Graph<Node,Link> targetGraph) and is provided
     * for convenience.
     * @param targetGraph the superset graph
     * @return true if targetGraph is a superset of this graph.
     */
    public boolean isSubGraphOf (GraphResource targetGraph) {
        try {
            return this.isSubGraphOf(targetGraph.toGraph());
        } catch (Exception e) {
            return false;
        }
    }

    @JsonIgnore
    public boolean pathExists(Node srcNode, Node dstNode) {
        ConnectivityInspector<Node,Link> inspector =
                new ConnectivityInspector<Node,Link>((DirectedGraph) this.cachedGraph);
        return inspector.pathExists(srcNode,dstNode);
    }
}

