/*
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
package com.suse.manager.webui.controllers.virtualization.test;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.virtualization.BaseVirtualizationVolumeAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationPoolDeleteAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationPoolRefreshAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationPoolStartAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationPoolStopAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.frontend.dto.ScheduledAction;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.ServerTestUtils;

import com.suse.manager.reactor.messaging.test.SaltTestUtils;
import com.suse.manager.virtualization.PoolCapabilitiesJson;
import com.suse.manager.virtualization.PoolCapabilitiesJson.PoolType;
import com.suse.manager.virtualization.VirtualizationActionHelper;
import com.suse.manager.virtualization.test.TestVirtManager;
import com.suse.manager.webui.controllers.test.BaseControllerTestCase;
import com.suse.manager.webui.controllers.virtualization.VirtualPoolsController;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualStoragePoolInfoJson;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.test.TestSaltApi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.hamcrest.collection.IsMapContaining;
import org.jmock.Expectations;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;


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
        VirtualizationActionHelper.setTaskomaticApi(taskomaticMock);
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
                        "/com/suse/manager/webui/controllers/virtualization/test/virt.pool.info.json",
                        null,
                        new TypeToken<Map<String, JsonObject>>() { }).get();
            }

            @Override
            public Map<String, Map<String, JsonObject>> getVolumes(String minionId) {
                return SaltTestUtils.getSaltResponse(
                        "/com/suse/manager/webui/controllers/virtualization/test/virt.volume.info.json",
                        null,
                        new TypeToken<Map<String, Map<String, JsonObject>>>() { }).get();
            }

            @Override
            public Optional<PoolCapabilitiesJson> getPoolCapabilities(String minionId) {
                return SaltTestUtils.getSaltResponse(
                        "/com/suse/manager/webui/controllers/virtualization/test/virt.pool.caps.json",
                        null,
                        new TypeToken<PoolCapabilitiesJson>() { });
            }
        };
        SaltApi saltApi = new TestSaltApi();
        MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);
        ServerGroupManager serverGroupManager = new ServerGroupManager(saltApi);
        systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
                new SystemEntitler(saltApi, virtManager, monitoringManager, serverGroupManager)
        );

        host = ServerTestUtils.createVirtHostWithGuests(user, 1, true, systemEntitlementManager);
        Context.getCurrentContext().setTimezone(TimeZone.getTimeZone("Europe/Paris"));
    }

    public void testData() {
        VirtualPoolsController virtualPoolsController = new VirtualPoolsController(virtManager);
        String json = virtualPoolsController.data(getRequestWithCsrf(
                "/manager/api/systems/details/virtualization/pools/:sid/data", host.getId()), response, user, host);

        List<VirtualStoragePoolInfoJson> pools = GSON.fromJson(
                json, new TypeToken<List<VirtualStoragePoolInfoJson>>() { }.getType());
        VirtualStoragePoolInfoJson pool0 = pools.stream()
                .filter(pool -> pool.getName().equals("pool0")).findFirst().get();
        assertNull(pool0.getTargetPath());
        VirtualStoragePoolInfoJson pool1 = pools.stream()
                .filter(pool -> pool.getName().equals("pool1")).findFirst().get();
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

    public void testGetCapabilities() {
        VirtualPoolsController virtualPoolsController = new VirtualPoolsController(virtManager);
        String json = virtualPoolsController.getCapabilities(getRequestWithCsrf(
                "/manager/api/systems/details/virtualization/pools/:sid/capabilities",
                host.getId()), response, user, host);
        PoolCapabilitiesJson caps = GSON.fromJson(json, new TypeToken<PoolCapabilitiesJson>() { }.getType());
        assertTrue(caps.isComputed());
        PoolType pType = caps.getPoolTypes().stream().filter(type -> type.getName().equals("fs")).findFirst().get();
        assertTrue(pType.isSupported());
        assertEquals("auto", pType.getOptions().getPool().getDefaultFormat());
        assertTrue(pType.getOptions().getPool().getSourceFormatType().contains("iso9660"));
        assertEquals("raw", pType.getOptions().getVolume().getDefaultFormat());
        assertTrue(pType.getOptions().getVolume().getTargetFormatType().contains("cloop"));
    }

    public void testRefresh() throws Exception {
        VirtualPoolsController virtualPoolsController = new VirtualPoolsController(virtManager);
        String json = virtualPoolsController.poolRefresh(
                getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/pools/:sid/refresh",
                                              "{poolNames: [\"pool0\", \"pool1\"]}",
                                              host.getId()),
                response, user, host);

        // Ensure the two refresh actions are queued
        DataResult<ScheduledAction> actions = ActionManager.pendingActions(user, null);
        assertEquals(2, actions.size());
        assertTrue(actions.stream().allMatch(action -> action.getTypeName().equals(
                ActionFactory.TYPE_VIRTUALIZATION_POOL_REFRESH.getName())));

        List<String> actionsPools = actions.stream().map(scheduled -> {
            Action action = ActionManager.lookupAction(user, scheduled.getId());
            VirtualizationPoolRefreshAction virtAction = (VirtualizationPoolRefreshAction)action;
            return virtAction.getPoolName();
        }).collect(Collectors.toList());
        assertTrue(containsInAnyOrder("pool0", "pool1").matches(actionsPools));

        // Check the returned message
        Map<String, Long> actionsIds = actions.stream().collect(Collectors.toMap(
                scheduled -> {
                    Action action = ActionManager.lookupAction(user, scheduled.getId());
                    VirtualizationPoolRefreshAction virtAction = (VirtualizationPoolRefreshAction)action;
                    return virtAction.getPoolName();
                },
                scheduled -> scheduled.getId()));
        Map<String, Long> model = GSON.fromJson(json, new TypeToken<Map<String, Long>>() { }.getType());
        assertTrue(IsMapContaining.hasEntry("pool0", actionsIds.get("pool0")).matches(model));
        assertTrue(IsMapContaining.hasEntry("pool1", actionsIds.get("pool1")).matches(model));
    }

    public void testStart() throws Exception {
        VirtualPoolsController virtualPoolsController = new VirtualPoolsController(virtManager);
        String json = virtualPoolsController.poolStart(
                getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/pools/:sid/start",
                                              "{poolNames: [\"pool0\"]}",
                                              host.getId()),
                response, user, host);

        // Ensure the start action is queued
        DataResult<ScheduledAction> actions = ActionManager.pendingActions(user, null);
        assertEquals(1, actions.size());
        assertEquals(ActionFactory.TYPE_VIRTUALIZATION_POOL_START.getName(), actions.get(0).getTypeName());

        Action action = ActionManager.lookupAction(user, actions.get(0).getId());
        VirtualizationPoolStartAction virtAction = (VirtualizationPoolStartAction)action;
        assertEquals("pool0", virtAction.getPoolName());

        // Check the returned message
        Map<String, Long> model = GSON.fromJson(json, new TypeToken<Map<String, Long>>() { }.getType());
        assertTrue(IsMapContaining.hasEntry("pool0", action.getId()).matches(model));
    }

    public void testStop() throws Exception {
        VirtualPoolsController virtualPoolsController = new VirtualPoolsController(virtManager);
        String json = virtualPoolsController.poolStop(
                getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/pools/:sid/stop",
                                              "{poolNames: [\"pool0\"]}",
                                              host.getId()),
                response, user, host);

        // Ensure the start action is queued
        DataResult<ScheduledAction> actions = ActionManager.pendingActions(user, null);
        assertEquals(1, actions.size());
        assertEquals(ActionFactory.TYPE_VIRTUALIZATION_POOL_STOP.getName(), actions.get(0).getTypeName());

        Action action = ActionManager.lookupAction(user, actions.get(0).getId());
        VirtualizationPoolStopAction virtAction = (VirtualizationPoolStopAction)action;
        assertEquals("pool0", virtAction.getPoolName());

        // Check the returned message
        Map<String, Long> model = GSON.fromJson(json, new TypeToken<Map<String, Long>>() { }.getType());
        assertTrue(IsMapContaining.hasEntry("pool0", action.getId()).matches(model));
    }

    public void testDelete() throws Exception {
        VirtualPoolsController virtualPoolsController = new VirtualPoolsController(virtManager);
        String json = virtualPoolsController.poolDelete(
                getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/pools/:sid/delete",
                                              "{poolNames: [\"pool0\"], purge: true}",
                                              host.getId()),
                response, user, host);

        // Ensure the start action is queued
        DataResult<ScheduledAction> actions = ActionManager.pendingActions(user, null);
        assertEquals(1, actions.size());
        assertEquals(ActionFactory.TYPE_VIRTUALIZATION_POOL_DELETE.getName(), actions.get(0).getTypeName());

        Action action = ActionManager.lookupAction(user, actions.get(0).getId());
        VirtualizationPoolDeleteAction virtAction = (VirtualizationPoolDeleteAction)action;
        assertEquals("pool0", virtAction.getPoolName());
        assertTrue(virtAction.isPurge());

        // Check the returned message
        Map<String, Long> model = GSON.fromJson(json, new TypeToken<Map<String, Long>>() { }.getType());
        assertTrue(IsMapContaining.hasEntry("pool0", action.getId()).matches(model));
    }

    public void testVolumeDelete() throws Exception {
        VirtualPoolsController virtualPoolsController = new VirtualPoolsController(virtManager);
        String json = virtualPoolsController.volumeDelete(
                getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/volumes/:sid/delete",
                                              "{volumes: {\"pool0\": [\"vol0\", \"vol1\"], \"pool1\": [\"vol2\"]}}",
                                              host.getId()),
                response, user, host);

        // Ensure the start action is queued
        List<BaseVirtualizationVolumeAction> actions = ActionManager.pendingActions(user, null).stream()
            .filter(action -> ActionFactory.TYPE_VIRTUALIZATION_VOLUME_DELETE.getName().equals(action.getTypeName()))
            .map(action -> (BaseVirtualizationVolumeAction)ActionManager.lookupAction(user, action.getId()))
            .collect(Collectors.toList());

        assertEquals(3, actions.size());
        Optional<BaseVirtualizationVolumeAction> vol0 = actions.stream()
            .filter(action -> "pool0".equals(action.getPoolName()) && "vol0".equals(action.getVolumeName()))
            .findFirst();
        assertTrue(vol0.isPresent());

        Optional<BaseVirtualizationVolumeAction> vol1 = actions.stream()
                .filter(action -> "pool0".equals(action.getPoolName()) && "vol1".equals(action.getVolumeName()))
                .findFirst();
        assertTrue(vol1.isPresent());

        Optional<BaseVirtualizationVolumeAction> vol2 = actions.stream()
                .filter(action -> "pool1".equals(action.getPoolName()) && "vol2".equals(action.getVolumeName()))
                .findFirst();
        assertTrue(vol2.isPresent());

        // Check the returned message
        Map<String, Long> model = GSON.fromJson(json, new TypeToken<Map<String, Long>>() { }.getType());
        assertTrue(IsMapContaining.hasEntry("pool0/vol1", vol1.get().getId()).matches(model));
    }
}
