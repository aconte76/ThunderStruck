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

import eu.thunderstruck.rest.server.filter.RouteAnalyzer.Piece;
import org.eclipse.jetty.server.Request;

import java.util.Properties;

import static eu.thunderstruck.rest.server.filter.RouteAnalyzer.getRoutePieces;

public final class ServiceRequest {

	private final Properties requestParameters = new Properties();
	private final Request servletRequest;


	public ServiceRequest(Request servletRequest) {
		this.servletRequest = servletRequest;
	}


	// split request path and bind parameters
	public void splitPathParameters(final Piece[] routePieces) {
		Piece[] requestPieces = getRoutePieces(servletRequest.getPathInfo());

		for (int i = 0; i < requestPieces.length; i++) {
			if (i > routePieces.length) {
				break;
			}
			if (routePieces[i].getType() == RouteAnalyzer.Type.PARAMETER) {
				requestParameters.put(routePieces[i].getPiece(), requestPieces[i].getPiece());
			}
		}
	}

	public String getParameter(final String parameter) {
		return requestParameters.getProperty(parameter);
	}

	public String getParameter(final String parameter, final String defaultValue) {
		return requestParameters.getProperty(parameter, defaultValue);
	}

	public Request getServletRequest() {
		return servletRequest;
	}
}