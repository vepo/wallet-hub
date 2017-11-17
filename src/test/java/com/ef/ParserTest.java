package com.ef;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ef.db.hibernate.HibernateUtil;
import com.ef.services.AccessLogService;
import com.ef.utils.DateUtils;

public class ParserTest {
	@BeforeClass
	public static void setup() {
		clearDatabase();
	}

	@After
	public void tearDown() {
		try {
			clearDatabase();
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	private static void clearDatabase() {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Transaction tx = session.beginTransaction();
			session.createNativeQuery("TRUNCATE SCHEMA PUBLIC RESTART IDENTITY AND COMMIT NO CHECK").executeUpdate();
			tx.commit();
		}
	}

	private AccessLogService accessLogService = new AccessLogService();

	/*
	 * Check if the threshold is working
	 */
	@Test
	public void insertTest() {
		for (int i = 0; i < 10; ++i) {
			accessLogService.register(new Date(), "127.0.0.1", "GET / HTTP/1.1", 200,
					"Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Mobile Safari/537.36");

		}

		for (int i = 0; i < 9; ++i) {
			accessLogService.register(new Date(), "44.44.44.44", "GET / HTTP/1.1", 200,
					"Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Mobile Safari/537.36");

		}
		List<String> blockedIPs = accessLogService.createBlockedIPs(DateUtils.oneHourBefore(), DateUtils.oneHourAfter(),
				10);

		Assert.assertTrue(blockedIPs.contains("127.0.0.1"));
		Assert.assertFalse(blockedIPs.contains("44.44.44.44"));
	}

	/*
	 * Check if there is no rollback problem.
	 */
	@Test
	public void checkRollbackTest() {
		Date time = new Date();
		accessLogService.register(time, "127.0.0.1", "GET / HTTP/1.1", 200,
				"Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Mobile Safari/537.36");
		accessLogService.register(time, "127.0.0.1", "GET / HTTP/1.1", 200,
				"Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Mobile Safari/537.36");

		for (int i = 0; i < 3; ++i) {
			accessLogService.register(new Date(), "127.0.0.1", "GET / HTTP/1.1", 200,
					"Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Mobile Safari/537.36");

		}
	}
}
