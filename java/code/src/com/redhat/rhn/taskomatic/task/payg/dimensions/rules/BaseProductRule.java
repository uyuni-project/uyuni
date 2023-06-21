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

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.taskomatic.task.payg.dimensions.DimensionRule;
import com.redhat.rhn.taskomatic.task.payg.dimensions.RuleType;

import java.util.Set;

/**
 * Rule to check the installed base product
 */
public class BaseProductRule implements DimensionRule {

    private final RuleType ruleType;

    private final Set<String> baseProducts;

    private final boolean onlyPaygServers;

    /**
     * Build a new entitlement rule
     * @param ruleTypeIn The type of rule
     * @param baseProductsIn the set of products
     * @param onlyPaygServerIn consider only payg servers
     */
    public BaseProductRule(RuleType ruleTypeIn, Set<String> baseProductsIn, boolean onlyPaygServerIn) {
        this.ruleType = ruleTypeIn;
        this.baseProducts = baseProductsIn;
        this.onlyPaygServers = onlyPaygServerIn;
    }

    @Override
    public boolean test(Server server) {
        if (onlyPaygServers && !server.isPayg()) {
            return false;
        }

        return server.getInstalledProductSet()
                     .filter(suseProductSet -> baseProducts.contains(suseProductSet.getBaseProduct().getName()))
                     .isPresent();
    }

    @Override
    public RuleType getRuleType() {
        return ruleType;
    }

    @Override
    public String toString() {
        return String.format("BaseProductRule: %s if server base product is any of [%s]", ruleType,
            String.join(", ", baseProducts));
    }
}
