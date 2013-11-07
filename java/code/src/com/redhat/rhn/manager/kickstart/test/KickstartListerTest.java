/**
 * Copyright (c) 2009--2013 Red Hat, Inc.
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

package com.redhat.rhn.manager.kickstart.test;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.common.CommonFactory;
import com.redhat.rhn.domain.common.FileList;
import com.redhat.rhn.domain.common.test.FileListTest;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;
import com.redhat.rhn.domain.kickstart.crypto.test.CryptoTest;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.frontend.dto.ActivationKeyDto;
import com.redhat.rhn.frontend.dto.CryptoKeyDto;
import com.redhat.rhn.frontend.dto.FilePreservationDto;
import com.redhat.rhn.frontend.dto.kickstart.KickstartDto;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.kickstart.KickstartLister;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestStatics;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

/**
 * JUnit test case for the KickstartLister class.
 * @version $Rev: 49711 $
 */

public class KickstartListerTest extends BaseTestCaseWithUser {

   public void testKickstartsInOrg() throws Exception {
        KickstartData k = KickstartDataTest.createKickstartWithOptions(user.getOrg());
        DataResult dr = KickstartLister.getInstance().kickstartsInOrg(k.getOrg(), null);
        assertTrue(dr.size() > 0);
        KickstartDto row = (KickstartDto) dr.get(0);
        assertNotNull(row.getId());
        assertEquals(k.getOrg().getId().longValue(), row.getOrgId().longValue());
        assertNotNull(row.getOrgId());
        assertNotNull(row.getLabel());
        assertNotNull(row.getTreeLabel());
        assertFalse(row.isOrgDefault());
   }

   public void testListKeys() throws Exception {
       Org o = UserTestUtils.findNewOrg(TestStatics.TESTORG);
       CryptoKey key = CryptoTest.createTestKey(o);
       KickstartFactory.saveCryptoKey(key);
       flushAndEvict(key);

       DataResult dr = KickstartLister.getInstance().cryptoKeysInOrg(o);
       assertTrue(dr.size() > 0);
       assertTrue(dr.get(0) instanceof CryptoKeyDto);
   }

   public void testListFiles() throws Exception {
       Org o = UserTestUtils.findNewOrg(TestStatics.TESTORG);
       FileList f = FileListTest.createTestFileList(o);
       CommonFactory.saveFileList(f);
       flushAndEvict(f);

       DataResult dr = KickstartLister.getInstance().preservationListsInOrg(o, null);
       assertTrue(dr.get(0) instanceof FilePreservationDto);
       assertTrue(dr.size() > 0);
   }

    public void testGetActivationKeysInOrg() throws Exception {
        ActivationKeyFactory.createNewKey(user, null, "ak- " + TestUtils.randomString(),
                "", 1L, null, true);

        PageControl pc = new PageControl();
        pc.setStart(1);

        DataResult<ActivationKeyDto> result =
                KickstartLister.getInstance().getActivationKeysInOrg(user.getOrg(), pc);
        assertEquals(1, result.size());
    }

    public void testGetBootstrapActivationKeysInOrg() throws Exception {
        ActivationKey activationKey =
                ActivationKeyFactory.createNewKey(user, null,
                        "ak- " + TestUtils.randomString(), "", 1L, null, true);
        activationKey.setBootstrap("Y");

        PageControl pc = new PageControl();
        pc.setStart(1);

        DataResult<ActivationKeyDto> result =
                KickstartLister.getInstance().getActivationKeysInOrg(user.getOrg(), pc);
        assertEquals(0, result.size());
    }

    public void testGetActiveActivationKeysInOrg() throws Exception {
        ActivationKeyFactory.createNewKey(user, null, "ak- " + TestUtils.randomString(),
                "", 1L, null, true);

        PageControl pc = new PageControl();
        pc.setStart(1);

        @SuppressWarnings("unchecked")
        DataResult<ActivationKeyDto> result =
                KickstartLister.getInstance().getActiveActivationKeysInOrg(user.getOrg(),
                        pc);
        assertEquals(1, result.size());
    }

    public void testGetBootstrapActiveActivationKeysInOrg() throws Exception {
        ActivationKey activationKey =
                ActivationKeyFactory.createNewKey(user, null,
                        "ak- " + TestUtils.randomString(), "", 1L, null, true);
        activationKey.setBootstrap("Y");

        PageControl pc = new PageControl();
        pc.setStart(1);

        @SuppressWarnings("unchecked")
        DataResult<ActivationKeyDto> result =
                KickstartLister.getInstance().getActiveActivationKeysInOrg(user.getOrg(),
                        pc);
        assertEquals(0, result.size());
    }
}
