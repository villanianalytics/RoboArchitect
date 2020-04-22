package com.acceleratetechnology.main;

import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Transport;

import org.apache.log4j.PropertyConfigurator;
import org.apache.tools.ant.types.Commandline;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.acceleratetechnology.controller.EncryptDecryptAbstractCommand;

import lombok.Cleanup;

@RunWith(PowerMockRunner.class)
@PrepareForTest({javax.mail.Session.class,javax.mail.Transport.class, System.class, EncryptDecryptAbstractCommand.class })
@PowerMockIgnore("javax.crypto.*") 
public class EmailCommandTest {

	@Mock Console console;
	
	private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private PrintStream out = new PrintStream(outputStream);
	public static final String CONFIG_FILE = "src" + File.separator + "main" + File.separator + "resources"
			+ File.separator + "config_email.properties";
	public static final String LOG4J = "src" + File.separator + "main" + File.separator + "resources"
			+ File.separator + "log4j.properties";
	public static final String PASSWORD_FILE = "src" + File.separator + "test" + File.separator + "resources"
			+ File.separator + "password.txt";
	
	

	@Before
	public void before() throws Exception {
		MemberModifier.suppress(MemberModifier.method(Transport.class, "send", Message.class));
	}

	@Test
	public void testEmailCommand() throws Exception {
		System.setOut(out);
		RAMainApplication
				.main(Commandline.translateCommandline("-email /config=" + Paths.get(CONFIG_FILE) + " /password=test"));
		String actual = outputStream.toString().trim();
		assertFalse(actual.isEmpty());
	}
	
	@Test
	public void testEmailCommandWithAttachment() throws Exception {
		System.setOut(out);
		RAMainApplication
				.main(Commandline.translateCommandline("-email /config=" + Paths.get(CONFIG_FILE) + " /attachment="+ Paths.get(CONFIG_FILE)+" /password=test"));
		String actual = outputStream.toString().trim();
		assertFalse(actual.isEmpty());
	}
	
	@Test
	public void testEmailCommandWithSubject() throws Exception {
		System.setOut(out);
		RAMainApplication
				.main(Commandline.translateCommandline("-email /config=" + Paths.get(CONFIG_FILE) + " /subject=testsubject /password=test"));
		String actual = outputStream.toString().trim();
		assertFalse(actual.isEmpty());
	}
	
	@Test
	public void testEmailCommandWithPasswordFile() throws Exception {
		System.setOut(out);
		Properties properties = new Properties();
        @Cleanup FileReader reader = new FileReader(Paths.get(LOG4J).toFile());
        properties.load(reader);
        PropertyConfigurator.configure(properties);
        
		String command = "-password /passwordFile=\"" + Paths.get(PASSWORD_FILE) + "\"";
        PowerMockito.mockStatic(System.class);

        PowerMockito.when(System.console()).thenReturn(console);

        String password = "test";
        PowerMockito.when(console.readPassword(" ", "Enter")).thenReturn(password.toCharArray());

        RAMainApplication.main(Commandline.translateCommandline(command));
        
		RAMainApplication.main(Commandline.translateCommandline("-email /config=" + Paths.get(CONFIG_FILE) + " /subject=testsubject /passwordFile=" + Paths.get(PASSWORD_FILE)));
		String actual = outputStream.toString().trim();
		assertFalse(actual.isEmpty());
	}
}
