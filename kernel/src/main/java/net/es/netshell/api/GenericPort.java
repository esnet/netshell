package net.es.netshell.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lomax on 3/6/15.

 */
public class GenericPort extends Port {
    ArrayList<GenericLink> links;

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
