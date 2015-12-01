package com.suse.manager.webui.utils.test;

import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.webui.utils.TokenUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.security.Key;

/**
 * Tests for the TokenUtils class.
 */
public class TokenUtilsTest extends RhnBaseTestCase {

    public void testGetKey() {
        String secret = DigestUtils.sha256Hex(TestUtils.randomString());
        Key key = TokenUtils.getKeyForSecret(secret);
        assertNotNull(key);
        assertEquals(32, key.getEncoded().length);
    }

    public void testExpectsHexSecret() {
        try {
            // randomString() len is 13
            TokenUtils.getKeyForSecret(TestUtils.randomString());
            fail("secret should be a hex string");
        } catch(IllegalArgumentException e) {
            assertContains(e.getMessage(), "hexBinary needs to be even-length");
        }
    }
}
