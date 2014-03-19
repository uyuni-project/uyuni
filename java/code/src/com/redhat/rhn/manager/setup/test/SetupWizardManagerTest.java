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
import com.suse.manager.model.ncc.Subscription;
import java.net.ServerSocket;
import java.util.Date;
import simple.http.connect.Connection;
import simple.http.connect.ConnectionFactory;
import simple.http.load.LoaderEngine;

/**
 * Tests for {@link SetupWizardManager}.
 */
public class SetupWizardManagerTest extends RhnBaseTestCase {

    Connection nccConnection;
    ServerSocket nccSocket;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        LoaderEngine engine = new LoaderEngine();
        engine.load("NCC", "com.redhat.rhn.manager.setup.test.NCCServerStub");
        engine.link("*", "NCC");
        nccConnection = ConnectionFactory.getConnection(engine);
        nccSocket= new ServerSocket(7730);
        nccConnection.connect(nccSocket);
    }

    public void testDownloadSubscriptions() throws Exception {
        setTestCredentials("testuser0", "testpass0", "testemail0", 0);
        List<MirrorCredentials> credentials = SetupWizardManager.findMirrorCredentials();
        System.out.println(credentials.get(0));
        List<Subscription> subs = SetupWizardManager.downloadSubscriptions(credentials.get(0));
        assertEquals(1, subs.size());
        Subscription s = subs.get(0);
        assertEquals("1", s.getSubid());
        assertEquals("1234", s.getRegcode());
        assertEquals("subname0", s.getSubname());
        assertEquals("Gold", s.getType());
        assertEquals("Turbo", s.getSubstatus());
        assertEquals(new Date(1333231200000L), s.getStartDate());
        assertEquals(new Date(1427839200000L), s.getEndDate());
        assertEquals(3, s.getDuration());
        assertEquals("Blade", s.getProductClass());
        assertEquals("Blade", s.getServerClass());
        assertEquals(10, s.getNodecount());
        assertEquals(2, s.getConsumed());
        assertEquals(3, s.getConsumedVirtual());
    }

    public void testFindMirrorCredsEmpty() throws Exception {
        List<MirrorCredentials> credentials = SetupWizardManager.findMirrorCredentials();
        assertEquals(0, credentials.size());
    }

    public void testFindAllMirrorCreds() throws Exception {
        setTestCredentials("testuser0", "testpass0", "testemail0", 0);
        setTestCredentials("testuser1", "testpass1", "testemail1", 1);
        setTestCredentials("testuser2", "testpass2", "testemail2", 2);
        List<MirrorCredentials> credentials = SetupWizardManager.findMirrorCredentials();
        assertEquals(3, credentials.size());

        for (MirrorCredentials creds : credentials) {
            int i = credentials.indexOf(creds);
            assertEquals("testuser" + i, creds.getUser());
            assertEquals("testpass" + i, creds.getPassword());
            assertEquals("testemail" + i, creds.getEmail());
        }
    }

    public void testFindMirrorCredsMissing() throws Exception {
        setTestCredentials("testuser0", "testpass0", "testemail0", 0);
        setTestCredentials("testuser2", "testpass2", "testemail2", 2);
        List<MirrorCredentials> credentials = SetupWizardManager.findMirrorCredentials();
        assertEquals(1, credentials.size());
        assertNull(SetupWizardManager.findMirrorCredentials(1));
    }

    public void testFindMirrorCredsById() throws Exception {
        setTestCredentials("testuser0", "testpass0", "testemail0", 0);
        setTestCredentials("testuser1", "testpass1", "testemail1", 1);
        setTestCredentials("testuser2", "testpass2", "testemail2", 2);

        for (int i = 0; i <= 2; i++) {
            MirrorCredentials creds = SetupWizardManager.findMirrorCredentials(i);
            assertEquals("testuser" + i, creds.getUser());
            assertEquals("testpass" + i, creds.getPassword());
            assertEquals("testemail" + i, creds.getEmail());
        }
    }

    @Override
    protected void tearDown() throws Exception {
        // Clear credentials from config
        super.tearDown();
        for (int i=0; i<=10; i++) {
            setTestCredentials("", "", "", i);
        }

        nccSocket.close();
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
}
