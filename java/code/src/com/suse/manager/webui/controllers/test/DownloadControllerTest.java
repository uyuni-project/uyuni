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

import com.mockobjects.servlet.MockHttpServletResponse;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.AccessToken;
import com.redhat.rhn.domain.channel.AccessTokenFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.Comps;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.webui.controllers.DownloadController;
import com.suse.manager.webui.utils.SparkTestUtils;
import com.suse.manager.webui.utils.TokenBuilder;
import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import spark.Request;
import spark.RequestResponseFactory;
import spark.Response;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannel;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestPackage;

/**
 * Tests for the {@link DownloadController} endpoint.
 */
public class DownloadControllerTest extends BaseTestCaseWithUser {

    private Channel channel;
    private String uriFile;
    private String debUriFile;
    private File packageFile;
    private File debPackageFile;
    private MockHttpServletResponse mockResponse;
    private Response response;
    private Package pkg;
    private Package debPkg;

    private static String originalMountPoint;

    /**
     * One-time setup. Does not seem to run if single test method is run.
     * @return Test
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(DownloadControllerTest.class);
        TestSetup wrapper = new TestSetup(suite) {
            protected void setUp() throws Exception {
                // Config class keeps the config files sorted by a TreeSet with a File
                // comparator, which makes it sometimes override the test rhn.conf with
                // the defaults, nullifying server.secret_key.
                // Until this is fixed, set it manually
                Config.get().setString("server.secret_key",
                        DigestUtils.sha256Hex(TestUtils.randomString()));
                originalMountPoint = Config.get().getString(ConfigDefaults.MOUNT_POINT);
            }
        };

        return wrapper;
    }

    /**
     * {@inheritDoc}
     */
    public void setUp() throws Exception {
        super.setUp();

        this.channel = createTestChannel(user);
        this.mockResponse = new RhnMockHttpServletResponse();
        this.response = RequestResponseFactory.create(mockResponse);

        this.pkg = createTestPackage(user, channel, "noarch");
        this.debPkg = createTestPackage(user, channel, "all-deb");
        final String nvra = String.format("%s-%s-%s.%s",
                pkg.getPackageName().getName(), pkg.getPackageEvr().getVersion(),
                pkg.getPackageEvr().getRelease(), pkg.getPackageArch().getLabel());
        this.uriFile = String.format("%s.rpm", nvra);

        final String debNvra = String.format("%s_%s-%s.%s",
                debPkg.getPackageName().getName(), debPkg.getPackageEvr().getVersion(),
                debPkg.getPackageEvr().getRelease(), debPkg.getPackageArch().getLabel());
        this.debUriFile = String.format("%s.deb", debNvra);

        this.packageFile = File.createTempFile(nvra, ".rpm");
        // Write a fake file for the package
        Files.write(packageFile.getAbsoluteFile().toPath(),
                TestUtils.randomString().getBytes());

        this.debPackageFile = File.createTempFile(debNvra, ".deb");
        Files.write(debPackageFile.getAbsoluteFile().toPath(),
                TestUtils.randomString().getBytes());

        pkg.setPath(FilenameUtils.getName(packageFile.getAbsolutePath()));
        debPkg.setPath(FilenameUtils.getName(debPackageFile.getAbsolutePath()));
        TestUtils.saveAndFlush(pkg);
        TestUtils.saveAndFlush(debPkg);

        // Change mount point to the parent of the temp file
        Config.get().setString(ConfigDefaults.MOUNT_POINT, packageFile.getParent());

        DownloadController.setCheckTokens(true);
    }

    /**
     * {@inheritDoc}
     */
    public void tearDown() throws Exception {
        super.tearDown();
        Config.get().setString(ConfigDefaults.MOUNT_POINT, originalMountPoint);
        Files.deleteIfExists(packageFile.toPath());
        Files.deleteIfExists(debPackageFile.toPath());
    }

    /**
     * Helper method for creating a Spark Request with parameters.
     *
     * @param params - parameters
     * @return - Spark Request
     */
    private Request getMockRequestWithParams(Map<String, String> params) {
        return getMockRequestWithParamsAndHeaders(params, Collections.emptyMap());
    }

    /**
     * Helper method for creating a Spark Request with parameters.
     *
     * @param params - parameters
     * @return - Spark Request
     */
    private Request getMockRequestWithParamsAndHeaders(Map<String, String> params, Map<String, String> headers) {
        return getMockRequestWithParamsAndHeaders(params, headers, uriFile);
    }

    private Request getMockRequestWithParamsAndHeaders(Map<String, String> params, Map<String, String> headers, String file) {
        return SparkTestUtils.createMockRequestWithParams(
                "http://localhost:8080/rhn/manager/download/:channel/getPackage/:file",
                params,
                headers,
                channel.getLabel(), file);
    }

    /**
     * Tests download without a token.
     */
    public void testDownloadWithoutToken() {
        Request request = getMockRequestWithParams(new HashMap<>());

        try {
            DownloadController.downloadPackage(request, response);
            fail("Controller should fail if no token was given");
        } catch (spark.HaltException e) {
            assertEquals(403, e.getStatusCode());
            assertNull(response.raw().getHeader("X-Sendfile"));
        }
    }

    /**
     * Test download with an invalid token parameter.
     */
    public void testDownloadWithInvalidToken() {
        Map<String, String> params = new HashMap<>();
        params.put("invalid-token-should-return-403", "");
        Request request = getMockRequestWithParams(params);

        try {
            DownloadController.downloadPackage(request, response);
            fail("Controller should fail if wrong token was given");
        } catch (spark.HaltException e) {
            assertEquals(403, e.getStatusCode());
            assertNull(response.raw().getHeader("X-Sendfile"));
        }
    }

    /**
     * Test download with an invalid token parameter, but token checking disabled.
     */
    public void testDownloadPackageWithDisabledTokenChecking() {
        Map<String, String> params = new HashMap<>();
        params.put("invalid-token-should-be-ignored", "");
        Request request = getMockRequestWithParams(params);

        DownloadController.setCheckTokens(false);
        try {
            DownloadController.downloadPackage(request, response);
            assertEquals(packageFile.getAbsolutePath(), response.raw().getHeader("X-Sendfile"));
            assertEquals("application/octet-stream", response.raw().getHeader("Content-Type"));
            assertEquals("attachment; filename=" + packageFile.getName(),
                    response.raw().getHeader("Content-Disposition"));
        }
        catch (spark.HaltException e) {
            fail("No HaltException should be thrown with a valid token!");
        }
    }

    /**
     * Test download with an invalid token parameter, but token checking disabled.
     * @throws IOException in case of unexpected exceptions
     */
    public void testDownloadMetadataWithDisabledTokenChecking() throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("invalid-token-should-be-ignored", "");
        Request request =  SparkTestUtils.createMockRequestWithParams(
                "http://localhost:8080/rhn/manager/download/:channel/repodata/:file",
                params,
                Collections.emptyMap(),
                channel.getLabel(), "comps.xml");

        DownloadController.setCheckTokens(false);

        String compsRelativeDirPath = "rhn/comps/" + channel.getName();
        String compsDirPath = Config.get().getString(ConfigDefaults.MOUNT_POINT) + "/"
                + compsRelativeDirPath;
        String compsName = compsRelativeDirPath + "123hash123-comps-Server.x86_64";
        File compsDir = new File(compsDirPath);
        try {
            compsDir.mkdirs();
            File compsFile = File.createTempFile(compsDirPath + "/" + compsName, ".xml", compsDir);
            Files.write(compsFile.getAbsoluteFile().toPath(),
                    TestUtils.randomString().getBytes());

            // create comps object
            Comps comps = new Comps();
            comps.setChannel(channel);
            comps.setRelativeFilename(compsRelativeDirPath + "/" + compsFile.getName());
            channel.setComps(comps);

            try {
                assertNotNull(DownloadController.downloadMetadata(request, response));

                assertEquals(compsFile.getAbsolutePath(), response.raw().getHeader("X-Sendfile"));
                assertEquals("application/octet-stream", response.raw().getHeader("Content-Type"));
                assertEquals("attachment; filename=" + compsFile.getName(),
                        response.raw().getHeader("Content-Disposition"));
            }
            catch (spark.HaltException e) {
                fail("No HaltException should be thrown with a valid token!");
            }
        }
        finally {
            FileUtils.deleteDirectory(compsDir);
        }
    }
    /**
     * Test that the download is rejected when two tokens are present (even though the 1st
     * one is valid).
     *
     * @throws Exception if anything goes wrong
     */
    public void testTwoTokens() throws Exception {
        TokenBuilder tokenBuilder = new TokenBuilder(user.getOrg().getId());
        tokenBuilder.useServerSecret();
        String token = tokenBuilder.getToken();

        Map<String, String> params = new HashMap<>();
        params.put(token, "");
        params.put("2ndtoken", "");
        Request request = getMockRequestWithParams(params);

        try {
            DownloadController.downloadPackage(request, response);
            fail(String.format("%s should halt 400 if 2 tokens given",
                    DownloadController.class.getSimpleName()));
        } catch (spark.HaltException e) {
            assertEquals(400, e.getStatusCode());
            assertNull(response.raw().getHeader("X-Sendfile"));
        }
    }

    /**
     * Tests a download with a wrong channel in the token.
     *
     * @throws Exception if anything goes wrong
     */
    public void testTokenDifferentChannel() throws Exception {
        // The added token is for a different channel
        TokenBuilder tokenBuilder = new TokenBuilder(user.getOrg().getId());
        tokenBuilder.useServerSecret();
        tokenBuilder.onlyChannels(
                new HashSet<>(
                        Arrays.asList(channel.getLabel() + "WRONG")));
        String tokenOtherChannel = tokenBuilder.getToken();


        Map<String, String> params = new HashMap<>();
        params.put(tokenOtherChannel, "");
        Request request = getMockRequestWithParams(params);

        try {
            DownloadController.downloadPackage(request, response);
            fail(String.format("%s should halt 403 if a different channel token is given",
                    DownloadController.class.getSimpleName()));
        } catch (spark.HaltException e) {
            assertEquals(403, e.getStatusCode());
            assertNull(response.raw().getHeader("X-Sendfile"));
        }
    }

    /**
     * Tests a download with a wrong organization in the token.
     *
     * @throws Exception if anything goes wrong
     */
    public void testTokenWrongOrg() throws Exception {
        TokenBuilder tokenBuilder = new TokenBuilder(user.getOrg().getId() + 1);
        tokenBuilder.useServerSecret();
        tokenBuilder.onlyChannels(
                new HashSet<>(
                        Arrays.asList(channel.getLabel())));
        String tokenOtherOrg = tokenBuilder.getToken();

        Map<String, String> params = new HashMap<>();
        params.put(tokenOtherOrg, "");
        Request request = getMockRequestWithParams(params);

        try {
            DownloadController.downloadPackage(request, response);
            fail(String.format("%s should halt 403 if a different org token is given",
                    DownloadController.class.getSimpleName()));
        } catch (spark.HaltException e) {
            assertEquals(403, e.getStatusCode());
            assertNull(response.raw().getHeader("X-Sendfile"));
        }
    }

    /**
     * Tests a download with a token not assigned to a minion.
     *
     * @throws Exception if anything goes wrong
     */
    public void testTokenNotValid() throws Exception {
        MinionServer testMinionServer = MinionServerFactoryTest.createTestMinionServer(user);
        testMinionServer.getChannels().add(channel);
        AccessTokenFactory.refreshTokens(testMinionServer);

        AccessToken token = testMinionServer.getAccessTokens().iterator().next();
        token.setValid(false);
        AccessTokenFactory.save(token);

        Map<String, String> params = new HashMap<>();
        params.put(token.getToken(), "");
        Request request = getMockRequestWithParams(params);

        try {
            DownloadController.downloadPackage(request, response);
            fail(String.format("%s should halt 403 if the token is not assigned to a minion",
                    DownloadController.class.getSimpleName()));
        } catch (spark.HaltException e) {
            assertEquals(403, e.getStatusCode());
            assertNull(response.raw().getHeader("X-Sendfile"));
        }
    }

    /**
     * Test a download with a correct channel in the token and the token
     * in a query param.
     *
     * @throws Exception if anything goes wrong
     */
    public void testCorrectChannelWithTokenInUrl() throws Exception {
        testCorrectChannel((tokenChannel) -> {
            Map<String, String> params = new HashMap<>();
            params.put(tokenChannel, "");
            return getMockRequestWithParams(params);
        });
    }

    /**
     * Test a download with a correct channel in the token and the token
     * in an http header.
     *
     * @throws Exception if anything goes wrong
     */
    public void testCorrectChannelWithTokenInHeader() throws Exception {
        testCorrectChannel((tokenChannel) -> {
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Mgr-Auth", tokenChannel);
            return getMockRequestWithParamsAndHeaders(Collections.emptyMap(), headers);
        });
    }

    public void testDownloadDebPackage() throws Exception {
        testCorrectChannel(() -> debPackageFile, (tokenChannel) -> {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(tokenChannel.getBytes()));
            return getMockRequestWithParamsAndHeaders(Collections.emptyMap(), headers, debUriFile);
        });
    }

    private void testCorrectChannel(Function<String, Request> requestFactory) throws Exception {
        testCorrectChannel(() -> packageFile, requestFactory);
    }

    /**
     * Test a download with a correct channel in the token.
     *
     * @throws Exception if anything goes wrong
     */
    private void testCorrectChannel(Supplier<File> pkgFile, Function<String, Request> requestFactory) throws Exception {
        TokenBuilder tokenBuilder = new TokenBuilder(user.getOrg().getId());
        tokenBuilder.useServerSecret();
        tokenBuilder.onlyChannels(
                new HashSet<>(
                        Arrays.asList(channel.getLabel())));
        String tokenChannel = tokenBuilder.getToken();

        Request request = requestFactory.apply(tokenChannel);
        try {
            assertNotNull(DownloadController.downloadPackage(request, response));

            assertEquals(pkgFile.get().getAbsolutePath(), response.raw().getHeader("X-Sendfile"));
            assertEquals("application/octet-stream", response.raw().getHeader("Content-Type"));
            assertEquals("attachment; filename=" + pkgFile.get().getName(),
                    response.raw().getHeader("Content-Disposition"));
        } catch (spark.HaltException e) {
            fail("No HaltException should be thrown with a valid token!");
        }
    }

    /**
     * Test a download with a correct organization in the token.
     *
     * @throws Exception if anything goes wrong
     */
    public void testCorrectOrg() throws Exception {
        TokenBuilder tokenBuilder = new TokenBuilder(user.getOrg().getId());
        tokenBuilder.useServerSecret();
        String tokenOrg = tokenBuilder.getToken();

        Map<String, String> params = new HashMap<>();
        params.put(tokenOrg, "");
        Request request = getMockRequestWithParams(params);

        try {
            assertNotNull(DownloadController.downloadPackage(request, response));

            assertEquals(packageFile.getAbsolutePath(), response.raw().getHeader("X-Sendfile"));
            assertEquals("application/octet-stream", response.raw().getHeader("Content-Type"));
            assertEquals("attachment; filename=" + packageFile.getName(),
                    response.raw().getHeader("Content-Disposition"));
        } catch (spark.HaltException e) {
            fail("No HaltException should be thrown with a valid token!");
        }
    }

    /**
     * Tests that a expired token does not allow access
     *
     * @throws Exception if anything goes wrong
     */
    public void testExpiredToken() throws Exception {
        TokenBuilder tokenBuilder = new TokenBuilder(user.getOrg().getId());
        tokenBuilder.useServerSecret();
        // already expired
        tokenBuilder.setExpirationTimeMinutesInTheFuture(-1);
        String expiredToken = tokenBuilder.getToken();

        Map<String, String> params = new HashMap<>();
        params.put(expiredToken, "");
        Request request = getMockRequestWithParams(params);

        try {
            DownloadController.downloadPackage(request, response);
            fail(String.format("%s should halt 403 if an expired token is given",
                    DownloadController.class.getSimpleName()));
        } catch (spark.HaltException e) {
            assertEquals(403, e.getStatusCode());
            assertTrue(e.getBody().contains("The JWT is no longer valid"));
            assertNull(response.raw().getHeader("X-Sendfile"));
        }
    }

    /**
     * Test for setting correct headers for comps.xml file.
     *
     * @throws Exception if anything goes wrong
     */
    public void testDownloadComps() throws Exception {
        TokenBuilder tokenBuilder = new TokenBuilder(user.getOrg().getId());
        tokenBuilder.useServerSecret();
        String tokenOrg = tokenBuilder.getToken();

        Map<String, String> params = new HashMap<>();
        params.put(tokenOrg, "");
        Request request =  SparkTestUtils.createMockRequestWithParams(
                "http://localhost:8080/rhn/manager/download/:channel/repodata/:file",
                params,
                Collections.emptyMap(),
                channel.getLabel(), "comps.xml");

        String compsRelativeDirPath = "rhn/comps/" + channel.getName();
        String compsDirPath = Config.get().getString(ConfigDefaults.MOUNT_POINT) + "/"
                + compsRelativeDirPath;
        String compsName = compsRelativeDirPath + "123hash123-comps-Server.x86_64";
        File compsDir = new File(compsDirPath);
        try {
            compsDir.mkdirs();
            File compsFile = File.createTempFile(compsDirPath + "/" + compsName, ".xml", compsDir);
            Files.write(compsFile.getAbsoluteFile().toPath(),
                    TestUtils.randomString().getBytes());

            // create comps object
            Comps comps = new Comps();
            comps.setChannel(channel);
            comps.setRelativeFilename(compsRelativeDirPath + "/" + compsFile.getName());
            channel.setComps(comps);

            try {
                assertNotNull(DownloadController.downloadMetadata(request, response));

                assertEquals(compsFile.getAbsolutePath(), response.raw().getHeader("X-Sendfile"));
                assertEquals("application/octet-stream", response.raw().getHeader("Content-Type"));
                assertEquals("attachment; filename=" + compsFile.getName(),
                        response.raw().getHeader("Content-Disposition"));
            } catch (spark.HaltException e) {
                fail("No HaltException should be thrown with a valid token!");
            }
        } finally {
            FileUtils.deleteDirectory(compsDir);
        }
    }
}
