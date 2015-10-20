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
package net.es.netshell.controller.core;

import net.es.netshell.odlcorsa.impl.OdlCorsaImpl;
import net.es.netshell.odlmdsal.impl.OdlMdsalImpl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by bmah on 10/14/15.
 */
public class Controller {

    // This is a quasi-singleton.  In theory there can be multiple of these objects
    // in a system, but in practice it seems that each one of these is associated
    // with a single instance of the OSGi bundle, which basically just means just
    // one per system.  So we can somewhat safely say there should be at most one
    // instance, and keep a class member variable pointing to that one instance.
    static private volatile Controller instance = null;

    private OdlMdsalImpl omi;
    private OdlCorsaImpl oci;

    public OdlMdsalImpl getOdlMdsalImpl() {
        return omi;
    }

    public OdlCorsaImpl getOdlCorsaImpl() {
        return oci;
    }

    public class L2Translation {
        byte [] dpid;

        int priority;
        BigInteger c;

        String inPort;
        public short vlan1;
        public MacAddress dstMac1;

        String outPort;
        public short vlan2;
        MacAddress dstMac2;

        short pcp;
        short queue;
        long meter;

        FlowRef flowRef;
    }

    public Controller(OdlMdsalImpl omi, OdlCorsaImpl oci) {
        if (instance == null) {
            instance = this;
        }
        else {
            throw new RuntimeException("Attempt to create multiple " + Controller.class.getName());
        }

        this.omi = omi;
        this.oci = oci;
    }

    public static Controller getInstance() { return instance; }

    // Logging
    static final private Logger logger = LoggerFactory.getLogger(Controller.class);

    // Glue to get some stuff from MD-SAL without the caller needing to have a handle
    // to the MD-SAL implementation
    public List<Node> getNetworkDevices() {
        return this.omi.getNetworkDevices();
    }

    static public NodeConnector getNodeConnector(Node node, String nodeConnectorName) {
        return OdlMdsalImpl.getNodeConnector(node, nodeConnectorName);
    }

    public FlowRef installL2ForwardingRule(L2Translation translation) {
        Node node;
        NodeConnector inNc;
        NodeConnector outNc;

        FlowRef fr = null;

        node = omi.getNetworkDeviceByDpidArray(translation.dpid);
        if (node == null) {
            return null;
        }
        inNc = OdlMdsalImpl.getNodeConnector(node, translation.inPort);
        outNc = OdlMdsalImpl.getNodeConnector(node, translation.outPort);
        if (inNc == null || outNc == null) {
            return null;
        }

        try {
            if (isCorsa(translation.dpid)) {
                fr = oci.createTransitVlanMacCircuit(node, translation.priority, translation.c,
                        translation.dstMac1, inNc.getId(), translation.vlan1,
                        translation.dstMac2, outNc.getId(), translation.vlan2,
                        translation.pcp, translation.queue, translation.meter);
            } else if (isOVS(translation.dpid)) {
                fr = omi.createTransitVlanMacCircuit(node, translation.priority, translation.c,
                        translation.dstMac1, inNc.getId(), translation.vlan1,
                        translation.dstMac2, outNc.getId(), translation.vlan2,
                        translation.pcp, translation.queue, translation.meter);
            } else {
                // XXX log something
            }
        }
        catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        translation.flowRef = fr;
        return fr;
    }

    public FlowRef installL2ControllerRule(L2Translation translation) {
        // XXX sendtocontroller
        // OVS doesn't do this yet.  Probably want for consistency
        //
        Node node;
        NodeConnector inNc;

        FlowRef fr = null;

        node = omi.getNetworkDeviceByDpidArray(translation.dpid);
        if (node == null) {
            return null;
        }
        inNc = OdlMdsalImpl.getNodeConnector(node, translation.inPort);
        if (inNc == null) {
            return null;
        }

        try {
            if (isCorsa(translation.dpid)) {
                fr = oci.sendVlanMacToController(node, translation.priority, translation.c,
                        translation.dstMac1, inNc.getId(), translation.vlan1);
            } else if (isOVS(translation.dpid)) {
//                fr = omi.sendVlanMacToController(node, translation.priority, translation.c,
//                        translation.dstMac1, inNc.getId(), translation.vlan1);
            } else {
                // XXX log something
            }
        }
        catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        translation.flowRef = fr;
        return fr;
    }

    public boolean deleteL2ForwardingRule(L2Translation translation) {
        return deleteFlow(translation.dpid, translation.flowRef);
    }

    public boolean deleteFlow(byte [] dpid, FlowRef flowRef) {
        boolean rc = false;

        try {
            // XXX in theory if we have the FlowRef we know what switch it applies to,
            // and therefore know what the switch vendor is.
            if (isCorsa(dpid)) {
                oci.deleteFlow(flowRef);
                rc = true;
            } else if (isOVS(dpid)) {
                rc = omi.deleteFlow(flowRef);
            } else {
                /// XXX log something
            }
        }
        catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return rc;
    }

    public boolean transmitDataPacket(byte [] dpid, String outPort, byte [] payload) {
        Node node;
        node = omi.getNetworkDeviceByDpidArray(dpid);
        if (node == null) {
            // XXX log something
            return false;
        }
        NodeConnector outNc = omi.getNodeConnector(node, outPort);
        if (outNc == null) {
            // XXX log something
            return false;
        }
        boolean rc = false;
        omi.transmitDataPacket(node, outNc, payload);
        return true;
    }

    /**
     * Return whether the DPID parameter encodes a Corsa switch.
     *
     * Encodes a policy specific to the ESnet 100G SDN testbed.
     * @param dpid
     * @return
     */
    public static boolean isCorsa(byte [] dpid) {
        return dpid[0] == 2;
    }

    /**
     * Return whether the DPID parameter encodes an OVS switch.
     *
     * Encodes a policy specific to the ESnet 100G SDN testbed.
     * @param dpid
     * @return
     */
    public static boolean isOVS(byte [] dpid) {
        return dpid[0] == 1;
    }


}
