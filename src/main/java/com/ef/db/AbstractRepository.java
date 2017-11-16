package com.ef.db;

import javax.persistence.PersistenceException;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ef.db.exception.RollbackException;
import com.ef.db.hibernate.HibernateUtil;

/**
 * Abstract repository
 * 
 * @author victor
 *
 */
public abstract class AbstractRepository {
	private static Logger LOGGER = LoggerFactory.getLogger(AbstractRepository.class);

	/**
	 * Execute database Insert.
	 * 
	 * @param obj
	 *            The new object
	 * @throws RollbackException
	 *             Couldn't insert object. Constraint violation.
	 */
	public <T> void insert(T obj) throws RollbackException {
		try {
			HibernateUtil.getSessionFactory().getCurrentSession().persist(obj);
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
	 * @param obj
	 *            Object to update
	 * @throws RollbackException
	 *             Couldn't update object. Constraint violation.
	 */
	protected <T> void update(T obj) throws RollbackException {
		HibernateUtil.getSessionFactory().getCurrentSession().merge(obj);
	}

}
