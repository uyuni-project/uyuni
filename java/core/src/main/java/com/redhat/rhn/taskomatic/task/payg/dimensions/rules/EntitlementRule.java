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

import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.taskomatic.task.payg.dimensions.DimensionRule;
import com.redhat.rhn.taskomatic.task.payg.dimensions.RuleType;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Rule that checks if the given server has any of or all the specified entitlement
 */
public class EntitlementRule implements DimensionRule {

    private final RuleType ruleType;

    private final RequirementType requirementType;

    private final Set<Entitlement> entitlements;

    /**
     * Build a new entitlement rule
     * @param ruleTypeIn The type of rule
     * @param requirementTypeIn specifies how to match the entitlement to the server:<br>
     *                          <ul>
     *                              <li>ANY: The server must have at least one of the specified entitlements</li>
     *                              <li>ALL: The server must have all the given entitlements</li>
     *                          </ul>
     * @param entitlementsIn the set of entitlements
     */
    public EntitlementRule(RuleType ruleTypeIn, RequirementType requirementTypeIn, Set<Entitlement> entitlementsIn) {
        this.ruleType = ruleTypeIn;
        this.requirementType = requirementTypeIn;
        this.entitlements = entitlementsIn;
    }

    @Override
    public boolean test(Server server) {
        Set<Entitlement> serverEntitlements = server.getEntitlements();

        switch (requirementType) {
            case ANY:
                return entitlements.stream().anyMatch(e -> serverEntitlements.contains(e));

            case ALL:
                return serverEntitlements.containsAll(entitlements);

            default:
                throw new IllegalStateException("Unexpected requirement type: " + requirementType);
        }
    }

    @Override
    public RuleType getRuleType() {
        return ruleType;
    }

    @Override
    public String toString() {
        return String.format("EntitlementRule: %s if server has %s of [%s]", ruleType, requirementType,
            entitlements.stream()
                        .map(e -> e.getHumanReadableLabel())
                        .collect(Collectors.joining(", ")));
    }
}
