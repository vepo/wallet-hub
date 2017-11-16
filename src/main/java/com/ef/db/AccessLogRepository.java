package com.ef.db;

import java.util.Date;
import java.util.List;

import com.ef.db.hibernate.HibernateUtil;
import com.ef.domain.AccessLog;

/**
 * Repository for {@link AccessLog}
 * 
 * @author victor
 *
 */
public class AccessLogRepository extends AbstractRepository {
	/**
	 * Get all IPs that matches with the parameters:
	 * <ul>
	 * <li>Time Window</li>
	 * <li>Resquest threshold</li>
	 * </ul>
	 * 
	 * @param startTime
	 *            time window start
	 * @param endTime
	 *            time window end
	 * @param threshold
	 *            request threshold
	 * @return IP list
	 */
	public List<String> getIPs(Date startTime, Date endTime, long threshold) {
		return HibernateUtil.query(session -> session.createQuery(
				"SELECT ip FROM AccessLog WHERE time >= :startTime AND time < :endTime GROUP BY ip HAVING COUNT(ip) >= :threshold",
				String.class).setParameter("startTime", startTime).setParameter("endTime", endTime)
				.setParameter("threshold", threshold).list());
	}
}
