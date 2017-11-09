package com.ef.db;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ef.domain.AccessLog;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

	@Query("SELECT ip FROM AccessLog WHERE time >= (:startTime) AND time < (:endTime) GROUP BY ip HAVING COUNT(ip) > (:threshold)")
	public List<String> getBlockedIP(@Param("startTime") Date startTime, @Param("endTime") Date endTime,
			@Param("threshold") long threshold);
}
