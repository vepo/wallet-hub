package com.ef.db;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;

import com.ef.domain.AccessLog;

/**
 * Repository for {@link AccessLog}
 * 
 * @author victor
 *
 */
public class AccessLogRepository extends AbstractRepository {

	public AccessLogRepository(Session session) {
		super(session);
	}

	public List<String> getIPs(Date startDate, Date endDate, long threshold) {
		return session.createQuery(
				"SELECT ip FROM AccessLog WHERE time >= :startTime AND time < :endTime GROUP BY ip HAVING COUNT(ip) >= :threshold",
				String.class).setParameter("startTime", startDate).setParameter("endTime", endDate)
				.setParameter("threshold", threshold).list();
	}
}
