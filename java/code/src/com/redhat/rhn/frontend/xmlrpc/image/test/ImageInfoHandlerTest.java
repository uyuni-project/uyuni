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

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoCustomDataValue;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImageOverview;
import com.redhat.rhn.domain.image.ImagePackage;
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
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.dto.ErrataOverview;
import com.redhat.rhn.frontend.dto.ScheduledAction;
import com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler;
import com.redhat.rhn.frontend.xmlrpc.image.profile.test.ImageProfileHandlerTest;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.TestUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ImageInfoHandlerTest extends BaseHandlerTestCase {

    private ImageInfoHandler handler = new ImageInfoHandler();

    private static final Mockery CONTEXT = new JUnit3Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    @Override
    public void setUp() throws Exception {
        super.setUp();
        CONTEXT.setImposteriser(ClassImposteriser.INSTANCE);

    }

    public final void testScheduleImageBuild() throws Exception {
        TaskomaticApi taskomaticMock = CONTEXT.mock(TaskomaticApi.class);
        ImageInfoFactory.setTaskomaticApi(taskomaticMock);

        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        } });

        MinionServer server = MinionServerFactoryTest.createTestMinionServer(admin);
        SystemManager.entitleServer(server, EntitlementManager.CONTAINER_BUILD_HOST);
        ImageStore store = ImageProfileHandlerTest.createImageStore("registry.reg", admin);
        ActivationKey ak = ImageProfileHandlerTest.createActivationKey(admin);
        ImageProfile prof = ImageProfileHandlerTest.createImageProfile(admin, store, ak);

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
        ImageStore store = ImageProfileHandlerTest.createImageStore("registry.reg", admin);
        createImageInfo(admin, "1.0.0", store);
        createImageInfo(admin, "2.0.0", store);

        List<ImageInfo> listInfo = handler.listImages(admin);
        assertEquals(2, listInfo.size());
    }

    public final void testGetImageDetails() throws Exception {
        ImageStore store = ImageProfileHandlerTest.createImageStore("registry.reg", admin);
        ImageInfo inf1 = createImageInfo(admin, "1.0.0", store);

        ImageOverview imageOverview = handler.getDetails(admin, inf1.getId().intValue());
        assertEquals(inf1.getVersion(), imageOverview.getVersion());
    }

    public final void testGetRelevantErrata() throws Exception {
        Channel channel1 = ChannelFactoryTest.createTestChannel(admin);
        Set<Channel> errataChannels = new HashSet<>();
        errataChannels.add(channel1);

        ImageStore store = ImageProfileHandlerTest.createImageStore("registry.reg", admin);

        Errata e = ErrataFactoryTest.createTestErrata(admin.getOrg().getId());
        e.setAdvisoryType(ErrataFactory.ERRATA_TYPE_BUG);
        e.setChannels(errataChannels);
        TestUtils.flushAndEvict(e);

        ImageInfo inf1 = createImageInfo(admin, "1.0.0", store);
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
        ImageStore store = ImageProfileHandlerTest.createImageStore("registry.reg", admin);
        ImageInfo inf1 = createImageInfo(admin, "1.0.0", store);
        TestUtils.flushAndEvict(inf1);

        Package p1 = PackageTest.createTestPackage(admin.getOrg());
        TestUtils.flushAndEvict(p1);
        ImagePackage ip1 = new ImagePackage();
        ip1.setName(p1.getPackageName());
        ip1.setEvr(p1.getPackageEvr());
        ip1.setArch(p1.getPackageArch());
        ip1.setImageInfo(inf1);
        ImageInfoFactory.save(ip1);

        Package p2 = PackageTest.createTestPackage(admin.getOrg());
        TestUtils.flushAndEvict(p2);
        ImagePackage ip2 = new ImagePackage();
        ip2.setName(p2.getPackageName());
        ip2.setEvr(p2.getPackageEvr());
        ip2.setArch(p2.getPackageArch());
        ip2.setImageInfo(inf1);
        ImageInfoFactory.save(ip2);

        UserFactory.save(admin);
        TestUtils.flushAndEvict(admin);

        List<Map<String, Object>> result = handler.listPackages(admin,
                inf1.getId().intValue());
        assertEquals(2, result.size());
    }

    public final void testGetCustomValues() throws Exception {
        ImageStore store = ImageProfileHandlerTest.createImageStore("registry.reg", admin);
        ImageInfo inf1 = createImageInfo(admin, "1.0.0", store);

        // Create custom data keys for the organization
        CustomDataKey orgKey1 = CustomDataKeyTest.createTestCustomDataKey(admin);
        CustomDataKey orgKey2 = CustomDataKeyTest.createTestCustomDataKey(admin);
        admin.getOrg().addCustomDataKey(orgKey1);
        admin.getOrg().addCustomDataKey(orgKey2);

        Set<ImageInfoCustomDataValue> cdSet = new HashSet<>();
        ImageInfoCustomDataValue cd1 = new ImageInfoCustomDataValue();
        cd1.setImageInfo(inf1);
        cd1.setKey(orgKey1);
        cd1.setValue("newvalue1");
        TestUtils.saveAndFlush(cd1);
        cdSet.add(cd1);

        ImageInfoCustomDataValue cd2 = new ImageInfoCustomDataValue();
        cd2.setImageInfo(inf1);
        cd2.setKey(orgKey2);
        cd2.setValue("newvalue2");
        TestUtils.saveAndFlush(cd2);
        cdSet.add(cd2);

        inf1.setCustomDataValues(cdSet);
        TestUtils.saveAndFlush(inf1);

        Map<String, String> result = handler.getCustomValues(admin,
                inf1.getId().intValue());
        assertEquals(2, result.size());
        assertEquals("newvalue2", result.get(orgKey2.getLabel()));
        assertEquals("newvalue1", result.get(orgKey1.getLabel()));
    }

    public static ImageInfo createImageInfo(User user, String version, ImageStore store) {
        ImageInfo inf = new ImageInfo();
        inf.setName("image-" + RandomStringUtils.randomAscii(10));
        inf.setVersion(version);
        inf.setChecksum(null);
        inf.setImageArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        inf.setOrg(user.getOrg());
        inf.setStore(store);
        return TestUtils.saveAndReload(inf);
    }
}
