package com.acceleratetechnology.main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.apache.tools.ant.types.Commandline;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import lombok.Cleanup;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.management.*" })
public class SqlMsAccess {

	private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private PrintStream out = new PrintStream(outputStream);
	private PrintStream originOut = new PrintStream(System.out);
	public static String csvFile = "a,b,c,d,e,f,g,h,i\n" + "a,b,c,d,e,f,g,h,i\n" + "aa,bb,cc,dd,ee,ff,gg,hh,ii\n"
			+ "ab,cd,ef,gh,ij,kl,mn,op,qr";

	@Test
	public void testSqlMsAccess() throws IOException {
		File fileToCopy = new File(
				"src" + File.separator + "test" + File.separator + "resources" + File.separator + "newdb.accdb");
		File newFile = new File(
				"src" + File.separator + "test" + File.separator + "resources" + File.separator + "newdb_temp.accdb");

		FileUtils.copyFile(fileToCopy, newFile);

		testSQL("jdbc:ucanaccess://src/test/resources/newdb_temp.accdb", "newdb_temp.accdb");

		FileUtils.deleteQuietly(newFile);
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

		Assert.assertEquals(csvFile, actual);

		System.setOut(originOut);
		outputStream.reset();
	}
}
