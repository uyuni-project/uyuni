/*
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.frontend.dto.ActivationKeyDto;
import com.redhat.rhn.frontend.dto.CryptoKeyDto;
import com.redhat.rhn.frontend.dto.FilePreservationDto;
import com.redhat.rhn.frontend.dto.kickstart.CobblerProfileDto;
import com.redhat.rhn.frontend.dto.kickstart.KickstartDto;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.kickstart.KickstartLister;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestStatics;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.cobbler.CobblerConnection;
import org.cobbler.Distro;
import org.cobbler.Profile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JUnit test case for the KickstartLister class.
 */

public class KickstartListerTest extends BaseTestCaseWithUser {

    @Test
   public void testKickstartsInOrg() throws Exception {
        KickstartData k = KickstartDataTest.createKickstartWithOptions(user.getOrg());
        DataResult dr = KickstartLister.getInstance().kickstartsInOrg(k.getOrg(), null);
        assertFalse(dr.isEmpty());
        KickstartDto row = (KickstartDto) dr.get(0);
        assertNotNull(row.getId());
        assertEquals(k.getOrg().getId().longValue(), row.getOrgId().longValue());
        assertNotNull(row.getOrgId());
        assertNotNull(row.getLabel());
        assertNotNull(row.getTreeLabel());
        assertFalse(row.isOrgDefault());
   }

    @Test
   public void testListKeys() {
       Org o = UserTestUtils.findNewOrg(TestStatics.TESTORG);
       CryptoKey key = CryptoTest.createTestKey(o);
       KickstartFactory.saveCryptoKey(key);
       flushAndEvict(key);

       DataResult<CryptoKeyDto> dr = KickstartLister.getInstance().cryptoKeysInOrg(o);
        assertFalse(dr.isEmpty());
        assertNotNull(dr.get(0));
   }

    @Test
   public void testListFiles() {
       Org o = UserTestUtils.findNewOrg(TestStatics.TESTORG);
       FileList f = FileListTest.createTestFileList(o);
       CommonFactory.saveFileList(f);
       flushAndEvict(f);

       DataResult<FilePreservationDto> dr = KickstartLister.getInstance().preservationListsInOrg(o, null);
       assertNotNull(dr.get(0));
        assertFalse(dr.isEmpty());
   }

    @Test
    public void testGetActivationKeysInOrg() {
        ActivationKeyFactory.createNewKey(user, null, "ak- " + TestUtils.randomString(),
                "", 1L, null, true);

        PageControl pc = new PageControl();
        pc.setStart(1);

        DataResult<ActivationKeyDto> result =
                KickstartLister.getInstance().getActivationKeysInOrg(user.getOrg(), pc);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetActiveActivationKeysInOrg() {
        ActivationKeyFactory.createNewKey(user, null, "ak- " + TestUtils.randomString(),
                "", 1L, null, true);

        PageControl pc = new PageControl();
        pc.setStart(1);

        DataResult<ActivationKeyDto> result =
                KickstartLister.getInstance().getActiveActivationKeysInOrg(user.getOrg(),
                        pc);
        assertEquals(1, result.size());
    }

    @Test
    public void testListCobblerProfiles() {
        CobblerConnection connection = CobblerXMLRPCHelper.getConnection("test");
        Distro distro = new Distro.Builder<Map<String, Object>>()
                .setName("test-distro")
                .setKernel("test-kernel")
                .setInitrd("test-initrd")
                .setKsmeta(Optional.empty())
                .build(connection);
        Profile.create(connection, "test-profile", distro);
        KickstartLister kickstartLister = KickstartLister.getInstance();
        List<CobblerProfileDto> profiles = kickstartLister.listCobblerProfiles(user);

        assertEquals(1, profiles.size());

        // Bootstrap profile should NOT be returned
        Profile.create(connection, Profile.BOOTSTRAP_NAME, distro);
        profiles = kickstartLister.listCobblerProfiles(user);
        assertEquals(1, profiles.size());
    }
}
