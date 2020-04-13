package com.acceleratetechnology.main;

import static com.wix.mysql.EmbeddedMysql.anEmbeddedMysql;
import static com.wix.mysql.config.Charset.UTF8;
import static com.wix.mysql.config.MysqldConfig.aMysqldConfig;
import static com.wix.mysql.distribution.Version.v5_7_27;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.PropertyConfigurator;
import org.apache.tools.ant.types.Commandline;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.config.MysqldConfig;

import lombok.Cleanup;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*"})
public class SqlMysqlTest {

	private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private PrintStream out = new PrintStream(outputStream);
    private PrintStream originOut = new PrintStream(System.out);

    public static String csvFile = "a,b,c,d,e,f,g,h,i\n" +
                                   "a,b,c,d,e,f,g,h,i\n" +
                                   "aa,bb,cc,dd,ee,ff,gg,hh,ii\n" +
                                   "ab,cd,ef,gh,ij,kl,mn,op,qr";
	
	@Test
	public void testSqlMysql() throws IOException {
		MysqldConfig config = aMysqldConfig(v5_7_27)
				.withCharset(UTF8)
				.withPort(2215)
				.withUser("user", "pass")
				.withTimeout(2, TimeUnit.MINUTES)
				.withServerVariable("max_connect_errors", 666).build();

		EmbeddedMysql mysqld = anEmbeddedMysql(config).addSchema("testdb").start();
		String jdbcConnection = "jdbc:mysql://user:pass@localhost:2215/testDB";
		String db = "testDB";
		RAMainApplication.main(Commandline.translateCommandline("-sql /connection=\"" + jdbcConnection + "\" /op=createDB /db=\"" + db + "\""));

		RAMainApplication.main(Commandline.translateCommandline("-sql /connection=\"" + jdbcConnection + "\" /op=importTable /db=\"" + db + "\" /mode=OVERWRITE /table=test /srcFile=src/test/resources/test_mysql.csv"));
		
		testSQL(jdbcConnection, db);
		
		mysqld.stop(); 
	}
	
    private void testSQL(String jdbcConnection, String db) throws IOException {
        System.setOut(out);
        Properties properties = new Properties();
        @Cleanup FileReader reader = new FileReader(Paths.get("src/main/resources/log4j.properties").toFile());
        properties.load(reader);
        PropertyConfigurator.configure(properties);
        RAMainApplication.main(Commandline.translateCommandline("-sql /connection=\"" + jdbcConnection + "\" /op=queryDB /db=\"" + db + "\" /query=\"SELECT * from test\" /header=true"));

        String actual = outputStream.toString().trim();

        Assert.assertEquals(csvFile, actual);

        System.setOut(originOut);
    }
}
