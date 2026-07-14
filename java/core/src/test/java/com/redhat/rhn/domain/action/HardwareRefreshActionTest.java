/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.action;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.testing.MockObjectTestCase;

import com.suse.utils.Json;

import com.google.gson.JsonElement;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

/**
 * Tests for {@link HardwareRefreshAction}.
 */
public class HardwareRefreshActionTest extends MockObjectTestCase {

    private HardwareRefreshAction action;
    private ServerAction serverAction;
    private Server server;
    private MinionServer minionServer;

    @BeforeEach
    void setUp() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        action = new HardwareRefreshAction();
        serverAction = context.mock(ServerAction.class);
        server = context.mock(Server.class);
        minionServer = context.mock(MinionServer.class);
    }

    @Test
    void testNonTransactionalResultIsFinal() {
        JsonElement result = Json.GSON.fromJson("{}", JsonElement.class);

        context.checking(new Expectations() {{
            oneOf(serverAction).getServer();
            will(returnValue(server));
            oneOf(server).asMinionServer();
            will(returnValue(Optional.of(minionServer)));
            oneOf(minionServer).doesOsSupportsTransactionalUpdate();
            will(returnValue(false));
        }});

        assertTrue(action.isFinalResult(serverAction, result));
    }

    @Test
    void testTransactionalPrerequisiteResultIsNotFinal() {
        JsonElement result = Json.GSON.fromJson("""
                {
                  "pkg_|-mgr_install_dmidecode_|-dmidecode_|-installed": {
                    "changes": {},
                    "result": true
                  }
                }
                """, JsonElement.class);

        context.checking(new Expectations() {{
            oneOf(serverAction).getServer();
            will(returnValue(server));
            oneOf(server).asMinionServer();
            will(returnValue(Optional.of(minionServer)));
            oneOf(minionServer).doesOsSupportsTransactionalUpdate();
            will(returnValue(true));
        }});

        assertFalse(action.isFinalResult(serverAction, result));
    }

    @Test
    void testTransactionalHardwareProfileResultIsFinal() {
        JsonElement result = Json.GSON.fromJson("""
                {
                  "module_|-grains_|-grains.items_|-run": {
                    "changes": {
                      "ret": {}
                    },
                    "result": true
                  }
                }
                """, JsonElement.class);

        context.checking(new Expectations() {{
            oneOf(serverAction).getServer();
            will(returnValue(server));
            oneOf(server).asMinionServer();
            will(returnValue(Optional.of(minionServer)));
            oneOf(minionServer).doesOsSupportsTransactionalUpdate();
            will(returnValue(true));
        }});

        assertTrue(action.isFinalResult(serverAction, result));
    }
}
