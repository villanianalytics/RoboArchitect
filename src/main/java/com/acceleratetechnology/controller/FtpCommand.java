package com.acceleratetechnology.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;

/**
 * The Class FtpCommand.
 */
public class FtpCommand extends EncryptDecryptAbstractCommand {

	/** The logger. */
	private Logger logger = Logger.getLogger(FtpCommand.class);

	/** The Constant DOWNLOAD. */
	private static final String DOWNLOAD = "download";

	/** The Constant UPLOAD. */
	private static final String UPLOAD = "upload";

	/** The Constant TYPE. */
	private static final String TYPE = "/type";

	/** The Constant USER_NAME. */
	private static final String USER_NAME = "/userName";

	/** The Constant HOST. */
	private static final String HOST = "/host";

	/** The Constant PORT. */
	private static final String PORT = "/port";

	/** The Constant FROM_FILE. */
	private static final String FROM_FILE = "/fromFile";

	/** The Constant TO_FILE. */
	private static final String TO_FILE = "/to";

	/**
	 * Instantiates a new ftp command.
	 *
	 * @param args the args
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws MissedParameterException the missed parameter exception
	 */
	@Command("-ftp")
	public FtpCommand(String[] args) throws IOException, MissedParameterException {
		super(args);
	}

	/**
	 * Execute.
	 *
	 * @throws Exception the exception
	 */
	@Override
	public void execute() throws Exception {
		logger.info("Ftp command initiliaze");
		String user = getDefaultAttribute(USER_NAME, "");
		String host = getDefaultAttribute(HOST, "");
		int port = Integer.parseInt(getDefaultAttribute(PORT, "21"));
		String password = getPassword();
		String fromFile = getDefaultAttribute(FROM_FILE, "");
		String toFile = getDefaultAttribute(TO_FILE, "");
		String type = getRequiredAttribute(TYPE);

		if (StringUtils.isEmpty(type))
			throw new MissedParameterException("A ftp type needs to be provided.");

		logger.info(String.format(
				"Parameters: type %s, host %s, port %s, password %s, fromFile %s, toFile %s",
				type, host, String.valueOf(port), password, fromFile, toFile));

		if (DOWNLOAD.equalsIgnoreCase(type)) {
			ftpDownload(host, port, user, password, fromFile, toFile);
		}

		if (UPLOAD.contentEquals(type)) {
			ftpUpload(host, port, user, password, fromFile, toFile);
		}

		logger.info("Ftp command finished");
	}
	
	/**
	 * Ftp upload.
	 *
	 * @param host the host
	 * @param port the port
	 * @param user the user
	 * @param password the password
	 * @param privateKeyLocation the private key location
	 * @param fromFilePath the from file path
	 * @param toFilePath the to file path
	 */
	private void ftpUpload(String host, int port, String user, String password,
			String fromFilePath, String toFilePath){
		FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect(host, port);
			ftpClient.login(user, password);
			ftpClient.enterLocalPassiveMode();
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			
			File file = new File(fromFilePath);
			InputStream inputStream = new FileInputStream(file);
			logger.info("Uploading File");
			boolean done = ftpClient.storeFile(toFilePath, inputStream);
			inputStream.close();
			logger.info("Upload Completed : " + done);

		} catch (IOException ex) {
			logger.error(ex.getMessage());
		} finally {
			try {
				if (ftpClient.isConnected()) {
					ftpClient.logout();
					ftpClient.disconnect();
				}
			} catch (IOException ex) {
				logger.error(ex.getMessage());
			}
		}
	}

	/**
	 * Ftp download.
	 *
	 * @param host the host
	 * @param port the port
	 * @param user the user
	 * @param password the password
	 * @param privateKeyLocation the private key location
	 * @param fromFilePath the from file path
	 * @param toFilePath the to file path
	 */
	private void ftpDownload(String host, int port, String user, String password,
			String fromFilePath, String toFilePath) {
		FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect(host, port);
			ftpClient.login(user, password);
			ftpClient.enterLocalPassiveMode();
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			
			logger.info("Download File");
			OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(toFilePath));
	        boolean success = ftpClient.retrieveFile(fromFilePath, outputStream1);
	        outputStream1.close();
			logger.info("Download Completed : " + success);

		} catch (IOException ex) {
			logger.error(ex.getMessage());
		} finally {
			try {
				if (ftpClient.isConnected()) {
					ftpClient.logout();
					ftpClient.disconnect();
				}
			} catch (IOException ex) {
				logger.error( ex.getMessage());
			}
		}
	}
}
