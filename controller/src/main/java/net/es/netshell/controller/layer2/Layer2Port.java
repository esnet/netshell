package net.es.netshell.controller.layer2;

import net.es.netshell.api.Node;
import net.es.netshell.api.Port;

/**
 * Created by lomax on 7/6/15.
 */
public class Layer2Port extends Port {
    private int vlan;
    private Port port;

    public Layer2Port (String name, Port port, int vlan){
        super(name);
        this.vlan = vlan;
        this.port = port;
        this.setNode(this.port.getNode());
        this.setDescription(this.port.getDescription() + ":vlan-" + this.vlan);
    }

    public int getVlan() {
        return vlan;
    }

    public void setVlan(int vlan) {
        this.vlan = vlan;
    }

    public Port getPort() {
        return port;
    }

    public void setPort(Port port) {
        this.port = port;
    }

    @Override
    public Node getNode() {
        return super.getNode();
    }

    @Override
    public String getResourceName() {
        return super.getResourceName();
    }

    @Override
    public String getDescription() {
        return super.getDescription();
    }

    @Override
    public String getCreationStackTrace() {
        return super.getCreationStackTrace();
    }
}
