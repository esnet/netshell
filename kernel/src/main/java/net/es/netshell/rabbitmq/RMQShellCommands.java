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

import net.es.netshell.api.TopologyFactory;
import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.shell.annotations.ShellCommand;
import org.jgrapht.graph.DefaultListenableGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.UUID;


/**
 * Created by davidhua on 7/30/14.
 */

public class RMQShellCommands {

	@ShellCommand(name = "GET_TOPOLOGY",
			shortHelp = "Sends topology over RMQ",
			longHelp = "Sends topology over RMQ")
	public static void queueRequest(String[] args, InputStream in, OutputStream out, OutputStream err) throws Exception {
		String host = "summer1.es.net";
		String username = "david2";
		String password = "123";
		int port = 5672;
		boolean ssl = false;

		PrintStream o = new PrintStream(out);
		try {
			Logger logger = LoggerFactory.getLogger(RMQShellCommands.class);

			logger.info("GET_TOPOLOGY", args.length);

			String currentUser = KernelThread.currentKernelThread().getUser().getName().toString();
			String queueName = UUID.randomUUID().toString();
			String sendQueue = args[1];

			o.println(args);
			BrokerInfo info = new BrokerInfo(host, username, password, port, ssl);
			Publish thisPublish = new Publish(info, sendQueue);
			TopologyFactory topology = TopologyFactory.instance();
			DefaultListenableGraph topo = topology.retrieveTopology("localLayer2");
			thisPublish.Publish(topo);
		} catch (Exception e) {
			o.println("ERROR");
		}
	}
}
