package com.ef.db.exception;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * Rollback exceptions. This exception means that the transaction will not be
 * commited.
 * 
 * @author victor
 *
 */
public class RollbackException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1668135233134864800L;

	public RollbackException(SQLIntegrityConstraintViolationException e) {
		super(e);
	}
}
