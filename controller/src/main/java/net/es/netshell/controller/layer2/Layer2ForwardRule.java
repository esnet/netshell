package net.es.netshell.controller.layer2;

import net.es.netshell.controller.ForwardRule;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lomax on 7/6/15.
 */
public class Layer2ForwardRule extends ForwardRule {

    private String inMac,outMac;

    public Layer2ForwardRule (String name, Layer2Port inPort, Layer2Port outPort, byte[] inMac, byte[] outMac) {
        super(name,inPort,outPort);
        this.inMac = Base64.encodeBase64String(inMac);
        this.outMac = Base64.encodeBase64String(outMac);
    }

    public String getInMac() {
        return this.inMac;
    }

    public void setInMac(String mac) {
        this.inMac = mac;
    }

    public String getOutMac() {
        return this.outMac;
    }

    public void setOutMac(String mac) {
        this.outMac = mac;
    }

    @JsonIgnore
    public byte[] inMacToByteArray() {
        return Base64.decodeBase64(this.inMac);
    }

    @JsonIgnore
    public byte[] outMacToByteArray() {
        return Base64.decodeBase64(this.outMac);
    }
}
