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
package com.redhat.rhn.domain.action.test;

import com.redhat.rhn.domain.action.VirtualInstanceRefreshAction;
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

import java.util.Map;
import java.util.Optional;


/**
 * VirtualInstanceRefreshActionTest
 */
public class VirtualInstanceRefreshActionTest extends MockObjectTestCase {

    private static final String SUCCESS = "Success";
    private static final JsonElement VMINFO_NULL = Json.GSON.fromJson("{\"vminfo\": null}", JsonElement.class);

    private VirtualInstanceRefreshAction action;
    private ServerAction serverAction;
    private Server server;
    private MinionServer minionServer;

    @BeforeEach
    void setUp() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        action = new VirtualInstanceRefreshAction();
        serverAction = context.mock(ServerAction.class);
        server = context.mock(Server.class);
        minionServer = context.mock(MinionServer.class);
    }

    /**
     * Test handleUpdateServerAction when server is not a MinionServer
     * Expected: Result message should be "Success" and no exception should be thrown
     */
    @Test
    void testHandleUpdateServerActionWhenServerIsNotMinionServer() {
        context.checking(new Expectations() {{
            oneOf(serverAction).isStatusFailed();
            will(returnValue(false));
            oneOf(serverAction).setResultMsg(SUCCESS);
            oneOf(serverAction).getServer();
            will(returnValue(server));
            oneOf(server).asMinionServer();
            will(returnValue(Optional.empty()));
        }});

        action.handleUpdateServerAction(serverAction, VMINFO_NULL, null);
    }


    /**
     * Test handleUpdateServerAction when server action status is successful but returns no VMs
     * Expected: Result message should be "Success"
     */
    @Test
    void testHandleUpdateServerActionWhenActionIsSuccessfulAndEmptyVMs() {
        JsonElement jsonResult = Json.GSON.fromJson(VMINFO_NULL, JsonElement.class);

        context.checking(new Expectations() {{
            oneOf(serverAction).isStatusFailed();
            will(returnValue(false));
            oneOf(serverAction).setResultMsg(SUCCESS);
            oneOf(serverAction).getServer();
            will(returnValue(server));
            oneOf(server).asMinionServer();
            will(returnValue(Optional.of(minionServer)));
        }});

        action.handleUpdateServerAction(serverAction, jsonResult, null);
    }

    /**
     * Test handleUpdateServerAction when server action fails and returns no VMs
     * Expected: Result message should be "Failure" and no exception should be thrown
     */
    @Test
    void testHandleUpdateServerActionWhenActionFailsAndEmptyVMs() {
        JsonElement jsonResult = Json.GSON.fromJson(VMINFO_NULL, JsonElement.class);

        context.checking(new Expectations() {{
            oneOf(serverAction).isStatusFailed();
            will(returnValue(true));
            oneOf(serverAction).setResultMsg("Failure");
            oneOf(serverAction).getServer();
            will(returnValue(server));
            oneOf(server).asMinionServer();
            will(returnValue(Optional.of(minionServer)));
        }});

        action.handleUpdateServerAction(serverAction, jsonResult, null);
    }

    /**
     * Test handleUpdateServerAction with several VMs, in different states
     * Expected: Result message should be "Success"
     */
    @Test
    void testHandleUpdateServerActionWithMultipleVMs()  {
        Map<String, Object> runningVM = Map.of(
                "state", "running",
                "uuid", "uuid-running",
                "maxMem", 4096.0,
                "cpu", 4.0
        );

        Map<String, Object> pausedVM = Map.of(
                "state", "paused",
                "uuid", "uuid-paused",
                "maxMem", 2048.0,
                "cpu", 2.0
        );

        Map<String, Object> shutdownVM = Map.of(
                "state", "shutdown",
                "uuid", "uuid-shutdown",
                "maxMem", 1024.0,
                "cpu", 1.0
        );

        Map<String, Map<String, Object>> vmsMap = Map.of(
                "running-vm", runningVM,
                "paused-vm", pausedVM,
                "stopped-vm", shutdownVM
        );

        String jsonString = Json.GSON.toJson(Map.of("vminfo", vmsMap));
        JsonElement jsonResult = Json.GSON.fromJson(jsonString, JsonElement.class);

        context.checking(new Expectations() {{
            oneOf(serverAction).isStatusFailed();
            will(returnValue(false));
            oneOf(serverAction).setResultMsg(SUCCESS);
            oneOf(serverAction).getServer();
            will(returnValue(server));
            oneOf(server).asMinionServer();
            will(returnValue(Optional.of(minionServer)));
        }});

        action.handleUpdateServerAction(serverAction, jsonResult, null);
    }
}
