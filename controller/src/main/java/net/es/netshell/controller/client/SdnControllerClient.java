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

package net.es.netshell.controller.client;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import net.es.netshell.controller.intf.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Implementation of the client-side interface to the SDN controller
 */
public class SdnControllerClient implements Runnable, AutoCloseable {

    // Logging
    static final private Logger logger = LoggerFactory.getLogger(SdnControllerClient.class);

    private ConnectionFactory factory;
    private Connection connection;
    private Channel replyChannel;
    private String replyQueueName;
    private QueueingConsumer replyConsumer;

    private Channel packetInChannel;
    private QueueingConsumer packetInConsumer;

    private ObjectMapper mapper;
    private Thread packetInThread;

    private SdnControllerClientCallback callback;
    void setCallback(SdnControllerClientCallback c) {
        this.callback = c;
    }
    void clearCallback() {
        this.callback = null;
    }

    class SdnControllerClientFlowHandleImpl implements SdnControllerClientFlowHandle {
        public byte[] dpid;
        public short tableId;
        public String flowId;
    }

    public SdnControllerClient() {
        try {
            // Get a connection to the AMPQ broker (RabbitMQ server)
            factory = new ConnectionFactory();
            factory.setHost("localhost");
            connection = factory.newConnection();
            replyChannel = connection.createChannel();

            // Set up to receive replies from the SDN controller when we get them
            replyQueueName = replyChannel.queueDeclare().getQueue();
            replyConsumer = new QueueingConsumer(replyChannel);
            replyChannel.basicConsume(replyQueueName, true, replyConsumer);

            // JSON parser setup
            mapper = new ObjectMapper();

            logger.info(SdnControllerClient.class.getName() + " ready");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() throws Exception {
        clearCallback();
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    /**
     * Common code for doing a request-reply sequence with SDN Controller.
     * Use this for various command types.
     * @param req Request object
     * @param clazz Return object class
     * @param <T> Return object class
     * @return Return object
     * @throws Exception
     */
    public <T> T SdnReqRep(SdnRequest req, Class<T> clazz) throws Exception {
        String corrId = UUID.randomUUID().toString();

        BasicProperties props = new BasicProperties.Builder().
            correlationId(corrId).replyTo(replyQueueName).build();

        // Maybe we can factor out the next few lines of code because they
        // should be common to all request types.  Convert message object to JSON.
        String message = mapper.writeValueAsString(req);
        String response = null;

        // Send it.
        replyChannel.basicPublish("", Common.controllerRequestQueueName, props, message.getBytes("UTF-8"));

        // Wait for reply.
        while (true) {
            QueueingConsumer.Delivery delivery = replyConsumer.nextDelivery(); // timeout?
            // No explicit acknowledgement, this is an auto-acknowledge channel

            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response = new String(delivery.getBody(), "UTF-8");
                break;
            }
        }

        // Got response, now convert that to an object.
        return mapper.readValue(response, clazz);
    }

    /**
     * Ping the SDN controller to make sure it's alive
     * @param payload ping payload
     * @return boolean to indicate success
     */
    public boolean SdnPing(String payload) {
        SdnPingRequest req = new SdnPingRequest();
        req.setPayload(payload);

        try {
            SdnPingReply rep = SdnReqRep(req, SdnPingReply.class);

            // Make sure the inbound payload equals the outbound payload
            if (rep.isError()) {
                logger.error("Error: " + rep.getErrorMessage());
                return false;
            }
            if (rep.getPayload().equals(payload)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Install a meter
     *
     * XXX There should be a better way to specify a switch than its DPID.
     */
    public boolean SdnInstallMeter(byte [] dpid, long meter, long cr, long cbs,
                                   long er, long ebs) {
        SdnInstallMeterRequest req = new SdnInstallMeterRequest();
        req.setDpid(dpid);
        req.setMeter(meter);
        req.setCr(cr);
        req.setCbs(cbs);
        req.setEr(er);
        req.setEbs(ebs);

        try {
            SdnInstallMeterReply rep = SdnReqRep(req, SdnInstallMeterReply.class);

            if (rep.isError()) {
                logger.error(rep.getErrorMessage());
                return false;
            }
            else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Delete a meter
     */
    public boolean SdnDeleteMeter(byte[] dpid, long meter) {
        SdnDeleteMeterRequest req = new SdnDeleteMeterRequest();
        req.setDpid(dpid);
        req.setMeter(meter);

        try {
            SdnDeleteMeterReply rep = SdnReqRep(req, SdnDeleteMeterReply.class);

            if (rep.isError()) {
                logger.error(rep.getErrorMessage());
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public SdnControllerClientFlowHandle SdnInstallForward(byte [] dpid, int priority, BigInteger cookie,
                                     String inPort, int vlan1, String srcMac, String dstMac,
                                     SdnControllerClientL2Forward [] outputs,
                                     int pcp, int queue, int meter) {
        SdnForwardRequest req = new SdnForwardRequest();
        req.setDpid(dpid);
        req.setPriority(priority);
        req.setC(cookie);
        req.setInPort(inPort);
        req.setVlan1(vlan1);
        req.setSrcMac1(srcMac);
        req.setDstMac1(dstMac);

        req.outputs = new SdnForwardRequest.L2TranslationOutput[outputs.length];
        for (int i = 0; i < outputs.length; i++) {
            req.outputs[i] = new SdnForwardRequest.L2TranslationOutput();
            req.outputs[i].outPort = outputs[i].outPort;
            req.outputs[i].vlan =  outputs[i].vlan;
            req.outputs[i].dstMac = outputs[i].dstMac;
        }

        req.setPcp(pcp);
        req.setQueue(queue);
        req.setMeter(meter);

        try {
            SdnForwardReply rep = SdnReqRep(req, SdnForwardReply.class);

            if (rep.isError()) {
                logger.error(rep.getErrorMessage());
                return null;
            }
            else {
                // Set dpid, table, flowid
                SdnControllerClientFlowHandleImpl fhi = new SdnControllerClientFlowHandleImpl();
                fhi.dpid = rep.getDpid();
                fhi.tableId = rep.getTableId();
                fhi.flowId = rep.getFlowId();
                return fhi;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public SdnControllerClientFlowHandle SdnInstallForward1(byte [] dpid, int priority, BigInteger cookie,
                                                            String inPort, int vlan1, String srcMac1, String dstMac1,
                                                            String outPort, int vlan2, String dstMac2,
                                                            int pcp, int queue, int meter) {

        // Special case for Layer 2 forwarding to one output port only.
        SdnControllerClientL2Forward [] output1 = new SdnControllerClientL2Forward[1];

        output1[0] = new SdnControllerClientL2Forward();
        output1[0].outPort = outPort;
        output1[0].vlan = vlan2;
        output1[0].dstMac = dstMac2;

        return SdnInstallForward(dpid, priority, cookie, inPort, vlan1, srcMac1, dstMac1, output1, pcp, queue, meter);
    }

    public SdnControllerClientFlowHandle SdnInstallForwardToController(byte [] dpid, int priority, BigInteger cookie,
                                                           String inPort, int vlan1, String srcMac, String dstMac) {
        SdnForwardToControllerRequest req = new SdnForwardToControllerRequest();
        req.setDpid(dpid);
        req.setPriority(priority);
        req.setC(cookie);
        req.setInPort(inPort);
        req.setVlan1(vlan1);
        req.setSrcMac1(srcMac);
        req.setDstMac1(dstMac);

        try {
            SdnForwardReply rep = SdnReqRep(req, SdnForwardReply.class);

            if (rep.isError()) {
                logger.error(rep.getErrorMessage());
                return null;
            }
            else {
                // Set dpid, table, flowid
                SdnControllerClientFlowHandleImpl fhi = new SdnControllerClientFlowHandleImpl();
                fhi.dpid = rep.getDpid();
                fhi.tableId = rep.getTableId();
                fhi.flowId = rep.getFlowId();
                return fhi;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean SdnDeleteForward(SdnControllerClientFlowHandle fh) {
        SdnControllerClientFlowHandleImpl fhi = (SdnControllerClientFlowHandleImpl) fh;

        SdnDeleteForwardRequest req = new SdnDeleteForwardRequest();
        req.setDpid(fhi.dpid);
        req.setTableId(fhi.tableId);
        req.setFlowId(fhi.flowId);

        try {
            SdnDeleteForwardReply rep = SdnReqRep(req, SdnDeleteForwardReply.class);

            if (rep.isError()) {
                logger.error(rep.getErrorMessage());
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    /**
     * Send a PACKET_OUT
     */
    public boolean SdnTransmitPacket(byte [] dpid, String outPort, byte [] payload) {
        SdnTransmitPacketRequest req = new SdnTransmitPacketRequest();
        req.setDpid(dpid);
        req.setOutPort(outPort);
        req.setPayload(payload);

        try {
            SdnTransmitPacketReply rep = SdnReqRep(req, SdnTransmitPacketReply.class);

            if (rep.isError()) {
                logger.error(rep.getErrorMessage());
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    /**
     * Listen for SdnReceivePacket messages, corresponding to PACKET_IN messages
     * received at the controller.
     */
    public void run() {
        try {
            packetInChannel = connection.createChannel();
            // Create the PACKET_IN queue, non-durable, non-exclusive, non-auto-delete, no other arguments
            packetInChannel.queueDeclare(Common.receivePacketReplyQueueName, false, false, false, null);
            packetInChannel.basicQos(1);
            // Set up consumer to read from this channel
            packetInConsumer = new QueueingConsumer(packetInChannel);
            packetInChannel.basicConsume(Common.receivePacketReplyQueueName, false, packetInConsumer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                // Grab the next PACKET_IN
                QueueingConsumer.Delivery delivery = packetInConsumer.nextDelivery();

                // Get the properties for the request message, set up the properties
                // for the reply message.
                BasicProperties props = delivery.getProperties();
                BasicProperties replyProps = new BasicProperties.Builder().
                        correlationId(props.getCorrelationId()).build();

                try {
                    // Parse the body.  Get the string containing the JSON data.
                    String message = new String(delivery.getBody(), "UTF-8");

                    // Figure out the message type as a string so we know how to parse it.
                    SdnReply rep = mapper.readValue(message, SdnReply.class);

                    if (rep.getReplyType().equals(SdnReceivePacketReply.TYPE)) {
                        // Do PACKET_IN processing here.  Log, and invoke callback if it's
                        // been configured.
                        logger.info("Got PACKET_IN");
                        SdnReceivePacketReply packetIn = mapper.readValue(message, SdnReceivePacketReply.class);
                        if (callback != null) {
                            callback.packetInCallback(packetIn.getDpid(), packetIn.getInPort(), packetIn.getPayload());
                        }
                    }
                    else {
                        // Unknown message.
                        logger.error("Unknown message when PACKET_IN expected");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    // ACK the old message
                    packetInChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
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

}
