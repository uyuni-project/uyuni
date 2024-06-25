/*
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.AccessToken;
import com.redhat.rhn.domain.channel.AccessTokenFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.Comps;
import com.redhat.rhn.domain.channel.MediaProducts;
import com.redhat.rhn.domain.channel.Modules;
import com.redhat.rhn.domain.product.Tuple3;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.rhnpackage.PackageType;
import com.redhat.rhn.domain.rhnpackage.test.PackageEvrFactoryTest;
import com.redhat.rhn.domain.rhnpackage.test.PackageNameTest;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ErrataTestUtils;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.SparkTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.cloud.CloudPaygManager;
import com.suse.cloud.test.TestCloudPaygManagerBuilder;
import com.suse.manager.webui.controllers.DownloadController;
import com.suse.manager.webui.utils.DownloadTokenBuilder;
import com.suse.manager.webui.utils.TokenBuilder;

import com.mockobjects.servlet.MockHttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpStatus;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import spark.Request;
import spark.RequestResponseFactory;
import spark.Response;

/**
 * Tests for the {@link DownloadController} endpoint.
 */
public class DownloadControllerTest extends BaseTestCaseWithUser {

    private Channel channel;
    private String uriFile;
    private String uriFile2;
    private String debUriFile;
    private String debUriFile2;
    private File packageFile;

    private File packageFile2;
    private File debPackageFile;
    private File debPackageFile2;
    private MockHttpServletResponse mockResponse;
    private Response response;
    private Package pkg;
    private Package pkg2;
    private Package debPkg;
    private Package debPkg2;

    private static String originalMountPoint;

    private DownloadController downloadController;
    @BeforeAll
    public static void beforeAll() {
        Config.get().setString("server.secret_key",
                DigestUtils.sha256Hex(TestUtils.randomString()));
        originalMountPoint = Config.get().getString(ConfigDefaults.MOUNT_POINT);
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        this.channel = ErrataTestUtils.createTestChannel(user);
        this.mockResponse = new RhnMockHttpServletResponse();
        this.response = RequestResponseFactory.create(mockResponse);

        this.pkg = ErrataTestUtils.createTestPackage(user, channel, "noarch");
        final String nvra = String.format("%s-%s-%s.%s",
                pkg.getPackageName().getName(), pkg.getPackageEvr().getVersion(),
                pkg.getPackageEvr().getRelease(), pkg.getPackageArch().getLabel());
        this.uriFile = String.format("%s.rpm", nvra);

        this.pkg2 = ErrataTestUtils.createLaterTestPackage(user, null, channel, pkg,
                null, "1000+git001^20220524", pkg.getPackageEvr().getRelease());
        final String nvra2 = String.format("%s-%s-%s.%s",
                pkg2.getPackageName().getName(), pkg2.getPackageEvr().getVersion(),
                pkg2.getPackageEvr().getRelease(), pkg2.getPackageArch().getLabel());
        this.uriFile2 = new URI(String.format("%s.rpm", nvra2.replace("^", "%5e"))).toString();

        Tuple3<Package, File, String> dpkg = createDebPkg(channel, "1", "1", "0", "all-deb");
        this.debPkg = dpkg.getA();
        this.debPackageFile = dpkg.getB();
        this.debUriFile = dpkg.getC();

        Tuple3<Package, File, String> dpkg2 = createDebPkg(channel, null, "8-20180414",
                "1ubuntu2", "all-deb");
        this.debPkg2 = dpkg2.getA();
        this.debPackageFile2 = dpkg2.getB();
        this.debUriFile2 = dpkg2.getC();

        this.packageFile = File.createTempFile(nvra, ".rpm");
        // Write a fake file for the package
        Files.write(packageFile.getAbsoluteFile().toPath(),
                TestUtils.randomString().getBytes());

        pkg.setPath(FilenameUtils.getName(packageFile.getAbsolutePath()));
        TestUtils.saveAndFlush(pkg);

        this.packageFile2 = File.createTempFile(nvra2, ".rpm");
        // Write a fake file for the package
        Files.write(packageFile2.getAbsoluteFile().toPath(),
                TestUtils.randomString().getBytes());

        pkg2.setPath(FilenameUtils.getName(packageFile2.getAbsolutePath()));
        TestUtils.saveAndFlush(pkg2);

        // Change mount point to the parent of the temp file
        Config.get().setString(ConfigDefaults.MOUNT_POINT, packageFile.getParent());

        downloadController = new DownloadController(new TestCloudPaygManagerBuilder().build());
        downloadController.setCheckTokens(true);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        if (originalMountPoint != null) {
            Config.get().setString(ConfigDefaults.MOUNT_POINT, originalMountPoint);
        }
        Files.deleteIfExists(packageFile.toPath());
        Files.deleteIfExists(debPackageFile.toPath());
        Files.deleteIfExists(debPackageFile2.toPath());
    }

    /**
     * helper method to save a token to the database
     * @param tokenBuilder
     * @return the access token database object
     * @throws JoseException if an error happens during token build
     */
    private AccessToken saveTokenToDataBase(TokenBuilder tokenBuilder) throws JoseException {
        AccessToken newToken = new AccessToken();
        newToken.setStart(Date.from(tokenBuilder.getIssuedAt()));
        newToken.setToken(tokenBuilder.getToken());
        Instant expiration = tokenBuilder.getIssuedAt()
                .plus(tokenBuilder.getExpirationTimeMinutesInTheFuture(),
                        ChronoUnit.MINUTES);
        newToken.setExpiration(Date.from(expiration));
        TestUtils.saveAndFlush(newToken);
        return newToken;
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

    private Request getMockRequestWithParamsAndHeaders(Map<String, String> params,
                                                       Map<String, String> headers, String file) {
        return SparkTestUtils.createMockRequestWithParams(
                "http://localhost:8080/rhn/manager/download/:channel/getPackage/:file",
                params,
                headers,
                channel.getLabel(), file);
    }

    private Tuple3<Package, File, String> createDebPkg(Channel debChannel, String epoch,
                                                       String version, String release, String arch)
            throws Exception {
        Package dpkg = new Package();
        PackageName pname = PackageNameTest.createTestPackageName();
        PackageEvr pevr = PackageEvrFactoryTest.createTestPackageEvr(epoch, version, release, PackageType.DEB);
        PackageArch parch = PackageFactory.lookupPackageArchByLabel(arch);
        PackageTest.populateTestPackage(dpkg, user.getOrg(), pname, pevr, parch);
        TestUtils.saveAndFlush(dpkg);

        List<Long> list = new ArrayList<>(1);
        list.add(dpkg.getId());
        Map<String, Long> params = new HashMap<>();
        params.put("cid", debChannel.getId());
        WriteMode m = ModeFactory.getWriteMode("Channel_queries", "add_channel_packages");
        m.executeUpdate(params, list);
        HibernateFactory.getSession().refresh(debChannel);

        TestUtils.saveAndFlush(debChannel);

        final String debNvra = String.format("%s_%s-%s.%s",
                dpkg.getPackageName().getName(), dpkg.getPackageEvr().getVersion(),
                dpkg.getPackageEvr().getRelease(), dpkg.getPackageArch().getLabel());
        File dpkgFile = File.createTempFile(debNvra, ".deb");
        Files.write(dpkgFile.getAbsoluteFile().toPath(),
                TestUtils.randomString().getBytes());
        dpkg.setPath(FilenameUtils.getName(dpkgFile.getAbsolutePath()));
        TestUtils.saveAndFlush(dpkg);

        String debUri = String.format("%s.deb", debNvra);

        return new Tuple3<>(dpkg, dpkgFile, debUri);
    }

    /**
     * Tests download without a token.
     */
    @Test
    public void testDownloadWithoutToken() {
        Request request = getMockRequestWithParams(new HashMap<>());

        try {
            downloadController.downloadPackage(request, response);
            fail("Controller should fail if no token was given");
        }
        catch (spark.HaltException e) {
            assertEquals(403, e.getStatusCode());
            assertNull(response.raw().getHeader("X-Sendfile"));
        }
    }

    /**
     * Test download with an invalid token parameter.
     */
    @Test
    public void testDownloadWithInvalidToken() {
        Map<String, String> params = new HashMap<>();
        params.put("invalid-token-should-return-403", "");
        Request request = getMockRequestWithParams(params);

        try {
            downloadController.downloadPackage(request, response);
            fail("Controller should fail if wrong token was given");
        }
        catch (spark.HaltException e) {
            assertEquals(403, e.getStatusCode());
            assertNull(response.raw().getHeader("X-Sendfile"));
        }
    }

    /**
     * Test download with an invalid token parameter, but token checking disabled.
     */
    @Test
    public void testDownloadPackageWithDisabledTokenChecking() {
        Map<String, String> params = new HashMap<>();
        params.put("invalid-token-should-be-ignored", "");
        Request request = getMockRequestWithParams(params);

        downloadController.setCheckTokens(false);
        try {
            downloadController.downloadPackage(request, response);
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
    @Test
    public void testDownloadMetadataWithDisabledTokenChecking() throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("invalid-token-should-be-ignored", "");
        Request request =  SparkTestUtils.createMockRequestWithParams(
                "http://localhost:8080/rhn/manager/download/:channel/repodata/:file",
                params,
                Collections.emptyMap(),
                channel.getLabel(), "comps.xml");

        downloadController.setCheckTokens(false);

        String compsRelativeDirPath = "rhn/comps/" + channel.getName();
        String compsDirPath = Config.get().getString(ConfigDefaults.MOUNT_POINT) + "/" +
                compsRelativeDirPath;
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
                assertNotNull(downloadController.downloadMetadata(request, response));

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
    @Test
    public void testTwoTokens() throws Exception {
        DownloadTokenBuilder tokenBuilder = new DownloadTokenBuilder(user.getOrg().getId());
        tokenBuilder.useServerSecret();
        String token = tokenBuilder.getToken();

        Map<String, String> params = new HashMap<>();
        params.put(token, "");
        params.put("2ndtoken", "");
        Request request = getMockRequestWithParams(params);

        try {
            downloadController.downloadPackage(request, response);
            fail(String.format("%s should halt 400 if 2 tokens given",
                    DownloadController.class.getSimpleName()));
        }
        catch (spark.HaltException e) {
            assertEquals(400, e.getStatusCode());
            assertNull(response.raw().getHeader("X-Sendfile"));
        }
    }

    /**
     * Tests a download with a wrong channel in the token.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testTokenDifferentChannel() throws Exception {
        // The added token is for a different channel
        DownloadTokenBuilder tokenBuilder = new DownloadTokenBuilder(user.getOrg().getId());
        tokenBuilder.useServerSecret();
        tokenBuilder.onlyChannels(
                new HashSet<>(
                        Arrays.asList(channel.getLabel() + "WRONG")));
        String tokenOtherChannel = tokenBuilder.getToken();


        Map<String, String> params = new HashMap<>();
        params.put(tokenOtherChannel, "");
        Request request = getMockRequestWithParams(params);

        try {
            downloadController.downloadPackage(request, response);
            fail(String.format("%s should halt 403 if a different channel token is given",
                    DownloadController.class.getSimpleName()));
        }
        catch (spark.HaltException e) {
            assertEquals(403, e.getStatusCode());
            assertNull(response.raw().getHeader("X-Sendfile"));
        }
    }

    /**
     * Tests a download with a wrong organization in the token.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testTokenWrongOrg() throws Exception {
        DownloadTokenBuilder tokenBuilder = new DownloadTokenBuilder(user.getOrg().getId() + 1);
        tokenBuilder.useServerSecret();
        tokenBuilder.onlyChannels(
                new HashSet<>(
                        Arrays.asList(channel.getLabel())));
        String tokenOtherOrg = tokenBuilder.getToken();

        Map<String, String> params = new HashMap<>();
        params.put(tokenOtherOrg, "");
        Request request = getMockRequestWithParams(params);

        try {
            downloadController.downloadPackage(request, response);
            fail(String.format("%s should halt 403 if a different org token is given",
                    DownloadController.class.getSimpleName()));
        }
        catch (spark.HaltException e) {
            assertEquals(403, e.getStatusCode());
            assertNull(response.raw().getHeader("X-Sendfile"));
        }
    }

    /**
     * Tests a download with a token not assigned to a minion.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
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
            downloadController.downloadPackage(request, response);
            fail(String.format("%s should halt 403 if the token is not assigned to a minion",
                    DownloadController.class.getSimpleName()));
        }
        catch (spark.HaltException e) {
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
    @Test
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
    @Test
    public void testCorrectChannelWithTokenInHeader() throws Exception {
        testCorrectChannel((tokenChannel) -> {
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Mgr-Auth", tokenChannel);
            return getMockRequestWithParamsAndHeaders(Collections.emptyMap(), headers);
        });
    }

    @Test
    public void testDownloadDebPackage() throws Exception {
        testCorrectChannel(() -> debPackageFile, (tokenChannel) -> {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(tokenChannel.getBytes()));
            return getMockRequestWithParamsAndHeaders(Collections.emptyMap(), headers, debUriFile);
        });
    }

    @Test
    public void testParseDebPackageVersion() throws Exception {
        testCorrectChannel(() -> debPackageFile2, (tokenChannel) -> {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(tokenChannel.getBytes()));
            return getMockRequestWithParamsAndHeaders(Collections.emptyMap(), headers, debUriFile2);
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
        DownloadTokenBuilder tokenBuilder = new DownloadTokenBuilder(user.getOrg().getId());
        tokenBuilder.useServerSecret();
        tokenBuilder.onlyChannels(
                new HashSet<>(
                        Arrays.asList(channel.getLabel())));
        AccessToken accessToken = saveTokenToDataBase(tokenBuilder);
        String tokenChannel = accessToken.getToken();

        Request request = requestFactory.apply(tokenChannel);
        try {
            assertNotNull(downloadController.downloadPackage(request, response));

            assertEquals(pkgFile.get().getAbsolutePath(), response.raw().getHeader("X-Sendfile"));
            assertEquals("application/octet-stream", response.raw().getHeader("Content-Type"));
            assertEquals("attachment; filename=" + pkgFile.get().getName(),
                    response.raw().getHeader("Content-Disposition"));
        }
        catch (spark.HaltException e) {
            fail("No HaltException should be thrown with a valid token!");
        }
    }

    @Test
    public void testDownloadPackageWithSpecialCharacters() throws Exception {
        DownloadTokenBuilder tokenBuilder = new DownloadTokenBuilder(user.getOrg().getId());
        tokenBuilder.useServerSecret();
        tokenBuilder.onlyChannels(new HashSet<>(Arrays.asList(channel.getLabel())));
        AccessToken accessToken = saveTokenToDataBase(tokenBuilder);
        String tokenChannel = accessToken.getToken();

        Map<String, String> params = new HashMap<>();
        params.put(tokenChannel, "");
        Request request = getMockRequestWithParamsAndHeaders(params, Collections.emptyMap(), uriFile2);
        try {
            assertNotNull(downloadController.downloadPackage(request, response));

            assertEquals(packageFile2.getAbsolutePath(), response.raw().getHeader("X-Sendfile"));
            assertEquals("application/octet-stream", response.raw().getHeader("Content-Type"));
            assertEquals("attachment; filename=" + packageFile2.getName(),
                    response.raw().getHeader("Content-Disposition"));
        }
        catch (spark.HaltException e) {
            fail(String.format("No HaltException should be thrown! %s", e.body()));
        }
    }

    /**
     * Test a download with a correct organization in the token.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testCorrectOrg() throws Exception {
        DownloadTokenBuilder tokenBuilder = new DownloadTokenBuilder(user.getOrg().getId());
        tokenBuilder.useServerSecret();
        AccessToken accessToken = saveTokenToDataBase(tokenBuilder);
        String tokenOrg = accessToken.getToken();

        Map<String, String> params = new HashMap<>();
        params.put(tokenOrg, "");
        Request request = getMockRequestWithParams(params);

        try {
            assertNotNull(downloadController.downloadPackage(request, response));

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
     * Tests that a expired token does not allow access
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testExpiredToken() throws Exception {
        DownloadTokenBuilder tokenBuilder = new DownloadTokenBuilder(user.getOrg().getId());
        tokenBuilder.useServerSecret();
        // already expired
        tokenBuilder.setExpirationTimeMinutesInTheFuture(-1);
        AccessToken accessToken = saveTokenToDataBase(tokenBuilder);
        String expiredToken = accessToken.getToken();

        Map<String, String> params = new HashMap<>();
        params.put(expiredToken, "");
        Request request = getMockRequestWithParams(params);

        try {
            downloadController.downloadPackage(request, response);
            fail(String.format("%s should halt 403 if an expired token is given",
                    DownloadController.class.getSimpleName()));
        }
        catch (spark.HaltException e) {
            assertEquals(403, e.getStatusCode());
            assertTrue(e.getBody().contains("This token is not valid"));
            assertNull(response.raw().getHeader("X-Sendfile"));
        }
    }

    @Test
    public void testPaygNotCompliant() {
        CloudPaygManager pmgr = new TestCloudPaygManagerBuilder()
            .withPaygInstance()
            .withModifiedPackages()
            .build();

        DownloadController ctl = new DownloadController(pmgr);

        Map<String, String> params = new HashMap<>();
        params.put("abcdef1234567890", "");
        Request request = getMockRequestWithParams(params);

        try {
            ctl.downloadPackage(request, response);
            fail(String.format("%s should halt 403 if an expired token is given",
                    DownloadController.class.getSimpleName()));
        }
        catch (spark.HaltException e) {
            assertEquals(403, e.getStatusCode());
            assertTrue(e.getBody().contains("Server is not compliant"));
            assertNull(response.raw().getHeader("X-Sendfile"));
        }
    }

    /**
     * Test for setting correct headers for comps.xml file.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testDownloadComps() throws Exception {
        DownloadTokenBuilder tokenBuilder = new DownloadTokenBuilder(user.getOrg().getId());
        tokenBuilder.useServerSecret();
        AccessToken accessToken = saveTokenToDataBase(tokenBuilder);
        String tokenOrg = accessToken.getToken();

        Map<String, String> params = new HashMap<>();
        params.put(tokenOrg, "");
        Request request =  SparkTestUtils.createMockRequestWithParams(
                "http://localhost:8080/rhn/manager/download/:channel/repodata/:file",
                params,
                Collections.emptyMap(),
                channel.getLabel(), "comps.xml");

        String compsRelativeDirPath = "rhn/comps/" + channel.getName();
        String compsDirPath = Config.get().getString(ConfigDefaults.MOUNT_POINT) + "/" +
                compsRelativeDirPath;
        String compsName = compsRelativeDirPath + "123hash123-comps-Server.x86_64";
        File compsDir = new File(compsDirPath);
        try {
            assertTrue(compsDir.mkdirs());
            File compsFile = File.createTempFile(compsDirPath + "/" + compsName, ".xml", compsDir);
            Files.write(compsFile.getAbsoluteFile().toPath(),
                    TestUtils.randomString().getBytes());

            // create comps object
            Comps comps = new Comps();
            comps.setChannel(channel);
            comps.setRelativeFilename(compsRelativeDirPath + "/" + compsFile.getName());
            channel.setComps(comps);

            try {
                assertNotNull(downloadController.downloadMetadata(request, response));

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
     * Test if modules.yaml file is served correctly.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testDownloadModules() throws Exception {
        DownloadTokenBuilder tokenBuilder = new DownloadTokenBuilder(user.getOrg().getId());
        tokenBuilder.useServerSecret();
        AccessToken accessToken = saveTokenToDataBase(tokenBuilder);
        String tokenOrg = accessToken.getToken();

        Map<String, String> params = new HashMap<>();
        params.put(tokenOrg, "");
        Request request =  SparkTestUtils.createMockRequestWithParams(
                "http://localhost:8080/rhn/manager/download/:channel/repodata/:file",
                params,
                Collections.emptyMap(),
                channel.getLabel(), "modules.yaml");

        String modulesRelativeDirPath = "rhn/modules/" + channel.getName();
        String modulesDirPath = Config.get().getString(ConfigDefaults.MOUNT_POINT) + "/" + modulesRelativeDirPath;
        String modulesName = modulesRelativeDirPath + "123hash123-modules";
        File modulesDir = new File(modulesDirPath);
        try {
            assertTrue(modulesDir.mkdirs());
            File modulesFile = File.createTempFile(modulesDirPath + "/" + modulesName, ".yaml", modulesDir);
            Files.write(modulesFile.getAbsoluteFile().toPath(),
                    TestUtils.randomString().getBytes());

            // create modules object
            Modules modules = new Modules();
            modules.setChannel(channel);
            modules.setRelativeFilename(modulesRelativeDirPath + "/" + modulesFile.getName());
            channel.setModules(modules);

            try {
                assertNotNull(downloadController.downloadMetadata(request, response));

                assertEquals(modulesFile.getAbsolutePath(), response.raw().getHeader("X-Sendfile"));
                assertEquals("application/octet-stream", response.raw().getHeader("Content-Type"));
                assertEquals("attachment; filename=" + modulesFile.getName(),
                        response.raw().getHeader("Content-Disposition"));
            }
            catch (spark.HaltException e) {
                fail("No HaltException should be thrown with a valid token!");
            }
        }
        finally {
            FileUtils.deleteDirectory(modulesDir);
        }
    }

    /**
     * Test if media.1/products file is served correctly.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testDownloadMediaProducts() throws Exception {
        DownloadTokenBuilder tokenBuilder = new DownloadTokenBuilder(user.getOrg().getId());
        tokenBuilder.useServerSecret();
        AccessToken accessToken = saveTokenToDataBase(tokenBuilder);
        String tokenOrg = accessToken.getToken();

        Map<String, String> params = new HashMap<>();
        params.put(tokenOrg, "");
        Request request =  SparkTestUtils.createMockRequestWithParams(
                "http://localhost:8080/rhn/manager/download/:channel/media.1/:file",
                params,
                Collections.emptyMap(),
                channel.getLabel(), "products");

        String productsRelativeDirPath = "suse/media.1/" + channel.getName() + "/";
        String productsDirPath = Config.get().getString(ConfigDefaults.MOUNT_POINT) + "/" + productsRelativeDirPath;
        String productsName = productsRelativeDirPath + "products";
        File productsDir = new File(productsDirPath);
        try {
            assertTrue(productsDir.mkdirs());
            File productsFile = File.createTempFile(productsDirPath + "/" + productsName, "", productsDir);
            Files.write(productsFile.getAbsoluteFile().toPath(),
                    "/ Basesystem-Module 15.3-0".getBytes());

            // create modules object
            MediaProducts prd = new MediaProducts();
            prd.setChannel(channel);
            prd.setRelativeFilename(productsRelativeDirPath + "/" + productsFile.getName());
            channel.setMediaProducts(prd);

            try {
                assertNotNull(downloadController.downloadMediaFiles(request, response));

                assertEquals(productsFile.getAbsolutePath(), response.raw().getHeader("X-Sendfile"));
                assertEquals("application/octet-stream", response.raw().getHeader("Content-Type"));
                assertEquals("attachment; filename=" + productsFile.getName(),
                        response.raw().getHeader("Content-Disposition"));
            }
            catch (spark.HaltException e) {
                fail("No HaltException should be thrown with a valid token!");
            }
        }
        finally {
            FileUtils.deleteDirectory(productsDir);
        }
    }

    /**
     * Test for missing file. Should not handover to xsendfile, but throw 404 Not Found
     * directly
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testDownloadMissingFile() throws Exception {
        DownloadTokenBuilder tokenBuilder = new DownloadTokenBuilder(user.getOrg().getId());
        tokenBuilder.useServerSecret();
        AccessToken accessToken = saveTokenToDataBase(tokenBuilder);
        String tokenOrg = accessToken.getToken();

        Map<String, String> params = new HashMap<>();
        params.put(tokenOrg, "");
        Request request =  SparkTestUtils.createMockRequestWithParams(
                "http://localhost:8080/rhn/manager/download/:channel/repodata/:file",
                params,
                Collections.emptyMap(),
                channel.getLabel(), "repomd.xml");

        try {
            assertNotNull(downloadController.downloadMetadata(request, response));
            fail("HaltException expected for missing file!");
        }
        catch (spark.HaltException e) {
            assertEquals(404, e.getStatusCode(), "Not Found Exception expected");
        }
    }

    @Test
    public void testParseDebPkgFilename1() {
        DownloadController.PkgInfo pack =
                downloadController.parsePackageFileName(
                        "/rhn/manager/download/debchannel/getPackage/gcc-8-base_8-20180414-1ubuntu2.amd64-deb.deb");
        assertEquals("gcc-8-base", pack.getName());
        assertNull(pack.getEpoch());
        assertEquals("8-20180414", pack.getVersion());
        assertEquals("1ubuntu2", pack.getRelease());
        assertEquals("amd64-deb", pack.getArch());
    }

    @Test
    public void testParseDebPkgFilename2() {
        DownloadController.PkgInfo pack =
                downloadController.parsePackageFileName(
                        "/rhn/manager/download/debchannel/getPackage/python-tornado_4.2.1-1ubuntu3.amd64-deb.deb");
        assertEquals("python-tornado", pack.getName());
        assertNull(pack.getEpoch());
        assertEquals("4.2.1", pack.getVersion());
        assertEquals("1ubuntu3", pack.getRelease());
        assertEquals("amd64-deb", pack.getArch());
    }

    @Test
    public void testParseDebPkgFilename3() {
        DownloadController.PkgInfo pack =
                downloadController.parsePackageFileName(
                        "/rhn/manager/download/ubuntu-18.04-amd64-main/getPackage/ruby_1:2.5.1-X.amd64-deb.deb");
        assertEquals("ruby", pack.getName());
        assertEquals("1", pack.getEpoch());
        assertEquals("2.5.1", pack.getVersion());
        assertEquals("X", pack.getRelease());
        assertEquals("amd64-deb", pack.getArch());
    }

    @Test
    public void testValidateMinionInPaygShortToken() {
        CloudPaygManager cpg = new TestCloudPaygManagerBuilder()
            .withPaygInstance()
            .withoutSCCCredentials()
            .build();

        downloadController = new DownloadController(cpg);

        // Test case - Token passed is not a short-token (must fail)
        DownloadTokenBuilder tokenBuilderFail = new DownloadTokenBuilder(user.getOrg().getId());
        tokenBuilderFail.useServerSecret();
        tokenBuilderFail.setExpirationTimeMinutesInTheFuture(360);
        try {
            downloadController.validateMinionInPayg(tokenBuilderFail.getToken());
            fail("Long lived token shouldn't have been accepted");
        }
        catch (spark.HaltException e) {
            assertEquals(HttpStatus.SC_FORBIDDEN, e.getStatusCode());
            assertTrue(e.getBody().contains("Forbidden: Token is expired or is not a short-token"));
        }
        catch (JoseException e) {
            fail("There was an issue when building the test token");
        }

        // Test case - Token passed is short-lived (must pass)
        DownloadTokenBuilder tokenBuilderPass = new DownloadTokenBuilder(user.getOrg().getId());
        tokenBuilderPass.useServerSecret();
        tokenBuilderPass.setExpirationTimeMinutesInTheFuture(30);
        try {
            downloadController.validateMinionInPayg(tokenBuilderPass.getToken());
        }
        catch (spark.HaltException e) {
            fail("Short-lived token must've been accepted");
        }
        catch (JoseException e) {
            fail("There was an issue when building the test token");
        }

        // Test case - Token passed is expired
        DownloadTokenBuilder tokenBuilderExpired = new DownloadTokenBuilder(user.getOrg().getId());
        tokenBuilderExpired.useServerSecret();
        tokenBuilderExpired.setExpirationTimeMinutesInTheFuture(-15);
        try {
            downloadController.validateMinionInPayg(tokenBuilderExpired.getToken());
            fail("A token in the past must no be accepted");
        }
        catch (spark.HaltException e) {
            assertEquals(HttpStatus.SC_FORBIDDEN, e.getStatusCode());
            assertTrue(e.getBody().contains("Forbidden: Short-token is not valid or is expired"));
        }
        catch (JoseException e) {
            fail("There was an issue when building the test token");
        }
    }
}
