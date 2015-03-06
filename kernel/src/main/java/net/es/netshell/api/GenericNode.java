package net.es.netshell.api;

import java.util.List;

/**
 * Created by lomax on 3/6/15.
 */
public class GenericNode extends Node {
    private List<GenericPort> ports;

    public List<GenericPort> getPorts() {
        return ports;
    }

    public void setPorts(List<GenericPort> ports) {
        this.ports = ports;
    }

}
