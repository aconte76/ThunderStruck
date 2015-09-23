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
package eu.thunderstruck.jdbc.pool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


public abstract class ConnectionPool {
	protected final String poolType;

	protected HikariConfig poolConfig;
	protected HikariDataSource poolDs;


	// Default Constructor
	protected ConnectionPool(String poolType) {
		this.poolType = poolType;
	}


	public void check() {
		if (poolType == null) {
			throw new RuntimeException("Missing PoolType");
		}
		if (poolDs == null) {
			throw new RuntimeException(String.format("PoolType: %s - missing mandatory Datasource", poolType));
		}
	}

	public DataSource getDataSource() {
		return poolDs;
	}

	public Connection getPlainConnection() throws SQLException {
		return poolDs.getConnection();
	}

}
