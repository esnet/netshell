/*
 * ESnet Network Operating System (ENOS) Copyright (c) 2016, The Regents
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

package net.es.netshell.controller.core;

import net.es.netshell.boot.BootStrap;
import net.es.netshell.controller.impl.SdnController;
import net.es.netshell.odlcorsa.OdlCorsaIntf;
import net.es.netshell.odlmdsal.impl.OdlMdsalImpl;
import net.es.netshell.osgi.OsgiBundlesClassLoader;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
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

    public OdlMdsalImpl getOdlMdsalImpl() {
        return OdlMdsalImpl.getInstance();
    }

    /**
     * Get an object that implements the Corsa glue interface.
     * Note that even loading the code/class for the Corsa glue is optional,
     * so we have to be able to do this even if the implementation class
     * isn't loaded.
     * @return OdlCorsaImpl object
     */
    public OdlCorsaIntf getOdlCorsaImpl() {
        try {
            // We need to use our OSGi-aware class loader to find the OdlCorsaImpl object
            // and then some reflection techniques to find its getInstance() method.
            Class c = null;
            try {
                // This is clunky.  We can't do this everytime we need to invoke this method,
                // maybe need to cache it or something like that?  The difficulty, as always,
                // is knowing when to invalidate the cache.
                BundleContext bc = BootStrap.getBootStrap().getBundleContext();
                Bundle[] bundles = bc.getBundles();
                OsgiBundlesClassLoader classLoader =
                        new OsgiBundlesClassLoader(bundles, OdlCorsaIntf.class.getClassLoader());
                c = classLoader.findClass("net.es.netshell.odlcorsa.impl.OdlCorsaImpl");
            }
            catch (ClassNotFoundException e) {
                return null;
            }

            Method m = null;
            try {
                m = c.getMethod("getInstance");
            }
            catch (NoSuchMethodException e) {
                return null;
            }

            // We found the method, invoke it.  Note that we pass a null
            // as the object to invoke on, because this is a static method.
            Object o = m.invoke(null);
            OdlCorsaIntf oci = (OdlCorsaIntf) o;
            return oci;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Abstract Layer 2 Translation specification
     * Somewhat decoupled from OpenFlow, and assuming exact matches on, and
     * forwarding to, port/VLAN/destMAC only.
     */
    static public class L2Translation {

        /**
         * Output specification.
         * A "normal" L2Translation structure will have ony one of these,
         * but broadcast and tapping will have more than one.
         */
        static public class L2TranslationOutput{
            public String outPort;
            public short vlan;
            public MacAddress dstMac;
        }

        public byte [] dpid;

        public int priority;
        public BigInteger c;

        public String inPort;
        public short vlan1;
        public MacAddress srcMac1;
        public MacAddress dstMac1;

        public L2TranslationOutput [] outputs;

        public short pcp;
        public short queue;
        public long meter;

        public FlowRef flowRef;

        public L2Translation() {
            // Initialize some fields that have reasonable defaults.
            this.priority = OFConstants.DEFAULT_FLOW_PRIORITY;
            this.c = BigInteger.ZERO;
            this.outputs = new L2TranslationOutput[0];
            this.pcp = 0;
            this.meter = 0;
        }
    }

    public Controller() {
        if (instance == null) {
            instance = this;
        }
        else {
            throw new RuntimeException("Attempt to create multiple " + Controller.class.getName());
        }

        // Automatically start up an instance of SdnController, running in its own thread,
        // to listen for RabbitMQ messages.
        //
        // XXX When we move the SdnController stuff to a different bundle, this code should be
        // a part of the activator of that bundle.
        //
        // XXX When we start up a Karaf container that has netshell-controller already loaded,
        // there appears to be a (not yet well understood) race condition with the startup of the ODL
        // MD-SAL and ODL Corsa bundles.  The symptom is that SdnController doesn't get
        // PacketReceived notifications.  If we delay the instantiation of the SdnController object
        // by 20 seconds, this appears to let things settle down so that notifications work.
        try {
            Thread.sleep(20000);
        }
        catch (InterruptedException e) {
        }
        try {
            SdnController sdncont = new SdnController();
            sdncont.setCallback();
            Thread sdnthr = new Thread(sdncont);
            sdnthr.start();
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to start SDNController instance");
        }
    }
    public static Controller getInstance() { return instance; }

    public void reinit() {
    }

    // Logging
    static final private Logger logger = LoggerFactory.getLogger(Controller.class);

    // Glue to get some stuff from MD-SAL without the caller needing to have a handle
    // to the MD-SAL implementation
    public List<Node> getNetworkDevices() {
        return this.getOdlMdsalImpl().getNetworkDevices();
    }

    static public NodeConnector getNodeConnector(Node node, String nodeConnectorName) {
        return OdlMdsalImpl.getNodeConnector(node, nodeConnectorName);
    }

    public FlowRef installL2ForwardingRule(L2Translation translation) {
        Node node;
        NodeConnector inNc;

        FlowRef fr = null;

        // Make sure the node and its input connector exist
        node = this.getOdlMdsalImpl().getNetworkDeviceByDpidArray(translation.dpid);
        if (node == null) {
            return null;
        }
        inNc = OdlMdsalImpl.getNodeConnector(node, translation.inPort);
        if (inNc == null) {
            return null;
        }

        logger.debug("checked node and ncid");

        try {
            if (isCorsa(translation.dpid)) {

                logger.debug("Corsa");

                // The Corsa can only do one translation at a time...no packet copying
                if (translation.outputs.length == 1) {

                    // Make sure we can resolve the output connector
                    NodeConnector outNc = OdlMdsalImpl.getNodeConnector(node, translation.outputs[0].outPort);
                    if (outNc != null) {
                        fr = this.getOdlCorsaImpl().createTransitVlanMacCircuit(node, translation.priority, translation.c,
                                translation.dstMac1, inNc.getId(), translation.vlan1,
                                translation.outputs[0].dstMac, outNc.getId(), translation.outputs[0].vlan,
                                translation.pcp, translation.queue, translation.meter);
                    } else {
                        return null;
                    }
                }
                else {
                    return null;
                }
            } else if (isOVS(translation.dpid)) {

                logger.debug("OVS");

                // For now we only support a single translation, like the Corsa.
                // We should be able to take a vector of translations.
                // XXX do this
                if (translation.outputs.length == 1) {

                    logger.debug("single");

                    // Make sure we can resolve the output connector
                    NodeConnector outNc = OdlMdsalImpl.getNodeConnector(node, translation.outputs[0].outPort);
                    if (outNc != null) {
                        fr = this.getOdlMdsalImpl().createTransitVlanMacCircuit(node, translation.priority, translation.c,
                                translation.srcMac1, translation.dstMac1, inNc.getId(), translation.vlan1,
                                translation.outputs[0].dstMac, outNc.getId(), translation.outputs[0].vlan);
                    } else {
                        return null;
                    }
                }
                else {
                    // Multipoint circuit

                    logger.debug("multi");

                    // Construct vector of output tuples
                    OdlMdsalImpl.L2Output[] outputs = new OdlMdsalImpl.L2Output[translation.outputs.length];
                    for (int i = 0; i < translation.outputs.length; i++) {
                        NodeConnector outNc = OdlMdsalImpl.getNodeConnector(node, translation.outputs[i].outPort);
                        if (outNc == null) {
                            return null;
                        }
                        outputs[i] = new OdlMdsalImpl.L2Output();
                        outputs[i].mac = translation.outputs[i].dstMac;
                        outputs[i].ncid = outNc.getId();
                        outputs[i].vlan = translation.outputs[i].vlan;
                    }
                    fr = this.getOdlMdsalImpl().createMultipointVlanMacCircuit(node, translation.priority, translation.c,
                            translation.srcMac1, translation.dstMac1, inNc.getId(), translation.vlan1,
                            outputs);
                }
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

        node = this.getOdlMdsalImpl().getNetworkDeviceByDpidArray(translation.dpid);
        if (node == null) {
            return null;
        }
        inNc = OdlMdsalImpl.getNodeConnector(node, translation.inPort);
        if (inNc == null) {
            return null;
        }

        try {
            if (isCorsa(translation.dpid)) {
                fr = this.getOdlCorsaImpl().sendVlanMacToController(node, translation.priority, translation.c,
                        translation.dstMac1, inNc.getId(), translation.vlan1);
            } else if (isOVS(translation.dpid)) {
//                fr = this.getOdlMdsalImpl().sendVlanMacToController(node, translation.priority, translation.c,
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
                this.getOdlCorsaImpl().deleteFlow(flowRef);
                rc = true;
            } else if (isOVS(dpid)) {
                rc = this.getOdlMdsalImpl().deleteFlow(flowRef);
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
        node = this.getOdlMdsalImpl().getNetworkDeviceByDpidArray(dpid);
        if (node == null) {
            // XXX log something
            return false;
        }
        NodeConnector outNc = this.getOdlMdsalImpl().getNodeConnector(node, outPort);
        if (outNc == null) {
            // XXX log something
            return false;
        }
        boolean rc = false;
        this.getOdlMdsalImpl().transmitDataPacket(node, outNc, payload);
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
