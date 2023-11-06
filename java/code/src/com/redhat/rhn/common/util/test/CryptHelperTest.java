/*
 * Copyright (c) 2023 SUSE LLC
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
package com.redhat.rhn.common.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.util.AESCryptException;
import com.redhat.rhn.common.util.CryptHelper;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CryptHelperTest {

    @Test
    public void aesCryptTest() {
        String clearText = "this is a clear text";
        String password = "This Is my secret password";
        try {
            String encrypted = CryptHelper.aes256Encrypt(clearText, password);
            assertEquals("U2FsdGVkX1", encrypted.substring(0, 10));
            String decrypted = CryptHelper.aes256Decrypt(encrypted, password);
            assertEquals(clearText, decrypted);
        }
        catch (AESCryptException e) {
            fail("Unexpected encryption or decryption error", e);
        }
    }

    @Test
    public void aesCryptOpensslTest() {
        String clearText = "this is a clear text";
        String password = "This Is my secret password";
        try {
            String encrypted = CryptHelper.aes256Encrypt(clearText, password);
            String[] cmd = {
                    "/bin/sh",
                    "-c",
                    String.format("echo '%s' | openssl enc -aes-256-ctr -pbkdf2 -d -a -pass pass:'%s'",
                            encrypted, password)
            };
            Process process = Runtime.getRuntime().exec(cmd);
            InputStreamReader isr = new InputStreamReader(process.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = br.readLine()) != null) {
                sb.append(s).append("\n");
            }
            process.waitFor();
            InputStream errorStream = process.getErrorStream();
            String error = new String(errorStream.readAllBytes());
            assertEquals("", error);
            assertEquals(clearText, StringUtils.chop(sb.toString()));
        }
        catch (AESCryptException e) {
            fail("Unexpected encryption or decryption error", e);
        }
        catch (IOException e) {
            fail(e);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail(e);
        }
    }

    @Test
    public void aesEncryptNotSameTest() {
        String clearText = "----- BEGIN RSA PRIVATE KEY ----\n" +
                "2383746aff3876874jdfiosudghd\n" +
                "----- END RSA PRIVATE KEY -----";
        String password = "This is a very secure password ";
        try {
            String encrypted1 = CryptHelper.aes256Encrypt(clearText, password);
            String encrypted2 = CryptHelper.aes256Encrypt(clearText, password);
            String encrypted3 = CryptHelper.aes256Encrypt(clearText, password);
            String encrypted4 = CryptHelper.aes256Encrypt(clearText, password);
            assertNotEquals(encrypted2, encrypted1);
            assertNotEquals(encrypted3, encrypted1);
            assertNotEquals(encrypted4, encrypted1);
            assertNotEquals(encrypted3, encrypted2);
            assertNotEquals(encrypted4, encrypted3);
        }
        catch (AESCryptException e) {
            fail("Unexpected encryption or decryption error", e);
        }
    }

    @Test
    public void aesOpensslDecryptTest() {
        String password = "This Is my secret password";
        String encrypted1 = "U2FsdGVkX18n9YyYTFoqWrDQZr+Pdoomp4nPxA0Fro2u65U4";
        try {
            assertEquals("this is a clear text", CryptHelper.aes256Decrypt(encrypted1, password));
        }
        catch (AESCryptException e) {
            fail("Unexpected decryption error", e);
        }
    }

    @Test
    public void aesSimpleOpensslDecryptTest() {
        String password = "pw";
        String encrypted1 = "U2FsdGVkX1956W8q8KfRw+4weyZXFDJVkuC4PzBEfSZscdqD6w==";
        try {
            assertEquals("this is a clear text\n", CryptHelper.aes256Decrypt(encrypted1, password));
        }
        catch (AESCryptException e) {
            fail("Unexpected decryption error", e);
        }
    }
}
