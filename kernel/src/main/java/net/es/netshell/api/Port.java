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

/**
 * Created by lomax on 5/30/14.
 */
public class Port extends Resource {

    public static final String CanOpenFlow1 = "canOpenFlow1";  // OpenFlow 1.0 support
    public static final String CanOpenFlow3 = "canOpenFlow2";  // OpenFlow 1.3 support
    public static final String PORTS_DIR = "ports";

    @JsonIgnore
    private Node node;

    public Port(String name) {
        super(name);
    }

    /**
     * Returns the Node from where the port belongs to
     * @return
     */
    public Node getNode() {
        return node;
    }

    /**
     * Set the Node where the port belongs to
     * @param node
     */
    public void setNode(Node node) {
        this.node = node;
    }

    public Port(Port port) {
        super(port);
        node = port.getNode();
    }

    public Port() {
        super();
    }
}
