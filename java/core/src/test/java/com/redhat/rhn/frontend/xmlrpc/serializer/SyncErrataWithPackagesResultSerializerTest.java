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
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.redhat.rhn.frontend.xmlrpc.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactoryTest;
import com.redhat.rhn.domain.errata.ErrataTestBuilder;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageTest;
import com.redhat.rhn.manager.errata.syncwithpackages.dto.SyncErrataWithPackagesResult;
import com.redhat.rhn.manager.errata.syncwithpackages.dto.SyncedErratumWithPackages;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.manager.api.SerializedApiResponse;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncErrataWithPackagesResultSerializerTest extends BaseTestCaseWithUser {

    /**
     * The result and each synchronized erratum are serialized with the documented keys; a package shared by two errata
     * is listed under both.
     */
    @Test
    public void testSerialize() {
        Errata errata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        Package pkg = errata.getPackages().iterator().next();
        SyncedErratumWithPackages synced = new SyncedErratumWithPackages(errata, List.of(pkg));

        // a second erratum with three packages, one of them shared with the first erratum
        Package pkg2 = PackageTest.createTestPackage(user.getOrg());
        Package pkg3 = PackageTest.createTestPackage(user.getOrg());
        Errata other = new ErrataTestBuilder().orgId(user.getOrg().getId()).addPackages(pkg, pkg2, pkg3)
                .buildAndSave();
        SyncedErratumWithPackages otherSynced = new SyncedErratumWithPackages(other, List.of(pkg, pkg2, pkg3));

        SyncErrataWithPackagesResult result = new SyncErrataWithPackagesResult(List.of(synced, otherSynced),
                List.of("MISSING"), 3);

        assertEquals(Map.of(
                "erratas", List.of(synced, otherSynced),
                "unmatched_advisory_names", List.of("MISSING"),
                "unmatched_remaining", 3), toMap(new SyncErrataWithPackagesResultSerializer().serialize(result)));

        assertEquals(Map.of(
                "id", errata.getId(),
                "advisory_name", errata.getAdvisoryName(),
                "packages", List.of(Map.of("id", pkg.getId(), "name_evra", pkg.getNameEvra()))),
                toMap(new SyncedErratumSerializer().serialize(synced)));

        assertEquals(Map.of(
                "id", other.getId(),
                "advisory_name", other.getAdvisoryName(),
                "packages", List.of(
                        Map.of("id", pkg.getId(), "name_evra", pkg.getNameEvra()),
                        Map.of("id", pkg2.getId(), "name_evra", pkg2.getNameEvra()),
                        Map.of("id", pkg3.getId(), "name_evra", pkg3.getNameEvra()))),
                toMap(new SyncedErratumSerializer().serialize(otherSynced)));
    }

    private Map<String, Object> toMap(SerializedApiResponse response) {
        Map<String, Object> map = new HashMap<>();
        response.forEach(map::put);
        return map;
    }
}
