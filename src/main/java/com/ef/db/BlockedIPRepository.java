package com.ef.db;

import org.hibernate.Session;

/**
 * BlockedIP Repository
 * 
 * @author victor
 *
 */
public class BlockedIPRepository extends AbstractRepository {

	public BlockedIPRepository(Session session) {
		super(session);
	}

}
