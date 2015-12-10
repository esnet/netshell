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

/**
 * Created by davidhua on 7/3/14.
 */

/**
 *  Parses messages to have spaces between words.
 */

public class ParseMessage {

	String[] args;

	public ParseMessage(String[] args) {
		this.args = args;
	}

	public String getMessage() {
		int length = args.length;
		String msg = "";
		if (length == 0) {
			return msg;
		} else if (length == 1) {
			msg = args[0];
		} else {
			msg = args[0];
			for (int i = 1; i < length; i++) {
				msg = msg + args[i] + " ";
			}
			msg = msg.substring(0, msg.length()-1);
		}
		return msg;
	}
}
