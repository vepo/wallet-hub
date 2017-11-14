package com.ef.db.services;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.ef.db.AccessLogRepository;
import com.ef.db.AgentRepository;
import com.ef.db.BlockedIPRepository;
import com.ef.db.exception.RollbackException;

/**
 * Access Log Business rules
 * 
 * @author victor
 *
 */
public class AccessLogService {

	private AgentRepository agentRepository = new AgentRepository();

	private AccessLogRepository accessLogRepository = new AccessLogRepository();

	private BlockedIPRepository blockedIPRepository = new BlockedIPRepository();

	/**
	 * Agent cache
	 */
	private Map<String, Long> cache = new HashMap<>();

	/**
	 * Register log.
	 * 
	 * @param time
	 *            log time
	 * @param ip
	 *            log IP
	 * @param request
	 *            HTTP request URL
	 * @param responseCode
	 *            HTTP response code
	 * @param agentDescription
	 *            HTTP agent
	 */
	public void register(Date time, String ip, String request, Integer responseCode, String agentDescription) {
		try {
			this.accessLogRepository.insert(time, ip, request, responseCode, getAgentId(agentDescription));
		} catch (RollbackException e) {
			System.err.println("Line already processed! Ignoring line.");
		} catch (SQLException e) {
			System.err.println("Error saving log data!");
		}
	}

	/**
	 * Get Agent Id for description
	 * 
	 * @param agentDescription
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	private Long getAgentId(String agentDescription) throws SQLException {
		if (!cache.containsKey(agentDescription)) {
			Long agentId = agentRepository.findIdByDescription(agentDescription);
			if (agentId != null) {
				cache.put(agentDescription, agentId);
			} else {
				try {
					cache.put(agentDescription, agentRepository.insert(agentDescription));
				} catch (RollbackException e) {
					// nothing! This exception will never be throwed because we look for the
					// description before
				}
			}
		}
		return cache.get(agentDescription);
	}

	/**
	 * Block IP according with parameters
	 * 
	 * @param startDate
	 *            The time window start end
	 * @param endDate
	 *            The time window start
	 * @param threshold
	 *            The minimum request for blocking
	 */
	public void createBlockedIPs(Date startDate, Date endDate, int threshold) {
		accessLogRepository.getIPs(startDate, endDate, (long) threshold).forEach(ip -> {
			try {
				blockedIPRepository.insert(ip);
			} catch (RollbackException e) {
				System.err.println("IP already blocked: " + ip);
			}
		});
	}
}
