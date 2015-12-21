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
import net.es.netshell.kernel.exec.KernelThread;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * Created by davidhua on 7/2/14.
 */


public class Publish {

	protected final String QUEUE_FILE = KernelThread.currentKernelThread().getUser().getHomePath().normalize().toString();
	protected BrokerInfo info;
	protected static String queueName = "";
	protected String token = "";

	public Publish(BrokerInfo info) {
		this.info = info;
	}

	public Publish(BrokerInfo info, String queueName) {
		this.info = info;
		this.queueName = queueName;
	}

	public void Publish(Object object) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutput objOut = new ObjectOutputStream(os);

		objOut.writeObject(object);
		byte byteForm[] = os.toByteArray();
		objOut.close();
		os.close();

		if (queueName == null) {
			queueName = new UUIDManager(QUEUE_FILE).checkUUID();
		}

		ConnectionFactory factory = new SSLConnection(info).createConnection();
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(queueName, false, false, true, null);
		channel.basicPublish("", queueName, null, byteForm);
		System.out.println(" [x] Sent Message");

		channel.close();
		connection.close();

	}
	public void Publish(String[] argv) throws Exception {

		if (queueName == null) {
			queueName = new UUIDManager(QUEUE_FILE).checkUUID();
		}
		String message = new ParseMessage(argv).getMessage();

		ConnectionFactory factory = new SSLConnection(info).createConnection();
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		token = new CreateToken(info, channel, queueName).getToken();

		message = token + ":" + message;

		channel.queueDeclare(queueName, false, false, true, null);
		channel.basicPublish("", queueName, null, message.getBytes());
		System.out.println(" [x] Sent '" + message + "'");

		channel.close();
		connection.close();
	}

}
