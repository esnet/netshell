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

import java.util.UUID;

/**
 * Created by bmah on 1/8/16.
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

                // Placeholder for a reply, if we have one to send
                String message2 = "";

                try {
                    // Parse the body.  Get the string containing the JSON data.
                    String message = new String(delivery.getBody(), "UTF-8");

                    // Figure out the message type as a string so we know how to parse it.
                    SdnRequest req = mapper.readValue(message, SdnRequest.class);
                    SdnReply rep = null;

                    if (req.getRequestType().equals(SdnReceivePacketReply.TYPE)) {
                        // XXX Do PACKET_IN processing here.  We should really have a callback
                        // of some sort that the user application registers.
                        System.out.println("Got PACKET_IN");
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
                }
                finally {
                    // If we have a reply to send, then great, send it and ACK the old message
                    if (message2 != null) {
                        packetInChannel.basicPublish("", props.getReplyTo(), replyProps, message2.getBytes("UTF-8"));
                    }
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
