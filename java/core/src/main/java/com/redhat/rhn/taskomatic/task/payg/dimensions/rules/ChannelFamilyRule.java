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

import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.taskomatic.task.payg.dimensions.DimensionRule;
import com.redhat.rhn.taskomatic.task.payg.dimensions.RuleType;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Rule to check the channel family of the installed products
 */
public class ChannelFamilyRule implements DimensionRule {

    private final RuleType ruleType;

    private final RequirementType requirementType;

    private final Set<String> channelFamilies;

    private final boolean considerOnlyBase;

    private final boolean considerChannels;

    /**
     * Default constructor.
     *
     * @param ruleTypeIn The type of rule
     * @param requirementTypeIn specifies how to match the entitlement to the server:<br>
     *          <ul>
     *              <li>ANY: The server must have at least one of the specified channel family</li>
     *              <li>ALL: The server must have all the given channel families</li>
     *          </ul>
     * @param considerOnlyBaseIn if {@code true}, match the channel family only on the base product, if {@code false}
     *  consider all the installed products.
     * @param considerChannelsIn if {@code true}, match the channel family also on the subscribed channels
     * @param channelFamiliesIn the set of channel families to filter
     */
    public ChannelFamilyRule(RuleType ruleTypeIn, RequirementType requirementTypeIn, boolean considerOnlyBaseIn,
                             boolean considerChannelsIn, Set<String> channelFamiliesIn) {
        ruleType = ruleTypeIn;
        requirementType = requirementTypeIn;
        channelFamilies = channelFamiliesIn;
        considerOnlyBase = considerOnlyBaseIn;
        considerChannels = considerChannelsIn;
    }

    @Override
    public RuleType getRuleType() {
        return ruleType;
    }

    @Override
    public boolean test(Server server) {
        Stream<ChannelFamily> familiesFromProducts;
        if (considerOnlyBase) {
            // Extract the family only from the base product
            familiesFromProducts = server.getInstalledProductSet().stream()
                .map(SUSEProductSet::getBaseProduct)
                .map(SUSEProduct::getChannelFamily);
        }
        else {
            // Extract the families from all the installed products
            familiesFromProducts = server.getInstalledProducts().stream()
                .map(InstalledProduct::getSUSEProduct)
                .map(SUSEProduct::getChannelFamily);
        }

        Stream<ChannelFamily> familiesFromChannels;
        if (considerChannels) {
            // Extract the families from the subscribed channels
            familiesFromChannels = server.getChannels().stream().flatMap(ch -> ch.getChannelFamilies().stream());
        }
        else {
            familiesFromChannels = Stream.of();
        }

        Set<String> serverChannelFamilies = Stream.concat(familiesFromProducts, familiesFromChannels)
            .filter(Objects::nonNull)
            .map(ChannelFamily::getLabel)
            .collect(Collectors.toSet());

        return switch (requirementType) {
            case ANY:
                yield serverChannelFamilies.stream().anyMatch(e -> channelFamilies.contains(e));

            case ALL:
                yield serverChannelFamilies.containsAll(channelFamilies);
        };
    }

}
