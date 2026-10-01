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
package com.redhat.rhn.taskomatic.task.payg.dimensions.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.taskomatic.task.payg.dimensions.DimensionRule;
import com.redhat.rhn.taskomatic.task.payg.dimensions.RuleType;
import com.redhat.rhn.testing.MockObjectTestCase;

import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

class ChannelFamilyRuleTest extends MockObjectTestCase {

    @BeforeEach
    void setup() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @Test
    void canIncludeWithAnyRequirementMatchingBaseProductFamily() {
        Set<InstalledProduct> installedProducts = Set.of(
            DimensionRuleTestUtils.mockBaseProduct(this.context(), "sles", "15.4", "7261")
        );

        Server server = DimensionRuleTestUtils.mockServer(this.context(), installedProducts);

        DimensionRule rule = new ChannelFamilyRule(RuleType.INCLUDE, RequirementType.ANY, true, false, Set.of("7261"));

        assertTrue(rule.includes(server));
        assertFalse(rule.excludes(server));
    }

    @Test
    void canExcludeWithAnyRequirementWhenBaseProductFamilyDoesNotMatch() {
        Set<InstalledProduct> installedProducts = Set.of(
            DimensionRuleTestUtils.mockBaseProduct(this.context(), "sles", "15.4", "7261")
        );

        Server server = DimensionRuleTestUtils.mockServer(this.context(), installedProducts);

        DimensionRule rule = new ChannelFamilyRule(RuleType.INCLUDE, RequirementType.ANY, true, false,
            Set.of("SLE-HAE", "SLE-POS"));

        assertFalse(rule.includes(server));
        assertTrue(rule.excludes(server));
    }

    @Test
    void canIncludeWithAnyRequirementMatchingAddonProductFamily() {
        Set<InstalledProduct> installedProducts = Set.of(
            DimensionRuleTestUtils.mockBaseProduct(this.context(), "sles", "15.4", "7261"),
            DimensionRuleTestUtils.mockAddonProduct(this.context(), "sle-manager-tools", "15.4", "SLE-M-T")
        );

        Server server = DimensionRuleTestUtils.mockServer(this.context(), installedProducts);

        // considerOnlyBase=false so all installed products are checked
        DimensionRule rule = new ChannelFamilyRule(RuleType.INCLUDE, RequirementType.ANY, false, false,
            Set.of("SLE-M-T"));

        assertTrue(rule.includes(server));
        assertFalse(rule.excludes(server));
    }

    @Test
    void canIncludeWithAllRequirementWhenAllProductFamiliesPresent() {
        Set<InstalledProduct> installedProducts = Set.of(
            DimensionRuleTestUtils.mockBaseProduct(this.context(), "sles", "15.4", "7261"),
            DimensionRuleTestUtils.mockAddonProduct(this.context(), "sle-ha", "15.4", "SLE-HAE"),
            DimensionRuleTestUtils.mockAddonProduct(this.context(), "sle-manager-tools", "15.4", "SLE-M-T")
        );

        Server server = DimensionRuleTestUtils.mockServer(this.context(), installedProducts);

        DimensionRule rule = new ChannelFamilyRule(RuleType.INCLUDE, RequirementType.ALL, false, false,
            Set.of("7261", "SLE-HAE", "SLE-M-T"));

        assertTrue(rule.includes(server));
        assertFalse(rule.excludes(server));
    }

    @Test
    void canExcludeWithAllRequirementWhenOneProductFamilyIsMissing() {
        Set<InstalledProduct> installedProducts = Set.of(
            DimensionRuleTestUtils.mockBaseProduct(this.context(), "sles", "15.4", "7261"),
            DimensionRuleTestUtils.mockAddonProduct(this.context(), "sle-ha", "15.4", "SLE-HAE")
        );

        Server server = DimensionRuleTestUtils.mockServer(this.context(), installedProducts, Collections.emptySet());

        // Server has 7261 and SLE-HAE, but not SLE-M-T
        DimensionRule rule = new ChannelFamilyRule(RuleType.INCLUDE, RequirementType.ALL, false, false,
            Set.of("7261", "SLE-HAE", "SLE-M-T"));

        assertFalse(rule.includes(server));
        assertTrue(rule.excludes(server));
    }

    @Test
    void canIncludeViaChannelFamilyWhenConsiderChannelsIsTrue() {
        Set<Channel> subscribedChannels = Set.of(
            DimensionRuleTestUtils.mockChannel(this.context(), "sle-manager-tools15-pool-x86_64-sp4", "SLE-M-T")
        );

        Server server = DimensionRuleTestUtils.mockServer(this.context(), Set.of(), subscribedChannels);

        // No matching products, but the subscribed channel has the matching family
        DimensionRule rule = new ChannelFamilyRule(RuleType.INCLUDE, RequirementType.ANY, false, true,
            Set.of("SLE-M-T"));

        assertTrue(rule.includes(server));
        assertFalse(rule.excludes(server));
    }

    @Test
    void canExcludeWhenChannelFamilyMatchesButConsiderChannelsIsFalse() {
        Set<Channel> subscribedChannels = Set.of(
            DimensionRuleTestUtils.mockChannel(this.context(), "sle-manager-tools15-pool-x86_64-sp4", "SLE-M-T")
        );

        Server server = DimensionRuleTestUtils.mockServer(this.context(), Set.of(), subscribedChannels);

        // The channel has the matching family, but considerChannels=false so it must be ignored
        DimensionRule rule = new ChannelFamilyRule(RuleType.INCLUDE, RequirementType.ANY, false, false,
            Set.of("SLE-M-T"));

        assertFalse(rule.includes(server));
        assertTrue(rule.excludes(server));
    }
}
