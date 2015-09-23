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
package eu.thunderstruck.rest.test;

import eu.thunderstruck.rest.RestModule;
import eu.thunderstruck.rest.RestService;

import static eu.thunderstruck.rest.server.ModuleVersion.version;


public class ServiceTest extends RestService {

	public ServiceTest() throws Exception {
		super("TEST-SERVICE", "1.0");
		initialize();
	}


	public void initialize() throws Exception {

		// NO MODULE
		get("/hello", (request, response) -> "No Module HelloWorld!");

		// *************************** Versioned MODULE-1 ***************************
		module(new RestModule(version(1, 0), () -> {
			get("/hello2", (request, response) -> "Module HelloWorld!!!");
			get("/groups/:groupId/getList", (request, response) -> "requested: " + request.getParameter("groupId"));
			put("/user/:USER_ID", (request, response) -> "hai richiesto il put di: " + request.getParameter("USER_ID"));
		}));

	}


	public static void main(String[] args) throws Exception {
		ServiceTest rs = new ServiceTest();
		rs.start(true);
	}

}