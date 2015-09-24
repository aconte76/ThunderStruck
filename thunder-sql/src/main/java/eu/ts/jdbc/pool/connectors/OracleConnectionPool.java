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
package eu.ts.jdbc.pool.connectors;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import eu.ts.jdbc.pool.ConnectionPool;
import oracle.jdbc.pool.OracleDataSource;

import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


public class OracleConnectionPool extends ConnectionPool {

	private final static String DEFAULT_MIN_POOL = "2";
	private final static String DEFAULT_MAX_POOL = "5";
	private final static String DEFAULT_IDLE_TIMEOUT = "60000";

	private final static String DATASOURCE_PROPS_PREFIX = "datasource.property";


	// CP Name from configuration file
	public OracleConnectionPool(String configFileName) throws Exception {
		this(null, configFileName);
	}


	public OracleConnectionPool(String poolName, String configFileName) throws Exception {
		super("OracleCP");
		// Load external properties
		Properties hikariProps = new Properties();
		hikariProps.load(new FileInputStream(configFileName));

		OracleDataSource oracleDs = new OracleDataSource();

		oracleDs.setURL(hikariProps.remove("datasource.url").toString());
		oracleDs.setUser(hikariProps.remove("datasource.user").toString());
		oracleDs.setPassword(hikariProps.remove("datasource.password").toString());

		// Tuning Direct Oracle Connection passing specific Datasource properties
		Set<String> props = new HashSet<>();
		for (Object key : hikariProps.keySet()) {
			if (key.toString().toLowerCase().startsWith(DATASOURCE_PROPS_PREFIX)) {
				props.add(key.toString());
			}
		}

		// copying keys from original Properties
		Properties pp = new Properties();
		for (String key : props) {
			pp.put(key.substring(DATASOURCE_PROPS_PREFIX.length() + 1), hikariProps.remove(key));
		}

		pp.put("defaultRowPrefetch", "10000");
		if (pp.size() != 0) {
			oracleDs.setConnectionProperties(pp);
		}

		// Load remaining properties to Hikari
		poolConfig = new HikariConfig(hikariProps);
		poolConfig.setDataSource(oracleDs);

		poolConfig.setMinimumIdle(Integer.parseInt(hikariProps.getProperty("minimumIdle", DEFAULT_MIN_POOL)));
		poolConfig.setMaximumPoolSize(Integer.parseInt(hikariProps.getProperty("maximumPoolSize", DEFAULT_MAX_POOL)));
		poolConfig.setIdleTimeout(Long.parseLong(hikariProps.getProperty("idleTimeout", DEFAULT_IDLE_TIMEOUT)));

//			poolConfig.setConnectionInitSql(
//					"begin execute immediate 'alter session set NLS_DATE_FORMAT=''YYYYMMDD-HH24MISS'''; " +
//							"execute immediate 'alter session set NLS_NUMERIC_CHARACTERS=''.,'''; end;"
//			);

		poolConfig.setAutoCommit(false);

		if (poolName != null) {
			poolConfig.setPoolName(poolName);
		}

		// Build of Connection Pool
		this.poolDs = new HikariDataSource(poolConfig);

		super.check();
	}
}