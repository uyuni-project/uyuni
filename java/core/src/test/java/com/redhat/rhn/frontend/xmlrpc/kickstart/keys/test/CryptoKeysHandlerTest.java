/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.kickstart.keys.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.common.util.SHA256Crypt;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;
import com.redhat.rhn.domain.kickstart.crypto.test.CryptoTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.CryptoKeyDto;
import com.redhat.rhn.frontend.xmlrpc.kickstart.keys.CryptoKeysHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.testing.UserTestUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Test cases for the {@link CryptoKeysHandler}.
 *
 */
public class CryptoKeysHandlerTest extends BaseHandlerTestCase {

    @Test
    public void testListAllKeys() {
        // Setup
        User otherOrg = UserTestUtils.findNewUser("testUser", "cryptoOrg", true);
        CryptoKey key = CryptoTest.createTestKey(otherOrg.getOrg());
        KickstartFactory.saveCryptoKey(key);
        flushAndEvict(key);

        // Test
        CryptoKeysHandler handler = new CryptoKeysHandler();

        List<CryptoKeyDto> allKeys = handler.listAllKeys(otherOrg);

        // Verify
        assertNotNull(allKeys);
        assertEquals(allKeys.size(), 1);

        CryptoKeyDto dto = (CryptoKeyDto)allKeys.get(0);
        assertEquals(key.getDescription(), dto.getDescription());
        assertEquals(key.getOrg().getId(), dto.getOrgId());
    }

    @Test
    public void testCreate() {
        // Setup
        String description = "CryptoKeysHandler.testCreate-Description";
        String content = SHA256Crypt.sha256Hex(RandomStringUtils.random(28));

        // Test
        CryptoKeysHandler handler = new CryptoKeysHandler();
        handler.create(admin, description, "GPG", content);

        // Verify
        CryptoKey cryptoKey =
            KickstartFactory.lookupCryptoKey(description, admin.getOrg());

        assertNotNull(cryptoKey);
        assertEquals(cryptoKey.getDescription(), description);
        assertEquals(cryptoKey.getCryptoKeyType().getLabel(), "GPG");
        assertEquals(cryptoKey.getKeyString(), content);
    }

    @Test
    public void testDelete() {
        // Setup
        CryptoKey key = CryptoTest.createTestKey(admin.getOrg());
        KickstartFactory.saveCryptoKey(key);
        assertNotNull(KickstartFactory.lookupCryptoKeyById(key.getId(), key.getOrg()));
        flushAndEvict(key);

        // Test
        CryptoKeysHandler handler = new CryptoKeysHandler();
        handler.delete(admin, key.getDescription());

        // Verify
        CryptoKey deletedKey =
            KickstartFactory.lookupCryptoKeyById(key.getId(), key.getOrg());
        assertNull(deletedKey);
    }

    @Test
    public void testGetDetails() {
        // Setup
        CryptoKey key = CryptoTest.createTestKey(admin.getOrg());
        KickstartFactory.saveCryptoKey(key);
        assertNotNull(KickstartFactory.lookupCryptoKeyById(key.getId(), key.getOrg()));
        flushAndEvict(key);

        // Test
        CryptoKeysHandler handler = new CryptoKeysHandler();
        CryptoKey cryptoKey = handler.getDetails(admin, key.getDescription());

        // Verify
        assertNotNull(cryptoKey);
        assertEquals(cryptoKey.getDescription(), cryptoKey.getDescription());
        assertEquals(cryptoKey.getCryptoKeyType().getLabel(),
                     cryptoKey.getCryptoKeyType().getLabel());
        assertEquals(cryptoKey.getKeyString(), cryptoKey.getKeyString());
    }

}
