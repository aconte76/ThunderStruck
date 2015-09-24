/*==============================================================================
 Copyright (C) 2015. Antonio Conte

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 =============================================================================*/
package eu.ts.rest.server;


import eu.ts.rest.server.filter.ServiceSessionHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;

public class JettyServer implements AutoCloseable {
	private final static Logger log = LoggerFactory.getLogger(JettyServer.class);

	protected static final String SERVER_DEFAULT_ADDRESS = "0.0.0.0";
	protected static final int SERVER_DEFAULT_PORT = 8080;
	protected static final int SERVER_MIN_THREADS = -1;
	protected static final int SERVER_MAX_THREADS = -1;
	protected static final int SERVER_THREADS_IDLE_TIMEOUT = -1;

	private Server jettyServerInstance;
	private ServerConnector connector;
	private final Handler sessionHandler;


	// Default Constructor
	public JettyServer() {
		this(SERVER_DEFAULT_PORT);
	}

	// Real Constructor
	public JettyServer(int port) {
		this(port, SERVER_MIN_THREADS, SERVER_MAX_THREADS, SERVER_THREADS_IDLE_TIMEOUT);
	}

	// Real Constructor
	public JettyServer(int port, int minThreads, int maxThreads, int threadIdleTimeoutMillis) {
		this.sessionHandler = new ServiceSessionHandler();
		System.setProperty("org.mortbay.log.class", "server.JettyLogger");
		ignite(SERVER_DEFAULT_ADDRESS, port, minThreads, maxThreads, threadIdleTimeoutMillis);
	}


	private void ignite(String host, int port, int minThreads, int maxThreads, int threadIdleTimeoutMillis) {
		int portToBind = port;
		if (portToBind == 0) {
			try (ServerSocket s = new ServerSocket(0)) {
				portToBind = s.getLocalPort();
			} catch (IOException e) {
				log.error("Could not get first available port (port set to 0), using default: {}", SERVER_DEFAULT_PORT);
				portToBind = SERVER_DEFAULT_PORT;
			}
		}

		jettyServerInstance = createServer(maxThreads, minThreads, threadIdleTimeoutMillis);
		connector = new ServerConnector(jettyServerInstance);

		connector.setHost(host);
		connector.setPort(portToBind);

		jettyServerInstance = connector.getServer();
		jettyServerInstance.setConnectors(new Connector[]{connector});

		// Handle static file routes
		jettyServerInstance.setHandler(sessionHandler);

//		try {
//			log.info("*** {} has ignited...", SERVER_NAME);
//			log.info(">> Listening on {}:{}", host, portToBind);
//
//			jettyServerInstance.start();
//		} catch (Exception e) {
//			log.error("*** {} ignition failed", SERVER_NAME, e);
//			System.exit(-1);
//		}
	}


	// Creates an ordinary Jetty connector
	private static Server createServer(int maxThreads, int minThreads, int threadTimeoutMillis) {
		if (maxThreads > 0) {
			int max = (maxThreads > 0) ? maxThreads : 800;
			int min = (minThreads > 0) ? minThreads : 8;
			int idleTimeout = (threadTimeoutMillis > 0) ? threadTimeoutMillis : 60000;
			return new Server(new QueuedThreadPool(max, min, idleTimeout));
		} else {
			return new Server();
		}
	}

	public ServiceSessionHandler getSessionHandler() {
		return (ServiceSessionHandler) sessionHandler;
	}


	public void start(final boolean debugMode) throws Exception {
		if (jettyServerInstance != null && !jettyServerInstance.isStarted()) {
			// *** DEBUG: some timeout to make debugging easier
			if (debugMode) {
				log.info(">> DEBUG MODE: ON");
				connector.setIdleTimeout(TimeUnit.HOURS.toMillis(1));
				connector.setSoLingerTime(-1);
			}
			jettyServerInstance.start();
		}
	}

	@Override
	public void close() throws Exception {
		// Jetty Instance Stop
		if (jettyServerInstance != null && jettyServerInstance.isStarted()) {
			jettyServerInstance.stop();
		}
	}
}
