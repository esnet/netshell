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
import java.util.UUID;

/**
 * Created by davidhua on 7/3/14.
 */

/**
 * Processes token requests by creating a random UUID. This will then be associated with the user in a hashmap with the consumer.
 */
public class ProcessTokenRequest {

	public String queue;
	public String userID;
	public Channel channel;

	public ProcessTokenRequest(String message, Channel channel) {
		String[] splitMessage = message.split(":");
		this.queue = splitMessage[1];
		this.userID = splitMessage[2];
		this.channel = channel;
	}

	public String[] sendToken() throws Exception {
		String token = UUID.randomUUID().toString();
		channel.basicPublish("", queue, null, token.getBytes());
		return new String[] {token, userID};
	}
}
