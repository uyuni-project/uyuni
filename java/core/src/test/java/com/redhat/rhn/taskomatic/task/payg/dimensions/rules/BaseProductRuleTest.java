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

import static com.redhat.rhn.taskomatic.task.payg.dimensions.rules.DimensionRuleTestUtils.mockBaseProduct;
import static com.redhat.rhn.taskomatic.task.payg.dimensions.rules.DimensionRuleTestUtils.mockPaygServer;
import static com.redhat.rhn.taskomatic.task.payg.dimensions.rules.DimensionRuleTestUtils.mockServer;
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

class BaseProductRuleTest extends MockObjectTestCase {

    @BeforeEach
    void setup() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @Test
    void canExcludePaygServerWithSlesForSap() {
        Set<InstalledProduct> productSet = Set.of(
            mockBaseProduct(context, "sles_sap")
        );

        Server server = mockPaygServer(context, productSet);

        DimensionRule rule = new BaseProductRule(RuleType.EXCLUDE, Set.of("sles_sap"), true);

        assertTrue(rule.excludes(server));
    }

    @Test
    void canIncludeIfServerIsNotPayg() {
        Set<InstalledProduct> productSet = Set.of(
            mockBaseProduct(context, "sles_sap")
        );

        Server server = mockServer(context, productSet);

        DimensionRule rule = new BaseProductRule(RuleType.EXCLUDE, Set.of("sles_sap"), true);

        assertTrue(rule.includes(server));
    }

    @Test
    void canTestCorrectlyWithMultipleValue() {
        Set<InstalledProduct> productSet = Set.of(
            mockBaseProduct(context, "suse-manager-server")
        );

        Server server = mockPaygServer(context, productSet);

        DimensionRule rule = new BaseProductRule(RuleType.INCLUDE, Set.of("sles", "sles_sap", "sles_bcl"), true);

        assertTrue(rule.excludes(server));
    }
}
