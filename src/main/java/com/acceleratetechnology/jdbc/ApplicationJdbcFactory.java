package com.acceleratetechnology.jdbc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.reflections.Reflections;

public class ApplicationJdbcFactory {
	
	private static Logger logger = Logger.getLogger(ApplicationJdbcFactory.class);
	private static final Map<String, ApplicationJdbc> instances = new HashMap<>();

	static {
		try {
			loadClasses("com.acceleratetechnology.jdbc.impl");
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	private ApplicationJdbcFactory() {
		throw new IllegalStateException("ApplicationJdbcFactory class");
	}

	public static void register(String type, ApplicationJdbc instance) {
		if (type != null && instance != null) {
			instances.put(type, instance);
		}
	}

	public static ApplicationJdbc getInstance(String type) {
		return instances.keySet().stream()
	       .filter(key -> type.toLowerCase().contains(key)).map(instances::get).findFirst().orElse(null);
	}

	private static void loadClasses(String packagePath) throws ClassNotFoundException {
		Reflections reflections = new Reflections(packagePath);    
		Set<Class<? extends ApplicationJdbc>> classes = reflections.getSubTypesOf(ApplicationJdbc.class);
		for (Class<? extends ApplicationJdbc> c : classes) {
			Class.forName(c.getCanonicalName());
		}
	}
}
