/*
 * Copyright (c) 2023 SUSE LLC
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

package com.redhat.rhn.taskomatic.task.payg.dimensions.rules;

import static org.jmock.AbstractExpectations.returnValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.taskomatic.task.payg.dimensions.DimensionRule;
import com.redhat.rhn.testing.RhnJmockBaseTestCase;

import org.apache.commons.lang3.RandomStringUtils;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class SUSEManagerToolsRuleTest extends RhnJmockBaseTestCase {

    @BeforeEach
    public void setup() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @Test
    public void canIncludeWhenProductHasToolsExtensionOrChannelIsSubscribed() {
        Set<InstalledProduct> productSetWithSUMATools = Set.of(
            mockProduct("sles", "15", true, "7261"),
            mockProduct("sle-module-basesystem", "15.4", false, "MODULE"),
            mockProduct("sle-module-server-applications", "15.4", false, "MODULE"),
            mockProduct("sle-manager-tools", "15.4", false, "SLE-M-T")
        );

        Set<Channel> channelSetWithSUMATools = Set.of(
            mockChannel("sle-product-sles15-sp4-pool-x86_64", "7261"),
            mockChannel("sle-product-sles15-sp4-updates-x86_64", "7261"),
            mockChannel("sle-module-basesystem15-sp4-pool-x86_64", "MODULE"),
            mockChannel("sle-module-basesystem15-sp4-updates-x86_64", "MODULE"),
            mockChannel("sle-module-server-applications15-sp4-pool-x86_64", "MODULE"),
            mockChannel("sle-module-server-applications15-sp4-updates-x86_64", "MODULE"),
            mockChannel("sle-manager-tools15-pool-x86_64-sp4", "SLE-M-T"),
            mockChannel("sle-manager-tools15-updates-x86_64-sp4", "SLE-M-T")
        );

        Server serverBoth = mockServer(productSetWithSUMATools, channelSetWithSUMATools);
        Server serverOnlyProduct = mockServer(productSetWithSUMATools, Collections.emptySet());
        Server serverOnlyChannel = mockServer(Collections.emptySet(), channelSetWithSUMATools);

        DimensionRule managerToolsRule = new SUSEManagerToolsRule();

        assertTrue(managerToolsRule.includes(serverBoth));
        assertFalse(managerToolsRule.excludes(serverBoth));

        assertTrue(managerToolsRule.includes(serverOnlyProduct));
        assertFalse(managerToolsRule.excludes(serverOnlyProduct));

        assertTrue(managerToolsRule.includes(serverOnlyChannel));
        assertFalse(managerToolsRule.excludes(serverOnlyChannel));
    }

    @Test
    public void canExcludeWhenNoToolsArePresent() {
        Set<Channel> channelSetWithoutSUMATools = Set.of(
            mockChannel("custom-channel-x86_64", null),
            mockChannel("custom-channel-updates-x86_64", null)
        );

        Server server = mockServer(Collections.emptySet(), channelSetWithoutSUMATools);

        DimensionRule managerToolsRule = new SUSEManagerToolsRule();

        assertTrue(managerToolsRule.excludes(server));
        assertFalse(managerToolsRule.includes(server));
    }

    @Test
    public void canIncludeSLES12Server() {
        Set<InstalledProduct> sle12ProductSet = Set.of(
            mockProduct("sles", "12.3", true, "7261")
        );

        Set<Channel> sles12ChannelSet = Set.of(
            mockChannel("sles12-sp3-pool-x86_64", "7261"),
            mockChannel("sles12-sp3-updates-x86_64", "7261"),
            mockChannel("sle-manager-tools12-pool-x86_64-sp3", "7261"),
            mockChannel("sle-manager-tools12-updates-x86_64-sp3", "7261")
        );

        Server server = mockServer(sle12ProductSet, sles12ChannelSet);

        DimensionRule managerToolsRule = new SUSEManagerToolsRule();

        assertTrue(managerToolsRule.includes(server));
        assertFalse(managerToolsRule.excludes(server));
    }

    private Channel mockChannel(String channelLabel, String channelFamily) {
        Channel channel = mock(Channel.class, channelLabel);

        checking(expectations -> {
            expectations.allowing(channel).getLabel();
            expectations.will(returnValue(channelLabel));

            expectations.allowing(channel).getChannelFamilies();
            expectations.will(returnValue(
                channelFamily != null ? Set.of(new ChannelFamily(channelFamily)) : Collections.emptySet()
            ));
        });

        return channel;
    }

    private Server mockServer(Set<InstalledProduct> installedProductsSet, Set<Channel> channelsSet) {
        Server server = mock(Server.class, RandomStringUtils.randomNumeric(10));

        checking(expectations -> {
            expectations.allowing(server).getInstalledProducts();
            expectations.will(returnValue(installedProductsSet));

            expectations.allowing(server).getInstalledProductSet();
            expectations.will(returnValue(Optional.of(new SUSEProductSet(installedProductsSet))));

            expectations.allowing(server).getChannels();
            expectations.will(returnValue(channelsSet));
        });

        return server;
    }

    private InstalledProduct mockProduct(String productName, String version, boolean isBase, String channelFamily) {
        InstalledProduct installedProduct = mock(InstalledProduct.class, "installed_" + productName);
        SUSEProduct suseProduct = mock(SUSEProduct.class, productName);

        checking(expectations -> {
            expectations.allowing(suseProduct).isBase();
            expectations.will(returnValue(isBase));

            expectations.allowing(suseProduct).getName();
            expectations.will(returnValue(productName));

            expectations.allowing(suseProduct).getVersion();
            expectations.will(returnValue(version));

            expectations.allowing(suseProduct).getFriendlyName();
            expectations.will(returnValue("Mock for " + productName + " " + version));

            expectations.allowing(suseProduct).getChannelFamily();
            expectations.will(returnValue(new ChannelFamily(channelFamily)));

            expectations.allowing(installedProduct).isBaseproduct();
            expectations.will(returnValue(isBase));

            expectations.allowing(installedProduct).getSUSEProduct();
            expectations.will(returnValue(suseProduct));
        });

        return installedProduct;
    }

}
