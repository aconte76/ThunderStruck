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

import eu.thunderstruck.rest.server.MediaType;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.session.SessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServiceSessionHandler extends SessionHandler {
	private final static Logger log = LoggerFactory.getLogger(ServiceSessionHandler.class);

	private Filter serviceFilter;

	public ServiceSessionHandler() {
		this.serviceFilter = new RestServiceFilter();
	}

	@Override
	public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			// wrap the request so 'getInputStream()' can be called multiple times
			serviceFilter.doFilter(request, response, null);
			baseRequest.setHandled(true);
		} catch (Exception ex) {
			response.setHeader(MediaType.TEXT_PLAIN.toString(), null);
			// TODO: Ritornare gli errori qui
			response.getOutputStream().print(ex.toString());
			log.error("Error handling request", ex);
			baseRequest.setHandled(false);
		}
	}

	public RestServiceFilter getFilter() {
		return (RestServiceFilter) this.serviceFilter;
	}

}
