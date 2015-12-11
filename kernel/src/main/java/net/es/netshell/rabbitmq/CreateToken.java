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

import java.util.UUID;

/**
 * Created by davidhua on 7/3/14.
 */

/**
 * Creates a token request for the producer, which will be processed with processTokenRequest.
 * This token is then stored in "token."
 */

public class CreateToken {

	BrokerInfo info;
	String token;

	public CreateToken(BrokerInfo info, Channel tokenChannel, String listenerID) throws Exception{
		// Info on data needed to create a connection
		this.info = info;

		// Create random UUID for producer's temporary queue
		String uuid = UUID.randomUUID().toString();
		// Declare this temporary queue and start listening (exclusive queue).
		tokenChannel.queueDeclare(uuid, false, true, true, null);
		QueueingConsumer consumer = new QueueingConsumer(tokenChannel);

		// Send TOKEN_REQUEST with current username.
		String message = "TOKEN_REQUEST" + ":" +  uuid + ":" + KernelThread.currentKernelThread().getUser().getName();

		tokenChannel.basicPublish("", listenerID, null, message.getBytes());
		// Start consuming to receive token.
		tokenChannel.basicConsume(uuid, true, "tokenRequest", false, false, null, consumer);
		QueueingConsumer.Delivery delivery = consumer.nextDelivery();

		// When token is received, store in "token."
		token = new String(delivery.getBody());
		// Delete temporary queue
		tokenChannel.queueDelete(uuid);
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
