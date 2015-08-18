/*
 * ENOS, Copyright (c) 2015, The Regents of the University of California,
 * through Lawrence Berkeley National Laboratory (subject to receipt of any
 * required approvals from the U.S. Dept. of Energy).  All rights reserved.
 *
 * If you have questions about your rights to use or distribute this software,
 * please contact Berkeley Lab's Technology Transfer Department at TTD@lbl.gov.
 *
 * NOTICE.  This software is owned by the U.S. Department of Energy.  As such,
 * the U.S. Government has been granted for itself and others acting on its
 * behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software
 * to reproduce, prepare derivative works, and perform publicly and display
 * publicly.  Beginning five (5) years after the date permission to assert
 * copyright is obtained from the U.S. Department of Energy, and subject to
 * any subsequent five (5) year renewals, the U.S. Government is granted for
 * itself and others acting on its behalf a paid-up, nonexclusive, irrevocable,
 * worldwide license in the Software to reproduce, prepare derivative works,
 * distribute copies to the public, perform publicly and display publicly, and
 * to permit others to do so.
 */

package net.es.netshell.odlmdsal;

import net.es.netshell.controller.OpenFlowNode;
import net.es.netshell.controller.layer2.Layer2Controller;
import net.es.netshell.controller.layer2.Layer2ForwardRule;
import net.es.netshell.controller.layer2.Layer2Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * This class is an interface to the OpenFlow controller functionality in
 * OpenDaylight, using the MD-SAL abstraction layer.
 */
public class Controller implements Layer2Controller {

    // This is a quasi-singleton.  In theory there can be multiple of these objects
    // in a system, but in practice it seems that each one of these is associated
    // with a single instance of the OSGi bundle, which basically just means just
    // one per system.  So we can somewhat safely say there should be at most one
    // instance, and keep a class member variable pointing to that one instance.
    static private volatile Controller instance = null;

    // The constructor needs to save a pointer to this object as "the" instance.
    // If there is more than one object construction attempted, that's bad.
    public Controller() {
        if (instance == null) {
            instance = this;
        }
        else {
            throw new RuntimeException("Attempt to create multiple " + Controller.class.getName());
        }
    }

    public static Controller getInstance() { return instance; }

    // Logging
    static final private Logger logger = LoggerFactory.getLogger(Controller.class);

    // Methods to be invoked from Python or other parts of netshell/ENOS.
    // Get all switches
    public List<NodeRef> getNetworkDevices() {
        List<NodeRef> switches = null;
        return switches;
    }

    // Get all ports (a.k.a. NodeConnectors) on a switch
    public Set<NodeConnectorRef> getNodeConnectors(NodeRef node) {
        Set<NodeConnectorRef> ports = null;
        return ports;
    }

    @Override
    public boolean addForwardRule(Layer2ForwardRule rule) {
        return false;
    }

    @Override
    public boolean removeForwardRule(Layer2ForwardRule rule) {
        return false;
    }


}
