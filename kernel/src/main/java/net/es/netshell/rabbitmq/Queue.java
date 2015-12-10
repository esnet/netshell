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

import java.util.List;
import java.util.ArrayList;


/**
 * Created by davidhua on 7/16/14.
 */
public class Queue {

	private String queueName;
	private String symLink;
	private List<String> userAccess;

	public Queue(String queueName, String symLink) {
		this.queueName = queueName;
		this.symLink = symLink;
		this.userAccess = new ArrayList<String> ();
	}

	public Queue(String queueName, String symLink, List<String> userPermissions) {
		this.queueName = queueName;
		this.symLink = symLink;
		this.userAccess = userPermissions;
	}

	public void addUserPermissions(String user) {
		userAccess.add(user);
	}

	public boolean hasPermission(String user) {
		return userAccess.contains(user);
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getSymLink() {
		return symLink;
	}

	public void setSymLink(String symLink) {
		this.symLink = symLink;
	}

}
