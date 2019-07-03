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
        assertTrue(Arrays.equals(new byte[]{-88, 44, -110, 39, -52, 84, -57, 71, 86, 32, -50, -123, -70, 31, -54, 30, 111,
                82, -84, -119, -99, 20, -82, 114, -21, 38, 65, 25, -50, 88, 44, -8}, key.getEncoded()));
    }

    public void testExpectsHexSecret() {
        try {
            // randomString() len is 13
            TokenBuilder.getKeyForSecret(TestUtils.randomString());
            fail("secret should be a hex string");
        } catch(IllegalArgumentException e) {
            assertContains(e.getMessage(), "Odd number of characters.");
        }
    }

    public void testDefaultExpiresInAYear() throws Exception {
        TokenBuilder tokenBuilder = new TokenBuilder();
        tokenBuilder.useServerSecret();
        NumericDate expDate = tokenBuilder.getClaims().getExpirationTime();
        assertNotNull(expDate);
    }
}
