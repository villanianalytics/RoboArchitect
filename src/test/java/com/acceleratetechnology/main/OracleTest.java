package com.acceleratetechnology.main;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.apache.tools.ant.types.Commandline;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.acceleratetechnology.jdbc.impl.Oracle;

import lombok.Cleanup;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DriverManager.class, Oracle.class})
@PowerMockIgnore("javax.management.*")
public class OracleTest {
	
	private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private PrintStream out = new PrintStream(outputStream);
    private PrintStream originOut = new PrintStream(System.out);
	
	@Test
	public void testOracle() throws SQLException, IOException {
		PowerMockito.mockStatic(DriverManager.class);
		
		final ResultSetMetaData resultSetMetadata=mock(ResultSetMetaData.class);
		when(resultSetMetadata.getColumnCount()).thenReturn(3);
		
		final ResultSet rs = mock(ResultSet.class);
        when(rs.getString(anyInt())).thenReturn("This is mocked value");
        when(rs.getMetaData()).thenReturn(resultSetMetadata);

        final Statement statement = mock(Statement.class);
        when(statement.getResultSet()).thenReturn(rs);
        when(statement.executeQuery(any())).thenReturn(rs);

        final Connection connection = mock(Connection.class);
        when(connection.createStatement()).thenReturn(statement);
        when(DriverManager.getConnection(anyString())).thenReturn(connection);

        testSQL("jdbc:oracle:thin:@localhost:1521/servicenameb", "testDB");
	}
	
	private void testSQL(String jdbcConnection, String db) throws IOException {
		RAMainApplication.main(Commandline
				.translateCommandline("-sql /connection=\"" + jdbcConnection + "\" /op=createDB /db=\"" + db + "\""));

		RAMainApplication.main(
				Commandline.translateCommandline("-sql /connection=\"" + jdbcConnection + "\" /op=importTable /db=\""
						+ db + "\" /mode=OVERWRITE /table=test /srcFile=src/test/resources/test_mysql.csv"));

		System.setOut(out);
		Properties properties = new Properties();
		@Cleanup
		FileReader reader = new FileReader(Paths.get("src/main/resources/log4j.properties").toFile());
		properties.load(reader);
		PropertyConfigurator.configure(properties);
		RAMainApplication.main(Commandline.translateCommandline("-sql /connection=\"" + jdbcConnection
				+ "\" /op=queryDB /db=\"" + db + "\" /query=\"SELECT * from test\" /header=true"));

		String actual = outputStream.toString().trim();

		assertFalse(actual.isEmpty());

		System.setOut(originOut);
		outputStream.reset();
	}
}
