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
