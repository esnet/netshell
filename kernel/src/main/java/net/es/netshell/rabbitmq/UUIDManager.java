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
import java.util.UUID;

/**
 * Created by davidhua on 7/2/14.
 */

/**
 * Checks to see if a UUID file exists (for the consumer queue), and if not creates one. If it does, read from it and
 * use as the queue name.
 */

public class UUIDManager {
	String queueName = " ";
	String queueFile = " ";
	String queueFileName = "/queueNameFile";

	public UUIDManager(String queue) {
		queueFile = queue;
	}

	public String checkUUID() throws IOException {
		File queueFile = new File(this.queueFile + queueFileName);

		if (!queueFile.exists()) {
			// File does not exist yet, create it.
			if (!queueFile.createNewFile()) {
				// File could not be created.
				throw new RuntimeException("Cannot create " + this.queueFile);
			}
		}
		// If file is empty, create random UUID and write in file.
		// Else, read first line from file. Return UUID.
		if (queueFile.length() == 0) {
			String uuid = UUID.randomUUID().toString();

			FileWriter fw = new FileWriter(queueFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(uuid);
			queueName = uuid;
			bw.close();
		} else {
			FileReader fr = new FileReader(queueFile);
			LineNumberReader ln = new LineNumberReader(fr);
			queueName = ln.readLine();
		}
		return queueName;
	}
}
