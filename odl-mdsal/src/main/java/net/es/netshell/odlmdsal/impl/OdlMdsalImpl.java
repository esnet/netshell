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

package net.es.netshell.odlmdsal.impl;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * This class is an interface to the OpenFlow controller (and related) functionality
 * in OpenDaylight, using the MD-SAL abstraction layer.
 */
public class OdlMdsalImpl implements AutoCloseable, PacketProcessingListener {

    /**
     * Callback function to pass a RawPacket to some of our code.  It's designed
     * to get a thread of control back into Python...the idea is to define and
     * instantiate an instance of a Python class that implements this interface.
     *
     * There needs to be some authorization around the getter and setter here.
     */
    public interface Callback {
        public void callback(PacketReceived notification);
    }
    private Callback packetInCallback;

    public Callback getPacketInCallback() {
        return packetInCallback;
    }

    public void setPacketInCallback(Callback packetInCallback) {
        this.packetInCallback = packetInCallback;
    }

    // ODL objects
    DataBroker dataBroker;
    NotificationProviderService notificationProviderService;
    RpcProviderRegistry rpcProviderRegistry;
    List<Registration> registrations;

    SalFlowService salFlowService;
    PacketProcessingService packetProcessingService;

    // XXX These getters are mostly here for debugging from the interactive
    // Python shell.  In theory there isn't any reason to expose these
    // member variables.
    public DataBroker getDataBroker() {
        return dataBroker;
    }

    public NotificationProviderService getNotificationProviderService() {
        return notificationProviderService;
    }

    public RpcProviderRegistry getRpcProviderRegistry() {
        return rpcProviderRegistry;
    }

    public SalFlowService getSalFlowService() {
        return salFlowService;
    }

    // Logging
    static final private Logger logger = LoggerFactory.getLogger(OdlMdsalImpl.class);

    // This is a quasi-singleton.  In theory there can be multiple of these objects
    // in a system, but in practice it seems that each one of these is associated
    // with a single instance of the OSGi bundle, which basically just means just
    // one per system.  So we can somewhat safely say there should be at most one
    // instance, and keep a class member variable pointing to that one instance.
    static private volatile OdlMdsalImpl instance = null;

    // The constructor needs to save a pointer to this object as "the" instance.
    // If there is more than one object construction attempted, that's bad.
    public OdlMdsalImpl(DataBroker d, NotificationProviderService n, RpcProviderRegistry r) {

        System.out.println("Hello ODL MD-SAL");

        if (instance == null) {
            instance = this;

            // Save the objects that allow us to access the SAL infrastructure.
            this.dataBroker = d;
            if (this.dataBroker == null) {
                throw new RuntimeException("this.dataBroker null");
            }
            this.notificationProviderService = n;
            if (this.notificationProviderService == null) {
                throw new RuntimeException("this.notificationProviderService null");
            }
            this.rpcProviderRegistry = r;
            if (this.rpcProviderRegistry == null) {
                throw new RuntimeException("this.rpcProviderRegistry null");
            }

            // Find some services that we need
            this.salFlowService = rpcProviderRegistry.getRpcService(SalFlowService.class);
            if (this.salFlowService == null) {
                throw new RuntimeException("this.salFlowService null");
            }

            this.packetProcessingService = rpcProviderRegistry.getRpcService(PacketProcessingService.class);
            if (this.packetProcessingService == null) {
                throw new RuntimeException("this.packetProcessingService null");
            }

            // Register for notifications
            this.registrations = Lists.newArrayList();
            Registration reg = notificationProviderService.registerNotificationListener(this);
            this.registrations.add(reg);

        }
        else {
            throw new RuntimeException("Attempt to create multiple " + OdlMdsalImpl.class.getName());
        }
    }
    public static OdlMdsalImpl getInstance() { return instance; }

    /**
     * Override the close() abstract method from java.lang.AutoCloseable.
     */
    @Override
    public void close() throws Exception {

        // Deregister notifications
        if (registrations != null) {
            for (Registration r : registrations) {
                r.close();
            }
        }
        registrations.clear();

        System.out.println("Goodbye ODL MD-SAL");
        instance = null;

        return;
    }

    /**
     * Override onPacketReceived() abstract method from PacketProcessingListener.
     */
    @Override
    public void onPacketReceived(PacketReceived notification) {
        logger.info("Received data packet " + notification.toString());

        EthernetFrame frame = EthernetFrame.packetToFrame(notification.getPayload());

        if (frame != null) {

            logger.info(" Dst " + EthernetFrame.byteString(frame.getDstMac()) +
                    " Src " + EthernetFrame.byteString(frame.getSrcMac()) +
                    " EtherType " + String.format("%04x", frame.getEtherType()) +
                    " VID " + String.format("%d", frame.getVid()) +
                    " payload " + frame.getPayload().length);

            if (packetInCallback != null) {
                packetInCallback.callback(notification);
            }
        }
        else {
            logger.info("Unable to parse inbound packet");
        }
    }

    /**
     * Get the set of switches
     * @return
     */
    public List<Node> getNetworkDevices() {
        List<Node> switches = null;

        InstanceIdentifier<Nodes> nodesIdentifier = InstanceIdentifier.builder(Nodes.class).build();

        try {
            Optional<Nodes> maybeNodes = null;
            ReadOnlyTransaction readTransaction = dataBroker.newReadOnlyTransaction();
            maybeNodes = readTransaction.read(LogicalDatastoreType.OPERATIONAL, nodesIdentifier).get();
            //
            if (maybeNodes.isPresent()) {
                switches = maybeNodes.get().getNode();
            }
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return switches;
    }

    /**
     * Get the set of node connectors for a switch
     */
    public List<NodeConnector> getNodeConnectors(Node node) {
        // The NodeConnector in getNodeConnector() below is a list of NodeCodeConnector type objects.
        return node.getNodeConnector();
    }

    /**
     * Get a specific port (a.k.a. NodeConnector) on a switch given its name
     * The node name is, at least in mininet-land, of the form "s1-eth1".
     * This depends on the FlowCapableNodeConnector augmentation, so it only
     * works with OpenFlow switches (and not surprisingly with the OpenFlow
     * plugin enabled).
     */
    public NodeConnector getNodeConnector(Node node, String nodeConnectorName) {
        List<NodeConnector> l = node.getNodeConnector();
        for (NodeConnector nc : l) {

            // We need to access an augmentation of the NodeConnector to get
            // its name (in mininet-land).
            FlowCapableNodeConnector fcnc = nc.getAugmentation(FlowCapableNodeConnector.class);
            if (fcnc != null) {
                if (fcnc.getName().equals(nodeConnectorName)) {
                    return nc;
                }
            }
        }
        return null;
    }

}
