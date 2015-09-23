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

public class QueryCache {

	private final static LRUCache<String, QueryParser> cache = new LRUCache<>();

	private QueryCache() {
		// will be a shared single instance
	}

	public static QueryParser registerNamedStatement(final String name, final String sql) {
		QueryParser qp = cache.get(name);
		if (qp == null) {
			qp = new QueryParser(name, sql);
			cache.cache(qp.getName(), qp);
		}
		return qp;
	}

	public static QueryParser registerSqlStatement(final String sql) {
		QueryParser qp = cache.get(QueryParser.queryId(sql));
		if (qp == null) {
			qp = new QueryParser(sql);
			cache.cache(qp.getName(), qp);
		}
		return qp;
	}

}