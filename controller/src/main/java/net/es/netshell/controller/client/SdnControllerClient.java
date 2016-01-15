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
import java.util.UUID;

/**
 * Created by bmah on 1/8/16.
 */
public class SdnControllerClient implements AutoCloseable {

    // Logging
    static final private Logger logger = LoggerFactory.getLogger(SdnControllerClient.class);

    private Connection connection;
    private Channel channel;
    private String replyQueueName;
    private QueueingConsumer consumer;

    private ObjectMapper mapper;

    public SdnControllerClient() {
        try {
            // Get a connection to the AMPQ broker (RabbitMQ server)
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            connection = factory.newConnection();
            channel = connection.createChannel();

            // Set up to receive replies from the SDN controller when we get them
            replyQueueName = channel.queueDeclare().getQueue();
            consumer = new QueueingConsumer(channel);
            channel.basicConsume(replyQueueName, true, consumer);

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
        channel.basicPublish("", Common.controllerRequestQueueName, props, message.getBytes("UTF-8"));

        // Wait for reply.
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery(); // timeout?
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
/*
    public boolean SdnPing(String payload) {
        String corrId = UUID.randomUUID().toString();

        try {
            BasicProperties props = new BasicProperties.Builder().
                        correlationId(corrId).replyTo(replyQueueName).build();

            // Make an SDN ping message
            SdnPingRequest req = new SdnPingRequest();
            req.setPayload(payload);

            // Maybe we can factor out the next few lines of code because they
            // should be common to all request types.  Convert message object to JSON.
            String message = mapper.writeValueAsString(req);
            String response = null;

            // Send it.
            channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));

            // Wait for reply.
            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery(); // timeout?
                if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                    response = new String(delivery.getBody(), "UTF-8");
                    break;
                }
            }

            // Got response, now convert that to an object.
            SdnPingReply rep = mapper.readValue(response, SdnPingReply.class);

            // Make sure the inbound payload equals the outbound payload
            if (rep.getPayload().equals(payload)) {
                return true;
            }
            else {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
*/

}
