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
import static com.redhat.rhn.taskomatic.task.payg.dimensions.rules.DimensionRuleTestUtils.mockServer;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.taskomatic.task.payg.dimensions.DimensionRule;
import com.redhat.rhn.taskomatic.task.payg.dimensions.RuleType;
import com.redhat.rhn.testing.MockObjectTestCase;

import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

class AddonProductRuleTest extends MockObjectTestCase {

    @BeforeEach
    void setup() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @Test
    void canExcludeWithAnAllRequirement() {
        Set<InstalledProduct> productSet = Set.of(
            mockBaseProduct(context, "sles"),
            mockAddonProduct(context, "sle-module-server-applications"),
            mockAddonProduct(context, "sle-module-containers")
        );

        Server server = mockServer(context, productSet, Set.of());

        // Create a rule that requires ALL the following addons
        DimensionRule rule = new AddonProductRule(RuleType.INCLUDE, RequirementType.ALL, Set.of(
            "sle-module-containers",
            "sle-module-web-scripting",
            "sle-module-development-tools"
        ));

        // Server must not be included by the rule
        assertFalse(rule.includes(server));
        assertTrue(rule.excludes(server));
    }

    @Test
    void canIncludeWithAnAllRequirement() {
        Set<InstalledProduct> productSet = Set.of(
            mockBaseProduct(context, "sles"),
            mockAddonProduct(context, "sle-module-server-applications"),
            mockAddonProduct(context, "sle-module-containers"),
            mockAddonProduct(context, "sle-module-web-scripting"),
            mockAddonProduct(context, "sle-module-development-tools")
        );

        Server server = mockServer(context, productSet, Set.of());

        // Create a rule that requires ALL the following addons
        DimensionRule rule = new AddonProductRule(RuleType.INCLUDE, RequirementType.ALL, Set.of(
            "sle-module-containers",
            "sle-module-web-scripting",
            "sle-module-development-tools"
        ));

        // Server must be included by the rule
        assertTrue(rule.includes(server));
        assertFalse(rule.excludes(server));
    }

    @Test
    void canIncludeWithAnAnyRequirement() {
        Set<InstalledProduct> productSet = Set.of(
            mockBaseProduct(context, "sles"),
            mockAddonProduct(context, "sle-module-server-applications"),
            mockAddonProduct(context, "sle-module-containers")
        );

        Server server = mockServer(context, productSet, Set.of());

        // Create a rule that requires ALL the following addons
        DimensionRule rule = new AddonProductRule(RuleType.INCLUDE, RequirementType.ANY, Set.of(
            "sle-module-containers",
            "sle-module-web-scripting",
            "sle-module-development-tools"
        ));

        // Server must not be included by the rule
        assertTrue(rule.includes(server));
        assertFalse(rule.excludes(server));
    }

    @Test
    void canExcludeWithAnAnyRequirement() {
        Set<InstalledProduct> productSet = Set.of(
            mockBaseProduct(context, "sles"),
            mockAddonProduct(context, "sle-module-server-applications")
        );

        Server server = mockServer(context, productSet, Set.of());

        // Create a rule that requires ALL the following addons
        DimensionRule rule = new AddonProductRule(RuleType.INCLUDE, RequirementType.ANY, Set.of(
            "sle-module-containers",
            "sle-module-web-scripting",
            "sle-module-development-tools"
        ));

        // Server must not be included by the rule
        assertFalse(rule.includes(server));
        assertTrue(rule.excludes(server));
    }
}
