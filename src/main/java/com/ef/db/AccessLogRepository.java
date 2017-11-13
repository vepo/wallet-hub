package com.ef.db;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ef.domain.AccessLog;

/**
 * Repository for {@link AccessLog}
 * 
 * @author victor
 *
 */
@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
	/**
	 * Retrieve all blocked IPs. Any IP that has more than <b>threshold</b> access
	 * in the specified time will be blocked.
	 * 
	 * @param startTime
	 *            The time window start
	 * @param endTime
	 *            The time window end
	 * @param threshold
	 *            The threshold for blocking an IP
	 * @return All IP that will be blocked
	 */
	@Query("SELECT ip FROM AccessLog WHERE time >= (:startTime) AND time < (:endTime) GROUP BY ip HAVING COUNT(ip) > (:threshold)")
	public List<String> getBlockedIP(@Param("startTime") Date startTime, @Param("endTime") Date endTime,
			@Param("threshold") long threshold);
}
