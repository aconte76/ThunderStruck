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

import eu.ts.rest.server.filter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public abstract class RestServer {
	private final static Logger log = LoggerFactory.getLogger(RestServer.class);

	private JettyServer jettyServer = null;
	protected ServiceDescriptor serviceDescriptor;

	protected String modulePrefix;
	protected final int port;


	public RestServer(final int port) {
		this.port = port;
		if (jettyServer == null) {
			jettyServer = new JettyServer(port);
		}
	}

	public RestServer(final int port, final int minThreads, final int maxThreads, final int threadIdleTimeoutMillis) {
		this.port = port;
		if (jettyServer == null) {
			jettyServer = new JettyServer(port, minThreads, maxThreads, threadIdleTimeoutMillis);
		}
	}

	public void start() throws Exception {
		check();
		jettyServer.start(false);
	}

	public void start(final boolean debugMode) throws Exception {
		check();
		jettyServer.start(debugMode);
		if (debugMode) {
			jettyServer.getSessionHandler().getFilter().printOut();
		}
	}

	public ServiceDescriptor getServiceDescriptor() {
		return serviceDescriptor;
	}

	public void setServiceDescriptor(ServiceDescriptor serviceDescriptor) {
		this.serviceDescriptor = serviceDescriptor;
	}

	//////////////////////////////////////////////////
	// Regular Response Routes
	//////////////////////////////////////////////////

	// Map the route for HTTP GET requests
	public void get(final String path, final RouteHandler route) {
		addRoute(HttpMethod.GET, wrap(path, route));
	}

	// Map the route for HTTP POST requests
	public void post(final String path, final RouteHandler route) {
		addRoute(HttpMethod.POST, wrap(path, route));
	}

	// Map the route for HTTP PUT requests
	public void put(final String path, final RouteHandler route) {
		addRoute(HttpMethod.PUT, wrap(path, route));
	}

	// Map the route for HTTP PUT requests
	public void delete(final String path, final RouteHandler route) {
		addRoute(HttpMethod.DELETE, wrap(path, route));
	}

	//////////////////////////////////////////////////
	// Transformer Response Routes
	//////////////////////////////////////////////////

	// Map the route for HTTP POST requests (CREATE)
	public void post(final String path, final RouteHandler route, final TransformerHandler transformer) {
		addRoute(HttpMethod.POST, RouteTransformer.create(path, route, transformer));
	}

	// Map the route for HTTP GET requests (READ)
	public void get(final String path, final RouteHandler route, final TransformerHandler transformer) {
		addRoute(HttpMethod.GET, RouteTransformer.create(path, route, transformer));
	}

	// Map the route for HTTP PUT requests (UPDATES)
	public void put(final String path, final RouteHandler route, final TransformerHandler transformer) {
		addRoute(HttpMethod.PUT, RouteTransformer.create(path, route, transformer));
	}

	// Map the route for HTTP DELETE requests (DELETE)
	public void delete(final String path, final RouteHandler route, final TransformerHandler transformer) {
		addRoute(HttpMethod.DELETE, RouteTransformer.create(path, route, transformer));
	}


	//////////////////////////////////////////////////
	// Filter Response Routes
	//////////////////////////////////////////////////

	public void before(final FilterHandler filter) {
		addFilter(HttpMethod.BEFORE, filter);
	}

	// Maps a filter to be executed after any matching routes
	public void after(final FilterHandler filter) {
		addFilter(HttpMethod.AFTER, filter);
	}


	//////////////////////////////////////////////////
	// Wrappers
	//////////////////////////////////////////////////

	// Wraps the route in AbstractRoute
	protected AbstractRoute wrap(final String path, final RouteHandler route) {
		return wrap(path, MediaType.EVERYTHING, route);
	}

	// Wraps the route in AbstractRoute
	protected AbstractRoute wrap(final String path, MediaType acceptType, final RouteHandler route) {
		String versionedPath = String.format("%s%s", (modulePrefix != null) ? modulePrefix : "", path);
		return AbstractRoute.build(versionedPath, route, (acceptType != null) ? acceptType : MediaType.EVERYTHING);
	}

	// Internal Server check
	private void check() {
		if (jettyServer == null) {
			jettyServer = new JettyServer();
		}
	}

	protected void addRoute(final HttpMethod httpMethod, final AbstractRoute route) {
		check();
		RestServiceFilter restServiceFilter = jettyServer.getSessionHandler().getFilter();

		try {
			restServiceFilter.addRoute(httpMethod, route);
		} catch (Exception e) {
			log.error("Error adding route {}:{}", httpMethod, route.getPath(), e);
			System.exit(-1);
		}
	}

	protected void addFilter(final HttpMethod httpMethod, final FilterHandler filter) {
		check();
		RestServiceFilter restServiceFilter = jettyServer.getSessionHandler().getFilter();
		restServiceFilter.addFilter(httpMethod, filter);
	}


	protected Map<HttpMethod, Set<AbstractRoute>> getRoutes() {
		if (jettyServer != null) {
			return jettyServer.getSessionHandler().getFilter().getAllRoutes();
		}
		return null;
	}


	public void setModuleVersionSet(String modulePrefix) {
		this.modulePrefix = modulePrefix;
	}

}

