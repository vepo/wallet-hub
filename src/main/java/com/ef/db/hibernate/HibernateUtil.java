package com.ef.db.hibernate;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import javax.persistence.Entity;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.scannotation.AnnotationDB;
import org.scannotation.ClasspathUrlFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ef.domain.AccessLog;

public class HibernateUtil {
	private static Logger LOGGER = LoggerFactory.getLogger(HibernateUtil.class);

	private static StandardServiceRegistry registry;
	private static SessionFactory sessionFactory = null;

	public static SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			try {
				// Create registry
				registry = new StandardServiceRegistryBuilder().configure().build();

				// Create MetadataSources
				MetadataSources sources = new MetadataSources(registry);

				scanAndAddAnnotatedClasses(sources);

				// Create Metadata
				Metadata metadata = sources.getMetadataBuilder().build();

				// Create SessionFactory
				sessionFactory = metadata.getSessionFactoryBuilder().build();

			} catch (Exception e) {
				LOGGER.error("Database error: " + e.getMessage());
				if (registry != null) {
					StandardServiceRegistryBuilder.destroy(registry);
				}
				throw e;
			}
		}
		return sessionFactory;
	}

	private static void scanAndAddAnnotatedClasses(MetadataSources configuration) {
		ClassLoader classLoader = AccessLog.class.getClassLoader();
		URL url = ClasspathUrlFinder.findClassBase(AccessLog.class);
		try {
			AnnotationDB db = new AnnotationDB();
			db.setScanClassAnnotations(true);
			db.setScanFieldAnnotations(false);
			db.scanArchives(url);
			Set<String> annotatedClasses = db.getAnnotationIndex().get(Entity.class.getName());
			for (String annotatedClass : annotatedClasses) {
				LOGGER.info("Loading class {} on Hibernate config.", annotatedClass);
				configuration.addAnnotatedClass(classLoader.loadClass(annotatedClass));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
