/*
 * Copyright (c) 2025 SUSE LLC
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

package com.suse.manager.model.products.migration.test;

import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannelFamily;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.testing.RhnBaseTestCase;

import com.suse.manager.model.products.migration.MigrationDataFactory;
import com.suse.manager.model.products.migration.MigrationProduct;
import com.suse.manager.model.products.migration.MigrationTarget;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

public class MigrationDataFactoryTest extends RhnBaseTestCase {

    private MigrationDataFactory migrationDataFactory;

    private SUSEProduct baseProduct;

    private SUSEProduct baseSystem;
    private SUSEProduct certifications;
    private SUSEProduct containers;
    private SUSEProduct transactional;
    private SUSEProduct serverApps;
    private SUSEProduct publicCloud;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();

        ChannelFamily channelFamily = createTestChannelFamily();

        // Setup some products
        baseProduct = SUSEProductTestUtils.createTestSUSEProduct(channelFamily, "Base");
        baseSystem = SUSEProductTestUtils.createTestSUSEProduct(channelFamily, "BaseSystem");
        certifications = SUSEProductTestUtils.createTestSUSEProduct(channelFamily, "Certifications");
        containers = SUSEProductTestUtils.createTestSUSEProduct(channelFamily, "Containers");
        transactional = SUSEProductTestUtils.createTestSUSEProduct(channelFamily, "Transactional Server");
        serverApps = SUSEProductTestUtils.createTestSUSEProduct(channelFamily, "Server Applications");
        publicCloud = SUSEProductTestUtils.createTestSUSEProduct(channelFamily, "Public Cloud");

        // Build the parent-child relation
        SUSEProductTestUtils.createTestSUSEExtension(baseProduct, baseSystem, baseProduct);
        SUSEProductTestUtils.createTestSUSEExtension(baseProduct, certifications, baseProduct);

        SUSEProductTestUtils.createTestSUSEExtension(baseSystem, containers, baseProduct);
        SUSEProductTestUtils.createTestSUSEExtension(baseSystem, transactional, baseProduct);
        SUSEProductTestUtils.createTestSUSEExtension(baseSystem, serverApps, baseProduct);

        SUSEProductTestUtils.createTestSUSEExtension(serverApps, publicCloud, baseProduct);

        migrationDataFactory = new MigrationDataFactory();
    }

    @Test
    @DisplayName("Convert a SUSEProductSet to a MigrationTarget")
    public void canConvertToMigrationTarget() {
        var productSet = new SUSEProductSet(baseProduct, List.of(baseSystem, containers));

        productSet.addMissingChannels(List.of("containers-pool", "containers-updates"));

        MigrationTarget migrationTarget = migrationDataFactory.toMigrationTarget(productSet);

        assertEquals(productSet.getSerializedProductIDs(), migrationTarget.id());
        assertEquals(List.of("containers-pool", "containers-updates"), migrationTarget.missingChannels());

        assertEquals("SUSE Test product Base", migrationTarget.targetProduct().name());

        List<MigrationProduct> extensions = migrationTarget.targetProduct().addons();
        assertEquals(1, extensions.size());
        assertEquals("SUSE Test product BaseSystem", extensions.get(0).name());

        extensions = extensions.get(0).addons();
        assertEquals(1, extensions.size());
        assertEquals("SUSE Test product Containers", extensions.get(0).name());
    }

    @Test
    @DisplayName("Convert a SUSEProductSet to a MigrationProduct")
    public void canConvertToMigrationProduct() {
        var productSet = new SUSEProductSet(baseProduct,
            List.of(baseSystem, certifications, transactional, serverApps, publicCloud));

        MigrationProduct migrationProduct = migrationDataFactory.toMigrationProduct(productSet);

        assertEquals("SUSE Test product Base", migrationProduct.name());

        List<MigrationProduct> extensions = sortByName(migrationProduct.addons());
        assertEquals(2, migrationProduct.addons().size());

        assertEquals("SUSE Test product BaseSystem", extensions.get(0).name());
        assertEquals(2, extensions.get(0).addons().size());

        assertEquals("SUSE Test product Certifications", extensions.get(1).name());
        assertEquals(0, extensions.get(1).addons().size());

        extensions = sortByName(extensions.get(0).addons());
        assertEquals("SUSE Test product Server Applications", extensions.get(0).name());
        assertEquals(1, extensions.get(0).addons().size());

        assertEquals("SUSE Test product Transactional Server", extensions.get(1).name());
        assertEquals(0, extensions.get(1).addons().size());

        extensions = sortByName(extensions.get(0).addons());
        assertEquals("SUSE Test product Public Cloud", extensions.get(0).name());
        assertEquals(0, extensions.get(0).addons().size());
    }

    private static List<MigrationProduct> sortByName(List<MigrationProduct> products) {
        return products.stream()
            .sorted(Comparator.comparing(MigrationProduct::name))
            .toList();
    }
}

