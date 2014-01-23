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

import java.util.List;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.manager.setup.MirrorCredentials;
import com.redhat.rhn.manager.setup.SetupWizardManager;
import com.redhat.rhn.testing.RhnBaseTestCase;

/**
 * Tests for {@link SetupWizardManager}.
 */
public class SetupWizardManagerTest extends RhnBaseTestCase {

    public void testGetMirrorCredsEmpty() throws Exception {
        List<MirrorCredentials> credentials = SetupWizardManager.getMirrorCredentials();
        assertEquals(0, credentials.size());
    }

    public void testGetMirrorCreds() throws Exception {
        writeTestCredentials("testuser", "testpass", "testemail", 0);
        writeTestCredentials("testuser1", "testpass1", "testemail1", 1);
        writeTestCredentials("testuser2", "testpass2", "testemail2", 2);
        List<MirrorCredentials> credentials = SetupWizardManager.getMirrorCredentials();
        assertEquals(3, credentials.size());
    }

    public void testGetMirrorCredsMissing() throws Exception {
        writeTestCredentials("testuser", "testpass", "testemail", 0);
        writeTestCredentials("testuser2", "testpass2", "testemail2", 2);
        List<MirrorCredentials> credentials = SetupWizardManager.getMirrorCredentials();
        assertEquals(1, credentials.size());
    }

    @Override
    protected void tearDown() throws Exception {
        // Clear credentials from config
        super.tearDown();
        for (int i=0; i<=10; i++) {
            writeTestCredentials("", "", "", i);
        }
    }

    private void writeTestCredentials(String user, String pass, String email, int index) {
        String keyUser = SetupWizardManager.KEY_MIRRCREDS_USER;
        String keyPass = SetupWizardManager.KEY_MIRRCREDS_PASS;
        String keyEmail = SetupWizardManager.KEY_MIRRCREDS_EMAIL;
        if (index >= 1) {
            keyUser += "." + index;
            keyPass += "." + index;
            keyEmail += "." + index;
        }
        Config.get().setString(keyUser, user);
        Config.get().setString(keyPass, pass);
        Config.get().setString(keyEmail, email);
    }
}
