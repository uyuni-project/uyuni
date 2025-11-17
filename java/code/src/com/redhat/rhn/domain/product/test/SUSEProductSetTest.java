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

package com.redhat.rhn.domain.product.test;

import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannelFamily;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

public class SUSEProductSetTest extends RhnBaseTestCase {

    private SUSEProduct baseProduct;
    private SUSEProduct alternativeBase;

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
        alternativeBase = SUSEProductTestUtils.createTestSUSEProduct(channelFamily, "Alternative");

        baseSystem = SUSEProductTestUtils.createTestSUSEProduct(channelFamily, "BaseSystem");
        certifications = SUSEProductTestUtils.createTestSUSEProduct(channelFamily, "Certifications");
        containers = SUSEProductTestUtils.createTestSUSEProduct(channelFamily, "Containers");
        transactional = SUSEProductTestUtils.createTestSUSEProduct(channelFamily, "Transactional Server");
        serverApps = SUSEProductTestUtils.createTestSUSEProduct(channelFamily, "Server Applications");
        publicCloud = SUSEProductTestUtils.createTestSUSEProduct(channelFamily, "Public Cloud");
    }

    @Test
    @DisplayName("SUSEProduct.merge() can merge two sets of products with the same base")
    public void canMergeTwoCompatibleProductSets() {

        var firstProductSet = new SUSEProductSet(baseProduct, List.of(baseSystem, serverApps, containers, publicCloud));
        var secondProductSet = new SUSEProductSet(baseProduct, List.of(baseSystem, transactional, certifications));

        SUSEProductSet result = SUSEProductSet.merge(firstProductSet, secondProductSet);

        assertEquals(baseProduct, result.getBaseProduct());
        // Sort the addons to ignore the ordering but keep duplicates if they are present (which should fail the test)
        assertEquals(
            sortByProductId(List.of(baseSystem, transactional, certifications, serverApps, containers, publicCloud)),
            sortByProductId(result.getAddonProducts())
        );
    }

    @Test
    @DisplayName("SUSEProductSet.merge() throws an exception when trying to merge two sets with different bases")
    public void throwsExceptionIfTheBaseAreIncompatible() {

        var firstProductSet = new SUSEProductSet(baseProduct, List.of(baseSystem, serverApps, containers, publicCloud));
        var secondProductSet = new SUSEProductSet(alternativeBase, List.of(baseSystem, transactional, certifications));

        var exception = assertThrows(
            IllegalArgumentException.class,
            () -> SUSEProductSet.merge(firstProductSet, secondProductSet)
        );

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().startsWith("The base products do not match between the given product sets"));
    }

    private static List<SUSEProduct> sortByProductId(List<SUSEProduct> addonsList) {
        return addonsList.stream()
            .sorted(Comparator.comparing(SUSEProduct::getProductId))
            .toList();
    }
}
