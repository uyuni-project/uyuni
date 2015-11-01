/**
 * Copyright (c) 2015 SUSE LLC
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

import com.mockobjects.servlet.MockHttpServletRequest;
import com.mockobjects.servlet.MockHttpServletResponse;
import com.mockobjects.servlet.MockHttpSession;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.webui.controllers.DownloadController;
import com.suse.manager.webui.controllers.TokensAPI;
import junit.framework.Test;
import org.apache.commons.io.FilenameUtils;
import spark.Request;
import spark.Response;
import spark.RequestResponseFactory;
import spark.route.HttpMethod;
import spark.route.RouteMatch;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import java.io.File;
import java.nio.file.Files;

import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannel;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestPackage;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestUser;

public class DownloadControllerTest extends RhnBaseTestCase {

    private String originalMountPoint;

    public void setUp() throws Exception {
        super.setUp();

        // Config class keeps the config files sorted by a TreeSet with a File
        // comparator, which makes it sometimes override the test rhn.conf with
        // the defaults, nullifying server.secret_key.
        // Until this is fixed, set it manually
        Config.get().setString("server.secret_key", TestUtils.randomString());

        originalMountPoint = Config.get().getString(ConfigDefaults.MOUNT_POINT);
    }

    public void tearDown() throws Exception {
        super.tearDown();
        Config.get().setString(ConfigDefaults.MOUNT_POINT, originalMountPoint);
    }

    public void testEndpoint() throws Exception {
        assertTrue(true);

        User user = createTestUser();
        Channel channel = createTestChannel(user);
        Package pkg = createTestPackage(user, channel, "noarch");

        // set a fake file for the package
        File packageFile = File.createTempFile("fake_rpm_package-1.0-4.x86_64", ".rpm");
        Files.write(packageFile.getAbsoluteFile().toPath(), TestUtils.randomString().getBytes());

        Config.get().setString(ConfigDefaults.MOUNT_POINT, packageFile.getParent());

        pkg.setPath(FilenameUtils.getName(packageFile.getAbsolutePath()));
        TestUtils.saveAndFlush(pkg);

        //assertEquals("", pkg.getPath());


        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockReponse = new MockHttpServletResponse();
        mockRequest.setSession(new MockHttpSession());
        mockRequest.setupGetRequestURI("http://localhost:8080");
        mockRequest.setupGetMethod("GET");

        Map<String, String> params = new HashMap<>();
        mockRequest.setupGetParameterMap(params);
        mockRequest.setupPathInfo(
                String.format("/rhn/manager/download/%s/getPackage/fake_rpm_package-1.0-4.x86_64.rpm", channel.getLabel()));

        RouteMatch match = new RouteMatch(HttpMethod.get, new Object(), "", "", "");
        Request request = RequestResponseFactory.create(match, mockRequest);
        Response response =  RequestResponseFactory.create(mockReponse);

        try {
            DownloadController.downloadPackage(request, response);
            fail("Controller should fail if no token given");
        } catch (spark.HaltException e) {
            assertEquals(403, e.getStatusCode());
            //assertContains(e.getMessage(), "foobar");
        }

        // now add the token
        params.put("randomtoken", "");
        try {
            DownloadController.downloadPackage(request, response);
            fail("Controller should fail if wrong token given");
        } catch (spark.HaltException e) {
            assertEquals(403, e.getStatusCode());
            //assertContains(e.getMessage(), "foobar");
        }

        // now create a token for a different org
        String tokenOtherOrg = TokensAPI.createTokenWithServerKey(
                Optional.of(user.getOrg().getId() + 1), Collections.emptySet());

        // now create a token for WRONG channel only
        String tokenOtherChannel = TokensAPI.createTokenWithServerKey(
                Optional.empty(), new HashSet<String>(Arrays.asList(channel.getLabel() + "WRONG")));

        // now create a token for the channel only
        String tokenChannel = TokensAPI.createTokenWithServerKey(
                Optional.empty(), new HashSet<String>(Arrays.asList(channel.getLabel())));

        // now create a the right token, only for the org
        String token = TokensAPI.createTokenWithServerKey(
                Optional.of(user.getOrg().getId()), Collections.emptySet());

        params.put(token, "");
        // add a second param, and the controller should reject it
        params.put("2ndtoken", "");
        try {
            DownloadController.downloadPackage(request, response);
            fail(String.format("%s should halt 400 if 2 tokens given",
                    DownloadController.class.getSimpleName()));
        } catch (spark.HaltException e) {
            assertEquals(400, e.getStatusCode());
        }

        // token for a different org
        params.clear();
        params.put(tokenOtherOrg, "");
        try {
            DownloadController.downloadPackage(request, response);
            fail(String.format("%s should halt 403 if a different org token is given",
                    DownloadController.class.getSimpleName()));
        } catch (spark.HaltException e) {
            assertEquals(403, e.getStatusCode());
        }

        // token for a different channel
        params.clear();
        params.put(tokenOtherChannel, "");
        try {
            DownloadController.downloadPackage(request, response);
            fail(String.format("%s should halt 403 if a different channel token is given",
                    DownloadController.class.getSimpleName()));
        } catch (spark.HaltException e) {
            assertEquals(403, e.getStatusCode());
        }

        // token for right channel
        params.clear();
        params.put(tokenChannel, "");
        try {
            DownloadController.downloadPackage(request, response);
        } catch (spark.HaltException e) {
            assertEquals(200, e.getStatusCode());
        }

        // token for right org
        params.clear();
        params.put(token, "");
        try {
            DownloadController.downloadPackage(request, response);
        } catch (spark.HaltException e) {
            assertEquals(200, e.getStatusCode());
        }

        Files.deleteIfExists(packageFile.toPath());
    }
}
