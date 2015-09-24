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

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ts.rest.server.MediaType;
import org.eclipse.jetty.server.Response;

// JSON Rendering Helper
public class JsonUtils {

	private static ObjectMapper mapper = new ObjectMapper();

	// will never instantiate
	private JsonUtils() {
	}

	public static String toJson(Object object, Response response) throws Exception {
//		response.setContentType("application/json");
		response.setContentType(MediaType.APPLICATION_JSON.toString());

		// each conversion needs a fresh instance of GSon because performances!!!
		// return new Gson().toJson(object);

		// best results with Jackson
		response.getOutputStream().print(mapper.writeValueAsString(object));
		return null;
	}

	public static TransformerHandler toJson() {
		return JsonUtils::toJson;
	}
}