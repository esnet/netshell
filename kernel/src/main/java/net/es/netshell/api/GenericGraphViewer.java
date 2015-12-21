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

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.swing.mxGraphComponent;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * Created by lomax on 8/7/14.
 */
public class GenericGraphViewer extends JFrame {

    private JGraphXAdapter<Node, Link> jgxAdapter;
    private Graph<Node, Link> graph;
    private Set<Node> vertices;
    private boolean bidirectional = false;
    private boolean fast = true;

    public GenericGraphViewer(Graph<Node, Link> graph) {
        this.graph = graph;
        this.vertices = graph.vertexSet();
    }

    public GenericGraphViewer(Graph<Node, Link> graph, int x, int y, int width, int height) {
        this.graph = graph;
        this.vertices = graph.vertexSet();
        this.setBounds(x,y,width,height);
    }


    public void display() {
        jgxAdapter = new JGraphXAdapter<Node, Link>(graph);
        mxGraphComponent graphComponent = new mxGraphComponent(jgxAdapter);
        getContentPane().add(new JScrollPane(graphComponent));

        if (fast) {
            //define layout
            mxFastOrganicLayout layout = new mxFastOrganicLayout(jgxAdapter);

            //set all properties
            layout.setMinDistanceLimit(200);
            layout.setInitialTemp(200);
            layout.setForceConstant(200);
            layout.setDisableEdgeStyle(true);
            layout.execute(jgxAdapter.getDefaultParent());
        } else {
            new mxHierarchicalLayout(jgxAdapter).execute(jgxAdapter.getDefaultParent());
        }
        if (bidirectional) {
            new mxParallelEdgeLayout(jgxAdapter).execute(jgxAdapter.getDefaultParent());
        }

        this.getContentPane().add(BorderLayout.CENTER, graphComponent);

        this.setVisible(true);
    }

    public void display(int x, int y, int width, int height) {
        this.setBounds(x,y,width,height);
        this.display();
    }

    public boolean isBidirectional() {
        return bidirectional;
    }

    public void setBidirectional(boolean bidrectional) {
        this.bidirectional = bidrectional;
    }

    public boolean isFast() {
        return fast;
    }

    public void setFast(boolean fast) {
        this.fast = fast;
    }
}
