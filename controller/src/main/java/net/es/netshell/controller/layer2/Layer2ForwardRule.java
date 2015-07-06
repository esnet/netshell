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

    private String mac;

    public Layer2ForwardRule (String name, Layer2Port inPort, Layer2Port outPort, byte[] mac) {
        super(name,inPort,outPort);
        this.mac = Base64.encodeBase64String(mac);
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @JsonIgnore
    public byte[] macToByteArray() {
        return Base64.decodeBase64(this.mac);
    }
}
