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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

// Service Descriptor
@Data
@AllArgsConstructor
public class ServiceDescriptor {
	private String name;
	private String version;

	@Getter(lazy = true)
	private final String description = String.format("%s:%s", name, version);

	private boolean debugMode;

	private Set<VersionedApi> versionedApiSet = new HashSet<>();

	private Map<String, String> environment;
	private Properties systemProperties;


	public ServiceDescriptor() {
	}
}