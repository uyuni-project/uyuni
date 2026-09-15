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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.common.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.Crypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA256Crypt
 * Utility class to create/check SHA256 passwords and create sha256 checksums
 * Passwords are in the format of $5$salt$encodedpassword.
 * Checksums are just hex values
 */
public class SHA256Crypt {

    private static final Integer SALT_LENGTH = 16; // SHA-256 encoded password salt length

    // buffer size to read file by chunks - 4 MB
    public static final int SHA256_BUFFER_SIZE = 1024 * 1024 * 4;

    /**
     * SHA256Crypt
     */
    private SHA256Crypt() {
    }

    /**
     * getSHA256MD - get SHA256 MessageDigest object instance
     * @return MessageDigest object instance
     */
    private static MessageDigest getSHA256MD() {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e) {
            throw new SHA256CryptException("Problem getting SHA-256 message digest");
        }

        return md;
    }

    /**
     * crypt - method to help in setting passwords.
     * @param key - The key to encode
     * @return Returns a string in the form of "$5$RandomSalt$encodedkey"
     */
    public static String crypt(String key) {
        return crypt(key, CryptHelper.generateRandomSalt(SALT_LENGTH));
    }

    /**
     * crypt
     * Encodes a key using a salt (s) in the same manner as the perl crypt() function
     * @param key - The key to encode
     * @param s - The salt
     * @return Returns a string in the form of "$5$salt$encodedkey"
     * @throws SHA256CryptException SHA256Crypt exception
     */
    public static String crypt(String key, String s) {
        //$5$ takes care that sha256 is used
        s = "$5$" + CryptHelper.getSalt(s, CryptHelper.getSHA256Prefix(), SALT_LENGTH);
        return Crypt.crypt(key, s);
    }

    /**
     * SHA256 and Hexify a string.  Take the input string, SHA256 encode it
     * and then turn it into Hex.
     * @param inputString you want SHA256hexed
     * @return sha256hexed String.
     */
    public static String sha256Hex(String inputString) {
        return sha256Hex(inputString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * SHA256 and Hexify an array of bytes.  Take the input array, SHA256 encodes it
     * and then turns it into Hex.
     * @param secretBytes you want sha256hexed
     * @return sha256hexed String.
     */
    public static String sha256Hex(byte[] secretBytes) {
        String retval = null;
        // add secret
        MessageDigest md;
        md = getSHA256MD();
        md.update(secretBytes);
        // generate the digest
        byte[] digest = md.digest();
        // hexify this puppy
        retval = new String(Hex.encodeHex(digest));
        return retval;
    }

    /**
     * Method: getFileSHA256Sum Purpose: get the SHA256 sum of a file.
     * @param f the file to read
     * @return the SHA256 sum string
     * @throws IOException on IO error
     * @throws SHA256CryptException on getting SHA-256 MessageDigest instance
     */
    public static String getFileSHA256Sum(File f) throws IOException, SHA256CryptException {
        MessageDigest md = getSHA256MD();
        md.reset();
        try (FileInputStream fis = new FileInputStream(f)) {

            byte[] dataBuffer = new byte[SHA256_BUFFER_SIZE];

            int nread = 0;

            while ((nread = fis.read(dataBuffer)) != -1) {
                md.update(dataBuffer, 0, nread);
            }
        }

        byte[] digest = md.digest();

        return new String(Hex.encodeHex(digest));
    }
}
