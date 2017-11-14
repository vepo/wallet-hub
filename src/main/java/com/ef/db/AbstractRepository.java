package com.ef.db;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;

import com.ef.db.exception.RollbackException;

/**
 * Abstract repository
 * 
 * @author victor
 *
 */
public abstract class AbstractRepository {

	protected interface SetPreparedStatementParameters {
		void applyParameters(PreparedStatement statement) throws SQLException;
	}

	protected interface MapResultSet<T> {
		T map(ResultSet resultSet) throws SQLException;
	}

	/**
	 * Execute database Insert.
	 * 
	 * @param sql
	 *            The insert SQL statement
	 * @param fn
	 *            set statement parameters function
	 * @return the last inserted id
	 * @throws RollbackException
	 *             Couldn't insert into table. Constraint violation.
	 */
	protected Long executeInsert(String sql, SetPreparedStatementParameters fn) throws RollbackException {
		try (Connection conn = ParserDataSource.getInstance().getConnection();
				PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {
			fn.applyParameters(statement);
			statement.executeUpdate();
			ResultSet tableKeys = statement.getGeneratedKeys();
			tableKeys.next();
			return tableKeys.getLong(1);
		} catch (SQLIntegrityConstraintViolationException e) {
			throw new RollbackException(e);
		} catch (SQLException | IOException | PropertyVetoException e) {
			System.err.println("Error accessing database: " + e.getMessage());
			throw new RuntimeException(e);
		}
	}

	/**
	 * Execute database Update.
	 * 
	 * @param sql
	 *            The SQL update statement
	 * @param fn
	 *            set statement parameters function
	 * @throws RollbackException
	 *             Couldn't insert into table. Constraint violation.
	 */
	protected void executeUpdate(String sql, SetPreparedStatementParameters fn) throws RollbackException {
		try (Connection conn = ParserDataSource.getInstance().getConnection();
				PreparedStatement statement = conn.prepareStatement(sql);) {
			fn.applyParameters(statement);
			statement.executeUpdate();
		} catch (SQLIntegrityConstraintViolationException e) {
			throw new RollbackException(e);
		} catch (SQLException | IOException | PropertyVetoException e) {
			System.err.println("Error accessing database: " + e.getMessage());
			throw new RuntimeException(e);
		}

	}

	/**
	 * Execute database query
	 * 
	 * @param sql
	 *            The SQL query statement
	 * @param fn
	 *            set statement parameters function
	 * @param mapResult
	 *            Map {@link ResultSet} into return type function
	 * @return the returned type of mapResult
	 */
	protected <T> T executeQuery(String sql, SetPreparedStatementParameters fn, MapResultSet<T> mapResult) {
		try (Connection conn = ParserDataSource.getInstance().getConnection();
				PreparedStatement statement = conn.prepareStatement(sql);) {
			fn.applyParameters(statement);
			return mapResult.map(statement.executeQuery());
		} catch (SQLException | IOException | PropertyVetoException e) {
			System.err.println("Error accessing database: " + e.getMessage());
			throw new RuntimeException(e);
		}
	}
}
