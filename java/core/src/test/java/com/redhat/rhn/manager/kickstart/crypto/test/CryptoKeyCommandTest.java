/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.manager.kickstart.crypto.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.util.SHA256Crypt;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;
import com.redhat.rhn.domain.kickstart.crypto.test.CryptoTest;
import com.redhat.rhn.manager.kickstart.crypto.BaseCryptoKeyCommand;
import com.redhat.rhn.manager.kickstart.crypto.CreateCryptoKeyCommand;
import com.redhat.rhn.manager.kickstart.crypto.DeleteCryptoKeyCommand;
import com.redhat.rhn.manager.kickstart.crypto.EditCryptoKeyCommand;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

/**
 * CryptoKeyCommandTest - test for CryptoKeyCommand
 */
public class CryptoKeyCommandTest extends BaseTestCaseWithUser {



    private BaseCryptoKeyCommand cmd;

    public void setupKey(BaseCryptoKeyCommand cmdIn) {
        this.cmd = cmdIn;
        assertNotNull(cmd.getCryptoKey().getOrg());
        cmd.setDescription("Test desc");
        cmd.setType("GPG");
        cmd.setContents(SHA256Crypt.sha256Hex(RandomStringUtils.random(28)));
        cmd.store();

    }

    @Test
    public void testCreateCommand() throws Exception {
        setupKey(new CreateCryptoKeyCommand(user.getOrg()));
        CryptoKey key = cmd.getCryptoKey();
        key = (CryptoKey) reload(key);
        assertNotNull(key.getId());
        assertNotNull(key.getKey());
    }

    @Test
    public void testDuplicate() throws Exception {
        setupKey(new CreateCryptoKeyCommand(user.getOrg()));
        String usedDesc = cmd.getCryptoKey().getDescription();
        cmd = new CreateCryptoKeyCommand(user.getOrg());
        cmd.setDescription(usedDesc);
        cmd.setType("GPG");
        cmd.setContents(SHA256Crypt.sha256Hex(RandomStringUtils.random(28)));
        assertNotNull(cmd.store());

    }

    @Test
    public void testEdit() throws Exception {
        CryptoKey key = CryptoTest.createTestKey(user.getOrg());
        KickstartFactory.saveCryptoKey(key);
        flushAndEvict(key);
        setupKey(new EditCryptoKeyCommand(user, key.getId()));
        assertNotNull(cmd.getCryptoKey());
        assertNull(cmd.store());

    }

    @Test
    public void testDelete() throws Exception {
        CryptoKey key = CryptoTest.createTestKey(user.getOrg());
        KickstartFactory.saveCryptoKey(key);
        assertNotNull(KickstartFactory.lookupCryptoKeyById(key.getId(), key.getOrg()));
        flushAndEvict(key);
        KickstartFactory.removeCryptoKey(key);
        assertNull(KickstartFactory.lookupCryptoKeyById(key.getId(), key.getOrg()));

        //second method
        CryptoKey key2 = CryptoTest.createTestKey(user.getOrg());
        KickstartFactory.saveCryptoKey(key2);
        assertNotNull(KickstartFactory.lookupCryptoKeyById(key2.getId(), key2.getOrg()));
        flushAndEvict(key2);

        //CryptoKey will be deleted by the cmd.store command in setupKey
        setupKey(new DeleteCryptoKeyCommand(user, key2.getId()));
        assertNull(KickstartFactory.lookupCryptoKeyById(key2.getId(), key2.getOrg()));
    }

    @Test
    public void testDuplicateDelete() throws Exception {
        CryptoKey key = CryptoTest.createTestKey(user.getOrg());
        KickstartFactory.saveCryptoKey(key);
        assertNotNull(KickstartFactory.lookupCryptoKeyById(key.getId(), key.getOrg()));
        flushAndEvict(key);

        //CryptoKey will be deleted by the cmd.store command in setupKey
        setupKey(new DeleteCryptoKeyCommand(user, key.getId()));
        assertNull(KickstartFactory.lookupCryptoKeyById(key.getId(), key.getOrg()));

        try {
            setupKey(new DeleteCryptoKeyCommand(user, key.getDescription()));

            // if no exception was thrown there is a problem
            fail();
        }
        catch (IllegalArgumentException e) {
            // this is what we want to see
        } // other exceptions are legitimate errors, throw them and fail the test
    }
}

