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

import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.packet.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bmah on 2/25/15.
 */
public class PacketHandler implements IListenDataPacket {

    // Logging
    static final private Logger logger = LoggerFactory.getLogger(PacketHandler.class);

    private IDataPacketService dataPacketService;

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

    @Override
    public PacketResult receiveDataPacket(RawPacket inPkt) {
        logger.info("Received data packet");

        NodeConnector ingressConnector = inPkt.getIncomingNodeConnector();
        Node node = ingressConnector.getNode();

        // Decode the packet
        Packet l2pkt = dataPacketService.decodeDataPacket(inPkt);
        return PacketResult.IGNORED;
    }

}
