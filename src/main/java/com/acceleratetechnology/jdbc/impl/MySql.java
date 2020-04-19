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

public class MySql implements ApplicationJdbc {

	private Logger logger = Logger.getLogger(MySql.class);
	private String jdbcString;

	static {
		ApplicationJdbcFactory.register("mysql", new MySql());
	}
	
	@Override
	public void initializeWithUrl(String jdbcString) {
		this.jdbcString = jdbcString;
	}

	@Override
	public void createDb(String dbName) throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		try (Connection c = DriverManager.getConnection(jdbcString)) {
			String sql = "create database if not exists " + dbName;
			logger.debug(sql);
			try (Statement statement = c.createStatement()) {
				statement.executeUpdate(sql);
				logger.debug("Done");
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	@Override
	public List<String[]> executeQuery(String dbName, String query) throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		List<String[]> results = new LinkedList<>();
		
		try (Connection c = DriverManager.getConnection(jdbcString); Statement stmt = c.createStatement()) {
			try (ResultSet rs = stmt.executeQuery(query)) {
				int numResultCols = rs.getMetaData().getColumnCount();
				
				// add headers
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
			
		}
		
		return results;
	}

	@Override
	public void executeUpdate(String dbName, String query) throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		try (Connection c = DriverManager.getConnection(jdbcString); 
				Statement stmt = c.createStatement()) {
			stmt.executeUpdate(query);
		}
	}

	@Override
	public void dropTable(String dbName, String tableName) throws SQLException, ClassNotFoundException {
		executeUpdate(dbName, "Drop Table if exists " + tableName);
		logger.info("Drop Table if exists " + tableName);
	}

	@Override
	public void createTable(String dbName, String tableName, String tableFields) throws SQLException, ClassNotFoundException {
		executeUpdate(dbName, "CREATE TABLE " + tableName + " (" + tableFields.replace("\"","") + ")");
		logger.info("Create table " + tableName + " (" + tableFields + ")");
	}

	@Override
	public String getDataType() {
		return " VARCHAR(255)";
	}
}
