/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.common.security;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * A class to generate Hashed Message Authentication Codes.
 *
 */
public class HMAC {

    private HMAC() {
    }

    private static final String HEXCHARS = "0123456789abcdef";

    /**
     * Convert a byte array to a hex string of the format
     * "1f 30 b7".  package protected so that SessionSwap can use it.
     * @param a The byte array to convert
     * @return the resulting hex string
     */
    public static String byteArrayToHex(byte[] a) {
        int hn, ln, cx;
        StringBuilder buf = new StringBuilder(a.length * 2);
        for (cx = 0; cx < a.length; cx++) {
            hn = ((a[cx]) & 0x00ff) / 16;
            ln = (a[cx]) & 0x000f;
            buf.append(HEXCHARS.charAt(hn));
            buf.append(HEXCHARS.charAt(ln));
        }
        return buf.toString();
    }

    /**
     * Generate an HMAC hash for the given text and key using SHA256 as the
     * hash function.
     * @param text The text to hash
     * @param key The key to use when generating the hash.
     * @return The resulting hash string
     */
    public static String sha256(String text, String key) {
        try {
            SecretKey skey = new SecretKeySpec(key.getBytes(), "HMACSHA256");
            Mac mac = Mac.getInstance(skey.getAlgorithm());
            mac.init(skey);
            mac.update(text.getBytes());
            return byteArrayToHex(mac.doFinal());
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No such alg: " + e);
        }
        catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid key: " + e);
        }
    }
}
