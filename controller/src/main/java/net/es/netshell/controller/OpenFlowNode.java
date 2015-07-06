package net.es.netshell.controller;

import net.es.netshell.api.Node;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Created by lomax on 7/6/15.
 */
public class OpenFlowNode extends Node {
    private String dpid;


    public OpenFlowNode (String name, byte[] dpid) {
        super(name);
        this.dpid = Base64.encodeBase64String(dpid);
    }

    public String getDpid() {
        return dpid;
    }

    public void setDpid(String dpid) {
        this.dpid = dpid;
    }

    @JsonIgnore
    public byte[] dpidToByteArray() {
        return Base64.decodeBase64(this.dpid);
    }

}
