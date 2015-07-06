package net.es.netshell.controller.layer2;

import net.es.netshell.api.Port;

import java.util.UUID;


/**
 * The Layer2 interface must be implemented by any controller that provides Layer2 switch configuration.
 */
public interface Layer2Controller {

    /**
     * Adds a Layer2ForwardRule into the controller.
     * @param rule
     * @return true if successful, otherwase returns false.
     */
    public boolean addForwardRule (Layer2ForwardRule rule);

    /**
     * Removes a Layer2ForwardRule into the controller.
     * @param rule
     * @return true if successful, otherwase returns false.
     */
    public boolean removeForwardRule (Layer2ForwardRule rule);

}
