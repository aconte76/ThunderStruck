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
package eu.thunderstruck.rest;


import eu.thunderstruck.rest.server.ModuleVersion;
import eu.thunderstruck.rest.server.RestServer;
import eu.thunderstruck.rest.server.ServiceDescriptor;
import eu.thunderstruck.rest.server.VersionedApi;
import eu.thunderstruck.rest.server.filter.AbstractRoute;
import eu.thunderstruck.rest.server.filter.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static eu.thunderstruck.rest.server.filter.JsonUtils.toJson;

public abstract class RestService extends RestServer {
	private final static Logger log = LoggerFactory.getLogger(RestService.class);

	private final static int DEFAULT_SERVICE_PORT = -1;


	// moduleVersion service interfaces from init
	public RestService(final String serviceName, final String serviceVersion) {
		this(serviceName, serviceVersion, DEFAULT_SERVICE_PORT);
	}


	// moduleVersion service interfaces from init
	public RestService(final String serviceName, final String serviceVersion, final int port) {
		super((port == DEFAULT_SERVICE_PORT) ? Integer.parseInt(System.getenv().getOrDefault("PORT", "8080")) : port);

		log.info("Initializing service {}:{}...", serviceName, serviceVersion);

		// configure descriptor
		ServiceDescriptor sd = new ServiceDescriptor();
		sd.setName(serviceName);
		sd.setVersion(serviceVersion);
		sd.setEnvironment(System.getenv());
		sd.setSystemProperties(System.getProperties());
		serviceDescriptor = sd;

		// Starting basic Service Routes for common design
		try {
			commonRoutes();
		} catch (Exception ex) {
			log.error("Unable to initialize {} correctly", serviceDescriptor.getDescription(), ex);
		}
	}


	// Start default real server
	public void start() {
		start(false);
	}

	// Start underlying real server
	public void start(final boolean debugMode) {
		try {
			super.start(debugMode);
		} catch (Exception e) {
			log.info("Unable to start service {}:{}...", serviceDescriptor.getDescription(), e);
		}
	}


	///////////////////////////////////////////////////////////////////////////////
	// Default Service Routes
	///////////////////////////////////////////////////////////////////////////////
	private void commonRoutes() throws Exception {
		// alive test route for service
		get("/", (request, response) -> (serviceDescriptor.getName() + " is alive"));

		// common service descriptor
		get("/descriptor", (request, response) -> serviceDescriptor, toJson());

		// all api documentations
		get("/api-docs", (request, response) -> {
			Map<HttpMethod, Set<AbstractRoute>> routeMap = getRoutes();
			Set<AbstractRoute.RouteDescriptor> docSet = new TreeSet<>();

			for (Map.Entry<HttpMethod, Set<AbstractRoute>> entry : routeMap.entrySet()) {
				for (AbstractRoute route : entry.getValue()) {
					docSet.add(route.getRouteDescriptor(
									entry.getKey(),
									new URL(request.getServletRequest().getRootURL().toString()))
					);
				}
			}
			return docSet;
		}, toJson());
	}


	// moduleVersion service common and module specific interfaces
	public void module(final RestModule restModule) throws Exception {

		// moduleVersion service specific routes
		try {
			if (null != restModule) {
				ModuleVersion version = restModule.getModuleVersion();
				log.info("Register Module: v{} {}", version.getVersion(), restModule.getClass().getName());

				String versionBasePath = String.format("/api/v%d", version.getRelease());

				Set<VersionedApi> versionSet = serviceDescriptor.getVersionedApiSet();
				for (VersionedApi versionedApi : versionSet) {
					if (versionedApi.getBasePath().startsWith(versionBasePath)) {
						throw new Exception(String.format("Module v%s - '%s' : already registered", version.getVersion(), versionBasePath));
					}
				}
				versionSet.add(new VersionedApi(version, versionBasePath));

				setModuleVersionSet(versionBasePath);
				restModule.getModuleHandler().initialize();
				setModuleVersionSet(null);
			}
		} catch (Exception ex) {
			throw new Exception(String.format("Unable to start %s...", serviceDescriptor.getName()), ex);
		}
	}

}

