/*
 * Copyright (c) 2014 SUSE LLC
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

import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.setup.MirrorCredentialsDto;
import com.redhat.rhn.manager.setup.MirrorCredentialsManager;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;
import com.redhat.rhn.testing.TestUtils;

import java.util.List;

/**
 * Tests for {@link MirrorCredentialsManager}.
 */
public class MirrorCredentialsManagerTest extends RhnMockStrutsTestCase {

    // Manager class instance
    private MirrorCredentialsManager credsManager;

    /**
     * Test findMirrorCredentials().
     * @throws Exception if something goes wrong
     */
    public void testFindAllMirrorCreds() throws Exception {
        MirrorCredentialsDto creds0 = storeTestCredentials();
        MirrorCredentialsDto creds1 = storeTestCredentials();
        MirrorCredentialsDto creds2 = storeTestCredentials();
        List<MirrorCredentialsDto> creds = credsManager.findMirrorCredentials();
        assertTrue(creds.size() >= 3);
        assertTrue(creds.contains(creds0));
        assertTrue(creds.contains(creds1));
        assertTrue(creds.contains(creds2));
    }

    /**
     * Test findMirrorCredentials() sort order.
     * @throws Exception if something goes wrong
     */
    public void testFindMirrorCredentialsSortOrder() throws Exception {
        // Store some credentials
        storeTestCredentials();
        storeTestCredentials();
        MirrorCredentialsDto primaryCreds = storeTestCredentials();
        storeTestCredentials();

        // Make one of them the primary
        credsManager.makePrimaryCredentials(primaryCreds.getId());
        List<MirrorCredentialsDto> creds = credsManager.findMirrorCredentials();

        // Remember the ID of the last iteration
        long lastId = -1;
        for (MirrorCredentialsDto c : creds) {
            // Primary should be first
            if (creds.indexOf(c) == 0) {
                assertEquals(primaryCreds, c);
            }
            // After that: ascending IDs
            else if (creds.indexOf(c) >= 2) {
                assertTrue(c.getId() > lastId);
            }
            lastId = c.getId();
        }
    }

    /**
     * Test findMirrorCredentials(long).
     * @throws Exception if something goes wrong
     */
    public void testFindMirrorCredsById() throws Exception {
        MirrorCredentialsDto creds0 = storeTestCredentials();
        MirrorCredentialsDto creds1 = storeTestCredentials();
        MirrorCredentialsDto creds2 = storeTestCredentials();
        assertEquals(creds0, credsManager.findMirrorCredentials(creds0.getId()));
        assertEquals(creds1, credsManager.findMirrorCredentials(creds1.getId()));
        assertEquals(creds2, credsManager.findMirrorCredentials(creds2.getId()));
    }

    /**
     * Test deleteMirrorCredentials().
     * @throws Exception if something goes wrong
     */
    public void testDeleteCredentials() throws Exception {
        MirrorCredentialsDto creds0 = storeTestCredentials();
        MirrorCredentialsDto creds1 = storeTestCredentials();
        int size = credsManager.findMirrorCredentials().size();
        assertTrue(size >= 2);
        credsManager.deleteMirrorCredentials(creds0.getId(), request);
        List<MirrorCredentialsDto> creds = credsManager.findMirrorCredentials();
        assertEquals(size - 1, creds.size());
        assertFalse(creds.contains(creds0));
        assertTrue(creds.contains(creds1));
    }

    /**
     * Test makePrimaryCredentials()
     * @throws Exception if something goes wrong
     */
    public void testMakePrimaryCredentials() throws Exception {
        MirrorCredentialsDto creds0 = storeTestCredentials();
        MirrorCredentialsDto creds1 = storeTestCredentials();
        MirrorCredentialsDto creds2 = storeTestCredentials();

        credsManager.makePrimaryCredentials(creds0.getId());
        assertTrue(credsManager.findMirrorCredentials(creds0.getId()).isPrimary());
        assertFalse(credsManager.findMirrorCredentials(creds1.getId()).isPrimary());
        assertFalse(credsManager.findMirrorCredentials(creds2.getId()).isPrimary());

        credsManager.makePrimaryCredentials(creds1.getId());
        assertFalse(credsManager.findMirrorCredentials(creds0.getId()).isPrimary());
        assertTrue(credsManager.findMirrorCredentials(creds1.getId()).isPrimary());
        assertFalse(credsManager.findMirrorCredentials(creds2.getId()).isPrimary());

        credsManager.makePrimaryCredentials(creds2.getId());
        assertFalse(credsManager.findMirrorCredentials(creds0.getId()).isPrimary());
        assertFalse(credsManager.findMirrorCredentials(creds1.getId()).isPrimary());
        assertTrue(credsManager.findMirrorCredentials(creds2.getId()).isPrimary());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        credsManager = new MirrorCredentialsManager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        // Tear down the manager class instance
        credsManager = null;
    }

    /**
     * Store test credentials for a given id.
     *
     * @param id the id of stored credentials
     */
    private MirrorCredentialsDto storeTestCredentials() throws ContentSyncException {
        MirrorCredentialsDto creds = new MirrorCredentialsDto();
        creds.setUser("testuser-" + TestUtils.randomString());
        creds.setPassword("testpass-" + TestUtils.randomString());
        long dbId = credsManager.storeMirrorCredentials(creds, request);
        creds.setId(dbId);
        return creds;
    }
}
