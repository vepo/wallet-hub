package com.ef.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ef.domain.Agent;

/**
 * Repository for Agent
 * 
 * @author victor
 *
 */
@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
	/**
	 * Retrieve an Agent with the description
	 * 
	 * @param desc
	 * @return
	 */
	@Query(value = "SELECT agent from Agent agent WHERE agent.description = :desc")
	public Agent find(@Param("desc") String desc);
}
