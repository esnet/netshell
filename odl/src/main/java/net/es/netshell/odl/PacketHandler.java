/*
 * ENOS, Copyright (c) $today.date, The Regents of the University of California, through Lawrence Berkeley National Laboratory (subject to receipt of any required approvals from the U.S. Dept. of Energy).  All rights reserved.
 *
 * If you have questions about your rights to use or distribute this software, please contact Berkeley Lab's Technology Transfer Department at TTD@lbl.gov.
 *
 * NOTICE.  This software is owned by the U.S. Department of Energy.  As such, the U.S. Government has been granted for itself and others acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software to reproduce, prepare derivative works, and perform publicly and display publicly.  Beginning five (5) years after the date permission to assert copyright is obtained from the U.S. Department of Energy, and subject to any subsequent five (5) year renewals, the U.S. Government is granted for itself and others acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software to reproduce, prepare derivative works, distribute copies to the public, perform publicly and display publicly, and to permit others to do so.
 */

package net.es.netshell.odl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.flowprogrammer.Flow;
import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.packet.*;

import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.sal.utils.StatusCode;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.opendaylight.controller.switchmanager.Switch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bmah on 2/25/15.
 */
public class PacketHandler implements IListenDataPacket {

    // This is a quasi-singleton.  In theory there can be multiple of these objects
    // in a system, but in practice it seems that each one of these is associated
    // with a single instance of the OSGi bundle, which basically just means just
    // one per system.  So we can somewhat safely say there should be at most one
    // instance, and keep a class member variable pointing to that one instance.
    static private volatile PacketHandler instance = null;

    // The constructor needs to save a pointer to this object as "the" instance.
    // If there is more than one object construction attempted, that's bad.
    public PacketHandler() {
        if (instance == null) {
            instance = this;
        }
        else {
            throw new RuntimeException("Attempt to create multiple " + PacketHandler.class.getName());
        }
    }

    public static PacketHandler getInstance() { return instance; }

    // Logging
    static final private Logger logger = LoggerFactory.getLogger(PacketHandler.class);

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

    // Callback to us on PACKET_IN.
    @Override
    public PacketResult receiveDataPacket(RawPacket inPkt) {
        logger.info("Received data packet");

        NodeConnector ingressConnector = inPkt.getIncomingNodeConnector();
        Node node = ingressConnector.getNode();

        // Decode the packet
        Packet l2pkt = dataPacketService.decodeDataPacket(inPkt);
        return PacketResult.IGNORED;
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

}
