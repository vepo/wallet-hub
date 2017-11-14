package com.ef.db;

import static com.ef.utils.StringUtils.first;

import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Parser DataSource. Uses the enviroment variables, with respective default
 * value:
 * <ul>
 * <li><b>DB_DRIVER</b>: <code>com.mysql.cj.jdbc.Driver</code></li>
 * <li><b>DB_URL</b>:
 * <code>jdbc:mysql://localhost:3306/log-db?verifyServerCertificate=false&useSSL=true</code></li>
 * <li><b>DB_USERNAME</b>: <code>log-user</code></li>
 * <li><b>DB_PASSWORD</b>: <code>log-pw</code></li>
 * </ul>
 * 
 * @author victor
 *
 */
public class ParserDataSource {

	private static ParserDataSource datasource;
	private ComboPooledDataSource cpds;

	private ParserDataSource() throws IOException, PropertyVetoException {
		Properties env = getEnv();

		cpds = new ComboPooledDataSource();
		// loads the jdbc driver
		cpds.setDriverClass(first(System.getenv("DB_DRIVER"), env.getProperty("db.connection.driver_class")));
		cpds.setJdbcUrl(first(System.getenv("DB_URL"), env.getProperty("db.connection.url")));
		cpds.setUser(first(System.getenv("DB_USERNAME"), env.getProperty("db.connection.username")));
		cpds.setPassword(first(System.getenv("DB_PASSWORD"), env.getProperty("db.connection.password")));

		// the settings below are optional -- c3p0 can work with defaults
		cpds.setMinPoolSize(5);
		cpds.setAcquireIncrement(5);
		cpds.setMaxPoolSize(20);
		cpds.setMaxStatements(180);

	}

	private Properties getEnv() throws IOException {
		Properties prop = new Properties();
		String propFileName = "db.properties";

		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		}
		return prop;
	}

	/**
	 * Retrieve DataSource instance
	 * 
	 * @return The datasource instance
	 * @throws IOException
	 *             Couldn't connect with database
	 * @throws PropertyVetoException
	 *             Invalid driver class
	 */
	public static ParserDataSource getInstance() throws IOException, PropertyVetoException {
		if (datasource == null) {
			datasource = new ParserDataSource();
		}
		return datasource;
	}

	/**
	 * Get database connection
	 * 
	 * @return The database connection
	 * @throws SQLException
	 *             Couldn't connect with the database
	 */
	public Connection getConnection() throws SQLException {
		return this.cpds.getConnection();
	}

}