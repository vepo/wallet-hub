package com.ef.db.exception;

import org.springframework.dao.DataIntegrityViolationException;

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

	public RollbackException(DataIntegrityViolationException e) {
		super(e);
	}
}
