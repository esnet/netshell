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

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by davidhua on 7/8/14.
 */

/**
 * Writes each message received to a file with a time stamp.
 */

public class WriteMsgToFile {
	String userFile = "";
	String messageFile = "/Messages.txt";

	public WriteMsgToFile(String user) {
		userFile = user;
	}

	public void writeMsg(String message, String user) throws IOException {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

		File msgFile = new File(this.userFile + messageFile);

		if (!msgFile.exists()) {
			if (!msgFile.createNewFile()) {
				throw new RuntimeException("Cannot create " + this.userFile);
			}
		}

		FileWriter fw = new FileWriter(msgFile.getAbsoluteFile(), true);
		BufferedWriter bw = new BufferedWriter(fw);
		String write = sdf.format(cal.getTime()) + " : [x] Received \"" + message + "\" from " + user + "\n";
		bw.write(write);
		bw.close();

	}
}
