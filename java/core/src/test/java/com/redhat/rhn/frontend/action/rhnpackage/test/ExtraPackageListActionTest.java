/*
 * Copyright (c) 2022 SUSE LLC
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

package com.redhat.rhn.frontend.action.rhnpackage.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.action.rhnpackage.ExtraPackagesListAction;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.testing.PackageTestUtils;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

import java.util.List;

public class ExtraPackageListActionTest extends RhnMockStrutsTestCase {

    @Test
    public void testExecute() {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerFactoryTest.createTestServer(user, true);
        Package standard = PackageTest.createTestPackage(user.getOrg());
        PackageTestUtils.installPackageOnServer(standard, server);
        server = TestUtils.reload(server);
        //.do?sid=1000010000
        setRequestPathInfo("/systems/details/packages/ExtraPackagesList");
        addRequestParameter("sid", server.getId().toString());
        actionPerform();
        verifyList(ExtraPackagesListAction.DATA_SET, PackageListItem.class);
    }

    @Test
    public void testExecuteWithPtf() {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerFactoryTest.createTestServer(user, true);

        Package standard = PackageTest.createTestPackage(user.getOrg());
        standard.setDescription("Standard package");

        Package ptfMaster = PackageTestUtils.createPtfMaster("123456", "1", user.getOrg());
        Package ptfPackage = PackageTestUtils.createPtfPackage("123456", "1", user.getOrg());

        PackageTestUtils.installPackagesOnServer(List.of(standard, ptfMaster, ptfPackage), server);
        server = TestUtils.reload(server);

        assertFalse(ptfPackage.isMasterPtfPackage());
        assertTrue(ptfPackage.isPartOfPtf());

        assertTrue(ptfMaster.isMasterPtfPackage());
        assertFalse(ptfMaster.isPartOfPtf());

        //.do?sid=1000010000
        setRequestPathInfo("/systems/details/packages/ExtraPackagesList");
        addRequestParameter("sid", server.getId().toString());
        actionPerform();

        @SuppressWarnings("unchecked")
        List<PackageListItem> result = (List<PackageListItem>) request.getAttribute(ExtraPackagesListAction.DATA_SET);
        assertNotNull(result, "The result list is null");
        assertTrue(result.size() > 0, "Your result list is empty");

        // Check the status for the normal package
        String standardPackageName = standard.getPackageName().getName();
        PackageListItem standardItem = result.stream()
                                             .filter(pli -> standardPackageName.equals(pli.getName()))
                                             .findFirst().orElse(null);
        assertNotNull(standardItem, "No entry in the list for the standard package");
        assertTrue(standardItem.isSelectable());

        // Check the status for the ptf package
        String ptfPackageName = ptfPackage.getPackageName().getName();
        PackageListItem ptfItem = result.stream()
                                        .filter(pli -> ptfPackageName.equals(pli.getName()))
                                        .findFirst().orElse(null);
        assertNotNull(ptfItem, "No entry in the list for the ptf package");
        assertFalse(ptfItem.isMasterPtfPackage());
        assertTrue(ptfItem.isPartOfPtf());
        assertFalse(ptfItem.isSelectable());

        // Check the status for the ptf package
        String ptfMasterName = ptfMaster.getPackageName().getName();
        PackageListItem ptfMasterItem = result.stream()
                                              .filter(pli -> ptfMasterName.equals(pli.getName()))
                                              .findFirst().orElse(null);
        assertNotNull(ptfMasterItem, "No entry in the list for the ptf master package");
        assertTrue(ptfMasterItem.isMasterPtfPackage());
        assertFalse(ptfMasterItem.isPartOfPtf());
        assertFalse(ptfMasterItem.isSelectable());
    }

}
