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

import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.taskomatic.task.payg.dimensions.DimensionRule;
import com.redhat.rhn.taskomatic.task.payg.dimensions.RuleType;
import com.redhat.rhn.taskomatic.task.payg.dimensions.rules.AddonProductRule;
import com.redhat.rhn.taskomatic.task.payg.dimensions.rules.RequirementType;
import com.redhat.rhn.testing.RhnJmockBaseTestCase;

import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class AddonProductRuleTest extends RhnJmockBaseTestCase {

    @BeforeEach
    public void setup() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @Test
    public void canExcludeWithAnAllRequirement() {
        SUSEProductSet productSet = mock(SUSEProductSet.class);
        Server server = mock(Server.class);

        checking(expectations -> {
            // mock a server with the following addons
            expectations.allowing(productSet).getAddonProducts();
            expectations.will(returnValue(List.of(
                new SUSEProduct("sle-module-server-applications"),
                new SUSEProduct("sle-module-containers")
            )));

            expectations.allowing(server).isPayg();
            expectations.will(returnValue(true));

            expectations.allowing(server).getInstalledProductSet();
            expectations.will(returnValue(Optional.of(productSet)));
        });

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
    public void canIncludeWithAnAllRequirement() {
        SUSEProductSet productSet = mock(SUSEProductSet.class);
        Server server = mock(Server.class);

        checking(expectations -> {
            // mock a server with the following addons
            expectations.allowing(productSet).getAddonProducts();
            expectations.will(returnValue(List.of(
                new SUSEProduct("sle-module-server-applications"),
                new SUSEProduct("sle-module-containers"),
                new SUSEProduct("sle-module-web-scripting"),
                new SUSEProduct("sle-module-development-tools")
            )));

            expectations.allowing(server).isPayg();
            expectations.will(returnValue(true));

            expectations.allowing(server).getInstalledProductSet();
            expectations.will(returnValue(Optional.of(productSet)));
        });

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
    public void canIncludeWithAnAnyRequirement() {
        SUSEProductSet productSet = mock(SUSEProductSet.class);
        Server server = mock(Server.class);

        checking(expectations -> {
            // mock a server with the following addons: sle-module-server-applications, sle-module-containers
            expectations.allowing(productSet).getAddonProducts();
            expectations.will(returnValue(List.of(
                new SUSEProduct("sle-module-server-applications"),
                new SUSEProduct("sle-module-containers")
            )));

            expectations.allowing(server).isPayg();
            expectations.will(returnValue(true));

            expectations.allowing(server).getInstalledProductSet();
            expectations.will(returnValue(Optional.of(productSet)));
        });

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
    public void canExcludeWithAnAnyRequirement() {
        SUSEProductSet productSet = mock(SUSEProductSet.class);
        Server server = mock(Server.class);

        checking(expectations -> {
            // mock a server with the following addons: sle-module-server-applications, sle-module-containers
            expectations.allowing(productSet).getAddonProducts();
            expectations.will(returnValue(List.of(
                new SUSEProduct("sle-module-server-applications")
            )));

            expectations.allowing(server).isPayg();
            expectations.will(returnValue(true));

            expectations.allowing(server).getInstalledProductSet();
            expectations.will(returnValue(Optional.of(productSet)));
        });

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
