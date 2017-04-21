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

package com.redhat.rhn.domain.image.test;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.common.Checksum;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoCustomDataValue;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImageOverview;
import com.redhat.rhn.domain.image.ImagePackage;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ProfileCustomDataValue;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.org.test.CustomDataKeyTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ImageTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.redhat.rhn.testing.UserTestUtils;
import com.suse.manager.webui.utils.salt.custom.ImageInspectSlsResult;
import org.apache.commons.codec.digest.DigestUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.redhat.rhn.testing.ImageTestUtils.createActivationKey;
import static com.redhat.rhn.testing.ImageTestUtils.createImageProfile;
import static com.redhat.rhn.testing.ImageTestUtils.createImageStore;
import static com.redhat.rhn.testing.ImageTestUtils.createProfileCustomDataValue;

public class ImageInfoFactoryTest extends BaseTestCaseWithUser {

    private static final Mockery CONTEXT = new JUnit3Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    @Override
    public void setUp() throws Exception {
        super.setUp();
        CONTEXT.setImposteriser(ClassImposteriser.INSTANCE);

    }

    public final void testConvertChecksum() {
        //SHA1
        String sha1Str = DigestUtils.sha1Hex("mychecksum");
        ImageInspectSlsResult.Checksum chksum =
                new ImageInspectSlsResult.SHA1Checksum(sha1Str);
        assertEquals(chksum.getChecksum(), sha1Str);

        Checksum converted = ImageInfoFactory.convertChecksum(chksum);
        assertNotNull(converted.getId());
        assertNotNull(converted.getChecksumType());
        assertNotNull(converted.getChecksumType().getId());
        assertEquals(converted.getChecksum(), sha1Str);
        assertEquals(converted.getChecksumType().getLabel(), "sha1");

        chksum = ImageInfoFactory.convertChecksum(converted);
        assertTrue(chksum instanceof ImageInspectSlsResult.SHA1Checksum);
        assertEquals(chksum.getChecksum(), sha1Str);

        //SHA256
        String sha256Str = DigestUtils.sha256Hex("mychecksum");
        chksum = new ImageInspectSlsResult.SHA256Checksum(sha256Str);
        assertEquals(chksum.getChecksum(), sha256Str);

        converted = ImageInfoFactory.convertChecksum(chksum);
        assertNotNull(converted.getId());
        assertNotNull(converted.getChecksumType());
        assertNotNull(converted.getChecksumType().getId());
        assertEquals(converted.getChecksum(), sha256Str);
        assertEquals(converted.getChecksumType().getLabel(), "sha256");

        chksum = ImageInfoFactory.convertChecksum(converted);
        assertTrue(chksum instanceof ImageInspectSlsResult.SHA256Checksum);
        assertEquals(chksum.getChecksum(), sha256Str);

        //SHA384
        String sha384Str = DigestUtils.sha384Hex("mychecksum");
        chksum = new ImageInspectSlsResult.SHA384Checksum(sha384Str);
        assertEquals(chksum.getChecksum(), sha384Str);

        converted = ImageInfoFactory.convertChecksum(chksum);
        assertNotNull(converted.getId());
        assertNotNull(converted.getChecksumType());
        assertNotNull(converted.getChecksumType().getId());
        assertEquals(converted.getChecksum(), sha384Str);
        assertEquals(converted.getChecksumType().getLabel(), "sha384");

        chksum = ImageInfoFactory.convertChecksum(converted);
        assertTrue(chksum instanceof ImageInspectSlsResult.SHA384Checksum);
        assertEquals(chksum.getChecksum(), sha384Str);

        //SHA512
        String sha512Str = DigestUtils.sha256Hex("mychecksum");
        chksum = new ImageInspectSlsResult.SHA512Checksum(sha256Str);
        assertEquals(chksum.getChecksum(), sha512Str);

        converted = ImageInfoFactory.convertChecksum(chksum);
        assertNotNull(converted.getId());
        assertNotNull(converted.getChecksumType());
        assertNotNull(converted.getChecksumType().getId());
        assertEquals(converted.getChecksum(), sha512Str);
        assertEquals(converted.getChecksumType().getLabel(), "sha512");

        chksum = ImageInfoFactory.convertChecksum(converted);
        assertTrue(chksum instanceof ImageInspectSlsResult.SHA512Checksum);
        assertEquals(chksum.getChecksum(), sha512Str);
    }

    public final void testList() throws Exception {
        ImageInfo img1 = ImageTestUtils.createImageInfo("myimage1", "1.0.0", user);
        ImageInfo img2 = ImageTestUtils.createImageInfo("myimage1", "2.0.0", user);
        ImageInfo img3 = ImageTestUtils.createImageInfo("myimage2", "1.0.0", user);

        List<ImageInfo> result = ImageInfoFactory.list();

        assertEquals(3, result.size());
        ImageInfo img = result.stream().filter(i -> i.equals(img1)).findFirst().get();
        assertEquals(img, img1);

        img = result.stream().filter(i -> i.equals(img2)).findFirst().get();
        assertEquals(img, img2);

        img = result.stream().filter(i -> i.equals(img3)).findFirst().get();
        assertEquals(img, img3);
    }

    public final void testListImageInfos() {
        User foreignUser = UserTestUtils.createUser("foreign-user",
                UserTestUtils.createOrg("foreign-org"));

        ImageInfo img1 = ImageTestUtils.createImageInfo("myimage1", "1.0.0", user);
        ImageInfo img2 = ImageTestUtils.createImageInfo("myimage2", "1.0.0", user);
        ImageTestUtils.createImageInfo("myimage1", "1.0.0", foreignUser);

        List<ImageInfo> result = ImageInfoFactory.listImageInfos(user.getOrg());

        assertEquals(2, result.size());
        ImageInfo img = result.stream().filter(i -> i.equals(img1)).findFirst().get();
        assertEquals(img1, img);

        img = result.stream().filter(i -> i.equals(img2)).findFirst().get();
        assertEquals(img2, img);
    }

    public final void testListImageOverviews() {
        User foreignUser = UserTestUtils.createUser("foreign-user",
                UserTestUtils.createOrg("foreign-org"));

        ImageInfo img1 = ImageTestUtils.createImageInfo("myimage1", "1.0.0", user);
        ImageInfo img2 = ImageTestUtils.createImageInfo("myimage2", "1.0.0", user);
        ImageTestUtils.createImageInfo("myimage1", "1.0.0", foreignUser);

        List<ImageOverview> result = ImageInfoFactory.listImageOverviews(user.getOrg());

        assertEquals(2, result.size());
        ImageOverview overview = result.stream().filter(i -> img1.getId().equals(i.getId()))
                .findFirst().get();
        assertEquals("myimage1", overview.getName());
        assertEquals("1.0.0", overview.getVersion());
        assertEquals(user.getOrg(), overview.getOrg());

        overview = result.stream().filter(i -> img2.getId().equals(i.getId()))
                .findFirst().get();
        assertEquals("myimage2", overview.getName());
        assertEquals("1.0.0", overview.getVersion());
        assertEquals(user.getOrg(), overview.getOrg());
    }

    public final void testLookupById() {
        ImageInfo image = ImageTestUtils.createImageInfo("myimage", "1.0.0", user);

        ImageInfo result = ImageInfoFactory.lookupById(image.getId()).get();
        assertEquals(image, result);
    }

    public final void testLookupByIdAndOrg() {
        User foreignUser = UserTestUtils.createUser("foreign-user",
                UserTestUtils.createOrg("foreign-org"));

        ImageInfo image = ImageTestUtils.createImageInfo("myimage", "1.0.0", user);

        ImageInfo result =
                ImageInfoFactory.lookupByIdAndOrg(image.getId(), user.getOrg()).get();
        assertEquals(image, result);

        assertFalse(ImageInfoFactory.lookupByIdAndOrg(image.getId(), foreignUser.getOrg())
                .isPresent());
    }

    public final void testLookupOverviewByIdAndOrg() {
        User foreignUser = UserTestUtils.createUser("foreign-user",
                UserTestUtils.createOrg("foreign-org"));

        ImageInfo image = ImageTestUtils.createImageInfo("myimage", "1.0.0", user);

        ImageOverview result = ImageInfoFactory
                .lookupOverviewByIdAndOrg(image.getId(), user.getOrg()).get();
        assertEquals("myimage", result.getName());
        assertEquals("1.0.0", result.getVersion());
        assertEquals(user.getOrg(), result.getOrg());

        assertFalse(ImageInfoFactory
                .lookupOverviewByIdAndOrg(image.getId(), foreignUser.getOrg()).isPresent());
    }

    public final void testLookupByName() {
        ImageStore store = ImageTestUtils.createImageStore("mystore", user);
        ImageStore anotherStore = ImageTestUtils.createImageStore("myotherstore", user);
        ImageInfo image = ImageTestUtils.createImageInfo("myimage", "1.0.0", store, user);

        ImageInfo result =
                ImageInfoFactory.lookupByName("myimage", "1.0.0", store.getId()).get();

        assertEquals(image, result);

        assertFalse(ImageInfoFactory
                .lookupByName("non-existent-name", "1.0.0", store.getId()).isPresent());
        assertFalse(ImageInfoFactory
                .lookupByName("myimage", "2.0.0", store.getId()).isPresent());
        assertFalse(ImageInfoFactory
                .lookupByName("myimage", "1.0.0", anotherStore.getId()).isPresent());
    }

    public final void testScheduleBuild() throws Exception {
        TaskomaticApi taskomaticMock = CONTEXT.mock(TaskomaticApi.class);
        ImageInfoFactory.setTaskomaticApi(taskomaticMock);

        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        } });

        MinionServer buildHost = MinionServerFactoryTest.createTestMinionServer(user);

        ImageStore store = createImageStore("myregistry", user);
        ActivationKey key = createActivationKey(user);
        ImageProfile profile =
                createImageProfile("suma-3.1-base", store, key, user);

        try {
            // Should not be processed because the server is not a build host yet.
            ImageInfoFactory
                    .scheduleBuild(buildHost.getId(), "v1.0", profile, new Date(), user);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Server is not a build host.", e.getMessage());
        }

        assertEquals(0, ImageInfoFactory.listImageInfos(user.getOrg()).size());

        SystemManager.entitleServer(buildHost, EntitlementManager.CONTAINER_BUILD_HOST);

        // Schedule
        ImageInfoFactory.scheduleBuild(buildHost.getId(), "v1.0", profile, new Date(),
                user);
        assertEquals(1, ImageInfoFactory.listImageInfos(user.getOrg()).size());
        ImageInfo info =
                ImageInfoFactory.lookupByName("suma-3.1-base", "v1.0", store.getId()).get();

        // Assertions
        assertEquals("suma-3.1-base", info.getName());
        assertEquals("v1.0", info.getVersion());
        assertEquals(buildHost, info.getBuildServer());
        assertEquals(buildHost.getServerArch(), info.getImageArch());
        assertEquals(profile, info.getProfile());
        assertEquals(store, info.getStore());
        assertEquals(user.getOrg(), info.getOrg());
        assertEquals(2, info.getChannels().size());
        assertTrue(info.getChannels().equals(key.getChannels()));
        assertTrue(info.getCustomDataValues().isEmpty());

        // Add inspection data after build
        Package p = PackageTest.createTestPackage(user.getOrg());
        ImagePackage pkg = new ImagePackage();
        pkg.setInstallTime(new Date());
        pkg.setImageInfo(info);
        pkg.setArch(p.getPackageArch());
        pkg.setEvr(p.getPackageEvr());
        pkg.setName(p.getPackageName());
        info.setPackages(Collections.singleton(pkg));

        InstalledProduct prd = new InstalledProduct();
        prd.setName("SLES");
        prd.setVersion("12.1");
        prd.setArch(p.getPackageArch());
        prd.setBaseproduct(true);
        TestUtils.saveAndReload(prd);
        info.setInstalledProducts(Collections.singleton(prd));
        ImageInfoFactory.save(info);
        TestUtils.saveAndFlush(info);

        // Update values
        CustomDataKey cdk = CustomDataKeyTest.createTestCustomDataKey(user);
        ProfileCustomDataValue val =
                createProfileCustomDataValue("Test value", cdk, profile, user);
        Set<ProfileCustomDataValue> cdvSet = new HashSet<>();
        cdvSet.add(val);
        profile.setCustomDataValues(cdvSet);
        TestUtils.saveAndFlush(profile);

        // Reschedule
        ImageInfoFactory.scheduleBuild(buildHost.getId(), "v1.0", profile, new Date(),
                user);

        // Image info should be reset
        assertEquals(1, ImageInfoFactory.listImageInfos(user.getOrg()).size());
        info = ImageInfoFactory.lookupByName("suma-3.1-base", "v1.0", store.getId()).get();

        assertEquals("suma-3.1-base", info.getName());
        assertEquals("v1.0", info.getVersion());
        assertEquals(buildHost, info.getBuildServer());
        assertEquals(buildHost.getServerArch(), info.getImageArch());
        assertEquals(profile, info.getProfile());
        assertEquals(store, info.getStore());
        assertEquals(user.getOrg(), info.getOrg());
        assertEquals(1, info.getCustomDataValues().size());
        assertEquals(2, info.getChannels().size());
        assertTrue(info.getChannels().equals(key.getChannels()));
        ImageInfoCustomDataValue cdv = info.getCustomDataValues().iterator().next();
        assertEquals(cdk, cdv.getKey());
        assertEquals("Test value", cdv.getValue());
        assertTrue(info.getPackages().isEmpty());
        assertTrue(info.getInstalledProducts().isEmpty());

        // Test without a token
        profile.setToken(null);
        TestUtils.saveAndFlush(profile);

        // Schedule
        ImageInfoFactory.scheduleBuild(buildHost.getId(), "v2.0", profile, new Date(),
                user);

        // We should have two image infos with same labels but different versions
        List<ImageInfo> infoList = ImageInfoFactory.listImageInfos(user.getOrg());

        assertEquals(2, infoList.size());
        infoList.forEach(i -> assertEquals("suma-3.1-base", i.getName()));
        assertFalse(infoList.get(0).getVersion().equals(infoList.get(1).getVersion()));

        info = ImageInfoFactory.lookupByName("suma-3.1-base", "v2.0", store.getId()).get();

        // Assertions
        assertEquals(0, info.getChannels().size());
    }
}
