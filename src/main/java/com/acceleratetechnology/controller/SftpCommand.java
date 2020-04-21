package com.acceleratetechnology.controller;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;


/**
 * The Class SftpCommand.
 */
public class SftpCommand extends EncryptDecryptAbstractCommand {
	
	/** The logger. */
	private Logger logger = Logger.getLogger(SftpCommand.class);
	
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
	
	/** The Constant PRIVATE_KEY. */
	private static final String PRIVATE_KEY = "/privateKey";
	
	/** The Constant FROM_FILE. */
	private static final String FROM_FILE = "/fromFile";
	
	/** The Constant TO_FILE. */
	private static final String TO_FILE = "/to";

	/**
	 * Instantiates a new sftp command.
	 *
	 * @param args the args
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws MissedParameterException the missed parameter exception
	 */
	@Command("-sftp")
	public SftpCommand(String[] args) throws IOException, MissedParameterException {
		super(args);
	}

	/**
	 * Execute.
	 *
	 * @throws Exception the exception
	 */
	@Override
	public void execute() throws Exception {
		logger.trace("SftpCommand.execute operation start");
		String user = getDefaultAttribute(USER_NAME, "");
		String host = getDefaultAttribute(HOST, "");
		int port = Integer.parseInt(getDefaultAttribute(PORT, "22"));
		String password = getPassword("");
		String privateKeyLocation = getDefaultAttribute(PRIVATE_KEY, "");
		String fromFile = getDefaultAttribute(FROM_FILE, "");
		String toFile = getDefaultAttribute(TO_FILE, "./");
		String type = getRequiredAttribute(TYPE);
		
		if (StringUtils.isEmpty(type)) throw new MissedParameterException("A sftp type needs to be provided.");
		
		logger.trace(String.format("Parameters: type %s, host %s, port %s, password %s, privateKeyLocation %s, fromFile %s, toFile %s",
				type, host, String.valueOf(port), password, privateKeyLocation, fromFile, toFile));

		if (DOWNLOAD.equalsIgnoreCase(type)) {
			sftpDownload(host, port, user, password, privateKeyLocation, fromFile, toFile);
			logResponse("Sftp download finished");
		}
		
		if (UPLOAD.contentEquals(type)) {
			sftpUpload(host, port, user, password, privateKeyLocation, fromFile, toFile);
			logResponse("Sftp upload finished");
		}
	}
	
	/**
	 * Setup jsch.
	 *
	 * @param port the port
	 * @param host the host
	 * @param username the username
	 * @param password the password
	 * @param privateKeyLocation the private key location
	 * @return the channel sftp
	 * @throws JSchException the j sch exception
	 * @throws IOException 
	 */
	private Session setupJsch(int port, String host, String username, String password, String privateKeyLocation) throws JSchException {
		logger.trace("SftpCommand.setupJsch operation start");
	    JSch jsch = new JSch();
	    if (!StringUtils.isEmpty(privateKeyLocation)) {
	    	jsch.addIdentity(privateKeyLocation);
	    }
	    	    
	    Session jschSession = jsch.getSession(username, host, port);
	    java.util.Properties config = new java.util.Properties(); 
	    config.put("StrictHostKeyChecking", "no");
	    jschSession.setConfig(config);
	    if (!StringUtils.isEmpty(password)) jschSession.setPassword(password);
	   
	    logger.trace("sftp.setupJsch ended");
	    return jschSession;
	}
	
	/**
	 * Sftp upload.
	 *
	 * @param host the host
	 * @param port the port
	 * @param user the user
	 * @param password the password
	 * @param privateKeyLocation the private key location
	 * @param fromFilePath the from file path
	 * @param toFilePath the to file path
	 * @throws JSchException the j sch exception
	 * @throws SftpException the sftp exception
	 */
	private void sftpUpload(String host, int port, String user, String password, String privateKeyLocation,
			String fromFilePath, String toFilePath) throws JSchException, SftpException  {
		logger.trace("SftpCommand.sftpUpload operation start");
		Session jschSession = setupJsch(port, host, user, password, privateKeyLocation);
		jschSession.connect();
		ChannelSftp channelSftp = (ChannelSftp) jschSession.openChannel("sftp");
		
	    channelSftp.connect();
	    
	    channelSftp.cd(".");
	    File localFile = new File(fromFilePath);
	    channelSftp.put(localFile.getAbsolutePath(), toFilePath);
	    
	    channelSftp.exit();
	    
	    jschSession.disconnect();
	}
	
	/**
	 * Sftp download.
	 *
	 * @param host the host
	 * @param port the port
	 * @param user the user
	 * @param password the password
	 * @param privateKeyLocation the private key location
	 * @param fromFilePath the from file path
	 * @param toFilePath the to file path
	 * @throws JSchException the jsch exception
	 * @throws SftpException the sftp exception
	 */
	private void sftpDownload(String host, int port, String user, String password, String privateKeyLocation,
			String fromFilePath, String toFilePath) throws JSchException, SftpException  {
		logger.trace("SftpCommand.sftpDownload operation start");
		Session jschSession = setupJsch(port, host, user, password, privateKeyLocation);
		jschSession.connect();
		
		ChannelSftp channelSftp = (ChannelSftp) jschSession.openChannel("sftp");
		
	    channelSftp.connect();
	    channelSftp.get(fromFilePath, toFilePath);
	    channelSftp.exit();
	    
	    jschSession.disconnect();
	}
}
