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
package eu.ts.jdbc.dsl;

import eu.ts.jdbc.dsl.mapping.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class Query implements AutoCloseable {
	private final static Logger log = LoggerFactory.getLogger(Query.class);

	private final static boolean DEFAULT_AUTO_CLOSE = true;

	private QueryParser parsedQuery;

	private DataSource dataSource = null;
	private Connection connection = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	private final Map<String, QueryParameterApplier> paramAppliersMap = new HashMap<>();

	private final static QueryClassMapperCache queryClassMapperCache = new QueryClassMapperCache();
	private final Map<String, String> columnMapping = new HashMap<>();


	public Query(DataSource dataSource) {
		if (dataSource == null) {
			throw new RuntimeException("Unable proceed without a DataSource");
		}
		this.dataSource = dataSource;
	}

	private Query(DataSource dataSource, String sql) {
		this(dataSource);
		this.parsedQuery = QueryCache.registerSqlStatement(sql);
	}

	public Query select(final String sql) {
		this.parsedQuery = QueryCache.registerSqlStatement(sql);
		return this;
	}

	public Query select(final String sql, QueryParam... params) {
		Query retQuery = select(sql);
		for (QueryParam param : params) {
			retQuery.addParameter(param);
		}
		return retQuery;
	}

	public static Query open(DataSource dataSource) {
		return new Query(dataSource);
	}

	public static Query select(DataSource dataSource, final String sql) {
		return new Query(dataSource, sql);
	}

	public static Query select(DataSource dataSource, final String sql, final QueryParam... params) {
		return (new Query(dataSource, sql)).addParameters(params);
	}

	public static <T> QueryParam<T> parameter(String parameter, T value) {
		return new QueryParam<>(parameter, value);
	}

	public Query addParameters(final QueryParam... params) {
		for (QueryParam param : params) {
			this.addParameter(param);
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	public Query addParameter(final QueryParam param) {
		return ((param.getValue() == null)
				? addParameter(param.getName(), Object.class, null)
				: addParameter(param.getName(), (Class<Object>) param.getValue().getClass(), param.getValue())
		);
	}


	private <T> Query addParameter(String name, Class<T> parameterClass, T value) {
		paramAppliersMap.put(name, buildParameterApplier(parameterClass, value));
		return this;
	}


	private <T> QueryParameterApplier buildParameterApplier(Class<T> paramClass, T value) {
		//TODO: must cover most of types: BigDecimal,Boolean,SmallInt,Double,Float,byte[]
		if (paramClass == Integer.class)
			return (statement, position) -> statement.setInt(position, (Integer) value);
		if (paramClass == Long.class)
			return (statement, position) -> statement.setLong(position, (Long) value);
		if (paramClass == String.class)
			return (statement, position) -> statement.setString(position, (String) value);
		if (paramClass == Timestamp.class)
			return (statement, position) -> statement.setTimestamp(position, (Timestamp) value);
		if (paramClass == Time.class)
			return (statement, position) -> statement.setTime(position, (Time) value);
		if (paramClass == Date.class)
			return (statement, position) -> statement.setDate(position, (Date) value);
		if (paramClass == BigDecimal.class)
			return (statement, position) -> statement.setBigDecimal(position, (BigDecimal) value);
		if (paramClass == Double.class)
			return (statement, position) -> statement.setDouble(position, (Long) value);
		// fallback for unknown object type
		return (statement, position) -> statement.setObject(position, value);
	}


	public Query mapColumn(final String columnName, final String propertyName) {
//		if (columnMapping.containsKey(columnName.toLowerCase())) {
//			throw new DSLException(String.format("mapColumn '%s' -> '%s' already defined", columnName, propertyName));
//		}
		this.columnMapping.put(columnName.toLowerCase(), propertyName.toLowerCase());
		return this;
	}

	public Query mapColumns(final String[][] columnsMap) {
		for (String[] tupleMap : columnsMap) {
			mapColumn(tupleMap[0], tupleMap[1]);
		}
		return this;
	}

	public Query mapColumns(final Map<String, String> mapper) {
		for (Map.Entry<String, String> entry : mapper.entrySet()) {
			mapColumn(entry.getKey(), entry.getValue());
		}
		return this;
	}


	// Automatic Class Fetcher
	public <T> List<T> fetch(Class<T> clazz) {
		return fetch(null, clazz, DEFAULT_AUTO_CLOSE);
	}

	public <T> List<T> fetch(Class<T> clazz, boolean autoClose) throws Exception {
		return fetch(null, clazz, autoClose);
	}

	public <T> List<T> fetch(final ResultSetHandler<T> handler) {
		return fetch(handler, DEFAULT_AUTO_CLOSE);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> fetch(final ResultSetHandler<T> handler, boolean autoClose) {
		try {
			return fetch(
					handler,
					(Class<T>) Class.forName(handler.getClass().getGenericSuperclass().getTypeName()),
					autoClose
			);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}


	// Fetch with custom handler
	private <T> List<T> fetch(final ResultSetHandler<T> handler, final Class<T> clazz, boolean autoClose) {

		List<T> retList = new LinkedList<>();

		// check if clazz is instantiable
		if (clazz != null) {
			try {
				clazz.getDeclaredConstructor();
			} catch (NoSuchMethodException ex) {
				throw new RuntimeException("Unable to find default constructor for: " + clazz.getName() + "()", ex);
			}
		} else {
			throw new RuntimeException("Unable to fetch null resultSet class");
		}

		try {
			connection = dataSource.getConnection();
			ps = connection.prepareStatement(parsedQuery.getRunnableSql());

			// applying given parameters
			String[] queryParams = parsedQuery.getParameters();
			if (queryParams != null) {
				for (int i = 0; i < queryParams.length; i++) {
					paramAppliersMap.get(queryParams[i]).apply(ps, i + 1);
				}
			}

			rs = ps.executeQuery();

			// Parsing Query Metadata for late binding on destination clazz
			Map<Integer, ClassAttributeApplier> bindMap = null;
			if (handler == null) {
				bindMap = getBinding(rs.getMetaData(), clazz);
			}

			// produce result class list from query
			while (rs.next()) {

				T retObj;
				if (handler == null) {
					try {
						retObj = clazz.newInstance();
					} catch (Exception ex) {
						throw new RuntimeException("Unable to instantiate class: " + clazz.getName(), ex);
					}
					// applying bindMap rules
					for (ClassAttributeApplier applier : bindMap.values()) {
						applier.apply(retObj, rs);
					}
				} else {
					retObj = handler.handle(rs);
				}
				retList.add(retObj);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			if (autoClose) {
				try {
					close();
				} catch (Exception ignored) { /* ignored */ }
			}
		}
		return retList;
	}


	private <T> Map<Integer, ClassAttributeApplier> getBinding(ResultSetMetaData rsmd, Class<T> clazz) throws SQLException {
		String mappingId = String.format("%s[%s]", parsedQuery.getName(), clazz.getName());
		Map<Integer, ClassAttributeApplier> bindMap = queryClassMapperCache.get(mappingId);

		if (bindMap == null) {
			Map<String, Field> destPojo = ClassExplorer.build(clazz).getFields();

			// composing binding mapper
			bindMap = new HashMap<>();
			queryClassMapperCache.cache(mappingId, bindMap);

			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				String columnName = rsmd.getColumnName(i).toLowerCase();
				String mappedFiled = columnMapping.get(columnName);
				mappedFiled = (mappedFiled == null) ? columnName : mappedFiled;
				Field classField = destPojo.get(mappedFiled);

				if (classField != null) {
					// ClassAttributeApplier applier = buildApplier(classField, i);
					bindMap.put(i, buildApplier(classField, i));
				} else {
					log.warn("Unable to map '{}' to class '{}'",
							rsmd.getColumnName(i), clazz.getName()
					);
				}
			}
		}

		return bindMap;
	}


	private ClassAttributeApplier buildApplier(final Field field, final int col) {
		Class<?> fieldType = field.getType();

		// TODO: Performances are better on precise types mapped... add here!
		if (fieldType.isPrimitive()) {
			if (fieldType.equals(int.class))
				return (pojo, rs) -> field.set(pojo, rs.getInt(col));
			if (fieldType.equals(long.class))
				return (pojo, rs) -> field.setLong(pojo, rs.getLong(col));
			if (fieldType.equals(double.class))
				return (pojo, rs) -> field.setDouble(pojo, rs.getDouble(col));
			if (fieldType.equals(float.class))
				return (pojo, rs) -> field.setFloat(pojo, rs.getFloat(col));
		} else {
			// JDBC getter Optimization
			if (fieldType.equals(Integer.class))
				return (pojo, rs) -> field.set(pojo, rs.getInt(col));
			if (fieldType.equals(Long.class))
				return (pojo, rs) -> field.setLong(pojo, rs.getLong(col));
			if (fieldType.equals(String.class))
				return (pojo, rs) -> field.set(pojo, rs.getString(col));
			if (fieldType.equals(Double.class))
				return (pojo, rs) -> field.set(pojo, rs.getDouble(col));
			if (fieldType.equals(Float.class))
				return (pojo, rs) -> field.setFloat(pojo, rs.getFloat(col));
		}
		// return others generic appliers for Objects
		return (pojo, rs) -> field.set(pojo, rs.getObject(col, fieldType));
	}


	@Override
	public void close() throws Exception {
		try {
			if (rs != null && !rs.isClosed()) {
				rs.close();
			}
		} catch (SQLException ignored) { /* ignored */ }
		try {
			if (ps != null && !ps.isClosed()) {
				ps.close();
			}
		} catch (SQLException ignored) { /* ignored */ }
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException ignored) { /* ignored */ }
		}
	}

	@Override
	public String toString() {
		return parsedQuery.getRunnableSql();
	}

}
