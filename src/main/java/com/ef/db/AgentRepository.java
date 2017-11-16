package com.ef.db;

import java.util.Optional;

import com.ef.db.hibernate.HibernateUtil;
import com.ef.domain.Agent;

/**
 * Repository for Agent
 * 
 * @author victor
 *
 */
public class AgentRepository extends AbstractRepository {
	/**
	 * Find agent by description
	 * 
	 * @param description
	 *            agent description
	 * @return the database agent
	 */
	public Optional<Agent> find(String description) {
		return HibernateUtil.getSessionFactory().getCurrentSession()
				.createQuery("FROM Agent WHERE description = :desc", Agent.class).setParameter("desc", description)
				.list().stream().findFirst();
	}
}
