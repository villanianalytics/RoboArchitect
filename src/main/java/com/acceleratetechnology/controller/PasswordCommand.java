package com.acceleratetechnology.controller;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import org.apache.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Encrypt password.
 */
public class PasswordCommand extends EncryptDecryptAbstractCommand {
    /**
     * System logger.
     */
    private Logger logger = Logger.getLogger(PasswordCommand.class);
    /**
     * Password file attribute.
     */
    public static final String PASSWORD_FILE_PARAMETER = "/passwordFile";

    @Command("-password")
    public PasswordCommand(String[] args) throws IOException, MissedParameterException {
        super(args);
    }

    @Override
    public void execute() throws MissedParameterException, IOException, NoSuchAlgorithmException, BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, InvalidKeyException {
        String passwordFile = getRequiredAttribute(PASSWORD_FILE_PARAMETER);
        // If you want to change basic secret key, please uncomment the line below.
        // setSECRET_BYTES("SecretKey".getBytes(UTF_8));
        encrypt(passwordFile);
        logger.info("Password was successfully encrypted.");
    }
}
