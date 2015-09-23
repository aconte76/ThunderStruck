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
package eu.thunderstruck.jdbc.dsl.mapping;


import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> {

	private final static int DEFAULT_CACHE_SIZE = 1000;
	private final Map<K, V> cacheMap;

	public LRUCache() {
		this(DEFAULT_CACHE_SIZE);
	}

	public LRUCache(final int maxSize) {
		this.cacheMap = // Collections.synchronizedMap(
				new LinkedHashMap<K, V>(maxSize * 4 / 3, 0.75f, true) {
					@Override
					protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
						return size() > maxSize;
					}
				};
	}

	public LRUCache(final int maxSize, final Map<K, V> map) {
		this((maxSize > map.size()) ? maxSize : map.size());
		this.cacheMap.putAll(map);
	}

	public V get(K key) {
		return cacheMap.get(key);
	}

	public void cache(K key, V value) {
		cacheMap.put(key, value);
	}

	public void cache(Map<K, V> map) {
		cacheMap.putAll(map);
	}


	public Map<K, V> getMap() {
		return cacheMap;
	}
}

