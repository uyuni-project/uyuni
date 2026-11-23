/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.manager.kickstart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.kickstart.KickstartCommandName;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartDataTest;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.kickstart.KickstartOptionValue;
import com.redhat.rhn.testing.KickstartBaseTest;
import com.redhat.rhn.testing.TestStatics;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

/**
 * KickstartDetailsCommandTest - test for KickstartDetailsCommand
 */
public class KickstartOptionsCommandTest extends KickstartBaseTest {

    @Test
    public void testKickstartOptionsCommand() throws Exception {
        KickstartData k = KickstartDataTest.createKickstartWithOptions(user.getOrg());

        User ksUser = UserTestUtils.createUser(TestStatics.TEST_USER, k.getOrg().getId());

        KickstartOptionsCommand command = new KickstartOptionsCommand(k.getId(), ksUser);

        assertNotNull(command);
        assertFalse(command.getDisplayOptions().isEmpty());
        assertTrue(command.getDisplayOptions().size() >=
            command.getKickstartData().getOptions().size());

    }

    /**
     * On Enterprise Linux 10 the auth/authconfig kickstart command was removed,
     * so it must neither be displayed nor be mandatory for EL10 profiles.
     */
    @Test
    public void testEl10ProfileDoesNotShowOrRequireAuth() throws Exception {
        KickstartData k = createKickstartWithInstallType(KickstartInstallType.RHEL_10);
        User ksUser = UserTestUtils.createUser(TestStatics.TEST_USER, k.getOrg().getId());

        KickstartOptionsCommand command = new KickstartOptionsCommand(k.getId(), ksUser);

        List<KickstartOptionValue> displayOptions = command.getDisplayOptions();
        assertTrue(displayOptions.stream().noneMatch(v -> "auth".equals(v.getName())),
                "auth must not be displayed for EL10 profiles");
        assertTrue(displayOptions.stream().anyMatch(v -> "rootpw".equals(v.getName())),
                "rootpw must still be displayed for EL10 profiles");

        List<KickstartOptionValue> refreshed = command.refreshOptions(new HashMap<>());
        assertTrue(refreshed.stream().noneMatch(v -> "auth".equals(v.getName())),
                "auth must not be redisplayed for EL10 profiles after a submit");

        assertTrue(command.getRequiredOptions().stream()
                        .noneMatch(cn -> "auth".equals(cn.getName())),
                "auth must not be mandatory for EL10 profiles");
        assertTrue(command.getRequiredOptions().stream()
                        .anyMatch(cn -> "rootpw".equals(cn.getName())),
                "rootpw must stay mandatory for EL10 profiles");
    }

    /**
     * Regression test for older Enterprise Linux versions: auth is still a
     * supported (and displayed) command there.
     */
    @Test
    public void testEl9ProfileStillShowsAuth() throws Exception {
        KickstartData k = createKickstartWithInstallType(KickstartInstallType.RHEL_9);
        User ksUser = UserTestUtils.createUser(TestStatics.TEST_USER, k.getOrg().getId());

        KickstartOptionsCommand command = new KickstartOptionsCommand(k.getId(), ksUser);

        assertTrue(command.getDisplayOptions().stream().anyMatch(v -> "auth".equals(v.getName())),
                "auth must still be displayed for EL9 profiles");
        assertTrue(command.refreshOptions(new HashMap<>()).stream()
                        .anyMatch(v -> "auth".equals(v.getName())),
                "auth must still be redisplayed for EL9 profiles after a submit");
        // the install command was removed in EL9
        assertTrue(command.getDisplayOptions().stream().noneMatch(v -> "install".equals(v.getName())),
                "install must not be displayed for EL9 profiles");
    }

    /**
     * The install-type-aware required options lookup must filter out commands
     * that the distro's kickstart syntax does not support anymore (auth on
     * EL10), while keeping the genuinely mandatory ones (rootpw, ...) and
     * keeping the full list for older EL versions.
     */
    @Test
    public void testLookupKickstartRequiredOptionsByInstallType() {
        List<KickstartCommandName> all = KickstartFactory.lookupKickstartRequiredOptions();
        assertFalse(all.isEmpty());

        KickstartInstallType el10 =
                KickstartFactory.lookupKickstartInstallTypeByLabel(KickstartInstallType.RHEL_10);
        List<KickstartCommandName> el10Required =
                KickstartFactory.lookupKickstartRequiredOptions(el10);
        assertTrue(el10Required.stream().noneMatch(cn -> !el10.isCommandSupported(cn.getName())),
                "EL10 required options must not contain unsupported commands");
        assertTrue(el10Required.stream().anyMatch(cn -> "rootpw".equals(cn.getName())),
                "rootpw must stay mandatory on EL10");
        assertTrue(el10Required.size() <= all.size());

        KickstartInstallType el9 =
                KickstartFactory.lookupKickstartInstallTypeByLabel(KickstartInstallType.RHEL_9);
        List<KickstartCommandName> el9Required =
                KickstartFactory.lookupKickstartRequiredOptions(el9);
        // every required command is still supported on EL9
        assertEquals(all.size(), el9Required.size());

        // null install type keeps the unfiltered list
        assertEquals(all.size(), KickstartFactory.lookupKickstartRequiredOptions(null).size());
    }

    private KickstartData createKickstartWithInstallType(String installTypeLabel) throws Exception {
        KickstartData k = KickstartDataTest.createKickstartWithOptions(user.getOrg());
        k.getTree().setInstallType(
                KickstartFactory.lookupKickstartInstallTypeByLabel(installTypeLabel));
        return TestUtils.saveAndFlush(k);
    }
}
