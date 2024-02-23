/*
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
import com.redhat.rhn.frontend.xmlrpc.EntityExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.EntityNotExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.testing.ImageTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.salt.netapi.datatypes.target.MinionList;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ExtendWith(JUnit5Mockery.class)
public class ImageInfoHandlerTest extends BaseHandlerTestCase {

    private ImageInfoHandler handler;

    @RegisterExtension
    protected final Mockery context = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    private static TaskomaticApi taskomaticApi;
    private SaltService saltServiceMock;
    private SystemEntitlementManager systemEntitlementManager;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        context.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        Config.get().setBoolean(ConfigDefaults.KIWI_OS_IMAGE_BUILDING_ENABLED, "true");
        saltServiceMock = context.mock(SaltService.class);
        ServerGroupManager serverGroupManager = new ServerGroupManager(saltServiceMock);
        VirtManager virtManager = new VirtManagerSalt(saltServiceMock);
        MonitoringManager monitoringManager = new FormulaMonitoringManager(saltServiceMock);
        systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
                new SystemEntitler(saltServiceMock, virtManager, monitoringManager, serverGroupManager)
        );
        handler = new ImageInfoHandler(saltServiceMock);
        context.checking(new Expectations() {{
            allowing(saltServiceMock).refreshPillar(with(any(MinionList.class)));
            allowing(saltServiceMock).removeFile(
                with(equal(Paths.get(String.format("/srv/www/os-images/%d/testimg.tgz", admin.getOrg().getId())))));
            will(returnValue(Optional.of(true)));
        }});
    }

    @Test
    public final void testimportContainerImage() throws Exception {
        ImageInfoFactory.setTaskomaticApi(getTaskomaticApi());

        MinionServer server = ImageTestUtils.createBuildHost(systemEntitlementManager, admin);
        ImageStore store = createImageStore("registry.reg", admin);
        ActivationKey ak = createActivationKey(admin);

        DataResult dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        int preScheduleSize = dr.size();

        long ret = handler.importContainerImage(admin, "my-external-image", "1.0",
                server.getId().intValue(), store.getLabel(), ak.getKey(), getNow());
        assertTrue(ret > 0);

        Optional<ImageInfo> info = ImageInfoFactory
                .lookupByName("my-external-image", "1.0", store.getId());
        assertTrue(info.isPresent());

        dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        assertEquals(1, dr.size() - preScheduleSize);
        assertEquals("Inspect an Image", ((ScheduledAction)dr.get(0)).getTypeName());

        try {
            handler.importContainerImage(admin, "my-external-image", "1.0",
                    server.getId().intValue(), store.getLabel(), ak.getKey(), getNow());
            fail("Overwriting image.");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Image already exists.", e.getMessage());
        }

        ImageInfoFactory.delete(info.get(), saltServiceMock);
        ret = handler.importContainerImage(admin, "my-external-image", "1.0",
                server.getId().intValue(), store.getLabel(), "", getNow());
        assertTrue(ret > 0);
    }

    @Test
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

    @Test
    public final void testScheduleOSImageBuild() throws Exception {
        ImageInfoFactory.setTaskomaticApi(getTaskomaticApi());
        MgrUtilRunner.ExecResult mockResult = new MgrUtilRunner.ExecResult();
        context.checking(new Expectations() {{
                allowing(saltServiceMock).generateSSHKey(with(equal(SaltSSHService.SSH_KEY_PATH)),
                        with(equal(SaltSSHService.SUMA_SSH_PUB_KEY)));
                will(returnValue(Optional.of(mockResult)));
        }});

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

    @Test
    public final void testListImages() {
        ImageStore store = createImageStore("registry.reg", admin);
        createImageInfo("myimage", "1.0.0", store, admin);
        createImageInfo("myimage", "2.0.0", store, admin);

        List<ImageInfo> listInfo = handler.listImages(admin);
        assertEquals(2, listInfo.size());
    }

    @Test
    public final void testGetImageDetails() {
        ImageStore store = createImageStore("registry.reg", admin);
        ImageInfo inf1 = createImageInfo("myimage", "1.0.0", store, admin);

        ImageOverview imageOverview = handler.getDetails(admin, inf1.getId().intValue());
        assertEquals(inf1.getVersion(), imageOverview.getVersion());
    }

    @Test
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

    @Test
    public final void testGetPackages() throws Exception {
        ImageStore store = createImageStore("registry.reg", admin);
        ImageInfo inf1 = createImageInfo("myimage", "1.0.0", store, admin);

        createImagePackage(PackageTest.createTestPackage(admin.getOrg()), inf1);
        createImagePackage(PackageTest.createTestPackage(admin.getOrg()), inf1);

        List<Map<String, Object>> result = handler.listPackages(admin,
                inf1.getId().intValue());
        assertEquals(2, result.size());
    }

    @Test
    public final void testGetCustomValues() {
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

    @Test
    public final void testImportOSImage() {
        Integer id1 = handler.importOSImage(admin, "testimg", "1.0.0", "x86_64-redhat-linux").intValue();
        handler.setPillar(admin, id1, Map.of("name1", "val1", "name2", "val2", "size", "10000000000"));
        handler.addImageFile(admin, id1, "testimg.tgz", "bundle", false);

        try {
            handler.addImageFile(admin, id1, "testimg.tgz", "bundle", false);
            fail("Add existing file.");
        }
        catch (EntityExistsFaultException e) {
            assertEquals("Entity already exists: testimg.tgz", e.getMessage());
        }

        Integer id2 = handler.importOSImage(admin, "testimg", "1.0.0", "x86_64-redhat-linux").intValue();

        ImageOverview details1 = handler.getDetails(admin, id1);
        assertEquals("testimg.tgz", details1.getImageFiles().iterator().next().getFile());
        assertEquals(1, (int)details1.getCurrRevisionNum());

        Map<String, Object> pillar1 = handler.getPillar(admin, id1);
        assertEquals("val1", pillar1.get("name1"));
        assertEquals("10000000000", pillar1.get("size"));

        // image size is stored as Long, but sent through the xml-rpc API as String
        Optional<ImageInfo> info = ImageInfoFactory.lookupById(id1.longValue());
        assertEquals(10000000000L, info.get().getPillar().getPillar().get("size"));


        ImageOverview details2 = handler.getDetails(admin, id2);
        System.out.println("details" + details2.getCurrRevisionNum());
        assertEquals(2, (int)details2.getCurrRevisionNum());

        Map<String, Object> pillar2 = handler.getPillar(admin, id2);
        assertTrue(pillar2.isEmpty());

        try {
            handler.deleteImageFile(admin, id2, "testimg.tgz");
            fail("Delete file attached to different image.");
        }
        catch (EntityNotExistsFaultException e) {
            assertEquals("testimg.tgz", e.getMessage());
        }

        handler.deleteImageFile(admin, id1, "testimg.tgz");

        try {
            handler.deleteImageFile(admin, id1, "testimg.tgz");
            fail("Delete file second time.");
        }
        catch (EntityNotExistsFaultException e) {
            assertEquals("testimg.tgz", e.getMessage());
        }


    }


    private TaskomaticApi getTaskomaticApi() throws TaskomaticApiException {
        if (taskomaticApi == null) {
            taskomaticApi = context.mock(TaskomaticApi.class);
            context.checking(new Expectations() {
                {
                    allowing(taskomaticApi)
                            .scheduleActionExecution(with(any(Action.class)));
                }
            });
        }

        return taskomaticApi;
    }
}
