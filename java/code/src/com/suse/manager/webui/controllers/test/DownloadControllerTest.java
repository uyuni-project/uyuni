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
import com.suse.manager.webui.utils.TokenUtils;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.client.utils.URIBuilder;
import spark.Request;
import spark.Response;
import spark.RequestResponseFactory;
import spark.routematch.RouteMatch;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import java.io.File;
import java.nio.file.Files;

import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannel;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestPackage;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestUser;

/**
 * Tests for the {@link DownloadController} endpoint.
 */
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
        Config.get().setString(ConfigDefaults.MOUNT_POINT, originalMountPoint);
        super.tearDown();
    }

    /**
     * This is just one method verifying various scenarios, it should rather be split up
     * into separate methods for each of the tested scenarios.
     *
     * @throws Exception in case of an error
     */
    public void testEndpoint() throws Exception {
        User user = createTestUser();
        Channel channel = createTestChannel(user);
        Package pkg = createTestPackage(user, channel, "noarch");

        // Write a fake file for the package
        final String nvra = String.format("%s-%s-%s.%s",
                pkg.getPackageName().getName(), pkg.getPackageEvr().getVersion(),
                pkg.getPackageEvr().getRelease(), pkg.getPackageArch().getLabel());
        File packageFile = File.createTempFile(nvra, ".rpm");
        Files.write(packageFile.getAbsoluteFile().toPath(),
                TestUtils.randomString().getBytes());

        // Change mount point to the parent of the temp file
        Config.get().setString(ConfigDefaults.MOUNT_POINT, packageFile.getParent());
        pkg.setPath(FilenameUtils.getName(packageFile.getAbsolutePath()));
        TestUtils.saveAndFlush(pkg);

        // Setup the URI
        URIBuilder uriBuilder = new URIBuilder("http://localhost:8080");
        final String uriPathFmt = "/rhn/manager/download/%s/getPackage/%s";
        final String uriFile = String.format("%s.rpm", nvra);
        final String uriPath = String.format(uriPathFmt,  channel.getLabel(), uriFile);
        uriBuilder.setPath(uriPath);
        final String uri = uriBuilder.toString();

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockReponse = new MockHttpServletResponse();
        mockRequest.setSession(new MockHttpSession());
        mockRequest.setupGetRequestURI(uri);
        mockRequest.setupGetMethod("GET");

        Map<String, String> params = new HashMap<>();
        mockRequest.setupGetParameterMap(params);
        mockRequest.setupPathInfo(uriPath);

        RouteMatch match = new RouteMatch(new Object(), uriBuilder.setPath(
                String.format(uriPathFmt, ":channel", ":file")).toString(), uri, "");
        Request request = RequestResponseFactory.create(match, mockRequest);
        Response response =  RequestResponseFactory.create(mockReponse);

        // Try to download package without a token
        try {
            DownloadController.downloadPackage(request, response);
            fail("Controller should fail if no token was given");
        } catch (spark.HaltException e) {
            assertEquals(403, e.getStatusCode());
        }

        // Add an invalid token parameter
        params.put("invalid-token-should-return-403", "");
        try {
            DownloadController.downloadPackage(request, response);
            fail("Controller should fail if wrong token was given");
        } catch (spark.HaltException e) {
            assertEquals(403, e.getStatusCode());
        }

        // Create a token for a different organization
        String tokenOtherOrg = TokenUtils.createTokenWithServerKey(
                user.getOrg().getId() + 1,
                Optional.of(new HashSet<String>(Arrays.asList(channel.getLabel()))));

        // Create a token for a WRONG channel only
        String tokenOtherChannel = TokenUtils.createTokenWithServerKey(
                user.getOrg().getId(),
                Optional.of(new HashSet<String>(Arrays.asList(channel.getLabel() + "WRONG"))));

        // Create a token for the channel only
        String tokenChannel = TokenUtils.createTokenWithServerKey(
                user.getOrg().getId(),
                Optional.of(new HashSet<String>(Arrays.asList(channel.getLabel()))));

        // Create a token for the organization only
        String token = TokenUtils.createTokenWithServerKey(user.getOrg().getId(), Optional.empty());

        params.clear();
        params.put(token, "");
        // Add a second param: the controller should reject it
        params.put("2ndtoken", "");
        try {
            DownloadController.downloadPackage(request, response);
            fail(String.format("%s should halt 400 if 2 tokens given",
                    DownloadController.class.getSimpleName()));
        } catch (spark.HaltException e) {
            assertEquals(400, e.getStatusCode());
        }

        // The added token is for a different organization
        params.clear();
        params.put(tokenOtherOrg, "");
        try {
            DownloadController.downloadPackage(request, response);
            fail(String.format("%s should halt 403 if a different org token is given",
                    DownloadController.class.getSimpleName()));
        } catch (spark.HaltException e) {
            assertEquals(403, e.getStatusCode());
        }

        // The added token is for a different channel
        params.clear();
        params.put(tokenOtherChannel, "");
        try {
            DownloadController.downloadPackage(request, response);
            fail(String.format("%s should halt 403 if a different channel token is given",
                    DownloadController.class.getSimpleName()));
        } catch (spark.HaltException e) {
            assertEquals(403, e.getStatusCode());
        }

        // The token is valid for the right channel
        params.clear();
        params.put(tokenChannel, "");
        try {
            assertNull(DownloadController.downloadPackage(request, response));
        } catch (spark.HaltException e) {
            fail("No HaltException should be thrown with a valid token!");
        }

        // The token is valid for the right org
        params.clear();
        params.put(token, "");
        try {
            assertNull(DownloadController.downloadPackage(request, response));
        } catch (spark.HaltException e) {
            fail("No HaltException should be thrown with a valid token!");
        }

        Files.deleteIfExists(packageFile.toPath());
    }
}
