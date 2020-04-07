package com.acceleratetechnology.main;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;

import org.apache.tools.ant.types.Commandline;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.WindowsFakeFileSystem;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class FtpTest {

	private FakeFtpServer fakeFtpServer;

	private TemporaryFolder tempFolder;

	private FileSystem fileSystem;

	@Before
	public void prepare() throws IOException {
		tempFolder = new TemporaryFolder();
		tempFolder.create();

		setupServer();
	}

	private void setupServer() throws IOException {
		Security.addProvider(new BouncyCastleProvider());

		fakeFtpServer = new FakeFtpServer();
		fakeFtpServer.setServerControlPort(8888);
		fileSystem = new WindowsFakeFileSystem();
		fileSystem.add(new DirectoryEntry("c:\\"));
		fileSystem.add(new FileEntry("c:\\fileDownload.txt", "abcdef 1234567890"));
		fakeFtpServer.setFileSystem(fileSystem);

		UserAccount userAccount = new UserAccount("joe", "joe123", "c:\\");
		fakeFtpServer.addUserAccount(userAccount);

		fakeFtpServer.start();
	}

	@After
	public void cleanup() throws InterruptedException {
		fakeFtpServer.stop();
		tempFolder.delete();
	}

	@Test
	public void testFtpUpload() throws IOException {
		File file = File.createTempFile("testUpload", ".txt");

		testSftp("-ftp /type=upload /userName=joe /host=127.0.0.1 /port=8888 /password=joe123 /fromFile="
				+ file.getAbsolutePath() + " /to=./");

		assertTrue(fileSystem.exists("c:\\" + file.getName()));
	}

	@Test
	public void testFtpDownload() throws IOException {
		// check if file exists
		File existingFile = new File(Paths.get("fileDownload.txt").toAbsolutePath().toString());
		Files.deleteIfExists(existingFile.toPath());

		testSftp(
				"-ftp /type=download /userName=joe /host=localhost /port=8888 /password=joe123 /fromFile=fileDownload.txt /to=fileDownload.txt");

		assertTrue(new File(Paths.get("fileDownload.txt").toAbsolutePath().toString()).exists());

		File deleteAfter = new File(Paths.get("fileDownload.txt").toAbsolutePath().toString());
		Files.deleteIfExists(deleteAfter.toPath());
	}

	private void testSftp(String command) throws IOException {
		RAMainApplication.main(Commandline.translateCommandline(command));
	}
}
