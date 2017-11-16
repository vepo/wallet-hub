package com.ef.db;

import javax.persistence.PersistenceException;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ef.db.exception.RollbackException;

/**
 * Abstract repository
 * 
 * @author victor
 *
 */
public abstract class AbstractRepository {
	private static Logger LOGGER = LoggerFactory.getLogger(AbstractRepository.class);
	protected Session session;

	public AbstractRepository(Session session) {
		this.session = session;
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
	public <T> void insert(T obj) throws RollbackException {
		try {
			session.persist(obj);
		} catch (ConstraintViolationException e) {
			throw new RollbackException(e);
		} catch (PersistenceException e) {
			if (e.getCause() instanceof ConstraintViolationException) {
				// Ignore this error. It will be handled in business layer
				throw new RollbackException((ConstraintViolationException) e.getCause());
			} else {
				LOGGER.warn("Error: " + e.getMessage(), e);
				throw e;
			}
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
	protected <T> void update(T obj) throws RollbackException {
		session.merge(obj);
	}

}
