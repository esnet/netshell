package net.es.netshell.controller;

import net.es.netshell.api.Port;
import net.es.netshell.api.Resource;

import java.io.IOException;

/**
 * Created by lomax on 7/6/15.
 */
public class ForwardRule extends Rule {
    private Port inPort,outPort;

    public ForwardRule(String name, Port inPort, Port outPort) {
        super (name);
        this.inPort = inPort;
        this.outPort = outPort;
    }

    public Port getInPort() {
        return inPort;
    }

    public void setInPort(Port inPort) {
        this.inPort = inPort;
    }

    public Port getOutPort() {
        return outPort;
    }

    public void setOutPort(Port outPort) {
        this.outPort = outPort;
    }
}
