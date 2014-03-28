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
import com.redhat.rhn.manager.setup.ProxySettingsDto;
import com.redhat.rhn.manager.setup.SetupWizardManager;
import com.redhat.rhn.testing.RhnBaseTestCase;

import java.util.List;

/**
 * Tests for {@link SetupWizardManager}.
 */
public class SetupWizardManagerTest extends RhnBaseTestCase {

    public void testGetProxySettings() throws Exception {
        ProxySettingsDto proxy = new ProxySettingsDto();
        proxy.setHostname("proxy.foobar.com");
        proxy.setUsername("foobaruser");
        proxy.setPassword("foobarpassword");
        setProxySettings(proxy);
        assertTrue(proxy.equals(SetupWizardManager.getProxySettings()));
    }

    public void testFindMirrorCredsEmpty() throws Exception {
        List<MirrorCredentialsDto> credentials = SetupWizardManager.findMirrorCredentials();
        assertEquals(0, credentials.size());
    }

    public void testFindAllMirrorCreds() throws Exception {
        setTestCredentials("testuser0", "testpass0", "testemail0", 0);
        setTestCredentials("testuser1", "testpass1", "testemail1", 1);
        setTestCredentials("testuser2", "testpass2", "testemail2", 2);
        List<MirrorCredentialsDto> credentials = SetupWizardManager.findMirrorCredentials();
        assertEquals(3, credentials.size());

        for (MirrorCredentialsDto creds : credentials) {
            int i = credentials.indexOf(creds);
            assertEquals("testuser" + i, creds.getUser());
            assertEquals("testpass" + i, creds.getPassword());
            assertEquals("testemail" + i, creds.getEmail());
        }
    }

    public void testFindMirrorCredsMissing() throws Exception {
        setTestCredentials("testuser0", "testpass0", "testemail0", 0);
        setTestCredentials("testuser2", "testpass2", "testemail2", 2);
        List<MirrorCredentialsDto> credentials = SetupWizardManager.findMirrorCredentials();
        assertEquals(1, credentials.size());
        assertNull(SetupWizardManager.findMirrorCredentials(1));
    }

    public void testFindMirrorCredsById() throws Exception {
        setTestCredentials("testuser0", "testpass0", "testemail0", 0);
        setTestCredentials("testuser1", "testpass1", "testemail1", 1);
        setTestCredentials("testuser2", "testpass2", "testemail2", 2);

        for (int i = 0; i <= 2; i++) {
            MirrorCredentialsDto creds = SetupWizardManager.findMirrorCredentials(i);
            assertEquals("testuser" + i, creds.getUser());
            assertEquals("testpass" + i, creds.getPassword());
            assertEquals("testemail" + i, creds.getEmail());
        }
    }

    private void setTestCredentials(String user, String pass, String email, int index) {
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

    private void setProxySettings(ProxySettingsDto proxy) {
        Config.get().setString(SetupWizardManager.KEY_PROXY_HOSTNAME, proxy.getHostname());
        Config.get().setString(SetupWizardManager.KEY_PROXY_USERNAME, proxy.getUsername());
        Config.get().setString(SetupWizardManager.KEY_PROXY_PASSWORD, proxy.getPassword());
    }

    @Override
    protected void tearDown() throws Exception {
        // Clear credentials from config
        super.tearDown();
        for (int i=0; i<=10; i++) {
            setTestCredentials("", "", "", i);
        }
    }
}
