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
package eu.thunderstruck.rest.server;


public enum MediaType {

	EVERYTHING("*/*"),
	TEXT_HTML("text/html"),
	TEXT_PLAIN("text/plain"),
	TEXT_XML("text/xml"),
	APPLICATION_JSON("application/json"),
	APPLICATION_XML("application/xml");

//	APPLICATION_ATOM_XML("application/atom+xml"),
//	APPLICATION_XHTML_XML("application/xhtml+xml"),
//	APPLICATION_SVG_XML("application/svg+xml"),
//	APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"),
//	MULTIPART_FORM_DATA("multipart/form-data"),
//	APPLICATION_OCTET_STREAM("application/octet-stream"),

	private final String type;


	MediaType(final String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}

}
