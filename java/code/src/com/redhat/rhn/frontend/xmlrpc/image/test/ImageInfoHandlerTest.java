/**
 * Copyright (c) 2017 SUSE LLC
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
package com.redhat.rhn.frontend.xmlrpc.image.test;

import static com.redhat.rhn.testing.ImageTestUtils.createActivationKey;
import static com.redhat.rhn.testing.ImageTestUtils.createImageInfo;
import static com.redhat.rhn.testing.ImageTestUtils.createImageInfoCustomDataValue;
import static com.redhat.rhn.testing.ImageTestUtils.createImagePackage;
import static com.redhat.rhn.testing.ImageTestUtils.createImageProfile;
import static com.redhat.rhn.testing.ImageTestUtils.createImageStore;
import static com.redhat.rhn.testing.ImageTestUtils.createKiwiImageProfile;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImageOverview;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.org.test.CustomDataKeyTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.dto.ErrataOverview;
import com.redhat.rhn.frontend.dto.ScheduledAction;
import com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.testing.ImageTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.utils.SaltUtils;
import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ImageInfoHandlerTest extends BaseHandlerTestCase {

    private ImageInfoHandler handler = new ImageInfoHandler();

    private static final Mockery CONTEXT = new JUnit3Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    private static TaskomaticApi taskomaticApi;
    private static SystemEntitlementManager systemEntitlementManager = SystemEntitlementManager.INSTANCE;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        CONTEXT.setImposteriser(ClassImposteriser.INSTANCE);
        Config.get().setBoolean(ConfigDefaults.KIWI_OS_IMAGE_BUILDING_ENABLED, "true");
    }

    public final void testImportImage() throws Exception {
        ImageInfoFactory.setTaskomaticApi(getTaskomaticApi());

        MinionServer server = ImageTestUtils.createBuildHost(admin);
        ImageStore store = createImageStore("registry.reg", admin);
        ActivationKey ak = createActivationKey(admin);

        DataResult dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        int preScheduleSize = dr.size();

        long ret = handler.importImage(admin, "my-external-image", "1.0",
                server.getId().intValue(), store.getLabel(), ak.getKey(), getNow());
        assertTrue(ret > 0);

        Optional<ImageInfo> info = ImageInfoFactory
                .lookupByName("my-external-image", "1.0", store.getId());
        assertTrue(info.isPresent());

        dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        assertEquals(1, dr.size() - preScheduleSize);
        assertEquals("Inspect an Image", ((ScheduledAction)dr.get(0)).getTypeName());

        try {
            handler.importImage(admin, "my-external-image", "1.0",
                    server.getId().intValue(), store.getLabel(), ak.getKey(), getNow());
            fail("Overwriting image.");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Image already exists.", e.getMessage());
        }

        ImageInfoFactory.delete(info.get());
        ret = handler.importImage(admin, "my-external-image", "1.0",
                server.getId().intValue(), store.getLabel(), "", getNow());
        assertTrue(ret > 0);
    }

    public final void testScheduleContainerImageBuild() throws Exception {
        ImageInfoFactory.setTaskomaticApi(getTaskomaticApi());

        MinionServer server = MinionServerFactoryTest.createTestMinionServer(admin);
        systemEntitlementManager.addEntitlementToServer(server, EntitlementManager.CONTAINER_BUILD_HOST);
        ImageStore store = createImageStore("registry.reg", admin);
        ActivationKey ak = createActivationKey(admin);
        ImageProfile prof = createImageProfile("myprofile", store, ak, admin);

        DataResult dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        int preScheduleSize = dr.size();

        long ret = handler.scheduleImageBuild(admin, prof.getLabel(), "1.0.0",
                server.getId().intValue(), getNow());
        assertTrue(ret > 0);

        dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        assertEquals(1, dr.size() - preScheduleSize);
        assertEquals("Build an Image Profile", ((ScheduledAction)dr.get(0)).getTypeName());
    }

    public final void testScheduleOSImageBuild() throws Exception {
        ImageInfoFactory.setTaskomaticApi(getTaskomaticApi());
        SaltService saltServiceMock = CONTEXT.mock(SaltService.class);
        MgrUtilRunner.ExecResult mockResult = new MgrUtilRunner.ExecResult();
        CONTEXT.checking(new Expectations() {{
                allowing(saltServiceMock).generateSSHKey(with(equal(SaltSSHService.SSH_KEY_PATH)));
                will(returnValue(Optional.of(mockResult)));
        }});
        SaltService saltService = new SaltService();
        SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(),
                new SystemEntitler(saltService, new VirtManagerSalt(saltService))
        );

        MinionServer server = MinionServerFactoryTest.createTestMinionServer(admin);
        server.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        ServerFactory.save(server);
        systemEntitlementManager.addEntitlementToServer(server, EntitlementManager.OSIMAGE_BUILD_HOST);
        ActivationKey ak = createActivationKey(admin);
        ImageProfile prof = createKiwiImageProfile("myprofile", ak, admin);

        DataResult dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        int preScheduleSize = dr.size();

        long ret = handler.scheduleImageBuild(admin, prof.getLabel(), "1.0.0",
                server.getId().intValue(), getNow());
        assertTrue(ret > 0);

        dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        assertEquals(1, dr.size() - preScheduleSize);
        assertEquals("Build an Image Profile", ((ScheduledAction)dr.get(0)).getTypeName());
    }

    public final void testListImages() throws Exception {
        ImageStore store = createImageStore("registry.reg", admin);
        createImageInfo("myimage", "1.0.0", store, admin);
        createImageInfo("myimage", "2.0.0", store, admin);

        List<ImageInfo> listInfo = handler.listImages(admin);
        assertEquals(2, listInfo.size());
    }

    public final void testGetImageDetails() throws Exception {
        ImageStore store = createImageStore("registry.reg", admin);
        ImageInfo inf1 = createImageInfo("myimage", "1.0.0", store, admin);

        ImageOverview imageOverview = handler.getDetails(admin, inf1.getId().intValue());
        assertEquals(inf1.getVersion(), imageOverview.getVersion());
    }

    public final void testGetRelevantErrata() throws Exception {
        Channel channel1 = ChannelFactoryTest.createTestChannel(admin);
        Set<Channel> errataChannels = new HashSet<>();
        errataChannels.add(channel1);

        ImageStore store = createImageStore("registry.reg", admin);

        Errata e = ErrataFactoryTest.createTestErrata(admin.getOrg().getId());
        e.setAdvisoryType(ErrataFactory.ERRATA_TYPE_BUG);
        e.setChannels(errataChannels);
        TestUtils.flushAndEvict(e);

        ImageInfo inf1 = createImageInfo("myimage", "1.0.0", store, admin);
        TestUtils.flushAndEvict(inf1);

        UserFactory.save(admin);
        TestUtils.flushAndEvict(admin);

        Package p = e.getPackages().iterator().next();
        ErrataCacheManager.insertImageNeededErrataCache(
                inf1.getId(), e.getId(), p.getId());
        List<ErrataOverview> array = handler.getRelevantErrata(admin,
                inf1.getId().intValue());
        assertEquals(1, array.size());
        ErrataOverview errata = array.get(0);
        assertEquals(e.getId().intValue(), errata.getId().intValue());
    }

    public final void testGetPackages() throws Exception {
        ImageStore store = createImageStore("registry.reg", admin);
        ImageInfo inf1 = createImageInfo("myimage", "1.0.0", store, admin);

        createImagePackage(PackageTest.createTestPackage(admin.getOrg()), inf1);
        createImagePackage(PackageTest.createTestPackage(admin.getOrg()), inf1);

        List<Map<String, Object>> result = handler.listPackages(admin,
                inf1.getId().intValue());
        assertEquals(2, result.size());
    }

    public final void testGetCustomValues() throws Exception {
        ImageStore store = createImageStore("registry.reg", admin);
        ImageInfo inf1 = createImageInfo("myimage", "1.0.0", store, admin);

        // Create custom data keys for the organization
        CustomDataKey orgKey1 = CustomDataKeyTest.createTestCustomDataKey(admin);
        CustomDataKey orgKey2 = CustomDataKeyTest.createTestCustomDataKey(admin);
        admin.getOrg().addCustomDataKey(orgKey1);
        admin.getOrg().addCustomDataKey(orgKey2);

        createImageInfoCustomDataValue("newvalue1", orgKey1, inf1, admin);
        createImageInfoCustomDataValue("newvalue2", orgKey2, inf1, admin);

        Map<String, String> result = handler.getCustomValues(admin,
                inf1.getId().intValue());
        assertEquals(2, result.size());
        assertEquals("newvalue2", result.get(orgKey2.getLabel()));
        assertEquals("newvalue1", result.get(orgKey1.getLabel()));
    }

    private TaskomaticApi getTaskomaticApi() throws TaskomaticApiException {
        if (taskomaticApi == null) {
            taskomaticApi = CONTEXT.mock(TaskomaticApi.class);
            CONTEXT.checking(new Expectations() {
                {
                    allowing(taskomaticApi)
                            .scheduleActionExecution(with(any(Action.class)));
                }
            });
        }

        return taskomaticApi;
    }
}
