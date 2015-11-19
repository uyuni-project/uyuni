package com.suse.manager.webui.utils.test;

import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.webui.utils.TokenUtils;

import java.security.Key;

public class TokenUtilsTest extends RhnBaseTestCase {

    public void setUp() throws Exception {
       super.setUp();
    }

    public void tearDown() throws Exception {
       super.tearDown();
    }

    public void testGetKey() {
        Key key = TokenUtils.getKeyForSecret(TestUtils.randomString());
        assertNotNull(key);
        assertEquals(16, key.getEncoded().length);
    }
}
