package com.suse.manager.webui.utils.test;

import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.webui.utils.TokenBuilder;
import org.apache.commons.codec.digest.DigestUtils;
import org.jose4j.jwt.NumericDate;

import java.security.Key;

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

    public void testExpectsHexSecret() {
        try {
            // randomString() len is 13
            TokenBuilder.getKeyForSecret(TestUtils.randomString());
            fail("secret should be a hex string");
        } catch(IllegalArgumentException e) {
            assertContains(e.getMessage(), "hexBinary needs to be even-length");
        }
    }

    public void testDefaultExpiresInAYear() throws Exception {
        TokenBuilder tokenBuilder = new TokenBuilder(user.getOrg().getId());
        tokenBuilder.useServerSecret();
        NumericDate expDate = tokenBuilder.getClaims().getExpirationTime();
        assertNotNull(expDate);
    }
}
