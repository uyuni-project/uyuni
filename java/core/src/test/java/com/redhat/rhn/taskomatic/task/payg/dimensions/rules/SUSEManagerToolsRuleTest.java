/*
 * Copyright (c) 2023--2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.taskomatic.task.payg.dimensions.rules;

import static com.redhat.rhn.taskomatic.task.payg.dimensions.rules.DimensionRuleTestUtils.mockAddonProduct;
import static com.redhat.rhn.taskomatic.task.payg.dimensions.rules.DimensionRuleTestUtils.mockBaseProduct;
import static com.redhat.rhn.taskomatic.task.payg.dimensions.rules.DimensionRuleTestUtils.mockChannel;
import static com.redhat.rhn.taskomatic.task.payg.dimensions.rules.DimensionRuleTestUtils.mockServer;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.taskomatic.task.payg.dimensions.DimensionRule;
import com.redhat.rhn.testing.MockObjectTestCase;

import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

class SUSEManagerToolsRuleTest extends MockObjectTestCase {

    @BeforeEach
    void setup() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @Test
    void canIncludeWhenProductHasToolsExtensionOrChannelIsSubscribed() {
        Set<InstalledProduct> productSetWithSUMATools = Set.of(
            mockBaseProduct(context, "sles", "15", "7261"),
            mockAddonProduct(context, "sle-module-basesystem", "15.4", "MODULE"),
            mockAddonProduct(context, "sle-module-server-applications", "15.4", "MODULE"),
            mockAddonProduct(context, "sle-manager-tools", "15.4", "SLE-M-T")
        );

        Set<Channel> channelSetWithSUMATools = Set.of(
            mockChannel(context, "sle-product-sles15-sp4-pool-x86_64", "7261"),
            mockChannel(context, "sle-product-sles15-sp4-updates-x86_64", "7261"),
            mockChannel(context, "sle-module-basesystem15-sp4-pool-x86_64", "MODULE"),
            mockChannel(context, "sle-module-basesystem15-sp4-updates-x86_64", "MODULE"),
            mockChannel(context, "sle-module-server-applications15-sp4-pool-x86_64", "MODULE"),
            mockChannel(context, "sle-module-server-applications15-sp4-updates-x86_64", "MODULE"),
            mockChannel(context, "sle-manager-tools15-pool-x86_64-sp4", "SLE-M-T"),
            mockChannel(context, "sle-manager-tools15-updates-x86_64-sp4", "SLE-M-T")
        );

        var serverBoth = mockServer(context, productSetWithSUMATools, channelSetWithSUMATools);
        var serverOnlyProduct = mockServer(context, productSetWithSUMATools, Set.of());
        var serverOnlyChannel = mockServer(context, Set.of(), channelSetWithSUMATools);

        DimensionRule managerToolsRule = new SUSEManagerToolsRule();

        assertTrue(managerToolsRule.includes(serverBoth));
        assertFalse(managerToolsRule.excludes(serverBoth));

        assertTrue(managerToolsRule.includes(serverOnlyProduct));
        assertFalse(managerToolsRule.excludes(serverOnlyProduct));

        assertTrue(managerToolsRule.includes(serverOnlyChannel));
        assertFalse(managerToolsRule.excludes(serverOnlyChannel));
    }

    @Test
    void canExcludeWhenNoToolsArePresent() {
        Set<Channel> channelSetWithoutSUMATools = Set.of(
            mockChannel(context, "custom-channel-x86_64", null),
            mockChannel(context, "custom-channel-updates-x86_64", null)
        );

        Server server = mockServer(context, Set.of(), channelSetWithoutSUMATools);

        DimensionRule managerToolsRule = new SUSEManagerToolsRule();

        assertTrue(managerToolsRule.excludes(server));
        assertFalse(managerToolsRule.includes(server));
    }

    @Test
    void canIncludeSLES12Server() {
        Set<InstalledProduct> sle12ProductSet = Set.of(
            mockBaseProduct(context, "sles", "12.3", "7261")
        );

        Set<Channel> sles12ChannelSet = Set.of(
            mockChannel(context, "sles12-sp3-pool-x86_64", "7261"),
            mockChannel(context, "sles12-sp3-updates-x86_64", "7261"),
            mockChannel(context, "sle-manager-tools12-pool-x86_64-sp3", "7261"),
            mockChannel(context, "sle-manager-tools12-updates-x86_64-sp3", "7261")
        );

        Server server = mockServer(context, sle12ProductSet, sles12ChannelSet);

        DimensionRule managerToolsRule = new SUSEManagerToolsRule();

        assertTrue(managerToolsRule.includes(server));
        assertFalse(managerToolsRule.excludes(server));
    }
}
