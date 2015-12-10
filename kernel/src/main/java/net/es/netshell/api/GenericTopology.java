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

import java.util.List;

/**
 * Created by lomax on 3/6/15.
 */
public class GenericTopology extends Resource {
    List<GenericNode> nodes;
    List<GenericHost> hosts;

    public List<GenericNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<GenericNode> nodes) {
        this.nodes = nodes;
    }

    public List<GenericHost> getHosts() {
        return hosts;
    }

    public void setHosts(List<GenericHost> hosts) {
        this.hosts = hosts;
    }
}
