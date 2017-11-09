package com.ef.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ef.domain.Agent;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {

	@Query(value = "SELECT agent from Agent agent WHERE agent.description = :desc")
	public Agent find(@Param("desc") String desc);
}
