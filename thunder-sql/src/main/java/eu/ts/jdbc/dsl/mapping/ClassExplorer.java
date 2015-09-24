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
package eu.ts.jdbc.dsl.mapping;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ClassExplorer {

	private final static LRUCache<String, Map<String, Field>> pojoCache = new LRUCache<>();

	private final String className;

	private ClassExplorer(Class<?> clazz) {
		this.className = clazz.getName();
		Field[] fields = clazz.getDeclaredFields();

		Map<String, Field> classFields = pojoCache.get(className);
		if (classFields == null) {
			classFields = new HashMap<>();

			for (Field field : fields) {
				field.setAccessible(true);
				classFields.put(field.getName().toLowerCase(), field);
			}
			pojoCache.cache(className, classFields);
		}
	}

	public static <T> ClassExplorer build(Class<T> clazz) {
		return new ClassExplorer(clazz);
	}

	public Map<String, Field> getFields() {
		return pojoCache.get(className);
	}


}