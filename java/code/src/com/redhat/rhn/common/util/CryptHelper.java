/*
 * Copyright (c) 2014 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.common.util;

import com.redhat.rhn.common.conf.UserDefaults;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * CryptHelper - utility class for crypto routines
 */
public class CryptHelper {
    public static final String MD5_PREFIX = "$1$";
    public static final String SHA256_PREFIX = "$5$";
    private static final byte[] SALTED_MAGIC = "Salted__".getBytes();
    private static final int ITERATIONS = 10000; // 10000 is the default in openssl
    private static final int KEY_LENGTH_IN_BYTE = 256 / 8;

    /**
     * CryptHelper
     */
    private CryptHelper() {
    }

    /**
     * getSalt - Cleans salt parameter
     * @param salt - string in question
     * @return Returns the salt portion of passed-in salt
     */
    static String getSalt(String salt, String prefix, Integer saltLength) {
        // If salt starts with prefix ($1$, $5$) then discard that portion of it
        if (salt.startsWith(prefix)) {
            salt = salt.substring(prefix.length());
        }

        // If we recieve a string such as $1$salt$something else, we only want
        // to keep the salt portion of it
        int end = salt.indexOf('$');
        if (end != -1) {
            salt = salt.substring(0, end);
        }

        // Ensure salt length is <= saltLength
        if (salt.length() > saltLength) {
            salt = salt.substring(0, saltLength);
        }

        return salt;
    }

    /**
     * generateRandomSalt - function to generate random salt string
     * @param saltLength - length of the salt string to generate
     * @return String
     */
    static String generateRandomSalt(Integer saltLength) {
        // a string containing acceptable salt chars
        String b64t = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder salt = new StringBuilder();
        Random r = new SecureRandom();

        for (int i = 0; i < saltLength; i++) {
            int rand = r.nextInt(b64t.length());
            salt.append(b64t.charAt(rand));
        }

        return salt.toString();
    }

    /**
     * Generate a random string as password for PAM Auth
     * @return a random password string
     */
    public static String getRandomPasswordForPamAuth() {
        // We don't require a password when
        // we set use pam authentication, yet the password field
        // in the database is NOT NULL.  So we have to create this
        // stupid HACK!  Actually this is beyond HACK.
        return RandomStringUtils.random(UserDefaults.get().getMaxPasswordLength(), 0, 0,
                true, true, null, new SecureRandom());
    }

    /**
     * Encrypt a text with a password using AES 256 CTR PKCS5 padding with PBKDF2
     * This is compatible with openssl enc command:
     * <p>
     * echo "this is a secret text" | openssl enc -aes-256-ctr -pbkdf2 -e -a
     * </p>
     * @param clearTextIn the text to encrypt
     * @param passwordIn the password
     * @return the encrypted text
     * @throws AESCryptException encrypt errors
     */
    public static String aes256Encrypt(String clearTextIn, String passwordIn) throws AESCryptException {
        if (StringUtils.isEmpty(clearTextIn)) {
            throw new AESCryptException("No data provided");
        }
        try {
            if (StringUtils.isEmpty(passwordIn)) {
                throw new InvalidKeyException("Missing password");
            }
            byte[] salt = new SecureRandom().generateSeed(8);

            PBEKeySpec keySpec = new PBEKeySpec(passwordIn.toCharArray(), salt, ITERATIONS, 48 * 8);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyAndIv = keyFactory.generateSecret(keySpec).getEncoded();
            byte[] sKey = Arrays.copyOfRange(keyAndIv, 0, KEY_LENGTH_IN_BYTE);
            byte[] iv = Arrays.copyOfRange(keyAndIv, KEY_LENGTH_IN_BYTE, KEY_LENGTH_IN_BYTE + 16);

            SecretKeySpec secKey = new SecretKeySpec(sKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/CTR/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secKey, new IvParameterSpec(iv));

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.writeBytes(SALTED_MAGIC);
            bos.writeBytes(salt);
            bos.writeBytes(cipher.doFinal(clearTextIn.getBytes(StandardCharsets.UTF_8)));
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException |
               InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e) {
            throw new AESCryptException("Unable to encrypt text", e);
        }
    }

    /**
     * Encrypt a text with a password using AES 256 CTR PKCS5 padding with PBKDF2
     * This is compatible with openssl enc command:
     * <p>
     * echo "U2FsdGVkX1/DriqjEQKkU/Za...." | openssl enc -aes-256-ctr -pbkdf2 -d -a
     * </p>
     * @param cipherTextIn the text to decrypt
     * @param passwordIn the password
     * @return the decrypted text
     * @throws AESCryptException decrypt error
     */
    public static String aes256Decrypt(String cipherTextIn, String passwordIn) throws AESCryptException {
        if (StringUtils.isEmpty(cipherTextIn)) {
            throw new AESCryptException("No encrypted data provided");
        }
        try {
            if (StringUtils.isEmpty(passwordIn)) {
                throw new InvalidKeyException("Missing password");
            }
            byte[] cipherBytes = Base64.getDecoder().decode(cipherTextIn);
            if (!Arrays.equals(Arrays.copyOfRange(cipherBytes, 0, SALTED_MAGIC.length), SALTED_MAGIC)) {
                throw new IllegalArgumentException(
                        "Bad magic number. Initial bytes from input do not match OpenSSL SALTED_MAGIC salt value.");
            }
            byte[] salt = Arrays.copyOfRange(cipherBytes, SALTED_MAGIC.length, SALTED_MAGIC.length + 8);
            cipherBytes = Arrays.copyOfRange(cipherBytes, SALTED_MAGIC.length + 8, cipherBytes.length);

            PBEKeySpec keySpec = new PBEKeySpec(passwordIn.toCharArray(), salt, ITERATIONS, 48 * 8);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyAndIv = keyFactory.generateSecret(keySpec).getEncoded();
            byte[] sKey = Arrays.copyOfRange(keyAndIv, 0, KEY_LENGTH_IN_BYTE);
            byte[] iv = Arrays.copyOfRange(keyAndIv, KEY_LENGTH_IN_BYTE, KEY_LENGTH_IN_BYTE + 16);

            SecretKeySpec secKey = new SecretKeySpec(sKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/CTR/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secKey, new IvParameterSpec(iv));
            return new String(cipher.doFinal(cipherBytes));
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException |
                InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e) {
            throw new AESCryptException("Unable to decrypt text", e);
        }
    }
}
