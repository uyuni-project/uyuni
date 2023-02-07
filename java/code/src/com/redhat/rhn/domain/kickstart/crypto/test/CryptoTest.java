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
package com.redhat.rhn.domain.kickstart.crypto.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

/**
 * CryptoTest - test
 */
public class CryptoTest extends BaseTestCaseWithUser {


    @Test
    public void testCryptoKey() {
        CryptoKey key = createTestKey(user.getOrg());
        KickstartFactory.saveCryptoKey(key);
        key = (CryptoKey) reload(key);
        assertNotNull(key.getId());
        String testString = "aaaaaabbbbb";
        key.setKey(new String(testString).getBytes(StandardCharsets.UTF_8));
        assertEquals(key.getKeyString(), testString);
    }

    @Test
    public void testBigKey() {
        CryptoKey key = createTestKey(user.getOrg());
        assertNotNull(key);

        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            buffer.append("1");
        }
        key.setKey(buffer.toString().getBytes());
        KickstartFactory.saveCryptoKey(key);
        key = (CryptoKey) reload(key);
        assertNotNull("You dont have the Oracle 10G drivers installed on your system!!!",
                key.getKeyString());
    }

    @Test
    public void testCryptoKeyKickstartAssoc() throws Exception {
        KickstartData ksdata = KickstartDataTest.createKickstartWithOptions(user.getOrg());
        ksdata = addKeyToKickstart(ksdata);
        assertNotNull(ksdata.getCryptoKeys());
        assertFalse(ksdata.getCryptoKeys().isEmpty());
    }

    public static CryptoKey createTestKey(Org orgIn) {
        CryptoKey key = new CryptoKey();
        key.setCryptoKeyType(KickstartFactory.KEY_TYPE_GPG);
        key.setDescription("some key test" + TestUtils.randomString());
        key.setOrg(orgIn);
        return key;
    }

    public static KickstartData addKeyToKickstart(KickstartData ksdata) {
        CryptoKey key = createTestKey(ksdata.getOrg());
        KickstartFactory.saveCryptoKey(key);
        ksdata.addCryptoKey(key);
        KickstartFactory.saveKickstartData(ksdata);
        ksdata = (KickstartData) TestUtils.reload(ksdata);
        return ksdata;
    }

    public static void addKeysToKickstart(KickstartData ksdata) throws Exception {
        String gpgContent = TestUtils.readAll(TestUtils.findTestData("foo.gpg"));
        CryptoKey gpgFooKey = new CryptoKey();
        gpgFooKey.setCryptoKeyType(KickstartFactory.KEY_TYPE_GPG);
        gpgFooKey.setDescription("gpg key test" + TestUtils.randomString());
        gpgFooKey.setKey(gpgContent.getBytes(StandardCharsets.UTF_8));
        gpgFooKey.setOrg(ksdata.getOrg());
        KickstartFactory.saveCryptoKey(gpgFooKey);

        String sslContent = TestUtils.readAll(TestUtils.findTestData("foo.ssl"));
        CryptoKey sslFooKey = new CryptoKey();
        sslFooKey.setCryptoKeyType(KickstartFactory.KEY_TYPE_SSL);
        sslFooKey.setDescription("ssl key test" + TestUtils.randomString());
        sslFooKey.setKey(sslContent.getBytes(StandardCharsets.UTF_8));
        sslFooKey.setOrg(ksdata.getOrg());
        KickstartFactory.saveCryptoKey(sslFooKey);

        ksdata.addCryptoKey(gpgFooKey);
        ksdata.addCryptoKey(sslFooKey);
        KickstartFactory.saveKickstartData(ksdata);
    }

}
