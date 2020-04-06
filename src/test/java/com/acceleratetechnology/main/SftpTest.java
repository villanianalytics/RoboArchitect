package com.acceleratetechnology.main;


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.apache.tools.ant.types.Commandline;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SftpTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

    private static final String USERNAME = "username";
    
    private static final String PASSWORD = "password";
    
    private SshServer sshd;
    
    @Before
    public void prepare() throws IOException {
        setupSSHServer();
    }

    private void setupSSHServer() throws IOException {
    	Security.addProvider(new BouncyCastleProvider());
		File homeFolder = tempFolder.getRoot();
		
		VirtualFileSystemFactory f = new VirtualFileSystemFactory(homeFolder.toPath());
		
        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(8001);
        sshd.setHost("localhost");
        sshd.setFileSystemFactory(f);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        sshd.setPublickeyAuthenticator((s, publicKey, serverSession) -> true);
        sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(final String username, final String password, final ServerSession session) {
                return StringUtils.equals(username, USERNAME) && StringUtils.equals(password, PASSWORD);
            }
        });
        sshd.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
        
        sshd.start();
    }
    
    @After
    public void cleanup() throws InterruptedException {
        try {
            sshd.stop(true);
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testSftpUpload() throws IOException {
    	File file = File.createTempFile( "test", "txt");
    	file.deleteOnExit();
    	
    	testSftp("-sftpUpload /type=upload /userName=username /host=localhost /port=8001 /password=password /fromFile=test.txt /to=./");
    	
    	assertTrue(new File(tempFolder.getRoot().getAbsolutePath() + "/test.txt").exists());
    }
    
    @Test
    public void testSftpDownload() throws IOException {
    	final File tempFile = tempFolder.newFile("tempFile.txt");
    	FileUtils.writeStringToFile(tempFile, "hello world", Charset.defaultCharset(), true);
        
    	// check if file exists 
    	File existingFile = new File(Paths.get("tempFile.txt").toAbsolutePath().toString());
    	Files.deleteIfExists(existingFile.toPath());
    	
    	testSftp("-sftpUpload /type=download /userName=username /host=localhost /port=8001 /password=password /fromFile=tempFile.txt /to=./");
    	
    	assertTrue(new File(Paths.get("tempFile.txt").toAbsolutePath().toString()).exists());
    	
    	File deleteAfter = new File(Paths.get("tempFile.txt").toAbsolutePath().toString());
    	Files.deleteIfExists(deleteAfter.toPath());
    }
    
    private void testSftp(String command) throws IOException {
        RAMainApplication.main(Commandline.translateCommandline(command)); 
    }
}
