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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.taskomatic.task.payg.dimensions.DimensionRule;
import com.redhat.rhn.taskomatic.task.payg.dimensions.RuleType;
import com.redhat.rhn.testing.RhnJmockBaseTestCase;

import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class BaseProductRuleTest extends RhnJmockBaseTestCase {

    @BeforeEach
    public void setup() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @Test
    public void canExcludePaygServerWithSlesForSap() {
        SUSEProduct slesSAP = new SUSEProduct("sles_sap");
        Server server = mock(Server.class);

        checking(expectations -> {
            expectations.allowing(server).isPayg();
            expectations.will(returnValue(true));

            expectations.allowing(server).getInstalledProductSet();
            expectations.will(returnValue(Optional.of(new SUSEProductSet(slesSAP, Collections.emptyList()))));
        });

        DimensionRule rule = new BaseProductRule(RuleType.EXCLUDE, Set.of("sles_sap"), true);

        assertTrue(rule.excludes(server));
    }

    @Test
    public void canIncludeIfServerIsNotPayg() {
        SUSEProduct slesSAP = new SUSEProduct("sles_sap");
        Server server = mock(Server.class);

        checking(expectations -> {
            expectations.allowing(server).isPayg();
            expectations.will(returnValue(false));

            expectations.allowing(server).getInstalledProductSet();
            expectations.will(returnValue(Optional.of(new SUSEProductSet(slesSAP, Collections.emptyList()))));
        });

        DimensionRule rule = new BaseProductRule(RuleType.EXCLUDE, Set.of("sles_sap"), true);

        assertTrue(rule.includes(server));
    }

    @Test
    public void canTestCorrectlyWithMultipleValue() {
        SUSEProduct slesSAP = new SUSEProduct("suse-manager-server");
        Server server = mock(Server.class);

        checking(expectations -> {
            expectations.allowing(server).isPayg();
            expectations.will(returnValue(true));

            expectations.allowing(server).getInstalledProductSet();
            expectations.will(returnValue(Optional.of(new SUSEProductSet(slesSAP, Collections.emptyList()))));
        });

        DimensionRule rule = new BaseProductRule(RuleType.INCLUDE, Set.of("sles", "sles_sap", "sles_bcl"), true);

        assertTrue(rule.excludes(server));
    }
}
