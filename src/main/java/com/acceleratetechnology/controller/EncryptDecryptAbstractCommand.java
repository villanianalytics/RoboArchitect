package com.acceleratetechnology.controller;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Encrypt and decrypt password by using AES algorithm.
 */
public abstract class EncryptDecryptAbstractCommand extends AbstractCommand {
    /**
     * Writer of logs to console and to file.
     */
    private final Logger logger = Logger.getLogger(EncryptDecryptAbstractCommand.class);
    /**
     * Secret key. Some random bytes encryption by default, otherwise specify as a command parameter.
     */
    @Setter private static byte[] SECRET_BYTES = {12, 54, 12, 1, 23, 42, 66, 102, 1, 10, 66, 122};
    /**
     * Max key length.
     */
    private static final int NEW_KEY_LENGTH = 16;
    /**
     * MessageDigest algorithm.
     */
    private static final String ALGORITHM = "SHA-1";
    /**
     * Encrypt method.
     */
    private static final String ENCRYPT_METHOD = "AES";
    /**
     * Secret key.
     */
    private static SecretKeySpec secretKey;
    /**
     * Password field in connect property file.
     */
    private static final String USER_PASSWORD_PARAMETER = "password";
    /**
     * Password file attribute.
     */
    private static final String PASSWORD_FILE_PARAM = "/passwordFile";

    public EncryptDecryptAbstractCommand(String[] args) throws IOException, MissedParameterException {
        super(args);
    }

    /**
     * Initialize secret key specification.
     *
     * @param myKey Secret key.
     * @throws NoSuchAlgorithmException thrown when a particular cryptographic algorithm is requested but is not available in the environment.
     */
    public void setKey(byte[] myKey) throws NoSuchAlgorithmException {
        MessageDigest sha;

        sha = MessageDigest.getInstance(ALGORITHM);
        myKey = sha.digest(myKey);
        myKey = Arrays.copyOf(myKey, NEW_KEY_LENGTH);
        secretKey = new SecretKeySpec(myKey, ENCRYPT_METHOD);
    }

    /**
     * Encrypt user password and write encrypted password to a file.
     *
     * @param passwordDestinationFile Password destination file.
     * @throws InvalidKeyException       thrown if {@link #secretKey} is invalid (invalid encoding, wrong length, uninitialized, etc).
     * @throws NoSuchPaddingException    thrown if transformation contains a padding scheme that is not available.
     * @throws NoSuchAlgorithmException  thrown if transformation is null, empty, in an invalid format, or if no Provider supports a CipherSpi implementation for the specified algorithm.
     * @throws IOException               thrown in case of an I/O error
     * @throws BadPaddingException       thrown if this cipher is in decryption mode, and (un)padding has been requested,
     *                                   but the decrypted data is not bounded by the appropriate padding bytes.
     * @throws IllegalBlockSizeException thrown if this cipher is a block cipher, no padding has been requested (only in encryption mode),
     *                                   and the total input length of the data processed by this cipher is not a multiple of block size;
     *                                   or if this encryption algorithm is unable to process the input data provided.
     */
    public void encrypt(String passwordDestinationFile) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException {
        Console console = System.console();

        logger.info("Please enter your password and press Enter:");
        if (console == null) {
            logger.error("No console available");
        }

        char[] pass = console.readPassword(" ", "Enter");
        String password = String.valueOf(pass);
        logger.info("Thanks, password was entered.");

        logger.debug("Start to encrypt it.");
        setKey(SECRET_BYTES);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        String encryptedPSWD = Base64.getEncoder().encodeToString(cipher.doFinal(password.getBytes(UTF_8)));
        logger.debug("Password encrypted.");

        logger.debug("Start writing to file \"" + passwordDestinationFile + "\".");
        Path path = Paths.get(passwordDestinationFile);
        File passwordFile = path.toAbsolutePath().toFile();

        passwordFile.getParentFile().mkdirs();

        FileUtils.write(passwordFile, encryptedPSWD, UTF_8);
        logger.debug("Done.");
    }

    /**
     * Decrypt user password from a file.
     *
     * @return Return encrypted password.
     * @throws InvalidKeyException       thrown if {@link #secretKey} is invalid (invalid encoding, wrong length, uninitialized, etc).
     * @throws NoSuchPaddingException    thrown if transformation contains a padding scheme that is not available.
     * @throws NoSuchAlgorithmException  thrown if transformation is null, empty, in an invalid format, or if no Provider supports a CipherSpi implementation for the specified algorithm.
     * @throws IOException               thrown in case of an I/O error
     * @throws BadPaddingException       thrown if this cipher is in decryption mode, and (un)padding has been requested,
     *                                   but the decrypted data is not bounded by the appropriate padding bytes.
     * @throws IllegalBlockSizeException thrown if this cipher is a block cipher, no padding has been requested (only in encryption mode),
     *                                   and the total input length of the data processed by this cipher is not a multiple of block size;
     *                                   or if this encryption algorithm is unable to process the input data provided.
     */
    public String decrypt(File passwordFile) throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        logger.debug("Read password from a file.");
        String password = FileUtils.readFileToString(passwordFile, UTF_8);
        logger.debug("Done.");
        logger.debug("Start to decrypt.");
        setKey(SECRET_BYTES);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(password)));
    }

    /**
     * Get password from file if it is specified or from config file..
     *
     * @return password.
     * @throws MissedParameterException  Throws if expected command is wrong or missed.
     * @throws InvalidKeyException       thrown if it is impossible or unsafe to wrap the key with this cipher (e.g., a hardware protected key is being passed to a software-only cipher).
     * @throws NoSuchPaddingException    thrown if transformation contains a padding scheme that is not available.
     * @throws NoSuchAlgorithmException  thrown if transformation is null, empty, in an invalid format, or if no Provider supports a CipherSpi implementation for the specified algorithm.
     * @throws IOException               thrown in case of an I/O error
     * @throws BadPaddingException       thrown if this cipher is in decryption mode, and (un)padding has been requested,
     *                                   but the decrypted data is not bounded by the appropriate padding bytes.
     * @throws IllegalBlockSizeException thrown if this cipher is a block cipher, no padding has been requested (only in encryption mode),
     *                                   and the total input length of the data processed by this cipher is not a multiple of block size;
     *                                   or if this encryption algorithm is unable to process the input data provided.
     */
    public String getPassword() throws MissedParameterException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, IOException {
        String passwordFile = getAttribute(PASSWORD_FILE_PARAM);

        String password;
        if (passwordFile == null) {
            logger.debug("Get user password from config file.");
            password = getRequiredAttribute(USER_PASSWORD_PARAMETER);
            logger.debug("Done.");
        } else {
            File pswdFile = Paths.get(passwordFile).toFile();
            // If you want to change basic secret key, please uncomment the line below.
            // setSECRET_BYTES("SecretKey".getBytes(UTF_8));
            password = decrypt(pswdFile);
            logger.debug("Decrypted.");
        }

        if (password == null || password.isEmpty()) {
            throw new MissedParameterException("Sorry, but firstly you must enter your password.");
        }
        return password;
    }
}