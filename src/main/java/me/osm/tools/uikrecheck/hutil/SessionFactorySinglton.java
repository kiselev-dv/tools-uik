package me.osm.tools.uikrecheck.hutil;

import me.osm.tools.uikrecheck.bobjects.Revision;
import me.osm.tools.uikrecheck.bobjects.Uik;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class SessionFactorySinglton {
	
	private SessionFactory factory;
	private Configuration configuration;

	private SessionFactorySinglton() {
		configuration = new Configuration().configure("hibernate.cfg.xml");
		
		configuration.addPackage("me.osm.tools.uikrecheck.bobjects");
		configuration.addAnnotatedClass(Uik.class);
		configuration.addAnnotatedClass(Revision.class);
		
		StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().
		applySettings(configuration.getProperties());
		
		factory = configuration.buildSessionFactory(builder.build());
	}
	
	private static final SessionFactorySinglton instance = new SessionFactorySinglton();
	
	public static SessionFactory getFactory() {
		return instance.factory;
	}
}
