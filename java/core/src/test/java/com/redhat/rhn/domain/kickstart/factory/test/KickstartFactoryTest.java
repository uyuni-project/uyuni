/*
 * Copyright (c) 2026 SUSE LLC
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

package com.redhat.rhn.domain.kickstart.factory.test;

import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartSession;
import com.redhat.rhn.domain.kickstart.KickstartSessionState;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.kickstart.test.KickstartSessionTest;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

public class KickstartFactoryTest extends BaseTestCaseWithUser {
    private static class MockKickstartFactory extends KickstartFactory {
        public static void setKickstartSessionHistoryMessage(KickstartSession ksSession,
                                                              KickstartSessionState state, String message) {
            KickstartFactory.setKickstartSessionHistoryMessage(ksSession, state, message);
        }
    }


    @Test
    public void generatedCoverageTestLookupSslCryptoKeys() {
        // this test has been generated programmatically to test KickstartFactory.lookupSslCryptoKeys
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        Org arg0 = user.getOrg();
        KickstartFactory.lookupSslCryptoKeys(arg0);
    }


    @Test
    public void generatedCoverageTestLookupSslCryptoKeyById() {
        // this test has been generated programmatically to test KickstartFactory.lookupSslCryptoKeyById
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        Org arg1 = user.getOrg();
        KickstartFactory.lookupSslCryptoKeyById(0L, arg1);
    }


    @Test
    public void generatedCoverageTestLookupKickstartTreesByChannelAndNullOrg() {
        // this test has been generated programmatically to test
        // KickstartFactory.lookupKickstartTreesByChannelAndNullOrg
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        KickstartFactory.lookupKickstartTreesByChannelAndNullOrg(0L);
    }


    @Test
    public void generatedCoverageTestSetKickstartSessionHistoryMessage() throws Exception {
        // this test has been generated programmatically to test KickstartFactory.setKickstartSessionHistoryMessage
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        KickstartData k = KickstartDataTest.createTestKickstartData(user.getOrg());
        KickstartSession arg0 = KickstartSessionTest.createKickstartSession(k, user);
        KickstartSessionState arg1 = KickstartFactory.SESSION_STATE_FAILED;
        TestUtils.save(arg0);

        MockKickstartFactory.setKickstartSessionHistoryMessage(arg0, arg1, "");
    }


    @Test
    public void generatedCoverageTestListUnsyncedKickstartTrees() {
        // this test has been generated programmatically to test KickstartFactory.listUnsyncedKickstartTrees
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        KickstartFactory.listUnsyncedKickstartTrees();
    }
}
