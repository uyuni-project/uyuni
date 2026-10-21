/*
 * Copyright (c) 2015--2021 SUSE LLC
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
package com.redhat.rhn.manager.kickstart.cobbler;

import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartDataTest;
import com.redhat.rhn.domain.kickstart.KickstartTestUtils;
import com.redhat.rhn.domain.kickstart.KickstartableTreeTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.cobbler.CobblerObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Base for Cobbler command tests.
 * Contains pre-filled KickstartData instance to be used by tests.
 */
public abstract class CobblerCommandTestBase extends BaseTestCaseWithUser {

    protected KickstartData ksdata;

    private final boolean connectToCobbler;

    /**
     * Default constructor
     */
    protected CobblerCommandTestBase() {
        this(false);
    }

    /**
     * Alternative constructor
     * @param connectToCobblerIn true to actually connect to cobbler
     */
    protected CobblerCommandTestBase(boolean connectToCobblerIn) {
        connectToCobbler = connectToCobblerIn;
    }


    /**
     * {@inheritDoc}
     *
     * @throws Exception if anything goes wrong
     */
    @BeforeEach
    public void setUpCobblerCommandTestBase() throws Exception {
        boolean realCobbler = connectToCobbler || KickstartTestUtils.useRealCobbler();
        KickstartTestUtils.setupTestConfiguration(user, realCobbler);

        // Make the default user admin
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        UserFactory.save(user);

        this.ksdata = KickstartDataTest.createKickstartWithDefaultKey(this.user.getOrg());
        this.ksdata.getTree().setBasePath("/tmp/opt/repo/f9-x86_64/");

        if (realCobbler) {
            TestUtils.commitAndCloseSession();
        }

        KickstartableTreeTest.createKickstartTreeItems(this.ksdata.getTree());
        CobblerDistroCreateCommand dcreate = new CobblerDistroCreateCommand(ksdata.getTree(), user);
        dcreate.store();
    }

    /**
     * Removes everything the test created on cobbler (real or mocked) -
     * mandatory per test: the fixtures reuse one fixed MAC address, which
     * real cobbler rejects on the next system record.
     */
    @AfterEach
    public void tearDownCobblerCommandTestBase() {
        KickstartTestUtils.cleanUpCobbler();
    }

    /**
     * Renders the kernel options of a cobbler object for assertions: the
     * mock keeps the raw string, a real cobbler returns an unordered map
     * whose blank values round-trip as "~" (normalized back to bare flags).
     *
     * @param obj the cobbler object owning the options
     * @param options the options value (string on the mock, map on a real
     *                cobbler)
     * @return the rendered options string
     */
    protected static String koptsString(CobblerObject obj, Optional<?> options) {
        Object value = options.get();
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            return obj.convertOptionsMap(map).replaceAll("=~(?=(\\s|$))", "");
        }
        return String.valueOf(value);
    }

    /**
     * Compares the rendered kernel options with the expectation as sorted
     * tokens: real cobbler returns the options as an unordered map, so the
     * option order is not part of the contract.
     *
     * @param obj the cobbler object owning the options
     * @param options the options value
     * @param expected the expected options string
     */
    protected static void assertKoptsEqual(CobblerObject obj, Optional<?> options, String expected) {
        List<String> actual = Arrays.stream(koptsString(obj, options).trim().split("\\s+"))
                .sorted()
                .collect(Collectors.toList());
        List<String> expectedTokens = Arrays.stream(expected.trim().split("\\s+"))
                .sorted()
                .collect(Collectors.toList());
        Assertions.assertEquals(expectedTokens, actual);
    }

}
