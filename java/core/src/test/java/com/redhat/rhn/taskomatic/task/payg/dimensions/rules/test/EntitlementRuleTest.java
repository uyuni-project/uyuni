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

package com.redhat.rhn.taskomatic.task.payg.dimensions.rules.test;

import static org.jmock.AbstractExpectations.returnValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.entitlement.ManagementEntitlement;
import com.redhat.rhn.domain.entitlement.MonitoringEntitlement;
import com.redhat.rhn.domain.entitlement.SaltEntitlement;
import com.redhat.rhn.domain.entitlement.VirtualizationEntitlement;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.taskomatic.task.payg.dimensions.DimensionRule;
import com.redhat.rhn.taskomatic.task.payg.dimensions.RuleType;
import com.redhat.rhn.taskomatic.task.payg.dimensions.rules.EntitlementRule;
import com.redhat.rhn.taskomatic.task.payg.dimensions.rules.RequirementType;
import com.redhat.rhn.testing.RhnJmockBaseTestCase;

import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class EntitlementRuleTest extends RhnJmockBaseTestCase {

    @BeforeEach
    public void setup() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @Test
    public void canExcludeWithAnAllRequirement() {
       Server server = mock(Server.class);

        checking(expectations -> {
            // mock a server with the following entitlements
            expectations.allowing(server).getEntitlements();
            expectations.will(returnValue(Set.of(
                new SaltEntitlement(),
                new MonitoringEntitlement())
            ));
        });

        // Create a rule that requires ALL the following addons
        DimensionRule rule = new EntitlementRule(RuleType.INCLUDE, RequirementType.ALL, Set.of(
            new SaltEntitlement(),
            new VirtualizationEntitlement()
        ));

        // Server must not be included by the rule
        assertFalse(rule.includes(server));
        assertTrue(rule.excludes(server));
    }

    @Test
    public void canIncludeWithAnAllRequirement() {
        Server server = mock(Server.class);

        checking(expectations -> {
            // mock a server with the following entitlements
            expectations.allowing(server).getEntitlements();
            expectations.will(returnValue(Set.of(
                new SaltEntitlement(),
                new VirtualizationEntitlement())
            ));
        });

        // Create a rule that requires ALL the following addons
        DimensionRule rule = new EntitlementRule(RuleType.INCLUDE, RequirementType.ALL, Set.of(
            new SaltEntitlement(),
            new VirtualizationEntitlement()
        ));

        // Server must be included by the rule
        assertTrue(rule.includes(server));
        assertFalse(rule.excludes(server));
    }

    @Test
    public void canIncludeWithAnAnyRequirement() {
        Server server = mock(Server.class);

        checking(expectations -> {
            // mock a server with the following entitlements
            expectations.allowing(server).getEntitlements();
            expectations.will(returnValue(Set.of(
                new SaltEntitlement(),
                new MonitoringEntitlement())
            ));
        });

        // Create a rule that requires ANY of the following addons
        DimensionRule rule = new EntitlementRule(RuleType.INCLUDE, RequirementType.ANY, Set.of(
            new SaltEntitlement(),
            new VirtualizationEntitlement()
        ));

        // Server must not be included by the rule
        assertTrue(rule.includes(server));
        assertFalse(rule.excludes(server));
    }

    @Test
    public void canExcludeWithAnAnyRequirement() {
        Server server = mock(Server.class);

        checking(expectations -> {
            // mock a server with the following entitlements
            expectations.allowing(server).getEntitlements();
            expectations.will(returnValue(Set.of(
                new ManagementEntitlement(),
                new MonitoringEntitlement())
            ));
        });


        // Create a rule that requires ANY of the following addons
        DimensionRule rule = new EntitlementRule(RuleType.INCLUDE, RequirementType.ANY, Set.of(
            new SaltEntitlement(),
            new VirtualizationEntitlement()
        ));

        // Server must not be included by the rule
        assertFalse(rule.includes(server));
        assertTrue(rule.excludes(server));
    }
}
