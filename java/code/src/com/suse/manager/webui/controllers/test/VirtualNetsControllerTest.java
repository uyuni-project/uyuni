/**
 * Copyright (c) 2018 SUSE LLC
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
package com.suse.manager.webui.controllers.test;

import com.google.gson.JsonObject;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.VirtualizationActionCommand;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.ServerTestUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.suse.manager.reactor.messaging.test.SaltTestUtils;
import com.suse.manager.webui.controllers.VirtualNetsController;
import com.suse.manager.virtualization.test.TestVirtManager;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.gson.VirtualNetworkInfoJson;

import org.jmock.Expectations;

import java.util.*;

public class VirtualNetsControllerTest extends BaseControllerTestCase {

    private TaskomaticApi taskomaticMock;
    private VirtManager virtManager;
    private Server host;
    private static final Gson GSON = new GsonBuilder().create();

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);
        VirtualizationActionCommand.setTaskomaticApi(taskomaticMock);
        context().checking(new Expectations() {{
            ignoring(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        }});

        virtManager = new TestVirtManager() {
            @Override
            public void updateLibvirtEngine(MinionServer minion) {
            }

            @Override
            public Map<String, JsonObject> getNetworks(String minionId) {
                return SaltTestUtils.getSaltResponse(
                        "/com/suse/manager/webui/controllers/test/virt.net.info.json",
                        null,
                        new TypeToken<Map<String, JsonObject>>() { })
                        .orElse(Collections.emptyMap());
            }
        };

        SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(),
                new SystemEntitler(new SaltService(), virtManager)
        );

        host = ServerTestUtils.createVirtHostWithGuests(user, 1, true, systemEntitlementManager);
        host.asMinionServer().get().setMinionId("testminion.local");
    }

    public void testData() throws Exception {
        VirtualNetsController virtualNetsController = new VirtualNetsController(virtManager);
        String json = virtualNetsController.data(getRequestWithCsrf(
                "/manager/api/systems/details/virtualization/nets/:sid/data", host.getId()), response, user);

        List<VirtualNetworkInfoJson> nets = GSON.fromJson(json, new TypeToken<List<VirtualNetworkInfoJson>>() {}.getType());
        assertTrue(nets.stream().filter(net -> net.getName().equals("net0")).findFirst().isPresent());
        VirtualNetworkInfoJson net1 = nets.stream().filter(net -> net.getName().equals("net1")).findFirst().get();
        assertEquals("virbr0", net1.getBridge());
        assertFalse(net1.isActive());
        assertFalse(net1.isAutostart());
        assertTrue(net1.isPersistent());
        assertEquals("860e49a3-d227-4105-95ca-d19dc8f0c8b6", net1.getUuid());
    }
}
