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
package eu.thunderstruck.rest.server.filter;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RestServiceFilter implements Filter {
	private static final Logger log = LoggerFactory.getLogger(RestServiceFilter.class);

	private static final Set<AbstractRoute> DEFAULT_EMPTY_ROUTE_SET = new ConcurrentHashSet<>();

	// http://stackoverflow.com/questions/20300490/which-is-faster-in-accessing-elements-from-java-collections
	private Set<AbstractRoute> routeGetSet = new ConcurrentHashSet<>();
	private Set<AbstractRoute> routePostSet = new ConcurrentHashSet<>();
	private Set<AbstractRoute> routePutSet = new ConcurrentHashSet<>();
	private Set<AbstractRoute> routeDeleteSet = new ConcurrentHashSet<>();

	private Set<FilterHandler> filterBeforeSet = new ConcurrentHashSet<>();
	private Set<FilterHandler> filterAfterSet = new ConcurrentHashSet<>();


	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.info("initialized...");
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		ServiceRequest sr = new ServiceRequest((Request) servletRequest);
		Request request = (Request) servletRequest;
		Response response = (Response) servletResponse;
		OutputStream out = servletResponse.getOutputStream();

		try {
			AbstractRoute routeToApply;

			// path cleaner: skip all ending "/"
			String pathFinder = request.getPathInfo();
			if (!pathFinder.equals("/")) {
				while (pathFinder.endsWith("/")) {
					pathFinder = pathFinder.substring(0, pathFinder.length() - 1);
				}
			}

			switch (request.getMethod()) {
				case "GET":
					routeToApply = findRoute(pathFinder, routeGetSet);
					break;
				case "POST":
					routeToApply = findRoute(pathFinder, routePostSet);
					break;
				case "PUT":
					routeToApply = findRoute(pathFinder, routePutSet);
					break;
				case "DELETE":
					routeToApply = findRoute(pathFinder, routeDeleteSet);
					break;
				default:
					log.info("{} not yet implemented", request.getMethod());
					return;
			}


			if (null != routeToApply) {
				// moduleVersion parameters for handler late usage...
				sr.splitPathParameters(routeToApply.getRoutePieces());

				// execute rendering

				// --- apply before filters ---
				if (filterBeforeSet.size() != 0) {
					for (FilterHandler filter : filterBeforeSet) {
						filter.handle(sr, response);
					}
				}

				// Apply proper Route or nested Transformer
				Object model = routeToApply.handle(sr, response);
				if (model != null) {
					if (routeToApply instanceof RouteTransformer) {
						serialize(out, ((RouteTransformer) routeToApply).render(model, response));
					} else {
						serialize(out, model);
					}
				}

				// --- apply after filters ---
				if (filterAfterSet.size() != 0) {
					for (FilterHandler filter : filterAfterSet) {
						filter.handle(sr, response);
					}
				}
			} else {
				log.info("Request not found: {}", ((Request) servletRequest).getPathInfo());
				response.setStatusWithReason(HttpServletResponse.SC_BAD_REQUEST, "route not found");
			}
		} catch (Exception e) {
			throw new ServletException("Unable to manage: " + ((Request) servletRequest).getPathInfo(), e);
		}

	}


	@Override
	public void destroy() {
		log.info("destroyed");
	}


	public static void serialize(OutputStream outputStream, Object element) throws IOException {
		try {
			if (element != null) {
				outputStream.write(element.toString().getBytes("utf-8"));
			}
		} catch (UnsupportedEncodingException e) {
			throw new IOException(e);
		}
	}


	public void addRoute(HttpMethod method, AbstractRoute route) throws Exception {
		// checking for duplicated rows
		Set<AbstractRoute> set = DEFAULT_EMPTY_ROUTE_SET;

		switch (method) {
			case GET:
				set = routeGetSet;
				break;
			case POST:
				set = routePostSet;
				break;
			case PUT:
				set = routePutSet;
				break;
			case DELETE:
				set = routeDeleteSet;
				break;
			default:
				log.info("Unable to manage method: {}... not yet implemented", method);
		}

//		boolean found = set.stream()
//				.filter(x -> route.getPath().equals(x.getPath()))
//				.findFirst()
//				.isPresent();

		for (AbstractRoute foundRoute : set) {
			if (foundRoute.getPath().equals(route.getPath())) {
				throw new Exception(String.format("Unable to add route %s that already exists", route.getPath()));
			}
		}
		set.add(route);
	}


	public void addFilter(HttpMethod method, FilterHandler filter) {
		switch (method) {
			case BEFORE:
				filterBeforeSet.add(filter);
				break;
			case AFTER:
				filterAfterSet.add(filter);
				break;
			default:
				log.info("Unable to manage method: {} for filter: {}", method, filter);
		}
	}


	private AbstractRoute findRoute(String path, Set<AbstractRoute> routeSet) {
		for (AbstractRoute route : routeSet) {
			if (route.matchesPattern(path)) {
				return route;
			}
		}
		return null;
	}


	public Map<HttpMethod, Set<AbstractRoute>> getAllRoutes() {
		Map<HttpMethod, Set<AbstractRoute>> retMap = new HashMap<>();
		if (!routeGetSet.isEmpty()) {
			retMap.put(HttpMethod.GET, routeGetSet);
		}
		if (!routePostSet.isEmpty()) {
			retMap.put(HttpMethod.POST, routePostSet);
		}
		if (!routePutSet.isEmpty()) {
			retMap.put(HttpMethod.PUT, routePutSet);
		}
		if (!routeDeleteSet.isEmpty()) {
			retMap.put(HttpMethod.DELETE, routeDeleteSet);
		}
		return retMap;
	}

	public void printOut() {
		for (AbstractRoute route : routeGetSet) {
			log.info("Route (GET) {}", route.getPath());
		}
		for (AbstractRoute route : routePostSet) {
			log.info("Route (POST) {}", route.getPath());
		}
		for (AbstractRoute route : routePutSet) {
			log.info("Route (PUT) {}", route.getPath());
		}
		for (AbstractRoute route : routeDeleteSet) {
			log.info("Route (DELETE) {}", route.getPath());
		}
	}
}
