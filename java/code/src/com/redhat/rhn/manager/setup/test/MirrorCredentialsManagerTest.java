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
import com.redhat.rhn.manager.setup.MirrorCredentialsDto;
import com.redhat.rhn.manager.setup.MirrorCredentialsManager;
import com.redhat.rhn.testing.RhnBaseTestCase;

import java.util.List;

/**
 * Tests for {@link MirrorCredentialsManager}.
 */
public class MirrorCredentialsManagerTest extends RhnBaseTestCase {

    /**
     * Tests findMirrorCredentials().
     * @throws Exception if something goes wrong
     */
    public void testFindMirrorCredsEmpty() throws Exception {
        List<MirrorCredentialsDto> credentials = MirrorCredentialsManager.findMirrorCredentials();
        assertEquals(0, credentials.size());
    }

    /**
     * Tests findMirrorCredentials().
     * @throws Exception if something goes wrong
     */
    public void testFindAllMirrorCreds() throws Exception {
        setTestCredentials("testuser0", "testpass0", "testemail0", 0);
        setTestCredentials("testuser1", "testpass1", "testemail1", 1);
        setTestCredentials("testuser2", "testpass2", "testemail2", 2);
        List<MirrorCredentialsDto> credentials = MirrorCredentialsManager.findMirrorCredentials();
        assertEquals(3, credentials.size());

        for (MirrorCredentialsDto creds : credentials) {
            int i = credentials.indexOf(creds);
            assertEquals("testuser" + i, creds.getUser());
            assertEquals("testpass" + i, creds.getPassword());
            assertEquals("testemail" + i, creds.getEmail());
        }
    }

    /**
     * Tests findMirrorCredentials().
     * @throws Exception if something goes wrong
     */
    public void testFindMirrorCredsMissing() throws Exception {
        setTestCredentials("testuser0", "testpass0", "testemail0", 0);
        setTestCredentials("testuser2", "testpass2", "testemail2", 2);
        List<MirrorCredentialsDto> credentials = MirrorCredentialsManager.findMirrorCredentials();
        assertEquals(1, credentials.size());
        assertNull(MirrorCredentialsManager.findMirrorCredentials(1));
    }

    /**
     * Tests findMirrorCredentials().
     * @throws Exception if something goes wrong
     */
    public void testFindMirrorCredsById() throws Exception {
        setTestCredentials("testuser0", "testpass0", "testemail0", 0);
        setTestCredentials("testuser1", "testpass1", "testemail1", 1);
        setTestCredentials("testuser2", "testpass2", "testemail2", 2);

        for (int i = 0; i <= 2; i++) {
            MirrorCredentialsDto creds = MirrorCredentialsManager.findMirrorCredentials(i);
            assertEquals("testuser" + i, creds.getUser());
            assertEquals("testpass" + i, creds.getPassword());
            assertEquals("testemail" + i, creds.getEmail());
        }
    }

    /**
     * Sets the test credentials.
     *
     * @param user the user
     * @param pass the pass
     * @param email the email
     * @param index the index
     */
    private void setTestCredentials(String user, String pass, String email, int index) {
        String keyUser = MirrorCredentialsManager.KEY_MIRRCREDS_USER;
        String keyPass = MirrorCredentialsManager.KEY_MIRRCREDS_PASS;
        String keyEmail = MirrorCredentialsManager.KEY_MIRRCREDS_EMAIL;
        if (index >= 1) {
            keyUser += MirrorCredentialsManager.KEY_MIRRCREDS_SEPARATOR + index;
            keyPass += MirrorCredentialsManager.KEY_MIRRCREDS_SEPARATOR + index;
            keyEmail += MirrorCredentialsManager.KEY_MIRRCREDS_SEPARATOR + index;
        }
        Config.get().setString(keyUser, user);
        Config.get().setString(keyPass, pass);
        Config.get().setString(keyEmail, email);
    }

    /**
     * Clean up (remove) test credentials.
     *
     * @param index
     */
    private void removeTestCredentials(int index) {
        String keyUser = MirrorCredentialsManager.KEY_MIRRCREDS_USER;
        String keyPass = MirrorCredentialsManager.KEY_MIRRCREDS_PASS;
        String keyEmail = MirrorCredentialsManager.KEY_MIRRCREDS_EMAIL;
        if (index >= 1) {
            keyUser += MirrorCredentialsManager.KEY_MIRRCREDS_SEPARATOR + index;
            keyPass += MirrorCredentialsManager.KEY_MIRRCREDS_SEPARATOR + index;
            keyEmail += MirrorCredentialsManager.KEY_MIRRCREDS_SEPARATOR + index;
        }
        Config.get().remove(keyUser);
        Config.get().remove(keyPass);
        Config.get().remove(keyEmail);
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
    }
}
