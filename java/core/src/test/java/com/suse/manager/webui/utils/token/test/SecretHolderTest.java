/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.webui.utils.token.test;

import static com.redhat.rhn.testing.RhnBaseTestCase.assertContains;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.webui.utils.token.SecretHolder;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.security.Key;

public class SecretHolderTest {

    @Test
    public void testGetKey() {
        String secret = DigestUtils.sha256Hex(TestUtils.randomString());
        SecretHolder secretHolder = new SecretHolder(secret);
        Key key = secretHolder.getKeyForSecret();

        assertNotNull(key);
        assertEquals(32, key.getEncoded().length);
    }

    @Test
    public void testGetKeyConvert() {
        String secret = DigestUtils.sha256Hex("0123456789abcd");
        SecretHolder secretHolder = new SecretHolder(secret);
        Key key = secretHolder.getKeyForSecret();

        assertNotNull(key);
        assertArrayEquals(new byte[]{
            -88, 44, -110, 39, -52, 84, -57, 71, 86, 32, -50, -123, -70, 31, -54, 30, 111,
            82, -84, -119, -99, 20, -82, 114, -21, 38, 65, 25, -50, 88, 44, -8}, key.getEncoded());
    }

    @Test
    public void testExpectsHexSecret() {
        try {
            // randomString() len is 13
            String wrongSecret = TestUtils.randomString();
            SecretHolder secretHolder = new SecretHolder(wrongSecret);
            secretHolder.getKeyForSecret();
            fail("secret should be a hex string");
        }
        catch (IllegalArgumentException e) {
            assertContains(e.getMessage(), "Odd number of characters.");
        }
    }
}
