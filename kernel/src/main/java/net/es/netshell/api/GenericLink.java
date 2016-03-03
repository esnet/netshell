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

/**
 * Created by lomax on 3/6/15.
 */
public class GenericLink extends Link {
    private int speed;
    private GenericNode srcNode;
    private GenericPort srcPort, dstPort;
    private GenericNode dstNode;

    public GenericLink() {
        super();
    }

    public GenericLink(GenericNode srcNode, GenericPort srcPort,
                       GenericNode dstNode, GenericPort dstPort) {
        this.srcNode = srcNode;
        this.srcPort = srcPort;
        this.dstNode = dstNode;
        this.dstPort = dstPort;
        String name = srcNode.getResourceName() + ":" + srcPort.getResourceName() + ":" +
                      dstNode.getResourceName() + ":" + dstPort.getResourceName();
        this.setResourceName(name);
        this.srcPort.addLink(this);
        this.dstPort.addLink(this);
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
