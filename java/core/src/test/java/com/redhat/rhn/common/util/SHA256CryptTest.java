/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Random;


/**
 * @author mmccune
 *
 */
public class SHA256CryptTest {

    @Test
    public void testSHA256Sum() throws Exception {
        File testFile = new File(TestUtils.findTestData("test.file").getFile());
        String sum = SHA256Crypt.getFileSHA256Sum(testFile);
        assertEquals("78944e7840d6b81e0aec269d65bda7964c6e45c22595b507959f2aa0f0afc9e4", sum);
    }

    /**
     * Note that this test creates a large 100MB file in /tmp
     * and then does an sha256sum on the file.
     *
     * With a previous implementation of SHA256Sum.getFileSHA256Sum() this would
     * cause an OOME with a max-heap size in junit of 256m.  This is configured in:
     * spacewalk/buildconf/build-utils.xml:
     * <pre>{@literal
     *       <junit forkmode="once" fork="yes" printsummary="off" showoutput="yes"
     *        haltonfailure="${halt-tests-on-failure}"
     *        failureproperty="junit_test_failure" maxmemory="256m">
     * }</pre>
     * The new implementation uses less memory and thus passes the test below when ran
     * from Junit CLI.
     *
     * @throws Exception something bad happened
     */
    @Test
    public void testOOMESHA256Sum() throws Exception {
        File large = new File("/tmp/large-file.dat");
        // Create a large 100mb file
        large.createNewFile();
        writeRandomLargeBytesToFile(large);
        String sum = SHA256Crypt.getFileSHA256Sum(large);
        assertNotNull(sum);
        large.delete();
    }

    private static void writeRandomLargeBytesToFile(File f) {
        byte[] ba = new byte[101326592];
        Random r = new Random();
        r.nextBytes(ba);
        TestUtils.writeByteArrayToFile(f, ba);
    }

    @Test
    public void testCrypt() {
        String key = "%43AazZ09!@#$%^&*()-+=/.~`?;:<>,";
        String salt = "testsalttestsalttest";

        /*
         * Ensure crypt(key) generates a random
         * 16 character salt.
         */
        String c1 = SHA256Crypt.crypt(key);
        assertNotNull(c1);
        assertEquals(c1.charAt(19), '$');

        /*
         * Make sure the crypt(key, salt) works
         */
        String c2 = SHA256Crypt.crypt(key, salt);
        String c3 = SHA256Crypt.crypt(key, salt);
        assertEquals(c2, c3);
        //Make sure salt was truncated
        assertEquals(c2.charAt(19), '$');
        //Make sure our salt was used
        assertTrue(c2.startsWith("$5$testsalttestsalt"));
        c2 = c2.substring(20); //get encoded password
        assertNotNull(c2);

        String password = "password";
        String newSalt = "tests.l/t3stSALTtest";
        String c4 = SHA256Crypt.crypt(password, newSalt);
        String c5 = SHA256Crypt.crypt(password, c4);
        assertEquals(c4, c5);
    }

}
