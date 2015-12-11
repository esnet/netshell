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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import net.es.netshell.kernel.exec.KernelThread;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by davidhua on 7/15/14.
 */

public class Box {

	HashMap<String, Queue> queueList;

	public Box() {
		this.queueList = new HashMap<String, Queue> ();
	}

	public Queue getQueue(String symLink) {
		return queueList.get(symLink);
	}

	public void registerQueue(Queue register) {
		queueList.put(register.getSymLink(), register);
	}

	public Queue createQueue(String symLink, Channel channel) throws Exception {
		String queueName = UUID.randomUUID().toString();
		channel.queueDeclare(queueName, false, false, true, null);
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, true, symLink, false, false, null, consumer);

		return new Queue(queueName, symLink);
	}

	public String queryQueue(String sendQueue, String token, Channel channel, String symLink) throws Exception {

		// Create random UUID for producer's temporary queue
		String uuid = UUID.randomUUID().toString();
		// Declare this temporary queue and start listening (exclusive queue).
		channel.queueDeclare(uuid, false, true, true, null);
		QueueingConsumer consumer = new QueueingConsumer(channel);

		// Send TOKEN_REQUEST with curent username.
		String message = token + ":QUEUE_QUERY" + ":" +  uuid + ":" + KernelThread.currentKernelThread().getUser().getName() + ":" + symLink;

		channel.basicPublish("", sendQueue, null, message.getBytes());
		// Start consuming to receive token.
		channel.basicConsume(uuid, true, "tokenRequest", false, true, null, consumer);
		QueueingConsumer.Delivery delivery = consumer.nextDelivery();

		// When token is received, store in "token."
		String queueName = new String(delivery.getBody());
		// Delete temporary queue
		channel.queueDelete(uuid);

		return queueName;

	}

	public void responseQuery(String responseQueue, Channel channel, String symLink, String userName) throws Exception {
		Queue queryQueue = getQueue(symLink);
		if (queryQueue.hasPermission(userName)) {
			String message = queryQueue.getQueueName();
			channel.basicPublish("", responseQueue, null, message.getBytes());
		} else {
			throw new Exception("User does not have access to this queue");
		}
	}

}
