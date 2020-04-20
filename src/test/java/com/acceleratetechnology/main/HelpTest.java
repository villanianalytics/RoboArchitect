package com.acceleratetechnology.main;

import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.tools.ant.types.Commandline;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class HelpTest {

	private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private PrintStream out = new PrintStream(outputStream);
	
	@Test
    public void testHelp() throws IOException {
        System.setOut(out);
    	RAMainApplication.main(Commandline.translateCommandline("-help"));
        String actual = outputStream.toString().trim();
        assertFalse(actual.isEmpty());
    }
}
