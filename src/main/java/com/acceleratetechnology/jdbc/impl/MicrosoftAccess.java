package com.acceleratetechnology.jdbc.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.acceleratetechnology.jdbc.ApplicationJdbc;
import com.acceleratetechnology.jdbc.ApplicationJdbcFactory;

import lombok.Cleanup;

public class MicrosoftAccess implements ApplicationJdbc {

	private Logger logger = Logger.getLogger(MicrosoftAccess.class);
	private String jdbcString;

	static {
		ApplicationJdbcFactory.register("ucanaccess", new MicrosoftAccess());
	}

	@Override
	public void initializeWithUrl(String jdbcString) {
		this.jdbcString = jdbcString;
	}

	@Override
	public void createDb(String dbName) throws SQLException, ClassNotFoundException {
		Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
		@Cleanup
		Connection c = DriverManager.getConnection(jdbcString);
		c.close();
	}

	@Override
	public List<String[]> executeQuery(String dbName, String query) throws ClassNotFoundException {
		Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
		List<String[]> results = new LinkedList<>();

		try (Connection c = DriverManager.getConnection(jdbcString); Statement stmt = c.createStatement()) {
			try (ResultSet rs = stmt.executeQuery(query)) {
				int numResultCols = rs.getMetaData().getColumnCount();
				List<String> headers = new ArrayList<>();
				for (int i = 1; i <= numResultCols; i++) {
					headers.add(rs.getMetaData().getColumnName(i));
				}
				results.add(headers.toArray(new String[0]));

				// add results
				while (rs.next()) {
					List<String> result = new ArrayList<>();
					for (int i = 1; i <= numResultCols; i++) {
						result.add(rs.getString(i));
					}

					results.add(result.toArray(new String[0]));
				}
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

		return results;
	}

	@Override
	public void executeUpdate(String dbName, String query) throws ClassNotFoundException {
		Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
		try (Connection c = DriverManager.getConnection(jdbcString); Statement stmt = c.createStatement()) {
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public void dropTable(String dbName, String tableName) throws ClassNotFoundException {
		executeUpdate(dbName, "Drop Table if exists " + tableName);
		logger.info("Drop Table if exists " + tableName);
	}

	@Override
	public void createTable(String dbName, String tableName, String tableFields) throws ClassNotFoundException {
		executeUpdate(dbName, "Create table " + tableName + " (" + tableFields.replace("\"","") + ")");
		logger.info("Create table " + tableName + " (" + tableFields + ")");	
	}

	@Override
	public String getDataType() {
		return " VARCHAR(255)";
	}
}
