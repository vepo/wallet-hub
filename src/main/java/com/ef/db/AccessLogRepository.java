package com.ef.db;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ef.db.exception.RollbackException;

/**
 * Repository for {@link AccessLog}
 * 
 * @author victor
 *
 */
public class AccessLogRepository extends AbstractRepository {
	/**
	 * Insert Access Log entry
	 * 
	 * @param time
	 *            the access time
	 * @param ip
	 *            the access ip
	 * @param request
	 *            the HTTP request
	 * @param responseCode
	 *            the response code
	 * @param agentId
	 *            the agent Id
	 * @throws RollbackException
	 *             Couldn't add the log entry. It already exists into database
	 */
	public void insert(Date time, String ip, String request, Integer responseCode, Long agentId)
			throws RollbackException {
		executeInsert(
				"INSERT INTO log_access (`time`, `ip`, `request`, `response_code`, `agent_id`) VALUES (?, ?, ?, ?, ?)",
				statement -> {
					statement.setTimestamp(1, new Timestamp(time.getTime()));
					statement.setString(2, ip);
					statement.setString(3, request);
					statement.setInt(4, responseCode);
					statement.setLong(5, agentId);
				});
	}

	/**
	 * Retrieve all IPs from access log according with parameters.
	 * 
	 * @param startTime
	 *            The time window start
	 * @param endTime
	 *            The time window end
	 * @param threshold
	 *            Filter IP according with the threshold
	 * @return All IP that access the server more than <code>threshold</code>
	 *         between <code>startTime</code> and <code>endTime</code>
	 */
	public List<String> getIPs(Date startTime, Date endTime, long threshold) {
		return executeQuery("SELECT ip FROM log_access WHERE `time` >= ? AND time < ? GROUP BY ip HAVING COUNT(ip) > ?",
				statement -> {
					statement.setTimestamp(1, new Timestamp(startTime.getTime()));
					statement.setTimestamp(2, new Timestamp(endTime.getTime()));
					statement.setLong(3, threshold);
				}, resultSet -> {
					List<String> ips = new ArrayList<>();
					while (resultSet.next()) {
						ips.add(resultSet.getString(1));
					}
					return ips;
				});

	}
}
