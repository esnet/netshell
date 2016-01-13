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

package net.es.netshell.controller.impl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import net.es.netshell.controller.core.Controller;
import net.es.netshell.controller.intf.*;
import net.es.netshell.odlmdsal.impl.OdlMdsalImpl;
import org.codehaus.jackson.map.ObjectMapper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Created by bmah on 1/8/16.
 */
public class SdnController implements Runnable, AutoCloseable, OdlMdsalImpl.Callback {

    // Logging
    static final private Logger logger = LoggerFactory.getLogger(SdnController.class);

    // RabbitMQ stuff

    private static final String RPC_QUEUE_NAME = "sdn-controller-request-queue";

    private Connection connection;
    private Channel channel;
    private QueueingConsumer consumer;

    private ObjectMapper mapper;

    // OpenFlow stuff
    Controller controller;

    public static String getRpcQueueName() {
        return RPC_QUEUE_NAME;
    }

    public SdnController() {
        try {
            // Get a connection to the AMPQ broker (e.g. RabbitMQ server)
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            connection = factory.newConnection();
            channel = connection.createChannel();
            // Create the SDN controller queue, non-durable, non-exclusive, non-auto-delete, no other args
            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
            channel.basicQos(1); // what does this do?
            // Set up a consumer who will read messages from the queue
            consumer = new QueueingConsumer(channel);
            channel.basicConsume(RPC_QUEUE_NAME, false, consumer);

            // XXX we need to do some error-checking here to handle the case that the AMPQ server is dead
            // or unreachable.

            // JSON parser setup
            mapper = new ObjectMapper();

            // OpenFlow controller setup
            controller = Controller.getInstance();

            logger.info(SdnController.class.getName() + " ready");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() throws Exception {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    /**
     * Program PACKET_IN callback to point to us
     * @return boolean true indicates success
     */
    public boolean setCallback() {
        if (controller.getOdlMdsalImpl() != null) {
            controller.getOdlMdsalImpl().setPacketInCallback(this);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Clear the PACKET_IN callback
     * @return boolean true indicates success
     */
    public boolean clearCallback() {
        controller.getOdlMdsalImpl().setPacketInCallback(null);
        return true;
    }



    private SdnDeleteMeterReply doSdnDeleteMeter(SdnDeleteMeterRequest req) {
        SdnDeleteMeterReply rep = new SdnDeleteMeterReply();
        rep.setError(false);
        return rep;
    }

    private SdnDeleteForwardReply doSdnDeleteForward(SdnDeleteForwardRequest req) {
        SdnDeleteForwardReply rep = new SdnDeleteForwardReply();
        rep.setError(false);
        return rep;
    }

    private SdnForwardReply doSdnForward(SdnForwardRequest req) {
        SdnForwardReply rep = new SdnForwardReply();
        rep.setError(false);
        return rep;
    }

    private SdnForwardReply doSdnForwardToController(SdnForwardToControllerRequest req) {
        SdnForwardReply rep = new SdnForwardReply();
        rep.setError(false);
        return rep;
    }

    private SdnInstallMeterReply doSdnInstallMeter(SdnInstallMeterRequest req) {
        SdnInstallMeterReply rep = new SdnInstallMeterReply();
        rep.setError(false);
        return rep;
    }

    private SdnTransmitPacketReply doSdnTransmitPacket(SdnTransmitPacketRequest req) {
        SdnTransmitPacketReply rep = new SdnTransmitPacketReply();
        rep.setError(false);
        return rep;
    }

    public void run() {

        // Loop forever
        while (true) {
            try {

                // Get the next message
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                // Get the properties for the request message, set up the properties
                // for the reply message.
                BasicProperties props = delivery.getProperties();
                BasicProperties replyProps = new BasicProperties.Builder().
                        correlationId(props.getCorrelationId()).build();

                // Placeholder for a reply, if we have one to send
                String message2 = "";

                try {
                    // Parse the body.  Get the string containing the JSON data.
                    String message = new String(delivery.getBody(), "UTF-8");

                    // Figure out the message type as a string so we know how to parse it.
                    SdnRequest req = mapper.readValue(message, SdnRequest.class);
                    SdnReply rep = null;

                    // Dispatch to command handlers depending on the type of message
                    //
                    // Handle ping requests here since they're pretty trivial
                    if (req.getRequestType().equals(SdnPingRequest.TYPE)) {
                        SdnPingRequest pingReq = mapper.readValue(message, SdnPingRequest.class);
                        SdnPingReply pingRep = new SdnPingReply();
                        pingRep.setError(false);
                        pingRep.setPayload(pingReq.getPayload());
                        rep = pingRep;
                    }

                    // Other request types dispatch to handler functions in this module.
                    // Place in alphabetical order.
                    else if (req.getRequestType().equals(SdnDeleteMeterRequest.TYPE)) {
                        SdnDeleteMeterRequest meterReq = mapper.readValue(message, SdnDeleteMeterRequest.class);
                        rep = doSdnDeleteMeter(meterReq);
                    }
                    else if (req.getRequestType().equals(SdnDeleteForwardRequest.TYPE)) {
                        SdnDeleteForwardRequest forwardReq = mapper.readValue(message, SdnDeleteForwardRequest.class);
                        rep = doSdnDeleteForward(forwardReq);
                    }
                    else if (req.getRequestType().equals(SdnForwardRequest.TYPE)) {
                        SdnForwardRequest forwardReq = mapper.readValue(message, SdnForwardRequest.class);
                        rep = doSdnForward(forwardReq);
                    }
                    else if (req.getRequestType().equals(SdnForwardToControllerRequest.TYPE)) {
                        SdnForwardToControllerRequest flowReq = mapper.readValue(message, SdnForwardToControllerRequest.class);
                        rep = doSdnForwardToController(flowReq);
                    }
                    else if (req.getRequestType().equals(SdnInstallMeterRequest.TYPE)) {
                        SdnInstallMeterRequest meterReq = mapper.readValue(message,SdnInstallMeterRequest.class);
                        rep = doSdnInstallMeter(meterReq);
                    }
                    else if (req.getRequestType().equals(SdnTransmitPacketRequest.TYPE)) {
                        SdnTransmitPacketRequest packetReq = mapper.readValue(message,SdnTransmitPacketRequest.class);
                        rep = doSdnTransmitPacket(packetReq);
                    }
                    else {
                        // Unknown message.
                        rep = new SdnReply();
                        rep.setError(true);
                        rep.setErrorMessage("Unknown message type");
                    }

                    // If there's a response, then get it in JSON representation
                    if (rep != null) {
                        message2 = mapper.writeValueAsString(rep);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // If we have a reply to send, then great, send it and ACK the old message
                    if (message2 != null) {
                        channel.basicPublish("", props.getReplyTo(), replyProps, message2.getBytes("UTF-8"));
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // We can get here if there was a problem reading a message from the AMPQ service.
                // Sleep for a second to avoid us busy-waiting in this loop.
                try {
                    Thread.sleep(1000);
                }
                catch (Exception e2) {
                }
            }
        }
    }

    /**
     * ODL callback for PacketReceived notification
     * Note:  This is the only part of this module that knows anything about ODL.  It'd be nice to be able
     * to get rid of it.  We could in theory push the ODL-specific bits into a method in core.Controller,
     * and then use a more abstract callback interface.  It's not clear if this is worth the effort though.
     * @param notification PACKET_IN message
     */
    public void callback(PacketReceived notification) {
        SdnReceivePacketReply rep = new SdnReceivePacketReply();

        try {
            // Compose this data structure.  Much of this processing was originally derived from Python
            // callback code.
            NodeConnectorRef nodeConnectorRef = notification.getIngress();
            String nodeId = nodeConnectorRef.getValue().firstIdentifierOf(Node.class).firstKeyOf(Node.class, NodeKey.class).getId().getValue();
            String nodeConnectorId = nodeConnectorRef.getValue().firstIdentifierOf(NodeConnector.class).firstKeyOf(NodeConnector.class, NodeConnectorKey.class).getId().getValue();

            // The NodeId in ODL is the literal string "openflow:" followed by a decimal representation
            // of the DPID.  We want just the DPID portion as an array of bytes.
            String[] nodeIdComponents = nodeId.split(":", 2);
            Long dpidNumeric = 0L;
            assert (nodeIdComponents[0].equals("openflow"));
            String dpidString = nodeIdComponents[1];
            dpidNumeric = Long.parseLong(dpidString);
            rep.dpid = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(dpidNumeric).array();
            rep.inPort = nodeConnectorId;
            rep.payload = notification.getPayload();

            String dpidHexString = String.format("[%02x, %02x, %02x, %02x, %02x, %02x, %02x, %02x]",
                    rep.dpid[0], rep.dpid[1], rep.dpid[2], rep.dpid[3], rep.dpid[4], rep.dpid[5], rep.dpid[6], rep.dpid[7]);
            logger.info("node " + nodeId + ", dpid " + dpidHexString + ", nodeConnector " + nodeConnectorId + " payload bytes ?");

            // JSON encode
            String callbackMessage = mapper.writeValueAsString(rep);

            // What message properties to set here?
            BasicProperties props = new BasicProperties.Builder().build();

            // Send it.
            channel.basicPublish("", "packet-receive", props, callbackMessage.getBytes("UTF-8"));

        }
        catch (Exception e) {
            // If we run into a problem dealing with the PacketReceived (e.g. parsing), note that
            // we won't be emitting a notification to the client side.
            e.printStackTrace();
            return;
        }
    }
}
