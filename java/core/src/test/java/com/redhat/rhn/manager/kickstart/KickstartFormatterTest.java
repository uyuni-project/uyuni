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
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.manager.kickstart;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.kickstart.KickstartCommand;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartDataTest;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.testing.KickstartBaseTest;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link KickstartFormatter}, in particular that commands removed
 * from the kickstart syntax of newer Enterprise Linux releases (issue #12252:
 * auth/authconfig on EL10) are never rendered into the generated kickstart
 * file, even if they are still stored on the profile.
 */
public class KickstartFormatterTest extends KickstartBaseTest {

    /**
     * An EL10 profile that still has an auth command stored (e.g. created
     * before the fix or via the API) must not get an auth line in the
     * generated kickstart file: anaconda on EL10 rejects the auth command.
     */
    @Test
    public void testEl10FileOmitsAuthCommand() throws Exception {
        String contents = renderKickstartFileWithAuth(KickstartInstallType.RHEL_10);

        assertFalse(hasCommandLine(contents, "auth"),
                "generated EL10 kickstart file must not contain an auth command");
        assertFalse(hasCommandLine(contents, "authconfig"),
                "generated EL10 kickstart file must not contain an authconfig command");
        // rootpw is the supported way to set the root password on EL10
        assertTrue(hasCommandLine(contents, "rootpw"),
                "generated EL10 kickstart file must still contain the rootpw command");
    }

    /**
     * Regression test for older Enterprise Linux versions: on EL9 (and older)
     * the auth command is still supported and must be rendered if stored on
     * the profile.
     */
    @Test
    public void testEl9FileKeepsAuthCommand() throws Exception {
        String contents = renderKickstartFileWithAuth(KickstartInstallType.RHEL_9);

        assertTrue(hasCommandLine(contents, "auth"),
                "generated EL9 kickstart file must still contain the auth command");
        assertTrue(hasCommandLine(contents, "rootpw"),
                "generated EL9 kickstart file must still contain the rootpw command");
    }

    private String renderKickstartFileWithAuth(String installTypeLabel) throws Exception {
        KickstartData k = KickstartDataTest.createKickstartWithOptions(user.getOrg());
        k.getTree().setInstallType(
                KickstartFactory.lookupKickstartInstallTypeByLabel(installTypeLabel));

        KickstartCommand auth = KickstartFactory.createKickstartCommand(k, "auth");
        auth.setArguments("--enableshadow --passalgo=sha256");

        k = TestUtils.saveAndFlush(k);
        KickstartFactory.saveKickstartData(k);
        k = TestUtils.reload(k);
        KickstartFactory.saveKickstartData(k);

        return FileUtils.readStringFromFile(k.buildCobblerFileName());
    }

    private boolean hasCommandLine(String contents, String commandName) {
        for (String line : contents.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.equals(commandName) || trimmed.startsWith(commandName + " ")) {
                return true;
            }
        }
        return false;
    }
}
