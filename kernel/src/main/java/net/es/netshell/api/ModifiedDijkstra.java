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

/**
 * Created by davidhua on 6/12/14.
 */

import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.GraphPathImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.lang.Math;

/**
 * This class implements a modified Dijkstra algorithm that allows us to calculate the max bandwidth
 * possible from one node to another node (instead of calculating the shortest path).
 */

public class ModifiedDijkstra<Node, Link> {
	DefaultListenableGraph graph;
	ArrayList<Link> path;
	HashMap<Node, Double> width;
	HashMap<Node, Node> prev;
	Node source;
	Node dest;

	public ModifiedDijkstra(DefaultListenableGraph graph, Node source, Node dest) {
		width = new HashMap<>();
		prev = new HashMap<>();
		this.source = source;
		this.dest = dest;
		findPath(graph, source, dest);
		this.path = bandwidth(source, dest);
	}

	public void findPath(DefaultListenableGraph graph, Node source, Node dest) {
		List<Node> arrayQueue = new ArrayList ();

		this.graph = graph;
		Set<Node> vertices = this.graph.vertexSet();

		// Initialize weights of all vertices to neg inf, prev pointers to null, and the visited hashmap to false.
		// Create queue that will hold all vertices
		for (Node v : vertices) {
			width.put(v, Double.NEGATIVE_INFINITY);
			prev.put(v, null);
		}
		// Initialize source vertex with weight of pos inf (bandwidth from and to same place)
		width.put(source, Double.POSITIVE_INFINITY);

		Set<Link> neighborLink = graph.outgoingEdgesOf(source);
		ArrayList<Node> neighbors = new ArrayList();
		for (Link edge : neighborLink) {
			neighbors.add((Node)graph.getEdgeTarget(edge));
		}

		for (Node neighbor : neighbors) {
			Link link = (Link) graph.getEdge(source, neighbor);
			width.put(neighbor, graph.getEdgeWeight(link));
			prev.put(neighbor, source);
			arrayQueue.add(neighbor);
		}

		while (width.get(dest) == Double.NEGATIVE_INFINITY || arrayQueue.contains(dest)) {
			// Find the node with the maximum width, and remove it
			Node maxNode = null;
			Double maxValue = 0d;
			for (Node node : arrayQueue) {
				if (width.get(node) > maxValue) {
					maxNode = node;
					maxValue = width.get(node);
				}
			}
			arrayQueue.remove(maxNode);

			// Find all neighbors of the max node.
			neighborLink = graph.outgoingEdgesOf(maxNode);
			neighbors = new ArrayList();
			for (Link edge : neighborLink) {
				neighbors.add((Node)graph.getEdgeTarget(edge));
			}

			// Iterate through all the neighbors, checking to see if its width field
			// needs to be changed (similar to normal Dijkstra algorithm)
			for (Node neighbor : neighbors) {
				Link link = (Link) graph.getEdge(maxNode, neighbor);
				Double temp = Math.min(width.get(maxNode), graph.getEdgeWeight(link));

				if (width.get(neighbor) == Double.NEGATIVE_INFINITY) {
					prev.put(neighbor, maxNode);
					width.put(neighbor, temp);
					arrayQueue.add(neighbor);
				} else if (arrayQueue.contains(neighbor) && width.get(neighbor) < temp ) {
					prev.put(neighbor, maxNode);
					width.put(neighbor, temp);
				}
			}
		}
	}

	// As modified Dijkstra outputs an array of nodes to the destination,
	// we need to convert this to an array of links for the actual output.
	private ArrayList<Link> bandwidth(Node source, Node dest) {
		ArrayList<Node> finalNode = new ArrayList<Node>();
		Node current = dest;
		finalNode.add(dest);
		while (current != source && current != null) {
			current = prev.get(current);
			if (current != null) {
				finalNode.add(current);
			}
		}
		finalNode.add(source);
		Collections.reverse(finalNode);

		ArrayList<Link> finalPath = new ArrayList<Link>();
		for (int i = 1; i < finalNode.size()-1; i++ ) {
			finalPath.add((Link) graph.getEdge(finalNode.get(i), finalNode.get(i+1)));
		}
		return finalPath;
	}

	public ArrayList<Link> getBandwidth() {
		return this.path;
	}

	public GraphPath<Node, Link> getPath() {
		return new GraphPathImpl<Node, Link>(graph, source, dest, path, 0);
	}
}
