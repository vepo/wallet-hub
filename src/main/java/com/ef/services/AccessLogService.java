package com.ef.services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ef.db.AccessLogRepository;
import com.ef.db.AgentRepository;
import com.ef.db.BlockedIPRepository;
import com.ef.db.exception.RollbackException;
import com.ef.db.hibernate.HibernateUtil;
import com.ef.domain.AccessLog;
import com.ef.domain.Agent;
import com.ef.domain.BlockedIP;
import com.ef.utils.StringUtils;

/**
 * Access Log Business rules
 * 
 * @author victor
 *
 */
public class AccessLogService {

	private static Logger LOGGER = LoggerFactory.getLogger(AccessLogService.class);

	private AccessLogRepository accessLogRepository = new AccessLogRepository();
	private BlockedIPRepository blockedIPRepository = new BlockedIPRepository();
	private AgentRepository agentRepository = new AgentRepository();
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");

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
		try (Session session = HibernateUtil.getSessionFactory().getCurrentSession()) {
			Transaction tx = session.beginTransaction();
			try {
				AccessLog logInfo = new AccessLog(time, ip, request, responseCode);
				if (!StringUtils.isEmpty(agentDescription)) {
					Optional<Agent> agent = agentRepository.find(agentDescription);
					if (!agent.isPresent()) {
						agent = Optional.of(new Agent(agentDescription));
						agentRepository.insert(agent.get());
					}
					logInfo.setAgent(agent.get());
				}
				accessLogRepository.insert(logInfo);
				tx.commit();
			} catch (RollbackException e) {
				tx.rollback();
				System.err.println("Line already processed! Ignoring line.");
			}
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
	 * 
	 * @return blocked IPs
	 */
	public List<String> createBlockedIPs(Date startDate, Date endDate, int threshold) {
		List<String> ips = accessLogRepository.getIPs(startDate, endDate, (long) threshold);
		ips.forEach(ip -> {
			try (Session session = HibernateUtil.getSessionFactory().getCurrentSession()) {
				Transaction tx = session.beginTransaction();
				try {
					blockedIPRepository.insert(new BlockedIP(ip, String.format("More than %d access between %s and %s",
							threshold, DATE_FORMAT.format(startDate), DATE_FORMAT.format(endDate))));
					tx.commit();
				} catch (RollbackException e) {
					tx.rollback();
					LOGGER.warn("Constraint violation. Rollback.", e);
					System.err.println("IP already blocked: " + ip);
				}
			}
		});
		return ips;
	}
}
