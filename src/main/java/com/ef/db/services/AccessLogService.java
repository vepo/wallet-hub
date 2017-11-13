package com.ef.db.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ef.db.AccessLogRepository;
import com.ef.db.AgentRepository;
import com.ef.db.BlockedIPRepository;
import com.ef.db.exception.RollbackException;
import com.ef.domain.AccessLog;
import com.ef.domain.Agent;
import com.ef.domain.BlockedIP;

/**
 * Access Log Business rules
 * 
 * @author victor
 *
 */
@Service
public class AccessLogService {

	@Autowired
	private AgentRepository agentRepository;

	@Autowired
	private AccessLogRepository accessLogRepository;

	@Autowired
	private BlockedIPRepository blockedIPRepository;

	@Autowired
	private DataSource dataSource;

	private Map<String, Long> cache = new HashMap<>();

	/**
	 * Register log without Hibernate. Using this method we can register faster than
	 * using {@link #register(Date, String, String, Integer, String)}.
	 * 
	 * <p>
	 * Execution times:
	 * 
	 * <pre>
	 * real	103m29.033s
	 * user	1m1.072s
	 * sys	0m19.144s
	 * </pre>
	 * 
	 * <pre>
	 * real	107m50.041s
	 * user	1m3.896s
	 * sys	0m16.200s
	 * </pre>
	 * </p>
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
	public void registerWithoutHibernate(Date time, String ip, String request, Integer responseCode,
			String agentDescription) {
		try (Connection conn = dataSource.getConnection();
				PreparedStatement statement = conn.prepareStatement(
						"INSERT INTO log_access (`time`, `ip`, `request`, `response_code`, `agent_id`) VALUES (?, ?, ?, ?, ?)");) {
			statement.setTimestamp(1, new Timestamp(time.getTime()));
			statement.setString(2, ip);
			statement.setString(3, request);
			statement.setInt(4, responseCode);
			statement.setLong(5, getAgentId(agentDescription, conn));
			statement.executeUpdate();
		} catch (SQLIntegrityConstraintViolationException e) {
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
	private Long getAgentId(String agentDescription, Connection conn) throws SQLException {
		if (!cache.containsKey(agentDescription)) {
			try (PreparedStatement existentAgent = conn
					.prepareStatement("SELECT `id` FROM agent WHERE `description`= ?")) {
				existentAgent.setString(1, agentDescription);
				ResultSet agentQuery = existentAgent.executeQuery();
				if (agentQuery.next()) {
					cache.put(agentDescription, agentQuery.getLong(1));
				} else {
					try (PreparedStatement insertAgentStatement = conn.prepareStatement(
							"INSERT INTO agent (`description`) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
						insertAgentStatement.setString(1, agentDescription);
						insertAgentStatement.executeUpdate();
						ResultSet tableKeys = insertAgentStatement.getGeneratedKeys();
						tableKeys.next();
						cache.put(agentDescription, tableKeys.getLong(1));
					}
				}
			}
		}
		return cache.get(agentDescription);
	}

	/**
	 * Register log using Hibernate. This methos is 7% slower than
	 * {@link #registerWithoutHibernate(Date, String, String, Integer, String)}
	 * 
	 * <p>
	 * Execution time:
	 * 
	 * <pre>
	 * real	111m34.630s
	 * user	2m43.636s
	 * sys	0m58.872s
	 * </pre>
	 * </p>
	 * 
	 * @param time
	 * @param ip
	 * @param request
	 * @param responseCode
	 * @param agentDescription
	 * @throws RollbackException
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW, rollbackFor = RollbackException.class)
	public void register(Date time, String ip, String request, Integer responseCode, String agentDescription)
			throws RollbackException {
		try {
			AccessLog logInfo = new AccessLog(time, ip, request, responseCode);
			if (!StringUtils.isEmpty(agentDescription)) {
				Agent agent = agentRepository.find(agentDescription);
				if (null == agent) {
					agent = new Agent(agentDescription);
					agentRepository.save(agent);
				}
				logInfo.setAgent(agent);
			}
			accessLogRepository.saveAndFlush(logInfo);
		} catch (DataIntegrityViolationException e) {
			throw new RollbackException(e);
		}
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
		accessLogRepository.getBlockedIP(startDate, endDate, (long) threshold).forEach(ip -> {
			try {
				blockedIPRepository.save(new BlockedIP(ip));
			} catch (DataIntegrityViolationException e) {
				System.err.println("IP already blocked: " + ip);
			}
		});
		blockedIPRepository.flush();
	}
}
