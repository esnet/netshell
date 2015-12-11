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
 */
package net.es.netshell.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import net.es.netshell.kernel.exec.KernelThread;
import org.jgrapht.graph.DefaultListenableGraph;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

/**
 * Created by davidhua on 7/2/14.
 */

public class Consume {

	protected BrokerInfo info;
	protected final String QUEUE_FILE = KernelThread.currentKernelThread().getUser().getHomePath().normalize().toString();
	protected  String queueName;

	private HashMap<String, String> permissions = new HashMap<String, String>();

	public Consume(BrokerInfo info) {
		this.info = info;
	}

	public Consume(BrokerInfo info, String queueName) {
		this.info = info;
		this.queueName = queueName;
	}

	public  void consumeMessage() throws Exception {
		if (queueName == null) {
			queueName = new UUIDManager(QUEUE_FILE).checkUUID();
		}
		ConnectionFactory factory = new SSLConnection(info).createConnection();
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(queueName, false, false, true, null);
		System.out.println(" [*] Waiting for messages.");

		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, true, "consumer", false, false, null, consumer);

		//while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			ByteArrayInputStream bais = new ByteArrayInputStream(delivery.getBody());
			ObjectInputStream in = new ObjectInputStream(bais);
			DefaultListenableGraph g = (DefaultListenableGraph) in.readObject();
			System.out.println(" [x] Received Message");

			// GraphViewer view = new GraphViewer(g);
			// view.init();

//			if (message.substring(0,13).equals("TOKEN_REQUEST")) {
//				String[] token = new ProcessTokenRequest(message, channel).sendToken();
//				permissions.put(token[0], token[1]);
//				//String[] messageSplit = message.split(":");
//				//sendToken(messageSplit[1], messageSplit[2], channel);
//			} else {
//				String[] messageSplit = message.split(":", 2);
//				if (permissions.containsKey(messageSplit[0])) {
//					System.out.println(" [x] Received '" + messageSplit[1] + "' from: " + permissions.get(messageSplit[0]));
//				} else {
//					System.out.println(" ERROR: INVALID TOKEN PROVIDED IN MESSAGE");
//				}
//			}
		//}

		channel.queueDelete(queueName);
		channel.close();
		connection.close();
	}
}
