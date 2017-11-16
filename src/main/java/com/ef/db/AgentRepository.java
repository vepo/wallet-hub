package com.ef.db;

import java.util.Optional;

import org.hibernate.Session;

import com.ef.domain.Agent;

/**
 * Repository for Agent
 * 
 * @author victor
 *
 */
public class AgentRepository extends AbstractRepository {

	public AgentRepository(Session session) {
		super(session);
	}

	public Optional<Agent> find(String agentDescription) {
		return session.createQuery("FROM Agent WHERE description = :desc", Agent.class)
				.setParameter("desc", agentDescription).list().stream().findFirst();
	}
}
