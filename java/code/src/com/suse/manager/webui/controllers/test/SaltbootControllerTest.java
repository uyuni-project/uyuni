/*
 * Copyright (c) 2023 SUSE LLC
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.testing.ImageTestUtils;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.SparkTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.webui.controllers.SaltbootController;
import com.suse.utils.Json;

import com.google.gson.reflect.TypeToken;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import spark.Request;

public class SaltbootControllerTest extends BaseControllerTestCase {
    private static final String TEST_DIR = "/com/suse/manager/webui/controllers/test/";

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testSaltbootController() throws Exception {
        ImageStore store = ImageTestUtils.createImageStore("registry.reg", user);
        ImageInfo imageNoPillar = ImageTestUtils.createImageInfo("myimage1", "1.0.0", store, user);
        ImageInfo image = ImageTestUtils.createImageInfo("myimage2", "1.0.0", store, user);

        String path = new File(TestUtils.findTestData(TEST_DIR + "image_pillar.json")
                     .getPath()).getAbsolutePath();
        Map<String, Object> pillarData = Json.GSON.fromJson(new FileReader(path),
                                         new TypeToken<Map<String, Object>>() { }.getType());


        String category = "Image" + image.getId();
        Pillar pillarEntry = new Pillar(category, pillarData, image.getOrg());
        HibernateFactory.getSession().save(pillarEntry);
        image.setPillar(pillarEntry);


        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("orgid", user.getOrg().getId().toString());

        ((RhnMockHttpServletResponse)response.raw()).setStatus(301);

        Request request = SparkTestUtils.createMockRequestWithParams(
            "http://localhost:8080/saltboot/boot/POS_Image_JeOS7.x86_64-7.1.0-1/POS_Image_JeOS7.x86_64-7.1.0.initrd",
            queryParams);
        SaltbootController.redirectImage(request, response);
        assertEquals("https://server.suse.com/os-images/1/POS_Image_JeOS7-7.1.0-1/POS_Image_JeOS7.x86_64-7.1.0.initrd",
            response.raw().getHeader("Location"));
        request = SparkTestUtils.createMockRequestWithParams("http://localhost:8080/saltboot/boot/" +
            "POS_Image_JeOS7.x86_64-7.1.0-1/POS_Image_JeOS7.x86_64-7.1.0-5.14.21-150400.24.55-default.kernel",
            queryParams);
        SaltbootController.redirectImage(request, response);
        assertEquals("https://server.suse.com/os-images/1/" +
            "POS_Image_JeOS7-7.1.0-1/POS_Image_JeOS7.x86_64-7.1.0-5.14.21-150400.24.55-default.kernel",
            response.raw().getHeader("Location"));
        request = SparkTestUtils.createMockRequestWithParams(
            "http://localhost:8080/saltboot/image/POS_Image_JeOS7.x86_64-7.1.0-1/POS_Image_JeOS7.x86_64-7.1.0",
            queryParams);
        SaltbootController.redirectImage(request, response);
        assertEquals("https://server.suse.com/os-images/1/POS_Image_JeOS7-7.1.0-1/POS_Image_JeOS7.x86_64-7.1.0",
            response.raw().getHeader("Location"));
    }


    @Test
    public void testSaltbootControllerWithInvalidUrl() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("orgid", user.getOrg().getId().toString());

        Request request = SparkTestUtils.createMockRequestWithParams(
                "http://localhost:8080/saltboot/boot/something",
                queryParams);

        try {
            SaltbootController.redirectImage(request, response);
            fail("Controller should fail on non-existing image");
        }
        catch (spark.HaltException e) {
            assertEquals(404, e.statusCode());
        }
    }
}
