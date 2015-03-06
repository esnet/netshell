package net.es.netshell.api;

/**
 * Created by lomax on 3/6/15.
 */
public class GenericLink extends Link {
    private int speed;
    private GenericNode srcNode;
    private GenericPort srcPort, dstPort;
    private GenericNode dstNode;

    public GenericLink(GenericNode srcNode, GenericPort srcPort,
                       GenericNode dstNode, GenericPort dstPort) {
        this.srcNode = srcNode;
        this.srcPort = srcPort;
        this.dstNode = dstNode;
        this.dstPort = dstPort;
        String name = srcNode.getResourceName() + ":" + srcPort.getResourceName() + ":" +
                      dstNode.getResourceName() + ":" + dstPort.getResourceName();
        this.setResourceName(name);
    }

    public GenericNode getSrcNode() {
        return srcNode;
    }

    public void setSrcNode(GenericNode srcNode) {
        this.srcNode = srcNode;
    }

    public GenericNode getDstNode() {
        return dstNode;
    }

    public GenericPort getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(GenericPort srcPort) {
        this.srcPort = srcPort;
    }

    public GenericPort getDstPort() {
        return dstPort;
    }

    public void setDstPort(GenericPort dstPort) {
        this.dstPort = dstPort;
    }

    public void setDstNode(GenericNode dstNode) {
        this.dstNode = dstNode;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
