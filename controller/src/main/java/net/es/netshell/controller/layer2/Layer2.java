package net.es.netshell.controller.layer2;

import net.es.netshell.api.Port;

import java.util.UUID;


/**
 * The Layer2 interface must be implemented by any controller that provides Layer2 switch configuration.
 */
public interface Layer2 {

    public Layer2ForwardRule newForwardRule (Port fromPort, int fromVLAN, Port destPort, int destVLAN,byte[] mac);

}
