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

public class LRUCacheTest {

	public static void main(String[] args) {
		LRUCache<Integer, String> cache = new LRUCache<>(3);

		cache.cache(1, "1");
		cache.cache(2, "2");
		cache.cache(3, "3");

		System.out.println(cache.getMap().toString());

		cache.get(1);

		cache.cache(4, "4");

		System.out.println(cache.getMap().toString());

		cache.cache(2, "2");
		cache.cache(5, "5");

		System.out.println(cache.getMap().toString());
	}
}