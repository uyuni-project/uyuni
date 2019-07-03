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

import static org.hamcrest.Matchers.containsString;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.VirtualizationActionCommand;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.ServerTestUtils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.suse.manager.reactor.messaging.test.SaltTestUtils;
import com.suse.manager.virtualization.VirtManager;
import com.suse.manager.webui.controllers.VirtualNetsController;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.gson.VirtualNetworkInfoJson;

import org.jmock.Expectations;

import java.util.List;
import java.util.Map;

public class VirtualNetsControllerTest extends BaseControllerTestCase {

    private TaskomaticApi taskomaticMock;
    private SaltService saltServiceMock;
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

        saltServiceMock = context().mock(SaltService.class);
        context().checking(new Expectations() {{
            allowing(saltServiceMock).callSync(
                    with(SaltTestUtils.functionEquals("state", "apply")),
                    with(containsString("serverfactorytest")));
        }});
        VirtManager.setSaltService(saltServiceMock);
        SystemManager.mockSaltService(saltServiceMock);

        host = ServerTestUtils.createVirtHostWithGuests(user, 1, true);
        host.asMinionServer().get().setMinionId("testminion.local");
    }

    public void testData() throws Exception {
        context().checking(new Expectations() {{
            oneOf(saltServiceMock).callSync(
                    with(SaltTestUtils.functionEquals("virt", "network_info")),
                    with(host.asMinionServer().get().getMinionId()));
            will(returnValue(SaltTestUtils.getSaltResponse(
                    "/com/suse/manager/webui/controllers/test/virt.net.info.json",
                    null,
                    new TypeToken<Map<String, JsonElement>>() { }.getType())));
        }});

        String json = VirtualNetsController.data(getRequestWithCsrf(
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
