package com.suse.manager.webui.utils.test;

import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.webui.utils.TokenUtils;

import java.security.Key;

/**
 * Tests for the TokenUtils class.
 */
public class TokenUtilsTest extends RhnBaseTestCase {

    public void testGetKey() {
        Key key = TokenUtils.getKeyForSecret(TestUtils.randomString());
        assertNotNull(key);
        System.out.println(key.toString());
        assertEquals(16, key.getEncoded().length);
    }
}
