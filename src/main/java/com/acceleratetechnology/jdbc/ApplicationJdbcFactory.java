package com.acceleratetechnology.jdbc;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class ApplicationJdbcFactory {
	
	private static Logger logger = Logger.getLogger(ApplicationJdbcFactory.class);
	private static final Map<String, ApplicationJdbc> instances = new HashMap<>();

	static {
		try {
			loadClasses(ApplicationJdbcFactory.class.getClassLoader(), "com/acceleratetechnology/jdbc/impl");
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
	       .filter(key -> type.contains(key)).map(instances::get).findFirst().orElse(null);
	}

	private static void loadClasses(ClassLoader cl, String packagePath) throws IOException, ClassNotFoundException {
		String dottedPackage = packagePath.replaceAll("[/]", ".");

		URL upackage = cl.getResource(packagePath);
		URLConnection conn = upackage.openConnection();

		String rr = IOUtils.toString(conn.getInputStream(), StandardCharsets.UTF_8);

		if (rr != null) {
			String[] paths = rr.split("\n");

			for (String p : paths) {
				if (p.endsWith(".class")) {
					Class.forName(dottedPackage + "." + p.substring(0, p.lastIndexOf('.')));
				}

			}
		}
	}
}
