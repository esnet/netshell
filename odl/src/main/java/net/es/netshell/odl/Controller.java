/*
 * ENOS, Copyright (c) $today.date, The Regents of the University of California, through Lawrence Berkeley National Laboratory (subject to receipt of any required approvals from the U.S. Dept. of Energy).  All rights reserved.
 *
 * If you have questions about your rights to use or distribute this software, please contact Berkeley Lab's Technology Transfer Department at TTD@lbl.gov.
 *
 * NOTICE.  This software is owned by the U.S. Department of Energy.  As such, the U.S. Government has been granted for itself and others acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software to reproduce, prepare derivative works, and perform publicly and display publicly.  Beginning five (5) years after the date permission to assert copyright is obtained from the U.S. Department of Energy, and subject to any subsequent five (5) year renewals, the U.S. Government is granted for itself and others acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software to reproduce, prepare derivative works, distribute copies to the public, perform publicly and display publicly, and to permit others to do so.
 */

package net.es.netshell.odl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.es.netshell.controller.OpenFlowNode;
import net.es.netshell.controller.layer2.Layer2Controller;
import net.es.netshell.controller.layer2.Layer2ForwardRule;
import net.es.netshell.controller.layer2.Layer2Port;
import org.apache.commons.codec.binary.Base64;
import org.opendaylight.controller.sal.action.Action;
import org.opendaylight.controller.sal.action.Output;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.flowprogrammer.Flow;
import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.match.MatchType;
import org.opendaylight.controller.sal.packet.*;
import org.opendaylight.controller.sal.match.Match;
import org.opendaylight.controller.sal.action.SetDlDst;
import org.opendaylight.controller.sal.action.SetDlSrc;
import org.opendaylight.controller.sal.action.SetVlanId;

import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.sal.utils.StatusCode;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.opendaylight.controller.switchmanager.Switch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is an interface to the OpenFlow controller functionality in
 * OpenDaylight.
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

    // Methods and callbacks for other ODL services to tell us that they exist and how to call them.
    // These were registered by Activator::configureInstance().
    private IDataPacketService dataPacketService;
    private IFlowProgrammerService flowProgrammerService;
    private ISwitchManager switchManager;

    void setDataPacketService(IDataPacketService s) {
        logger.info("Set DataPacketService");
        dataPacketService = s;
    }

    void unsetDataPacketService(IDataPacketService s) {
        logger.info("Unset DataPacketService");
        if (dataPacketService == s) {
            dataPacketService = null;
        }
    }

    void setFlowProgrammerService(IFlowProgrammerService s) {
        logger.info("Set FlowProgrammerService");
        flowProgrammerService = s;
    }

    void unsetFlowProgrammerService(IFlowProgrammerService s) {
        logger.info("Unset FlowProgrammerService");
        if (flowProgrammerService == s) {
            flowProgrammerService = null;
        }
    }

    void setSwitchManager(ISwitchManager s) {
        logger.info("Set SwitchManager");
        switchManager = s;
    }

    void unsetSwitchManager(ISwitchManager s) {
        logger.info("Unset SwitchManager");
        if (switchManager == s) {
            switchManager = null;
        }
    }

    // Methods to be invoked from Python or other parts of netshell/ENOS.
    // Get all switches
    public List<Switch> getNetworkDevices() {
        List<Switch> switches = null;
        if (switchManager != null) {
            switches = switchManager.getNetworkDevices();
        }
        return switches;
    }

    // Get all ports (a.k.a. NodeConnectors) on a switch
    public Set<NodeConnector> getNodeConnectors(Node node) {
        Set<NodeConnector> ports = null;
        if (switchManager != null) {
            ports = switchManager.getNodeConnectors(node);
        }
        return ports;
    }

    // Get a specific port (a.k.a. NodeConnector) on a switch given its name
    // The node name is, at least in mininet-land, of the form "s1-eth1".
    // There does not appear to be a way to get this information from the
    // NodeConnector.
    public NodeConnector getNodeConnector(Node node, String nodeConnectorName) {
        NodeConnector nc = null;
        if (switchManager != null) {
            nc = switchManager.getNodeConnector(node, nodeConnectorName);
        }
        return nc;
    }

    // Push a flow
    public Status addFlow(Node node, Flow flow) {
        Status stat = new Status(StatusCode.NOSERVICE);
        if (flowProgrammerService != null) {
            stat = flowProgrammerService.addFlow(node, flow);
        }
        return stat;
    }

    // Modify a flow
    public Status modifyFlow(Node node, Flow oldFlow, Flow newFlow) {
        Status stat = new Status(StatusCode.NOSERVICE);
        if (flowProgrammerService != null) {
            stat = flowProgrammerService.modifyFlow(node, oldFlow, newFlow);
        }
        return stat;
    }

    // Remove a flow
    public Status removeFlow(Node node, Flow flow) {
        Status stat = new Status(StatusCode.NOSERVICE);
        if (flowProgrammerService != null) {
            stat = flowProgrammerService.removeFlow(node, flow);
        }
        return stat;
    }

    // Remove all flows on one node
    public Status removeAllFlows(Node node) {
        Status stat = new Status(StatusCode.NOSERVICE);
        if (flowProgrammerService != null) {
            stat = flowProgrammerService.removeAllFlows(node);
        }
        return stat;
    }

    private Flow makeFlow(Layer2ForwardRule rule) {
        Flow flow = null;
        Match match = new Match();
        Layer2Port inPort, outPort;
        inPort = (Layer2Port) rule.getInPort();
        outPort = (Layer2Port) rule.getOutPort();

        match.setField(MatchType.IN_PORT, inPort.getResourceName());
        match.setField(MatchType.DL_VLAN,new Short((short) inPort.getVlan()));
        match.setField(MatchType.DL_DST, rule.inMacToByteArray());

        ArrayList<Action> actions = new ArrayList<Action>();
        SetVlanId vlanAction = new SetVlanId(new Short((short) outPort.getVlan()));
        actions.add(vlanAction);
        SetDlDst dlDstAction = new SetDlDst(rule.outMacToByteArray());
        actions.add(dlDstAction);
        Node odlNode = this.findODLSwitch((OpenFlowNode) outPort.getNode()).getNode();
        Output outputAction = new Output(this.getNodeConnector(odlNode,outPort.getResourceName()));
        actions.add(outputAction);

        return flow;
    }

    private Switch findODLSwitch(OpenFlowNode enosSwitch) {
        List<Switch> switches = this.getNetworkDevices();
        Switch sw = null;

        // TODO: ODL may still use 6 bytes DPID. To verified.
        String dpid = enosSwitch.getDpid();
        for (Switch s : switches) {
            if (Base64.encodeBase64String(s.getDataLayerAddress()).equals(dpid)) {
                sw = s;
                break;
            }
        }
        return sw;
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
