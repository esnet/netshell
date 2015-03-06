package net.es.netshell.api;

import java.util.List;

/**
 * Created by lomax on 3/6/15.
 */
public class GenericHost extends Host {
    List<Host> hosts;

    public List<Host> getHosts() {
        return hosts;
    }

    public void setHosts(List<Host> hosts) {
        this.hosts = hosts;
    }
}
