/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.common.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Pbkdf2Sha256Crypt
 * Utility class to create/check PBKDF2-SHA256 passwords.
 * Passwords are in the format of $pbkdf2-sha256$iterations$b64salt$b64hash
 * with base64 padding stripped.
 */
public class Pbkdf2Sha256Crypt {

    public static final String PREFIX = "$pbkdf2-sha256$";

    // PBKDF2 iteration bounds — must match python/spacewalk/server/rhnUser.py.
    private static final int MIN_ITERATIONS = 600_000;
    private static final int MAX_ITERATIONS = 2_000_000;
    private static final int KEY_LENGTH_BITS = 256;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private Pbkdf2Sha256Crypt() {
    }

    /**
     * Verify a candidate password against a stored PBKDF2-SHA256 hash produced by
     * the Python encrypt_password() helper. Stored format is
     * {@code $pbkdf2-sha256$<iterations>$<b64salt>$<b64hash>} with base64 padding stripped.
     *
     * @param candidate the plaintext password to verify
     * @param storedHash the stored hash with the {@link #PREFIX} prefix
     * @return true iff the candidate matches the stored hash
     */
    public static boolean verify(String candidate, String storedHash) {
        if (candidate == null || storedHash == null || !storedHash.startsWith(PREFIX)) {
            return false;
        }
        String[] parts = storedHash.split("\\$");
        // parts: ["", "pbkdf2-sha256", "<iter>", "<b64salt>", "<b64hash>"]
        if (parts.length != 5) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[2]);
            if (iterations < MIN_ITERATIONS || iterations > MAX_ITERATIONS) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(padBase64(parts[3]));
            byte[] expected = Base64.getDecoder().decode(padBase64(parts[4]));
            KeySpec spec = new PBEKeySpec(candidate.toCharArray(), salt, iterations, KEY_LENGTH_BITS);
            byte[] actual = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return MessageDigest.isEqual(actual, expected);
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Hash a plaintext password with PBKDF2-SHA256 in the same stored format the Python
     * encrypt_password() helper produces:
     * {@code $pbkdf2-sha256$<iterations>$<b64salt>$<b64hash>} with base64 padding stripped.
     * Returns {@code null} if the JCE provider does not support PBKDF2WithHmacSHA256, so the
     * caller can fall back to a legacy hash rather than failing the user-facing operation.
     *
     * @param plaintext the password to hash
     * @return the encoded hash, or {@code null} if PBKDF2 is unavailable
     */
    public static String crypt(String plaintext) {
        byte[] salt = new byte[32];
        SECURE_RANDOM.nextBytes(salt);
        try {
            KeySpec spec = new PBEKeySpec(plaintext.toCharArray(), salt,
                    MIN_ITERATIONS, KEY_LENGTH_BITS);
            byte[] dk = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            String b64salt = Base64.getEncoder().encodeToString(salt).replace("=", "");
            String b64hash = Base64.getEncoder().encodeToString(dk).replace("=", "");
            return PREFIX + MIN_ITERATIONS + "$" + b64salt + "$" + b64hash;
        }
        catch (Exception e) {
            return null;
        }
    }

    private static String padBase64(String s) {
        int pad = (4 - s.length() % 4) % 4;
        return pad == 0 ? s : s + "====".substring(0, pad);
    }
}
