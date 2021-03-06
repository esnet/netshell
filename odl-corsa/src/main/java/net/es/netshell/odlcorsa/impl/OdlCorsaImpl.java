/*
 * ESnet Network Operating System (ENOS) Copyright (c) 2015, The Regents
 * of the University of California, through Lawrence Berkeley National
 * Laboratory (subject to receipt of any required approvals from the
 * U.S. Dept. of Energy).  All rights reserved.
 *
 * If you have questions about your rights to use or distribute this
 * software, please contact Berkeley Lab's Innovation & Partnerships
 * Office at IPO@lbl.gov.
 *
 * NOTICE.  This Software was developed under funding from the
 * U.S. Department of Energy and the U.S. Government consequently retains
 * certain rights. As such, the U.S. Government has been granted for
 * itself and others acting on its behalf a paid-up, nonexclusive,
 * irrevocable, worldwide license in the Software to reproduce,
 * distribute copies to the public, prepare derivative works, and perform
 * publicly and display publicly, and to permit other to do so.
 *
 */

package net.es.netshell.odlcorsa.impl;

import net.es.netshell.odlcorsa.OdlCorsaIntf;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sdx3.rev150814.*;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * This class provides some glue to the Corsa SDX3 driver.
 */
public class OdlCorsaImpl implements AutoCloseable, OdlCorsaIntf {

    // ODL objects
    DataBroker dataBroker;
    NotificationProviderService notificationProviderService;
    RpcProviderRegistry rpcProviderRegistry;

    Sdx3Service sdx3Service;

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

    public Sdx3Service getSdx3Service() {
        return sdx3Service;
    }

    // Logging
    static final private Logger logger = LoggerFactory.getLogger(OdlCorsaImpl.class);

    // This is a quasi-singleton.  In theory there can be multiple of these objects
    // in a system, but in practice it seems that each one of these is associated
    // with a single instance of the OSGi bundle, which basically just means just
    // one per system.  So we can somewhat safely say there should be at most one
    // instance, and keep a class member variable pointing to that one instance.
    static private volatile OdlCorsaImpl instance = null;

    // The constructor needs to save a pointer to this object as "the" instance.
    // If there is more than one object construction attempted, that's bad.
    public OdlCorsaImpl(DataBroker d, NotificationProviderService n, RpcProviderRegistry r) {

        System.out.println("Hello ODL Corsa");

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
            this.sdx3Service = rpcProviderRegistry.getRpcService(Sdx3Service.class);
            if (this.sdx3Service == null) {
                throw new RuntimeException("this.sdx3Service null");
            }

        }
        else {
            throw new RuntimeException("Attempt to create multiple " + OdlCorsaImpl.class.getName());
        }
    }
    public static OdlCorsaImpl getInstance() { return instance; }

    /**
     * Override the close() abstract method from java.lang.AutoCloseable.
     */
    @Override
    public void close() throws Exception {

        System.out.println("Goodbye ODL Corsa");
        instance = null;
        return;
    }

    /**
     * delete-flow
     */
    public void deleteFlow(FlowRef flowRef) throws InterruptedException, ExecutionException {
        DeleteFlowInput deleteFlowInput = new DeleteFlowInputBuilder().setFlowRef(flowRef).build();
        Future<RpcResult<Void>> future = sdx3Service.deleteFlow(deleteFlowInput);
        RpcResult<Void> result = future.get();
        return;
    }

    /**
     * create-transit-vlan-mac-circuit
     */
    public FlowRef createTransitVlanMacCircuit(Node odlNode, int priority, BigInteger c,
                                               MacAddress m1, NodeConnectorId ncid1, int vlan1,
                                               MacAddress m2, NodeConnectorId ncid2, int vlan2,
                                               short vp2, short q2, long mt2)
        throws InterruptedException, ExecutionException {

        // Create the create-transit-vlan-mac-circuit match fields first
        // XXX it is not clear to me if we should be mucking around with methods from
        // openflowplugin directly, even though they're public and static.
        PortId portId1 = new PortId(InventoryDataServiceUtil.portNumberfromNodeConnectorId(OpenflowVersion.OF13, ncid1.getValue()).shortValue());
        VlanId vlanId1 = new VlanId(vlan1);

        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sdx3.rev150814.create.transit.vlan.mac.circuit.input.Match match =
            new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sdx3.rev150814.create.transit.vlan.mac.circuit.input.MatchBuilder().
                setEthernetDestination(m1).setInPort(portId1).setVlanId(vlanId1).build();

        // Create the create-transit-vlan-mac-circuit action fields
        PortId portId2 = new PortId(InventoryDataServiceUtil.portNumberfromNodeConnectorId(OpenflowVersion.OF13, ncid2.getValue()).shortValue());
        VlanId vlanId2 = new VlanId(vlan2);
        VlanPcp vlanPcp2 = new VlanPcp(vp2);
        QueueId queue2 = new QueueId(q2);
        MeterId meter2 = new MeterId(mt2);
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sdx3.rev150814.create.transit.vlan.mac.circuit.input.Action action =
            new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sdx3.rev150814.create.transit.vlan.mac.circuit.input.ActionBuilder().
                setEthernetDestination(m2).setVlanId(vlanId2).setVlanPcp(vlanPcp2).setQueueId(queue2).setMeterId(meter2).setOutPort(portId2).build();

        // Build the complete set of parameters
        FlowCookie cookie = new FlowCookie(c);
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sdx3.rev150814.CreateTransitVlanMacCircuitInput input =
            new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sdx3.rev150814.CreateTransitVlanMacCircuitInputBuilder().
                setNodeId(odlNode.getId()).setPriority(priority).setCookie(cookie).setMatch(match).setAction(action).build();

        Future<RpcResult<CreateTransitVlanMacCircuitOutput>> future =
                sdx3Service.createTransitVlanMacCircuit(input);
        RpcResult<CreateTransitVlanMacCircuitOutput> rpcResult = future.get();
        if (rpcResult.isSuccessful()) {
            CreateTransitVlanMacCircuitOutput result = rpcResult.getResult();
            return result.getFlowRef();
        }
        else {
            return null;
        }
    }

    /**
     * send-vlan-mac-to-controller
     */
    public FlowRef sendVlanMacToController(Node odlNode, int priority, BigInteger c,
                                           MacAddress m1, NodeConnectorId ncid1, int vlan1)
        throws InterruptedException, ExecutionException {

        // Create the match fields.
        // Because of the structure of the tables in the Corsa pipeline, the matches
        // are identical to create-transit-vlan-mac-circuit.
        // XXX it is not clear to me if we should be mucking around with methods from
        // openflowplugin directly, even though they're public and static.
        PortId portId1 = new PortId(InventoryDataServiceUtil.portNumberfromNodeConnectorId(OpenflowVersion.OF13, ncid1.getValue()).shortValue());
        VlanId vlanId1 = new VlanId(vlan1);

        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sdx3.rev150814.send.vlan.mac.to.controller.input.Match match =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sdx3.rev150814.send.vlan.mac.to.controller.input.MatchBuilder().
                        setEthernetDestination(m1).setInPort(portId1).setVlanId(vlanId1).build();

        // Build the rest of the parameters.
        FlowCookie cookie = new FlowCookie(c);
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sdx3.rev150814.SendVlanMacToControllerInput input =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sdx3.rev150814.SendVlanMacToControllerInputBuilder().
                        setNodeId(odlNode.getId()).setPriority(priority).setCookie(cookie).setMatch(match).build();

        Future<RpcResult<SendVlanMacToControllerOutput>> future =
                sdx3Service.sendVlanMacToController(input);
        RpcResult<SendVlanMacToControllerOutput> rpcResult = future.get();
        if (rpcResult.isSuccessful()) {
            SendVlanMacToControllerOutput result = rpcResult.getResult();
            return result.getFlowRef();
        }
        else {
            return null;
        }
    }

    /**
     * Create a green-only meter.
     * Note that creating a meter with the same ID as an existing meter causes a silent failure.
     *
     * @param odlNode
     * @param meter
     * @return Meter reference, for use in deleting the meter
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public MeterRef createGreenMeter(Node odlNode, long meter) throws InterruptedException, ExecutionException {
        org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId meterId =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId(meter);
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sdx3.rev150814.CreateGreenMeterInput input =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sdx3.rev150814.CreateGreenMeterInputBuilder()
                        .setNodeId(odlNode.getId()).setMeterId(meterId).build();

        Future<RpcResult<CreateGreenMeterOutput>> future = sdx3Service.createGreenMeter(input);
        RpcResult<CreateGreenMeterOutput> rpcResult = future.get();
        if (rpcResult.isSuccessful()) {
            CreateGreenMeterOutput result = rpcResult.getResult();
            return result.getMeterRef();
        }
        else {
            return null;
        }

    }

    /**
     * Create a green-yellow meter.
     * Note that creating a meter with the same ID as an existing meter causes a silent failure.
     *
     * @param odlNode
     * @param meter
     * @param cr
     * @param cbs
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public MeterRef createGreenYellowMeter(Node odlNode, long meter, long cr, long cbs)
            throws InterruptedException, ExecutionException {
        org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId meterId =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId(meter);

        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sdx3.rev150814.CreateGreenYellowMeterInput input =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sdx3.rev150814.CreateGreenYellowMeterInputBuilder()
                        .setNodeId(odlNode.getId()).setMeterId(meterId)
                        .setCommittedRate(cr).setCommittedBurstSize(cbs)
                        .build();

        Future<RpcResult<CreateGreenYellowMeterOutput>> future = sdx3Service.createGreenYellowMeter(input);
        RpcResult<CreateGreenYellowMeterOutput> rpcResult = future.get();
        if (rpcResult.isSuccessful()) {
            CreateGreenYellowMeterOutput result = rpcResult.getResult();
            return result.getMeterRef();
        }
        else {
            return null;
        }
    }

    /**
     * Create a green-red meter.
     * Note that creating a meter with the same ID as an existing meter causes a silent failure.
     *
     * @param odlNode
     * @param meter
     * @param er
     * @param ebs
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public MeterRef createGreenRedMeter(Node odlNode, long meter, long er, long ebs)
            throws InterruptedException, ExecutionException {
        org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId meterId =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId(meter);

        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sdx3.rev150814.CreateGreenRedMeterInput input =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sdx3.rev150814.CreateGreenRedMeterInputBuilder()
                        .setNodeId(odlNode.getId()).setMeterId(meterId)
                        .setExcessRate(er).setExcessBurstSize(ebs)
                        .build();

        Future<RpcResult<CreateGreenRedMeterOutput>> future = sdx3Service.createGreenRedMeter(input);
        RpcResult<CreateGreenRedMeterOutput> rpcResult = future.get();
        if (rpcResult.isSuccessful()) {
            CreateGreenRedMeterOutput result = rpcResult.getResult();
            return result.getMeterRef();
        }
        else {
            return null;
        }
    }

    /**
     * Create a green-yellow-red meter.
     * Note that creating a meter with the same ID as an existing meter causes a silent failure.
     *
     * @param odlNode
     * @param meter
     * @param cr
     * @param cbs
     * @param er
     * @param ebs
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public MeterRef createGreenYellowRedMeter(Node odlNode, long meter, long cr, long cbs, long er, long ebs)
            throws InterruptedException, ExecutionException {
        org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId meterId =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId(meter);

        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sdx3.rev150814.CreateGreenYellowRedMeterInput input =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sdx3.rev150814.CreateGreenYellowRedMeterInputBuilder()
                        .setNodeId(odlNode.getId()).setMeterId(meterId)
                        .setCommittedRate(cr).setCommittedBurstSize(cbs)
                        .setExcessRate(er).setExcessBurstSize(ebs)
                        .build();

        Future<RpcResult<CreateGreenYellowRedMeterOutput>> future = sdx3Service.createGreenYellowRedMeter(input);
        RpcResult<CreateGreenYellowRedMeterOutput> rpcResult = future.get();
        if (rpcResult.isSuccessful()) {
            CreateGreenYellowRedMeterOutput result = rpcResult.getResult();
            return result.getMeterRef();
        }
        else {
            return null;
        }
    }


    /**
     * Delete a previously-configured meter.
     *
     * @param meterRef
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public boolean deleteMeter(MeterRef meterRef) throws InterruptedException, ExecutionException {
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sdx3.rev150814.DeleteMeterInput input =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sdx3.rev150814.DeleteMeterInputBuilder().setMeterRef(meterRef).build();
        Future<RpcResult<Void>> future = sdx3Service.deleteMeter(input);
        RpcResult<Void> rpcResult = future.get();
        if (rpcResult.isSuccessful()) {
            return true;
        }
        else {
            return false;
        }
    }

}
