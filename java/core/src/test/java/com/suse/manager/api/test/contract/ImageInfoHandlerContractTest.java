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
package com.suse.manager.api.test.contract;

import com.redhat.rhn.domain.action.ActionStatus;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.common.Checksum;
import com.redhat.rhn.domain.errata.AdvisoryStatus;
import com.redhat.rhn.domain.image.ImageFile;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageOverview;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.server.ServerArch;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ErrataOverview;
import com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ImageInfoHandlerContractTest extends BaseOpenApiTest {

    private static final Integer IMAGE_ID = 101;
    private static final Date EARLIEST = new Date(0);

    @Override
    protected String getApiNamespace() {
        return "image";
    }

    @Override
    protected Class<ImageInfoHandler> getHandlerClass() {
        return ImageInfoHandler.class;
    }

    private ImageInfoHandler handler() {
        return (ImageInfoHandler) handlerMock;
    }

    /**
     * Builds an image serialized by the registered ImageInfoSerializer.
     *
     * @return the image
     */
    private ImageInfo imageInfo() {
        ImageInfo info = context.mock(ImageInfo.class, "imageInfo");
        var arch = context.mock(ServerArch.class, "imageArch");
        var store = context.mock(ImageStore.class, "imageInfoStore");
        var checksum = context.mock(Checksum.class, "imageInfoChecksum");

        context.checking(new Expectations() {{
            allowing(arch).getLabel();
            will(returnValue("x86_64"));
            allowing(store).getLabel();
            will(returnValue("test-store"));
            allowing(checksum).getChecksum();
            will(returnValue("0123456789abcdef"));

            allowing(info).getId();
            will(returnValue(IMAGE_ID.longValue()));
            allowing(info).getName();
            will(returnValue("test-image"));
            allowing(info).getImageType();
            will(returnValue("dockerfile"));
            allowing(info).getVersion();
            will(returnValue("1.0.0"));
            allowing(info).getRevisionNumber();
            will(returnValue(1));
            allowing(info).getImageArch();
            will(returnValue(arch));
            allowing(info).isExternalImage();
            will(returnValue(false));
            allowing(info).getStore();
            will(returnValue(store));
            allowing(info).getChecksum();
            will(returnValue(checksum));
            allowing(info).isObsolete();
            will(returnValue(false));
        }});

        return info;
    }

    /**
     * Builds an image overview serialized by the registered ImageOverviewSerializer. The inspect
     * action is left empty, which is the state the conditional inspectStatus documents as absent,
     * and the build server is unset, so buildServerId takes the serializer's zero.
     *
     * @return the image overview
     */
    private ImageOverview imageOverview() {
        ImageOverview overview = context.mock(ImageOverview.class, "imageOverview");
        var checksum = context.mock(Checksum.class, "overviewChecksum");
        var profile = context.mock(ImageProfile.class, "overviewProfile");
        var store = context.mock(ImageStore.class, "overviewStore");
        var buildAction = context.mock(ServerAction.class, "overviewBuildAction");
        var buildStatus = context.mock(ActionStatus.class, "overviewBuildStatus");

        context.checking(new Expectations() {{
            allowing(checksum).getChecksum();
            will(returnValue("0123456789abcdef"));
            allowing(profile).getLabel();
            will(returnValue("test-profile"));
            allowing(store).getLabel();
            will(returnValue("test-store"));
            allowing(buildStatus).getName();
            will(returnValue("Completed"));
            allowing(buildAction).getStatus();
            will(returnValue(buildStatus));

            allowing(overview).getId();
            will(returnValue(IMAGE_ID.longValue()));
            allowing(overview).getName();
            will(returnValue("test-image"));
            allowing(overview).getImageType();
            will(returnValue("dockerfile"));
            allowing(overview).getVersion();
            will(returnValue("1.0.0"));
            allowing(overview).getCurrRevisionNum();
            will(returnValue(1));
            allowing(overview).getArch();
            will(returnValue("x86_64"));
            allowing(overview).isExternalImage();
            will(returnValue(false));
            allowing(overview).getChecksum();
            will(returnValue(checksum));
            allowing(overview).getProfile();
            will(returnValue(profile));
            allowing(overview).getStore();
            will(returnValue(store));
            allowing(overview).getBuildServer();
            will(returnValue(null));
            allowing(overview).getSecurityErrata();
            will(returnValue(1));
            allowing(overview).getBugErrata();
            will(returnValue(2));
            allowing(overview).getEnhancementErrata();
            will(returnValue(3));
            allowing(overview).getOutdatedPackages();
            will(returnValue(4));
            allowing(overview).getInstalledPackages();
            will(returnValue(5));
            allowing(overview).getBuildServerAction();
            will(returnValue(Optional.of(buildAction)));
            allowing(overview).getInspectServerAction();
            will(returnValue(Optional.empty()));
            allowing(overview).getImageFiles();
            will(returnValue(Set.of(imageFile())));
            allowing(overview).isObsolete();
            will(returnValue(false));
        }});

        return overview;
    }

    /**
     * Builds an image file serialized by the registered ImageFileSerializer. The file is external,
     * so its url is the file name itself rather than a path below the OS image store.
     *
     * @return the image file
     */
    private ImageFile imageFile() {
        ImageFile file = context.mock(ImageFile.class, "imageFile");

        context.checking(new Expectations() {{
            allowing(file).getFile();
            will(returnValue("test-image-1.0.0.tar"));
            allowing(file).getType();
            will(returnValue("tar"));
            allowing(file).isExternal();
            will(returnValue(true));
        }});

        return file;
    }

    /**
     * Builds an erratum serialized by the registered ErrataOverviewSerializer.
     *
     * @return the erratum overview
     */
    private ErrataOverview errataOverview() {
        ErrataOverview errata = context.mock(ErrataOverview.class, "errataOverview");

        context.checking(new Expectations() {{
            allowing(errata).getId();
            will(returnValue(201L));
            allowing(errata).getIssueDateIsoFormat();
            will(returnValue("2026-01-01 00:00:00"));
            allowing(errata).getUpdateDateIsoFormat();
            will(returnValue("2026-01-02 00:00:00"));
            allowing(errata).getAdvisorySynopsis();
            will(returnValue("A test erratum"));
            allowing(errata).getAdvisoryType();
            will(returnValue("Security Advisory"));
            allowing(errata).getAdvisoryStatus();
            will(returnValue(AdvisoryStatus.FINAL));
            allowing(errata).getAdvisoryName();
            will(returnValue("SUSE-2026-0001"));
            allowing(errata).isRebootSuggested();
            will(returnValue(false));
            allowing(errata).isRestartSuggested();
            will(returnValue(false));
        }});

        return errata;
    }

    @Test
    public void testListImages() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listImages(with(mockUser));
            will(returnValue(List.of(imageInfo())));
        }});

        validateApiContract("/image/listImages", "GET")
                .onHandlerMethod("listImages", User.class);
    }

    @Test
    public void testGetDetails() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getDetails(with(mockUser), with(IMAGE_ID));
            will(returnValue(imageOverview()));
        }});

        validateApiContract("/image/getDetails", "GET")
                .withParams(Map.of("imageId", new String[]{IMAGE_ID.toString()}))
                .onHandlerMethod("getDetails", User.class, Integer.class);
    }

    @Test
    public void testGetPillar() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getPillar(with(mockUser), with(IMAGE_ID));
            will(returnValue(Map.of("size", "1024", "boot_image", "test-image")));
        }});

        validateApiContract("/image/getPillar", "GET")
                .withParams(Map.of("imageId", new String[]{IMAGE_ID.toString()}))
                .onHandlerMethod("getPillar", User.class, Integer.class);
    }

    @Test
    public void testSetPillar() throws Exception {
        var pillarData = Map.<String, Object>of("size", "1024");

        context.checking(new Expectations() {{
            oneOf(handler()).setPillar(with(mockUser), with(IMAGE_ID), with(pillarData));
            will(returnValue(1));
        }});

        validateApiContract("/image/setPillar", "POST")
                .withBody(Map.of("imageId", IMAGE_ID, "pillarData", pillarData))
                .onHandlerMethod("setPillar", User.class, Integer.class, Map.class);
    }

    @Test
    public void testImportImage() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).importImage(with(mockUser), with("test-image"), with("1.0.0"),
                    with(1001), with("test-store"), with("1-activation-key"), with(EARLIEST));
            will(returnValue(301L));
        }});

        validateApiContract("/image/importImage", "POST")
                .withBody(Map.of(
                        "name", "test-image",
                        "version", "1.0.0",
                        "buildHostId", 1001,
                        "storeLabel", "test-store",
                        "activationKey", "1-activation-key",
                        "earliestOccurrence", "1970-01-01T00:00:00Z"))
                .onHandlerMethod("importImage", User.class, String.class, String.class, Integer.class,
                        String.class, String.class, Date.class);
    }

    @Test
    public void testImportContainerImage() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).importContainerImage(with(mockUser), with("test-image"), with("1.0.0"),
                    with(1001), with("test-store"), with("1-activation-key"), with(EARLIEST));
            will(returnValue(302L));
        }});

        validateApiContract("/image/importContainerImage", "POST")
                .withBody(Map.of(
                        "name", "test-image",
                        "version", "1.0.0",
                        "buildHostId", 1001,
                        "storeLabel", "test-store",
                        "activationKey", "1-activation-key",
                        "earliestOccurrence", "1970-01-01T00:00:00Z"))
                .onHandlerMethod("importContainerImage", User.class, String.class, String.class, Integer.class,
                        String.class, String.class, Date.class);
    }

    @Test
    public void testImportOSImage() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).importOSImage(with(mockUser), with("test-image"), with("1.0.0"),
                    with("x86_64"));
            will(returnValue(303L));
        }});

        validateApiContract("/image/importOSImage", "POST")
                .withBody(Map.of("name", "test-image", "version", "1.0.0", "arch", "x86_64"))
                .onHandlerMethod("importOSImage", User.class, String.class, String.class, String.class);
    }

    @Test
    public void testScheduleImageBuild() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).scheduleImageBuild(with(mockUser), with("test-profile"), with("1.0.0"),
                    with(1001), with(EARLIEST));
            will(returnValue(304L));
        }});

        validateApiContract("/image/scheduleImageBuild", "POST")
                .withBody(Map.of(
                        "profileLabel", "test-profile",
                        "version", "1.0.0",
                        "buildHostId", 1001,
                        "earliestOccurrence", "1970-01-01T00:00:00Z"))
                .onHandlerMethod("scheduleImageBuild", User.class, String.class, String.class, Integer.class,
                        Date.class);
    }

    @Test
    public void testAddImageFile() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).addImageFile(with(mockUser), with(IMAGE_ID), with("test-image-1.0.0.tar"),
                    with("tar"), with(false));
            will(returnValue(305L));
        }});

        validateApiContract("/image/addImageFile", "POST")
                .withBody(Map.of(
                        "imageId", IMAGE_ID,
                        "file", "test-image-1.0.0.tar",
                        "type", "tar",
                        "external", false))
                .onHandlerMethod("addImageFile", User.class, Integer.class, String.class, String.class,
                        Boolean.class);
    }

    @Test
    public void testDeleteImageFile() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).deleteImageFile(with(mockUser), with(IMAGE_ID), with("test-image-1.0.0.tar"));
            will(returnValue(1));
        }});

        validateApiContract("/image/deleteImageFile", "POST")
                .withBody(Map.of("imageId", IMAGE_ID, "file", "test-image-1.0.0.tar"))
                .onHandlerMethod("deleteImageFile", User.class, Integer.class, String.class);
    }

    @Test
    public void testGetRelevantErrata() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getRelevantErrata(with(mockUser), with(IMAGE_ID));
            will(returnValue(List.of(errataOverview())));
        }});

        validateApiContract("/image/getRelevantErrata", "GET")
                .withParams(Map.of("imageId", new String[]{IMAGE_ID.toString()}))
                .onHandlerMethod("getRelevantErrata", User.class, Integer.class);
    }

    @Test
    public void testListPackages() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listPackages(with(mockUser), with(IMAGE_ID));
            will(returnValue(List.of(Map.<String, Object>of(
                    "name", "test-package",
                    "version", "1.0.0",
                    "release", "1",
                    "epoch", "",
                    "arch", "x86_64"))));
        }});

        validateApiContract("/image/listPackages", "GET")
                .withParams(Map.of("imageId", new String[]{IMAGE_ID.toString()}))
                .onHandlerMethod("listPackages", User.class, Integer.class);
    }

    /**
     * The custom values are a free-form map keyed by the custom info label, so the response is
     * only constrained to be an object.
     */
    @Test
    public void testGetCustomValues() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getCustomValues(with(mockUser), with(IMAGE_ID));
            will(returnValue(Map.of("test-key", "test-value")));
        }});

        validateApiContract("/image/getCustomValues", "GET")
                .withParams(Map.of("imageId", new String[]{IMAGE_ID.toString()}))
                .onHandlerMethod("getCustomValues", User.class, Integer.class);
    }

    @Test
    public void testDelete() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).delete(with(mockUser), with(IMAGE_ID));
            will(returnValue(1));
        }});

        validateApiContract("/image/delete", "POST")
                .withBody(Map.of("imageId", IMAGE_ID))
                .onHandlerMethod("delete", User.class, Integer.class);
    }
}
