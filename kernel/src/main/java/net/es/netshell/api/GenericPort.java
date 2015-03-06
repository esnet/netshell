package net.es.netshell.api;

import java.util.List;

/**
 * Created by lomax on 3/6/15.

 */
public class GenericPort extends Port {
    List<GenericLink> links;

    public List<GenericLink> getLinks() {
        return links;
    }

    public void setLinks(List<GenericLink> links) {
        this.links = links;
    }
}
