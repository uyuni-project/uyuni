/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.manager.rhnpackage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.ChannelFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageNevra;
import com.redhat.rhn.domain.rhnpackage.PackageTest;
import com.redhat.rhn.domain.rhnpackage.profile.Profile;
import com.redhat.rhn.domain.rhnpackage.profile.ProfileTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactoryTest;
import com.redhat.rhn.domain.server.ServerSnapshot;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.manager.profile.ProfileManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.hibernate.type.StandardBasicTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Exercise package sync blacklist filtering in each production query mode.
 */
public class PackageSyncBlacklistTest extends BaseTestCaseWithUser {

    private Server server;
    private Package blacklistedPackage;
    private Package unrelatedPackage;

    @BeforeEach
    public void setUp() {
        server = ServerFactoryTest.createTestServer(user, true);
        blacklistedPackage = PackageTest.createTestPackage(user.getOrg());
        unrelatedPackage = PackageTest.createTestPackage(user.getOrg());
    }

    @Test
    public void testSystemCanonicalPackageList() {
        installPackages();
        assertCanonicalBlacklistFiltering("system_canonical_package_list",
                Map.of("sid", server.getId(), "org_id", user.getOrg().getId()));
    }

    @Test
    public void testProfileCanonicalPackageList() {
        installPackages();
        Profile profile = ProfileTest.createTestProfile(user, ChannelFactoryTest.createTestChannel(user));
        ProfileManager.copyFrom(server, profile);
        assertCanonicalBlacklistFiltering("profile_canonical_package_list",
                Map.of("prid", profile.getId(), "org_id", user.getOrg().getId()));
    }

    @Test
    public void testSnapshotUnservablePackageList() {
        ServerSnapshot snapshot = new ServerSnapshot();
        snapshot.setServer(server);
        snapshot.setOrg(user.getOrg());
        snapshot.setReason("Package sync blacklist test");
        // Neither package is installed on the server or served by a snapshot channel.
        for (Package pkg : List.of(blacklistedPackage, unrelatedPackage)) {
            PackageNevra nevra = new PackageNevra();
            nevra.setName(pkg.getPackageName());
            nevra.setEvr(pkg.getPackageEvr());
            nevra.setArch(pkg.getPackageArch());
            snapshot.getPackages().add(TestUtils.saveAndFlush(nevra));
        }
        TestUtils.saveAndFlush(snapshot);

        assertBlacklistFiltering(() -> {
            DataResult<Row> rows = ModeFactory.getMode("Package_queries", "snapshot_unservable_package_list")
                    .execute(Map.of("sid", server.getId(), "ss_id", snapshot.getId(),
                            "org_id", user.getOrg().getId()));
            return rows.stream().map(row -> ((Number) row.get("name_id")).longValue()).sorted().toList();
        });
    }

    private void installPackages() {
        for (Package pkg : List.of(blacklistedPackage, unrelatedPackage)) {
            ModeFactory.getWriteMode("test_queries", "insert_into_rhnServerPackage_with_arch")
                    .executeUpdate(Map.of("server_id", server.getId(), "pn_id", pkg.getPackageName().getId(),
                            "evr_id", pkg.getPackageEvr().getId(), "arch_id", pkg.getPackageArch().getId()));
        }
    }

    private void assertCanonicalBlacklistFiltering(String mode, Map<String, Long> params) {
        assertBlacklistFiltering(() -> {
            DataResult<PackageListItem> packages = ModeFactory.getMode("Package_queries", mode).execute(params);
            return packages.stream().map(PackageListItem::getNameId).sorted().toList();
        });
    }

    private void assertBlacklistFiltering(Supplier<List<Long>> query) {
        List<Long> bothPackages = List.of(blacklistedPackage.getPackageName().getId(),
                unrelatedPackage.getPackageName().getId()).stream().sorted().toList();
        List<Long> unrelatedOnly = List.of(unrelatedPackage.getPackageName().getId());
        assertEquals(bothPackages, query.get(), "Both packages must be present before blacklisting");

        HibernateFactory.getSession().createNativeMutationQuery(
                "INSERT INTO rhnPackageSyncBlacklist (package_name_id, org_id) VALUES (:name_id, :org_id)")
                .setParameter("name_id", blacklistedPackage.getPackageName().getId())
                .setParameter("org_id", user.getOrg().getId())
                .executeUpdate();
        assertEquals(unrelatedOnly, query.get(), "An organization blacklist must leave unrelated packages");

        setBlacklistOrg(null);
        assertEquals(unrelatedOnly, query.get(), "A global blacklist must exclude only the named package");

        setBlacklistOrg(TestUtils.saveAndFlush(UserTestUtils.createOrg()).getId());
        assertEquals(bothPackages, query.get(), "Another organization's blacklist must not exclude either package");
    }

    private void setBlacklistOrg(Long orgId) {
        HibernateFactory.getSession().createNativeMutationQuery(
                "UPDATE rhnPackageSyncBlacklist SET org_id = :org_id WHERE package_name_id = :name_id")
                .setParameter("org_id", orgId, StandardBasicTypes.LONG)
                .setParameter("name_id", blacklistedPackage.getPackageName().getId())
                .executeUpdate();
    }
}
