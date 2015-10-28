package com.suse.manager.webui.utils.test;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.suse.manager.webui.utils.TokenUtils;
import junit.framework.TestCase;

import java.security.Key;

public class TokenUtilsTest extends RhnBaseTestCase {

    public void testGetKey() {
        Key key = TokenUtils.getServerKey();
        assertNotNull(key);
        assertEquals(32, key.getEncoded().length);
    }
}
