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


import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class QueryParser {
	private final static Pattern parameterPattern = Pattern.compile("(:[a-zA-Z0-9]+)");

	private String name;
	private final String originalSql;
	private String runnableSql;
	private String[] parameters;


	public QueryParser(String sql) {
		this.name = queryId(sql);
		this.originalSql = sql; // sqlClean(originalSql);
	}

	public QueryParser(String name, String sql) {
		this.name = name;
		this.originalSql = sql; // sqlClean(originalSql);
	}


	public static String queryId(String sql) {
		return UUID.nameUUIDFromBytes(sql.getBytes()).toString();
	}

//	private String sqlClean(String originalSql) {
//		return originalSql.replaceAll("\\s+", " ");
//	}

	private String parseSql(String sql) {
		ArrayList<String> paramsList = new ArrayList<>();

		StringBuilder outSql = new StringBuilder();
		int lastOccurrence = 0;
		Matcher m = parameterPattern.matcher(sql);

		// catch parameters
		while (m.find()) {
			String param = sql.substring(m.start() + 1, m.end()); // skip initial ":"
			paramsList.add(param);
			outSql.append(sql.substring(lastOccurrence, m.start())).append('?');
			lastOccurrence = m.end();
		}
		outSql.append(sql.substring(lastOccurrence));
		this.parameters = paramsList.toArray(new String[paramsList.size()]);

		return outSql.toString();
	}


	public String getName() {
		return name;
	}

	public String getOriginalSql() {
		return originalSql;
	}

	public String getRunnableSql() {
		if (runnableSql == null) {
			runnableSql = parseSql(this.originalSql);
		}
		return runnableSql;
	}

	public String[] getParameters() {
		return parameters;
	}
}
