package com.ef.db;

import org.springframework.dao.DataIntegrityViolationException;

public class RollbackException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1668135233134864800L;

	public RollbackException(DataIntegrityViolationException e) {
		super(e);
	}
}
