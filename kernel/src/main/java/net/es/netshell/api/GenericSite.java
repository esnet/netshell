/**

  ENOS, Copyright (c) 2015, The Regents of the University of California,
  through Lawrence Berkeley National Laboratory (subject to receipt of any
  required approvals from the U.S. Dept. of Energy).  All rights reserved.

  If you have questions about your rights to use or distribute this software,
  please contact Berkeley Lab's Technology Transfer Department at TTD@lbl.gov.

  NOTICE.  This software is owned by the U.S. Department of Energy.  As such,
  the U.S. Government has been granted for itself and others acting on its
  behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software
  to reproduce, prepare derivative works, and perform publicly and display
  publicly.  Beginning five (5) years after the date permission to assert
  copyright is obtained from the U.S. Department of Energy, and subject to
  any subsequent five (5) year renewals, the U.S. Government is granted for
  itself and others acting on its behalf a paid-up, nonexclusive, irrevocable,
  worldwide license in the Software to reproduce, prepare derivative works,
  distribute copies to the public, perform publicly and display publicly, and
  to permit others to do so.
 **/
package net.es.netshell.api;


import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.ArrayList;

public class GenericSite extends Resource {
    private String siteOwner;
    private ArrayList<GenericLink> links;
    private ArrayList<GenericHost> hosts;
    public GenericSite (String name) {
        super (name);
    }

    public GenericSite () {
        super();
    }
    @JsonIgnore
    public synchronized void addHost(GenericHost host) {
        if (this.hosts.contains(host)) {
            // nothing to do
            return;
        } else {
            this.hosts.add(host);
        }
    }

    @JsonIgnore
    public synchronized void removeHost(GenericHost host) {
        if (this.hosts.contains(host)) {
            // nothing to do
            return;
        } else {
            this.hosts.remove(host);
        }
    }

    @JsonIgnore
    public synchronized void addLink(GenericLink link) {
        if (this.links.contains(link)) {
            // nothing to do
            return;
        } else {
            this.links.add(link);
        }
    }

    @JsonIgnore
    public synchronized void removeLink(GenericLink link) {
        if (this.links.contains(link)) {
            // nothing to do
            return;
        } else {
            this.links.remove(link);
        }
    }

    public ArrayList<GenericHost> getHosts() {
        return hosts;
    }

    public void setHosts(ArrayList<GenericHost> hosts) {
        this.hosts = hosts;
    }

    public ArrayList<GenericLink> getLinks() {
        return links;
    }

    public void setLinks(ArrayList<GenericLink> links) {
        this.links = links;
    }

    public String getSiteOwner() {
        return this.siteOwner;
    }

    public void setOwner(String siteOwner) {
        this.siteOwner = siteOwner;
    }
}
