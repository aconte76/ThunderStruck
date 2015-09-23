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
import org.eclipse.jetty.server.Response;

public abstract class RouteTransformer extends AbstractRoute {

	protected RouteTransformer(String path, MediaType acceptType) {
		super(path, acceptType);
	}

	public static RouteTransformer create(
			final String path,
			final RouteHandler route,
			final TransformerHandler transformer
	) {
		return create(path, route, transformer, MediaType.EVERYTHING);
	}


	public static RouteTransformer create(
			final String path,
			final RouteHandler route,
			final TransformerHandler transformer,
			final MediaType acceptType
	) {
		return new RouteTransformer(path, acceptType) {
			@Override
			public Object render(Object model, Response response) throws Exception {
				return transformer.transform(model, response);
			}

			@Override
			public Object handle(ServiceRequest request, Response response) throws Exception {
				return route.handle(request, response);
			}
		};
	}

	public abstract Object render(Object model, Response response) throws Exception;

}
