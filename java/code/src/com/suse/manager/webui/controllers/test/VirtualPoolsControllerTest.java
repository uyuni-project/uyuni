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
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
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
import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.controllers.VirtualPoolsController;
import com.suse.manager.virtualization.test.TestVirtManager;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.gson.VirtualStoragePoolInfoJson;

import org.jmock.Expectations;

import java.util.List;
import java.util.Map;


public class VirtualPoolsControllerTest extends BaseControllerTestCase {

    private TaskomaticApi taskomaticMock;
    private Server host;
    private static final Gson GSON = new GsonBuilder().create();
    private VirtManager virtManager;
    private SystemEntitlementManager systemEntitlementManager;

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
            public Map<String, JsonObject> getPools(String minionId) {
                return SaltTestUtils.getSaltResponse(
                        "/com/suse/manager/webui/controllers/test/virt.pool.info.json",
                        null,
                        new TypeToken<Map<String, JsonObject>>() { }).get();
            }

            @Override
            public Map<String, Map<String, JsonObject>> getVolumes(String minionId) {
                return SaltTestUtils.getSaltResponse(
                        "/com/suse/manager/webui/controllers/test/virt.volume.info.json",
                        null,
                        new TypeToken<Map<String, Map<String, JsonObject>>>() { }).get();
            }

        };
        systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(),
                new SystemEntitler(new SaltService(), virtManager, new FormulaMonitoringManager())
        );

        host = ServerTestUtils.createVirtHostWithGuests(user, 1, true, systemEntitlementManager);
    }

    public void testData() {
        VirtualPoolsController virtualPoolsController = new VirtualPoolsController(virtManager);
        String json = virtualPoolsController.data(getRequestWithCsrf(
                "/manager/api/systems/details/virtualization/pools/:sid/data", host.getId()), response, user);

        List<VirtualStoragePoolInfoJson> pools = GSON.fromJson(json, new TypeToken<List<VirtualStoragePoolInfoJson>>() {}.getType());
        VirtualStoragePoolInfoJson pool0 = pools.stream().filter(pool -> pool.getName().equals("pool0")).findFirst().get();
        assertNull(pool0.getTargetPath());
        VirtualStoragePoolInfoJson pool1 = pools.stream().filter(pool -> pool.getName().equals("pool1")).findFirst().get();
        assertFalse(pool1.isAutostart());
        assertTrue(pool1.isPersistent());
        assertEquals("808befba-85b1-40d6-83dc-d248850962e4", pool1.getUuid());
        assertEquals("running", pool1.getState());
        assertEquals("dir", pool1.getType());
        assertEquals("/path/to/pool1", pool1.getTargetPath());
        assertEquals(Long.valueOf(14412120064L), pool1.getAllocation());
        assertEquals(Long.valueOf(21003628544L), pool1.getCapacity());
        assertEquals(Long.valueOf(6591508480L), pool1.getFree());
    }
}
