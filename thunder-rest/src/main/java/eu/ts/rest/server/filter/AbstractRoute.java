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
package eu.ts.rest.server.filter;


import eu.ts.rest.server.MediaType;
import eu.ts.rest.server.filter.RouteAnalyzer.Piece;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.jetty.server.Response;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public abstract class AbstractRoute implements RouteHandler {

	protected final String path;
	protected final MediaType acceptType;

	protected final String matchString;
	protected final Pattern matchPattern;
	protected final Piece[] routePieces;


	@Data
	@AllArgsConstructor
	public class RouteDescriptor implements Comparable<RouteDescriptor> {
		private String route;
		private String matchString;
		private String method;
		private URL url;

		@Override
		public int compareTo(RouteDescriptor o) {
			return this.toString().compareTo(o.toString());
		}
	}

	// Constructor
	protected AbstractRoute(final String path, final MediaType acceptType) {
		this.path = path;
		this.acceptType = acceptType;
		this.matchString = RouteAnalyzer.getWildcardPatternRoute(path);
		this.matchPattern = compile(this.matchString);
		this.routePieces = RouteAnalyzer.getRoutePieces(path);
	}


	public static AbstractRoute build(final String path, final RouteHandler route, final MediaType acceptType) {
		return new AbstractRoute(path, acceptType) {
			@Override
			public Object handle(ServiceRequest request, Response response) throws Exception {
				return route.handle(request, response);
			}
		};
	}

	// request handler on this route mathPattern
	public abstract Object handle(ServiceRequest request, Response response) throws Exception;


	public MediaType getAcceptType() {
		return acceptType;
	}

	public String getPath() {
		return this.path;
	}

	public boolean matchesPattern(String path) {
		return matchPattern.matcher(path).matches();
	}

	public Piece[] getRoutePieces() {
		return routePieces;
	}

	public RouteDescriptor getRouteDescriptor(HttpMethod httpMethod, URL baseUrl) throws MalformedURLException {
		return new RouteDescriptor(path, matchString, httpMethod.name(), new URL(baseUrl, path));
	}
}
