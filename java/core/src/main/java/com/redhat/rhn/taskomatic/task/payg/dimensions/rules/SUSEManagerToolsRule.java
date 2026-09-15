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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.taskomatic.task.payg.dimensions.rules;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.taskomatic.task.payg.dimensions.DimensionRule;
import com.redhat.rhn.taskomatic.task.payg.dimensions.RuleType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A rule to filter server that use the SUSE Manager tools
 */
public class SUSEManagerToolsRule implements DimensionRule {

    public static final String VERSION_12_REGEX = "12(\\.[1-9]*)?";
    public static final String SLE_PRODUCT_FAMILY = "sle";

    private static final List<String> TOOLS_CHANNEL_FAMILIES = new ArrayList<>();
    static {
        TOOLS_CHANNEL_FAMILIES.add(ChannelFamilyFactory.TOOLS_CHANNEL_FAMILY_LABEL);
        if (Config.get().getString(ConfigDefaults.PRODUCT_TREE_TAG, "").equals("Beta")) {
            String betaClass = ChannelFamilyFactory.TOOLS_CHANNEL_FAMILY_LABEL + "-BETA";
            TOOLS_CHANNEL_FAMILIES.add(betaClass);
        }
    }

    @Override
    public boolean test(Server server) {
        // If the SUSE Manager tools are part of the base we count it
        if (areToolsPartOfBaseProduct(server)) {
            return true;
        }

        // Match the Manager Tools family among the products or the subscribed channels
        return Stream.concat(
                         // Extract the families the installed products
                         server.getInstalledProducts().stream().map(prd -> prd.getSUSEProduct().getChannelFamily()),
                         // Extract the families from the subscribed channels
                         server.getChannels().stream().flatMap(ch -> ch.getChannelFamilies().stream())
                     )
                     .filter(Objects::nonNull)
                     .anyMatch(family -> TOOLS_CHANNEL_FAMILIES.contains(family.getLabel()));
    }

    @Override
    public RuleType getRuleType() {
        return RuleType.INCLUDE;
    }

    /**
     * Returns true if this server has a base product that includes already SUSE Manager tools. This is a special
     * case for all SLE 12 products.
     * @param server the server to check
     * @return true if the SUSE Manager tools are included in the base product, false otherwise.
     */
    private static boolean areToolsPartOfBaseProduct(Server server) {
        return server.getInstalledProductSet()
                     .map(SUSEProductSet::getBaseProduct)
                     .filter(product -> product.getName().startsWith(SLE_PRODUCT_FAMILY) &&
                         product.getVersion().matches(VERSION_12_REGEX))
                     .isPresent();
    }

    @Override
    public String toString() {
        return String.format("SUSEManagerToolsRule: %s if server has the SUSE Manager Tools", getRuleType());
    }
}
