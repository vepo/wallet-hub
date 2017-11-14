package com.ef.db;

import com.ef.db.exception.RollbackException;

/**
 * BlockedIP Repository
 * 
 * @author victor
 *
 */
public class BlockedIPRepository extends AbstractRepository {
	/**
	 * Insert blocked ip
	 * 
	 * @param ip
	 *            The ip to be blocked
	 * @return the blocked instance id
	 * @throws RollbackException
	 *             Couldn't add the ip. It already exist into database
	 */
	public Long insert(String ip) throws RollbackException {
		return executeInsert("INSERT INTO blocked_ip (ip) VALUES (?)", statement -> statement.setString(1, ip));
	}

}
