/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.redhat.rhn.frontend.xmlrpc.channel.software;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.errata.syncwithpackages.SyncErrataWithPackagesService;
import com.redhat.rhn.manager.errata.syncwithpackages.dto.SyncErrataWithPackagesResult;
import com.redhat.rhn.testing.MockObjectTestCase;
import com.redhat.rhn.testing.UserForTest;
import com.redhat.rhn.testing.UserForTestCaseExtension;

import org.jmock.Expectations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Date;
import java.util.List;

/**
 * Unit tests of the syncErrataWithPackages endpoints of {@link ChannelSoftwareHandler}.
 * They are kept apart from {@link ChannelSoftwareHandlerTest} so they can use a smaller, controlled setup.
 */
@ExtendWith(UserForTestCaseExtension.class)
public class ChannelSoftwareHandlerSyncErrataWithPackagesTest extends MockObjectTestCase {

    private static final List<String> NAMES = List.of("SUSE-2026-1234", "SUSE-2026-5678");
    private static final SyncErrataWithPackagesResult RESULT =
            new SyncErrataWithPackagesResult(List.of(), List.of("SUSE-2026-5678"), 0);
    public static final String SOURCE = "source";
    public static final String TARGET = "target";

    private SyncErrataWithPackagesService service;
    private ChannelSoftwareHandler handler;

    @UserForTest(useClassNameForOrg = true)
    protected User user;

    @BeforeEach
    public void setUp() {
        service = context.mock(SyncErrataWithPackagesService.class);
        handler = new ChannelSoftwareHandler(null, null, service);
    }

    /**
     * The source channel variant by advisory names without a strict flag delegates to the service in strict mode,
     * passing the other parameters unchanged.
     */
    @Test
    public void testFromSourceByNamesIsStrictByDefault() {
        context.checking(new Expectations() {{
            oneOf(service).syncFromSource(user, SOURCE, TARGET, NAMES, true, true, false);
            will(returnValue(RESULT));
        }});

        assertSame(RESULT, handler.syncErrataWithPackages(user, SOURCE, TARGET, NAMES, true, false));
    }

    /**
     * The source channel variant by advisory names delegates to the service with the given strict, async and
     * forceRefresh flags.
     */
    @Test
    public void testFromSourceByNames() {
        context.checking(new Expectations() {{
            oneOf(service).syncFromSource(user, SOURCE, TARGET, NAMES, false, true, false);
            will(returnValue(RESULT));
        }});

        assertSame(RESULT, handler.syncErrataWithPackages(user, SOURCE, TARGET, NAMES, true, false, false));
    }

    /**
     * The source channel variant by date range delegates to the service with the given dates and flags.
     */
    @Test
    public void testFromSourceByDates() {
        Date start = new Date(1_000L);
        Date end = new Date(2_000L);
        context.checking(new Expectations() {{
            oneOf(service).syncFromSource(user, SOURCE, TARGET, start, end, true, false);
            will(returnValue(RESULT));
        }});

        assertSame(RESULT, handler.syncErrataWithPackages(user, SOURCE, TARGET, start, end, true, false));
    }

    /**
     * The criteria variant without a strict flag delegates to the service in strict mode, passing the other parameters
     * unchanged.
     */
    @Test
    public void testByCriteriaIsStrictByDefault() {
        context.checking(new Expectations() {{
            oneOf(service).syncByCriteria(user, TARGET, NAMES, true, true, false);
            will(returnValue(RESULT));
        }});

        assertSame(RESULT, handler.syncErrataWithPackages(user, TARGET, NAMES, true, false));
    }

    /**
     * The criteria variant delegates to the service with the given strict, async and forceRefresh flags.
     */
    @Test
    public void testByCriteria() {
        context.checking(new Expectations() {{
            oneOf(service).syncByCriteria(user, TARGET, NAMES, false, true, false);
            will(returnValue(RESULT));
        }});

        assertSame(RESULT, handler.syncErrataWithPackages(user, TARGET, NAMES, true, false, false));
    }

    /**
     * A fault raised by the service reaches the API caller unchanged.
     */
    @Test
    public void testServiceFaultsArePropagated() {
        FaultException fault = new FaultException(-208, "no_such_patch", "Patches not found: SUSE-2026-5678");
        context.checking(new Expectations() {{
            oneOf(service).syncByCriteria(user, TARGET, NAMES, true, false, true);
            will(throwException(fault));
        }});

        assertSame(fault, assertThrows(FaultException.class,
                () -> handler.syncErrataWithPackages(user, TARGET, NAMES, false, true)));
    }
}
