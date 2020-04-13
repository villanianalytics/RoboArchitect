package com.acceleratetechnology.jdbc;

import java.sql.SQLException;
import java.util.List;

public interface ApplicationJdbc {
	
	public void initializeWithUrl(String jdbcString);
	
	public void createDb(String dbName) throws SQLException;
	
	public List<String[]> executeQuery(String dbName, String query) throws SQLException;
	
	public void executeUpdate(String dbName, String query) throws SQLException;
	
	public void dropTable(String dbName, String tableName) throws SQLException;
	
	public void createTable(String dbName, String tableName, String tableFields) throws SQLException;
	
	public String getDataType();
}
