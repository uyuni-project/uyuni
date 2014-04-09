/**
 * Copyright (c) 2014 SUSE
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
package com.redhat.rhn.manager.setup.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.manager.setup.MirrorCredentialsDto;
import com.redhat.rhn.manager.setup.MirrorCredentialsManager;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;

import java.util.List;

/**
 * Tests for {@link MirrorCredentialsManager}.
 */
public class MirrorCredentialsManagerTest extends RhnMockStrutsTestCase {

    // Manager class instance
    private MirrorCredentialsManager credsManager;

    /**
     * Tests findMirrorCredentials().
     * @throws Exception if something goes wrong
     */
    public void testFindMirrorCredsEmpty() throws Exception {
        List<MirrorCredentialsDto> credentials = credsManager.findMirrorCredentials();
        assertEquals(0, credentials.size());
    }

    /**
     * Test findMirrorCredentials().
     * @throws Exception if something goes wrong
     */
    public void testFindAllMirrorCreds() throws Exception {
        MirrorCredentialsDto creds0 = storeTestCredentials(0);
        MirrorCredentialsDto creds1 = storeTestCredentials(1);
        MirrorCredentialsDto creds2 = storeTestCredentials(2);
        List<MirrorCredentialsDto> creds = credsManager.findMirrorCredentials();
        assertEquals(3, creds.size());
        assertEquals(creds0, creds.get(0));
        assertEquals(creds1, creds.get(1));
        assertEquals(creds2, creds.get(2));
    }

    /**
     * Test findMirrorCredentials().
     * @throws Exception if something goes wrong
     */
    public void testFindMirrorCredsMissing() throws Exception {
        MirrorCredentialsDto creds0 = storeTestCredentials(0);
        MirrorCredentialsDto creds2 = storeTestCredentials(2);
        List<MirrorCredentialsDto> creds = credsManager.findMirrorCredentials();
        assertEquals(1, creds.size());
        assertEquals(creds0, creds.get(0));
        assertNull(credsManager.findMirrorCredentials(1));
        assertEquals(creds2, credsManager.findMirrorCredentials(2));
    }

    /**
     * Test findMirrorCredentials(long).
     * @throws Exception if something goes wrong
     */
    public void testFindMirrorCredsById() throws Exception {
        MirrorCredentialsDto creds0 = storeTestCredentials(0);
        MirrorCredentialsDto creds1 = storeTestCredentials(1);
        MirrorCredentialsDto creds2 = storeTestCredentials(2);
        assertEquals(creds0, credsManager.findMirrorCredentials(0));
        assertEquals(creds1, credsManager.findMirrorCredentials(1));
        assertEquals(creds2, credsManager.findMirrorCredentials(2));
    }

    /**
     * Test deleteMirrorCredentials().
     * @throws Exception if something goes wrong
     */
    public void testDeleteCredentials() {
        MirrorCredentialsDto creds0 = storeTestCredentials(0L);
        MirrorCredentialsDto creds1 = storeTestCredentials(1L);
        assertEquals(2, credsManager.findMirrorCredentials().size());
        credsManager.deleteMirrorCredentials(0L, user, request);
        List<MirrorCredentialsDto> creds = credsManager.findMirrorCredentials();
        assertEquals(1, creds.size());
        assertFalse(creds.contains(creds0));
        assertEquals(creds1, creds.get(0));
    }

    /**
     * Test makePrimaryCredentials()
     * @throws Exception if something goes wrong
     */
    public void testMakePrimaryCredentials() {
        MirrorCredentialsDto creds0 = storeTestCredentials(0);
        MirrorCredentialsDto creds1 = storeTestCredentials(1);
        MirrorCredentialsDto creds2 = storeTestCredentials(2);
        assertEquals(creds0, credsManager.findMirrorCredentials(0L));
        credsManager.makePrimaryCredentials(1L, user, request);
        assertEquals(creds1, credsManager.findMirrorCredentials(0L));
        credsManager.makePrimaryCredentials(2L, user, request);
        assertEquals(creds2, credsManager.findMirrorCredentials(0L));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        // User needs to be SAT_ADMIN
        user.addRole(RoleFactory.SAT_ADMIN);
        // Setup manager object
        credsManager = new MirrorCredentialsManager(NoopConfigureSatelliteCommand.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        // Clear credentials from config
        for (int i = 0; i <= 10; i++) {
            removeTestCredentials(i);
        }
        // Tear down the manager class instance
        credsManager = null;
    }

    /**
     * Store test credentials for a given id.
     *
     * @param id the id of stored credentials
     */
    private MirrorCredentialsDto storeTestCredentials(long id) {
        MirrorCredentialsDto creds = new MirrorCredentialsDto();
        creds.setUser("testuser" + id);
        creds.setPassword("testpass" + id);
        creds.setEmail("testemail" + id);
        creds.setId(id);
        credsManager.storeMirrorCredentials(creds, user, request);
        return creds;
    }

    /**
     * Clean up credentials from memory by calling remove() directly.
     *
     * @param id the index of credentials to remove
     */
    private void removeTestCredentials(int id) {
        String keyUser = MirrorCredentialsManager.KEY_MIRRCREDS_USER;
        String keyPass = MirrorCredentialsManager.KEY_MIRRCREDS_PASS;
        String keyEmail = MirrorCredentialsManager.KEY_MIRRCREDS_EMAIL;
        if (id >= 1) {
            keyUser += MirrorCredentialsManager.KEY_MIRRCREDS_SEPARATOR + id;
            keyPass += MirrorCredentialsManager.KEY_MIRRCREDS_SEPARATOR + id;
            keyEmail += MirrorCredentialsManager.KEY_MIRRCREDS_SEPARATOR + id;
        }
        Config.get().remove(keyUser);
        Config.get().remove(keyPass);
        Config.get().remove(keyEmail);
    }
}
