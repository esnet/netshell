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
import net.es.netshell.controller.OpenFlowNode;
import net.es.netshell.controller.layer2.Layer2Controller;
import net.es.netshell.controller.layer2.Layer2ForwardRule;
import net.es.netshell.controller.layer2.Layer2Port;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.dst.action._case.SetDlDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.id.action._case.SetVlanIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.id.action._case.SetVlanIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.MacAddressFilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.*;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class is an interface to the OpenFlow controller (and related) functionality
 * in OpenDaylight, using the MD-SAL abstraction layer.
 */
public class OdlMdsalImpl implements AutoCloseable, PacketProcessingListener, Layer2Controller {

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

    // We have to have something to set up the initial flows in the switches,
    // this is it.
    InitialFlowWriter initialFlowWriter;

    private AtomicLong flowIdInc = new AtomicLong();
    private AtomicLong flowCookieInc = new AtomicLong(0x8800000000000000L);

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

            // Register us for notifications
            this.registrations = Lists.newArrayList();
            Registration reg = notificationProviderService.registerNotificationListener(this);
            this.registrations.add(reg);

            // Create an InitialFlowWriter and register it for notifications
            this.initialFlowWriter = new InitialFlowWriter(this.salFlowService);
            Registration reg2 = notificationProviderService.registerNotificationListener(this.initialFlowWriter);
            this.registrations.add(reg2);

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

    public Node getNetworkDeviceByDpid(long dpid) {
        List<Node> switches = this.getNetworkDevices();
        Node sw = null;

        String targetId = OFConstants.OF_URI_PREFIX + String.format("%d", dpid);

        // Look for a switch in inventory that has that ID.
        for (Node s : switches) {
            if (s.getId().getValue().equals(targetId)) {
                sw = s;
                break;
            }
        }
        return sw;
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
     * The port name is, at least in mininet-land, of the form "s1-eth1".
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

    /**
     * Get the NodeConnector for the local port on a switch.
     * @param node
     * @return
     */
    public NodeConnector getLocalNodeConnector(Node node) {
        List<NodeConnector> l = node.getNodeConnector();
        for (NodeConnector nc : l) {
            // Get the augmentation of the NodeConnector so we can get to
            // its port number, see if that matches the reserved local port number
            FlowCapableNodeConnector fcnc = nc.getAugmentation(FlowCapableNodeConnector.class);
            if (fcnc != null) {
                String fcncPort = fcnc.getPortNumber().getString();
                if (fcncPort != null && fcncPort.equals("LOCAL")) {
                    return nc;
                }
            }
        }
        return null;
    }

    private InstanceIdentifier<Node> getNodeInstanceId(Node node) {
        NodeKey nodeKey = new NodeKey(node.getId());
        return InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey).build();
    }

    private InstanceIdentifier<NodeConnector> getNodeConnectorInstanceId(Node node, NodeConnector nc) {
        NodeKey nodeKey = new NodeKey(node.getId());
        NodeConnectorKey nckey = new NodeConnectorKey(nc.getId());
        return InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, nodeKey)
                .child(NodeConnector.class, nckey)
                .build();
    }

    private InstanceIdentifier<Table> getTableInstanceId(InstanceIdentifier<Node> nodeId, short flowTableId) {
        // get flow table key
        TableKey flowTableKey = new TableKey(flowTableId);
        return nodeId.builder()
                .augmentation(FlowCapableNode.class)
                .child(Table.class, flowTableKey)
                .build();
    }

    /**
     * Push a flow
     * This basically just writes the flow (which already needs to have been
     * constructed) to the datastore.  The FlowProgrammer module will pick this
     * up and actually push the flow to the switch.
     */
    public FlowRef addFlow(Node odlNode, Flow flow) throws ExecutionException, InterruptedException {

        AddFlowInputBuilder builder = new AddFlowInputBuilder(flow);
        NodeKey nodeKey = new NodeKey(odlNode.getId());
        //XXX need setNode, maybe setFlowRef, setFlowTable, setTransactionURI

        InstanceIdentifier<Node> nodeInstanceIdentifier = getNodeInstanceId(odlNode);
        builder.setNode(new NodeRef(nodeInstanceIdentifier));

        InstanceIdentifier<Flow> flowInstanceIdentifier =
                InstanceIdentifier.builder(Nodes.class).
                        child(Node.class, nodeKey).
                        augmentation(FlowCapableNode.class).
                        child(Table.class, new TableKey(flow.getTableId())).
                        child(Flow.class, flow.getKey()).
                        build();
        builder.setFlowRef(new FlowRef(flowInstanceIdentifier));
        // XXX note to self:  save the FlowRef?
        builder.setFlowTable(new FlowTableRef(getTableInstanceId(nodeInstanceIdentifier, flow.getTableId())));

        builder.setTransactionUri(new Uri(flow.getId().getValue()));

        Future<RpcResult<AddFlowOutput>> resultFuture =
                salFlowService.addFlow(builder.build());
        RpcResult<AddFlowOutput> rpcResult = resultFuture.get();
        if (rpcResult.isSuccessful()) {
            AddFlowOutput result = rpcResult.getResult();
            return new FlowRef(flowInstanceIdentifier);
        }
        else {
            logger.error(rpcResult.getErrors().toString());
            return null;
        }
    }

    /**
     * Create a forwarding flow between two switch ports
     * This function is mostly for testing.
     */
//    public Flow makeFlow(Layer2ForwardRule rule) {
    public Flow makeFlow(Node odlNode, String inPortName, String outPortName) {


//        Layer2Port inPort, outPort;
//        inPort = (Layer2Port) rule.getInPort();
//        outPort = (Layer2Port) rule.getOutPort();

        // Figure out the node
//        Node odlNode = this.findODLSwitch((OpenFlowNode) inPort.getNode());

        // Create a match object to match on the input port
        MatchBuilder matchBuilder = new MatchBuilder();
//         NodeConnector ncIn = this.getNodeConnector(odlNode, inPort.getResourceName());
        NodeConnector ncIn = this.getNodeConnector(odlNode, inPortName);
        matchBuilder.setInPort(ncIn.getId());

        // Create an output action to forward to the output port
        OutputActionBuilder output = new OutputActionBuilder();
//          NodeConnector ncOut = this.getNodeConnector(odlNode, outPort.getResourceName());
        NodeConnector ncOut = this.getNodeConnector(odlNode, outPortName);
        output.setOutputNodeConnector(ncOut.getId());

        // Put that in an action
        ActionBuilder ab = new ActionBuilder();
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));

        // Make an action list to hold the action
        List<Action> actionList = Lists.newArrayList();
        actionList.add(ab.build());

        // Create apply actions instruction
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Now create an instruction to include the instruction
        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));

        // List of instructions that stores individual instructions
        List<Instruction> instructions = Lists.newArrayList();
        instructions.add(ib.build());

        // Now we need an instruction set builder
        InstructionsBuilder isb = new InstructionsBuilder();
        isb.setInstruction(instructions);

        // Finally get to make the flow itself
        FlowBuilder flowBuilder = new FlowBuilder();

//         String flowId = "L2_Rule_" + inPort.getResourceName();
        FlowId flowId = new FlowId("L2_Rule_" + inPortName);
        flowBuilder.setId(flowId);
        flowBuilder.setKey(new FlowKey(flowId));
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId((short) 0);
        flowBuilder.setPriority(OFConstants.DEFAULT_FLOW_PRIORITY);
        flowBuilder.setFlowName(flowId.getValue());
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);
        flowBuilder.setBufferId(OFConstants.OFP_NO_BUFFER);
        flowBuilder.setFlags(new FlowModFlags(false, false, false, false, false));

        flowBuilder.setMatch(matchBuilder.build());
        flowBuilder.setInstructions(isb.build());

        return flowBuilder.build();

    }


    /**
     * Create a Layer 2 VLAN and MAC translation flow entry
     * @param odlNode
     * @param priority
     * @param c
     * @param m1
     * @param ncid1
     * @param vlan1
     * @param m2
     * @param ncid2
     * @param vlan2
     * @param vp2
     * @param q2
     * @param mt2
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public Flow createTransitVlanMacCircuitFlow(Node odlNode, int priority, BigInteger c,
                                                MacAddress m1, NodeConnectorId ncid1, int vlan1,
                                                MacAddress m2, NodeConnectorId ncid2, int vlan2,
                                                short vp2, short q2, long mt2) {

        // Create the new match object first.  We do exact matches on the port, VLAN,
        // and MAC address.
        MatchBuilder matchBuilder = new MatchBuilder();
        matchBuilder.setInPort(ncid1);

        org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder
          vlanIdBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder();
        vlanIdBuilder.setVlanId(new VlanId(vlan1));
        vlanIdBuilder.setVlanIdPresent(true);
        VlanMatchBuilder vlanMatchBuilder = new VlanMatchBuilder();
        vlanMatchBuilder.setVlanId(vlanIdBuilder.build());
        matchBuilder.setVlanMatch(vlanMatchBuilder.build());

        EthernetDestinationBuilder ethernetDestinationBuilder = new EthernetDestinationBuilder();
        ethernetDestinationBuilder.setAddress(new MacAddress(m1));
        EthernetMatchBuilder ethernetMatchBuilder = new EthernetMatchBuilder();
        ethernetMatchBuilder.setEthernetDestination(ethernetDestinationBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatchBuilder.build());

        // Create set and output actions
        SetVlanIdActionBuilder setVlanIdActionBuilder = new SetVlanIdActionBuilder();
        setVlanIdActionBuilder.setVlanId(new VlanId(vlan2));

        SetDlDstActionBuilder setDlDstActionBuilder = new SetDlDstActionBuilder();
        setDlDstActionBuilder.setAddress(m2);

        // Open vSwitch (and possibly other switches too?) has a restriction that
        // a packet cannot be output to the same port from which it entered the
        // switch.  In the SDN testbed we may very well have reasons for doing
        // things like this.  If we're asked to set up a flow like this, use an
        // instruction to overwrite the in_port metadata with something that can't
        // possibly be the output port.  This allows us to set the output port to
        // the original input port.  Only do this hack if necessary, both for
        // runtime performance and to avoid unnecessarily obfuscating the flow
        // entry.
        SetFieldBuilder setFieldBuilder = null;
        if (ncid1.getValue().equals(ncid2.getValue())) {
            setFieldBuilder = new SetFieldBuilder();
            setFieldBuilder.setInPort(this.getLocalNodeConnector(odlNode).getId());
        }

        OutputActionBuilder outputActionBuilder = new OutputActionBuilder();
        outputActionBuilder.setOutputNodeConnector(ncid2);

        ActionBuilder ab1 = new ActionBuilder();
        ab1.setAction(new SetVlanIdActionCaseBuilder().setSetVlanIdAction(setVlanIdActionBuilder.build()).build());
        ab1.setOrder(0);
        ab1.setKey(new ActionKey(0));

        ActionBuilder ab2 = new ActionBuilder();
        ab2.setAction(new SetDlDstActionCaseBuilder().setSetDlDstAction(setDlDstActionBuilder.build()).build());
        ab2.setOrder(1);
        ab2.setKey(new ActionKey(1));

        ActionBuilder ab2a = null;
        if (setFieldBuilder != null) {
            ab2a = new ActionBuilder();
            ab2a.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
            ab2a.setOrder(2);
            ab2a.setKey(new ActionKey(0));
        }

        ActionBuilder ab3 = new ActionBuilder();
        ab3.setAction(new OutputActionCaseBuilder().setOutputAction(outputActionBuilder.build()).build());
        ab3.setOrder(2);
        ab3.setKey(new ActionKey(2));

        // Make an action list to hold the actions
        List<Action> actionList = Lists.newArrayList();
        actionList.add(ab1.build());
        actionList.add(ab2.build());
        if (ab2a != null) {
            actionList.add(ab2a.build());
        }
        actionList.add(ab3.build());

        // Create APPLY ACTIONS instruction
        ApplyActionsBuilder applyActionsBuilder = new ApplyActionsBuilder();
        applyActionsBuilder.setAction(actionList);

        // Create an instruction to include the APPLY ACTION instruction
        InstructionBuilder instructionBuilder = new InstructionBuilder();
        instructionBuilder.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(applyActionsBuilder.build()).build());
        instructionBuilder.setOrder(0);
        instructionBuilder.setKey(new InstructionKey(0));

        // Now need a one-element list to hold this instruction
        List<Instruction> instructionList = Lists.newArrayList();
        instructionList.add(instructionBuilder.build());

        // Set the instructions
        InstructionsBuilder instructionsBuilder = new InstructionsBuilder();
        instructionsBuilder.setInstruction(instructionList);

        // Make the flow
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId((short) 0);
        flowBuilder.setPriority(OFConstants.DEFAULT_FLOW_PRIORITY);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);
        flowBuilder.setBufferId(OFConstants.OFP_NO_BUFFER);
        flowBuilder.setFlags(new FlowModFlags(false, false, false, false, false));

        flowBuilder.setMatch(matchBuilder.build());
        flowBuilder.setInstructions(instructionsBuilder.build());

        // Do these last
        FlowId flowId = new FlowId("TransitVlanMacCircuit_" + Long.toString(flowBuilder.hashCode()));
        flowBuilder.setId(flowId);
        flowBuilder.setKey(new FlowKey(flowId));
        flowBuilder.setFlowName(flowId.getValue());

        return flowBuilder.build();

        /*
        // Push the flow.  We should be getting something back that gives us a handle to the flow
        // if we need to get back to it.
        return addFlow(nid, flowBuilder.build());
        */
    }

    /**
     *
     * @param flowRef
     */
    public boolean deleteFlow(FlowRef flowRef, Flow flow) throws InterruptedException, ExecutionException {

        // Make a parameter block for doing removeFlow().  This seems to really really want
        // the actual flow being removed, although in theory it should be possible to
        // remove the flow just given a FlowRef object (because the FlowRef has the path
        // to the flow object in the datastore.
        RemoveFlowInputBuilder removeFlowInputBuilder = new RemoveFlowInputBuilder(flow);
        removeFlowInputBuilder.setBarrier(true);
        removeFlowInputBuilder.setFlowRef(flowRef);
        removeFlowInputBuilder.setNode(new NodeRef(flowRef.getValue().firstIdentifierOf(Node.class)));
        removeFlowInputBuilder.setFlowTable(new FlowTableRef(flowRef.getValue().firstIdentifierOf(Table.class)));
        salFlowService.removeFlow(removeFlowInputBuilder.build());

        Future<RpcResult<RemoveFlowOutput>> resultFuture =
                salFlowService.removeFlow(removeFlowInputBuilder.build());
        RpcResult<RemoveFlowOutput> rpcResult = resultFuture.get();
        if (rpcResult.isSuccessful() == true) {
            RemoveFlowOutput result = rpcResult.getResult();
            return true;
        }
        else {
            logger.error(rpcResult.getErrors().toString());
            return false;
        }


    }

    private Node findODLSwitch(OpenFlowNode enosSwitch) {
        List<Node> switches = this.getNetworkDevices();
        Node sw = null;

        // Construct the Node ID we're looking for, it'll be "openflow:xxx" where
        // xxx is the decimal representation of the DPID
        byte[] dpid = enosSwitch.dpidToByteArray();
        long dpidLong = 0;
        for (int i = 0; i < dpid.length; i++) {
            dpidLong <<= 8;
            dpidLong |= dpid[i];
        }
        // XXX return this.getNetworkDeviceByDpid(long dpidLong);
        String targetId = OFConstants.OF_URI_PREFIX + String.format("%ld", dpidLong);

        // Look for a switch in inventory that has that ID.
        for (Node s : switches) {
            if (s.getId().getValue().equals(targetId)) {
                sw = s;
                break;
            }
        }
        return sw;
    }

    public void transmitDataPacket(Node odlNode, NodeConnector ncid, byte [] payload) {
        TransmitPacketInput input =
                new TransmitPacketInputBuilder().setPayload(payload)
                        .setNode(new NodeRef(this.getNodeInstanceId(odlNode)))
                        .setEgress(new NodeConnectorRef(this.getNodeConnectorInstanceId(odlNode, ncid)))
                        .build();
        packetProcessingService.transmitPacket(input);
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
