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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lomax on 3/6/15.

 */
public class GenericPort extends Port {
    private ArrayList<GenericLink> links;

    public GenericPort(String name) {
        super(name);
        this.links = new ArrayList<GenericLink>();
    }

    public List<GenericLink> getLinks() {
        return links;
    }

    public void setLinks(List<GenericLink> links) {
        this.links = (ArrayList) links;
    }

    public void addLink(GenericLink link) {
        this.links.add(link);
    }

    public void removeLink(GenericLink link) {
        this.links.remove(link);
    }
}
