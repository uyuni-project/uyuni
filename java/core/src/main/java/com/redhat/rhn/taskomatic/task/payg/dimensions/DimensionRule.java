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

package com.redhat.rhn.taskomatic.task.payg.dimensions;

import com.redhat.rhn.domain.server.Server;

public interface DimensionRule {

    /**
     * Apply this rule to the given server.
     * @param server the server to test
     * @return true if the test is successful.
     */
    boolean test(Server server);

    /**
     * Define the type of rule, incluse or exclusive
     * @return the rule type
     */
    RuleType getRuleType();

    /**
     * Invert this rule.
     * @return an inverted version of this rule
     */
    default DimensionRule invert() {

        final DimensionRule rule = this;
        return new DimensionRule() {
            @Override
            public boolean test(Server server) {
                return !rule.test(server);
            }

            @Override
            public RuleType getRuleType() {
                return rule.getRuleType() == RuleType.INCLUDE ? RuleType.EXCLUDE : RuleType.INCLUDE;
            }
        };
    }

    /**
     * Check if the specified server is included by this rule
     * @param server the server to check
     * @return true if this rule is inclusive and the test passes or if it's exclusive and fails.
     */
    default boolean includes(Server server) {
        if (getRuleType() == RuleType.EXCLUDE) {
            return invert().test(server);
        }

        return test(server);
    }

    /**
     * Check if the specified server is excluded by this rule
     * @param server the server to check
     * @return true if this rule is exclusive and the test passes or it's inclusive and fails.
     */
    default boolean excludes(Server server) {
        if (getRuleType() == RuleType.INCLUDE) {
            return invert().test(server);
        }

        return test(server);
    }
}
