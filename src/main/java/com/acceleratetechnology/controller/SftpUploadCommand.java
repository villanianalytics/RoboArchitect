package com.acceleratetechnology.controller;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * SftpUpload command
 */
public class SftpUploadCommand extends EncryptDecryptAbstractCommand {
	
	/**
	 * System logger
	 */
	private Logger logger = Logger.getLogger(SftpUploadCommand.class);
	
	private static final String USER_NAME = "/userName";
	private static final String HOST = "/host";
	private static final String PORT = "/port";
	private static final String PASSWORD = "/password";
	private static final String PRIVATE_KEY = "/privateKey";
	private static final String FROM_FILE = "/fromFile";
	private static final String TO_FILE = "/to";
	
	/**
	 * @throws MissedParameterException 
	 * @throws IOException 
	 */
	@Command("-sftpUpload")
	public SftpUploadCommand(String[] args) throws IOException, MissedParameterException {
		super(args);
	}

	@Override
	public void execute() throws Exception {
		logger.info("Sftp command");
		String user = getDefaultAttribute(USER_NAME, "");
		String host = getDefaultAttribute(HOST, "");
		int port = Integer.parseInt(getDefaultAttribute(PORT, "22"));
		String password = getDefaultAttribute(PASSWORD, "");
		String privateKeyLocation = getDefaultAttribute(PRIVATE_KEY, "");
		String fromFile = getDefaultAttribute(FROM_FILE, "");
		String toFile = getDefaultAttribute(TO_FILE, "./");
		
		sftpUpload(host, port, user, password, privateKeyLocation, fromFile, toFile);
	}
	
	private void sftpUpload(String host, int port, String user, 
							String password, String privateKeyLocation, 
							String fromFilePath, String toFilePath) {
		Session session = null;
		Channel channel = null;
		
		try {
			final JSch ssh = new JSch();
			if (!StringUtils.isEmpty(privateKeyLocation)) ssh.addIdentity(privateKeyLocation);
			
			session = ssh.getSession(user, host, port);
			if (!StringUtils.isEmpty(password)) session.setPassword(password);
			
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();
			
			channel = session.openChannel("sftp");
			channel.connect();
			
			final ChannelSftp sftp = (ChannelSftp) channel;
			logger.info("Connexion SFTP to " + host + " estabilished. ");
			sftp.put(fromFilePath, toFilePath);
			logger.info("File sftp uploaded with success!");

		} catch (final JSchException e) {
			logger.error("Error: " + e.getMessage());
		} catch (SftpException e) {
			logger.error("Sftp Error: " + e.getMessage());
		} finally {
			if (channel != null) {
				channel.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}
		}
	}
}
