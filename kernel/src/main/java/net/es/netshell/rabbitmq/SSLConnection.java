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
import javax.net.ssl.*;
import java.security.*;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Created by davidhua on 7/2/14.
 */

/**
 * Creates a SSL connection from RabbitMQ broker to producer/consumer
 */
public class SSLConnection {

	private String host;
	private String user;
	private String password;
	private int port;
	private boolean ssl = false;
	private final String TRUSTPASS = "123456";
	private final String KEYPASS = "MySecretPassword";
	private final String KEYCERT = "/Users/davidhua/Desktop/ssl/client/keycert.p12"; // Replace with location of key cert
	private final String KEYSTORE = "/Users/davidhua/Desktop/ssl/rabbitstore";

	public SSLConnection(BrokerInfo info) {
		host = info.getHost();
		user = info.getUser();
		password = info.getPassword();
		port = info.getPort();
		ssl = info.getSSL();
	}

	public ConnectionFactory createConnection() throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		factory.setUsername(user);
		factory.setPassword(password);
		factory.setPort(port);

		if (ssl) {
			char[] keyPassphrase = KEYPASS.toCharArray();
			KeyStore ks = KeyStore.getInstance("PKCS12");
			ks.load(new FileInputStream(KEYCERT), keyPassphrase);

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, keyPassphrase);

			char[] trustPassphrase = TRUSTPASS.toCharArray();
			KeyStore tks = KeyStore.getInstance("JKS");
			tks.load(new FileInputStream(KEYSTORE), trustPassphrase);

			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(tks);

			SSLContext c = SSLContext.getInstance("SSLv3");
			c.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

			factory.useSslProtocol(c);
		}
		return factory;
	}
}
