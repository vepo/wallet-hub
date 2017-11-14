package com.ef.db;

import com.ef.db.exception.RollbackException;

/**
 * Repository for Agent
 * 
 * @author victor
 *
 */
public class AgentRepository extends AbstractRepository {
	/**
	 * Find Agent id by descriotion
	 * 
	 * @param agentDescription
	 *            the agent description
	 * @return the agent id
	 */
	public Long findIdByDescription(String agentDescription) {
		return executeQuery("SELECT `id` FROM agent WHERE `description`= ?",
				statement -> statement.setString(1, agentDescription),
				resultSet -> resultSet.next() ? resultSet.getLong(1) : null);
	}

	/**
	 * Insert agent
	 * 
	 * @param agentDescription
	 *            the agent description
	 * @return the agent id
	 * @throws RollbackException
	 *             Couldn't add the agent
	 */
	public Long insert(String agentDescription) throws RollbackException {
		return executeInsert("INSERT INTO agent (`description`) VALUES (?)", statement -> {
			statement.setString(1, agentDescription);
		});
	}
}
