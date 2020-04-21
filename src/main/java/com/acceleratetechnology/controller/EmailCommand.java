package com.acceleratetechnology.controller;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import org.apache.log4j.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.CC;

/**
 * Send an email to user with or without attached files.
 */
public class EmailCommand extends EncryptDecryptAbstractCommand {
    private static final String FROM_COMMAND = "/from";
    private static final String TO_COMMAND = "/to";
    private static final String CC_COMMAND = "/cc";
    private static final String BCC_COMMAND = "/bcc";
    private static final String ATTACHMENT_COMMAND = "/attachment";
    /**
     * System logger.
     */
    private Logger logger = Logger.getLogger(EmailCommand.class);
    /**
     * HTML text formatter.
     */
    private static final String TEXT_HTML_FORMAT = "text/html";
    /**
     * Positive answer.
     */
    private static final String Y_PARAMETER = "Y";
    /**
     * Connection type TLS protocol.
     */
    private static final String TLS_TYPE = "TLS";
    /**
     * Connection type SSL protocol.
     */
    private static final String SSL_TYPE = "SSL";
    /**
     * Sender email property field.
     */
    public static final String FROMADDRESS_CONFIG_PROPERTY = "fromaddress";
    /**
     * Recipient mail property field.
     */
    public static final String TOADDRESS_CONFIG_PROPERTY = "toaddress";
    /**
     * Property field with subject of a letter.
     */
    public static final String SUBJECT_CONFIG_PROPERTY = "subject";
    /**
     * Connection type property field.
     */
    public static final String CTYPE_CONFIG_PROPERTY = "ctype";
    /**
     * SMTP mail service property field.
     */
    public static final String SMTPSERVER_CONFIG_PROPERTY = "smtpserver";
    /**
     * Port property field.
     */
    public static final String PORT_CONFIG_PROPERTY = "port";
    /**
     * CC mail property field.
     */
    public static final String CCADDRESS_CONFIG_PROPERTY = "ccaddress";
    /**
     * BCC mail property field.
     */
    public static final String BCCADDRESS = "bccaddress";
    /**
     * Attachment property field.
     */
    public static final String ATTACHMENT_CONFIG_PROPERTY = "attachment";
    /**
     * Property file with body of a message.
     */
    public static final String BODY_CONFIG_PROPERTY = "body";
    /**
     * Html formatter field.
     */
    public static final String HTMLBODY_CONFIG_PROPERTY = "htmlbody";
    /**
     * Email optional attribute with subject of an email.
     */
    private static final String SUBJECT_PARAM = "/subject";
    /**
     * Email optional attribute with body of an email.
     */
    private static final String BODY_PARAM = "/body";
    //String parse
    /**
     * New line regex.
     */
    private static final String NEW_LINE_REGEX = "(\\\\n)";
    /**
     * Tab regex.
     */
    private static final String TAB_REGEX = "(\\\\t)";
    /**
     * New line string symbol.
     */
    private static final String NEW_LINE_STRING = "\n";
    /**
     * Tab string symbol.
     */
    private static final String TAB_STRING = "\t";
    /**
     * Negative user answer.
     */
    private static final String NEGATIVE_ANSWER = "N";

    @Command("-email")
    public EmailCommand(String[] args) throws IOException, MissedParameterException {
        super(args);
    }

    @Override
    public void execute() throws IOException, MissedParameterException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, MessagingException {
    	logger.trace("EmailCommand.execute started");
    	String subject = getFromConfOrAttribute(SUBJECT_CONFIG_PROPERTY, SUBJECT_PARAM);
        String body = getFromConfOrAttribute(BODY_CONFIG_PROPERTY, BODY_PARAM);
        String password = getPassword();

        String fromAddress = getRequiredFromConfOrAttribute(FROMADDRESS_CONFIG_PROPERTY, FROM_COMMAND);
        String toAddress = getRequiredFromConfOrAttribute(TOADDRESS_CONFIG_PROPERTY, TO_COMMAND);
        String ccAddress = getFromConfOrAttribute(CCADDRESS_CONFIG_PROPERTY, CC_COMMAND);
        String bccAddress = getFromConfOrAttribute(BCCADDRESS, BCC_COMMAND);
        String attachment = getFromConfOrAttribute(ATTACHMENT_CONFIG_PROPERTY, ATTACHMENT_COMMAND);
        String htmlBody = getAttribute(HTMLBODY_CONFIG_PROPERTY);

        String cType = getRequiredAttribute(CTYPE_CONFIG_PROPERTY);
        String smtpServer = getRequiredAttribute(SMTPSERVER_CONFIG_PROPERTY);
        String port = getRequiredAttribute(PORT_CONFIG_PROPERTY);

        if (subject != null) {
            subject = subject.replaceAll(NEW_LINE_REGEX, NEW_LINE_STRING)
                    .replaceAll(TAB_REGEX, TAB_STRING);
        } else {
            subject = "";
        }
        if (body != null) {
            body = body.replaceAll(NEW_LINE_REGEX, NEW_LINE_STRING)
                    .replaceAll(TAB_REGEX, TAB_STRING);
        } else {
            body = "";
        }
        if (htmlBody == null) {
            htmlBody = NEGATIVE_ANSWER;
        }

        sendEmail(cType, smtpServer, Integer.parseInt(port), fromAddress, password, htmlBody, subject, body, attachment, fromAddress, toAddress, ccAddress, bccAddress);
        
        logResponse("Mail sent successfully!");
    }

    /**
     * Sends an email.
     *
     * @param cType       Connection type.
     * @param smtpServer  Smtp server.
     * @param port        port.
     * @param username    Username of sender email.
     * @param password    Password from email.
     * @param htmlBody    Html body.
     * @param subject     Subject of a letter.
     * @param body        Body of a message.
     * @param attachment  Attachment file.
     * @param fromAddress Sender email address.
     * @param toAddress   Recipient email address.
     * @param ccAddress   Copy email address.
     * @param bccAddress  Secret copy email address.
     * @throws MessagingException
     * @throws MissedParameterException
     */
    private void sendEmail(String cType, String smtpServer, Integer port, String username, String password, String htmlBody, String subject, String body, String attachment, String fromAddress, String toAddress, String ccAddress, String bccAddress) throws MessagingException, MissedParameterException, FileNotFoundException {
    	logger.trace("EmailCommand.sendEmail started");
    	
    	final String user = username;
        final String pass = password;

        Properties props = new Properties();
        props.put("mail.smtp.host", smtpServer);
        props.put("mail.smtp.socketFactory.port", port);
        if (cType.equalsIgnoreCase(SSL_TYPE)) {
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        } else if (cType.equalsIgnoreCase(TLS_TYPE)) {
            props.put("mail.smtp.starttls.enable", "true");
        } else {
            throw new MissedParameterException("Invalid connection type");
        }

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", port);

        Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromAddress));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
        message.setSubject(subject);

        if (attachment == null || attachment.isEmpty()) {
            if (htmlBody.equals(Y_PARAMETER)) {
                message.setContent(body, TEXT_HTML_FORMAT);
            } else {
                message.setText(body);
            }
        } else {
            BodyPart messageBodyPart = new MimeBodyPart();
            if (htmlBody.equals(Y_PARAMETER)) {
                messageBodyPart.setContent(body, TEXT_HTML_FORMAT);
            } else {
                messageBodyPart.setText(body);
            }

            Multipart multipart = new MimeMultipart();

            multipart.addBodyPart(messageBodyPart);

            messageBodyPart = new MimeBodyPart();
            File attachedFile = Paths.get(attachment).toFile();
            if (!attachedFile.exists()) {
                throw new FileNotFoundException("Your attached file " + attachment + " does not exist.");
            }
            DataSource source = new FileDataSource(attachedFile);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(source.getName());
            multipart.addBodyPart(messageBodyPart);

            message.setContent(multipart);
        }

        if (ccAddress != null && !ccAddress.isEmpty()) {
            message.setRecipients(CC, InternetAddress.parse(ccAddress));
        }
        if (bccAddress != null && !bccAddress.isEmpty()) {
            message.setRecipients(BCC, InternetAddress.parse(bccAddress));
        }

        Transport.send(message);
    }


    public String getRequiredFromConfOrAttribute(String configKey, String attribute) throws MissedParameterException {
    	logger.trace("EmailCommand.getRequiredFromConfOrAttribute started");
    	String value = getFromConfOrAttribute(configKey, attribute);
        if (value == null) {
            throw new MissedParameterException("Attribute was missed. Please type \"" + configKey + "\" in a config file or \"" + attribute + "\" in a command.");
        }
        return value;
    }
}
