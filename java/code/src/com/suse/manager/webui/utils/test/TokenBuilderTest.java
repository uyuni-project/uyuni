/*
 * Copyright (c) 2015--2021 SUSE LLC
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
package com.suse.manager.webui.utils.test;

import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.webui.utils.TokenBuilder;

import org.apache.commons.codec.digest.DigestUtils;
import org.jose4j.jwt.NumericDate;

import java.security.Key;
import java.util.Arrays;

/**
 * Tests for the TokenBuilder class.
 */
public class TokenBuilderTest extends BaseTestCaseWithUser {

    public void testGetKey() {
        String secret = DigestUtils.sha256Hex(TestUtils.randomString());
        Key key = TokenBuilder.getKeyForSecret(secret);
        assertNotNull(key);
        assertEquals(32, key.getEncoded().length);
    }

    public void testGetKeyConvert() {
        String secret = DigestUtils.sha256Hex("0123456789abcd");
        Key key = TokenBuilder.getKeyForSecret(secret);
        assertNotNull(key);
        assertTrue(Arrays.equals(new byte[]{
                -88, 44, -110, 39, -52, 84, -57, 71, 86, 32, -50, -123, -70, 31, -54, 30, 111,
                82, -84, -119, -99, 20, -82, 114, -21, 38, 65, 25, -50, 88, 44, -8}, key.getEncoded()));
    }

    public void testExpectsHexSecret() {
        try {
            // randomString() len is 13
            TokenBuilder.getKeyForSecret(TestUtils.randomString());
            fail("secret should be a hex string");
        }
        catch (IllegalArgumentException e) {
            assertContains(e.getMessage(), "Odd number of characters.");
        }
    }

    public void testDefaultExpiresInAYear() throws Exception {
        TokenBuilder tokenBuilder = new TokenBuilder();
        tokenBuilder.useServerSecret();
        NumericDate expDate = tokenBuilder.getClaims().getExpirationTime();
        assertNotNull(expDate);
    }

    public void testVerifyToken() throws Exception {
        TokenBuilder tokenBuilder = new TokenBuilder();
        tokenBuilder.useServerSecret();
        String token = tokenBuilder.getToken();
        assertTrue(TokenBuilder.verifyToken(token));
    }

    public void testWrongOriginToken() {
        String wrongOriginToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ikpva" +
                "G4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        assertFalse(TokenBuilder.verifyToken(wrongOriginToken));
    }
}
