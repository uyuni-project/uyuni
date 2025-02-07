/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.hub.test;

import static com.suse.manager.hub.HubSparkHelper.usingTokenAuthentication;
import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static spark.Spark.post;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.channel.ProductName;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.channel.test.ChannelFamilyFactoryTest;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.ErrataTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.hub.HubController;
import com.suse.manager.model.hub.ChannelInfoJson;
import com.suse.manager.model.hub.CustomChannelInfoJson;
import com.suse.manager.model.hub.IssAccessToken;
import com.suse.manager.model.hub.IssRole;
import com.suse.manager.model.hub.ManagerInfoJson;
import com.suse.manager.model.hub.OrgInfoJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.token.IssTokenBuilder;
import com.suse.manager.webui.utils.token.Token;
import com.suse.utils.Json;

import com.google.gson.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

public class HubControllerTest extends JMockBaseTestCaseWithUser {

    private static final String DUMMY_SERVER_FQDN = "dummy-server.unit-test.local";

    private final ControllerTestUtils testUtils = new ControllerTestUtils();

    private static final String TEST_ERROR_MESSAGE = "test error message";

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        HubController dummyHubController = new HubController();
        dummyHubController.initRoutes();
        //add dummy route that throws
        post("/hub/testThrowsRuntimeException", asJson(usingTokenAuthentication(this::testThrowsRuntimeException)));
    }

    private String testThrowsRuntimeException(Request request, Response response, IssAccessToken token) {
        throw new NullPointerException(TEST_ERROR_MESSAGE);
    }

    private static Stream<Arguments> allApiEndpoints() {
        return Stream.of(Arguments.of(HttpMethod.post, "/hub/ping", null),
                Arguments.of(HttpMethod.post, "/hub/sync/deregister", null),
                Arguments.of(HttpMethod.post, "/hub/sync/registerHub", null),
                Arguments.of(HttpMethod.post, "/hub/sync/replaceTokens", IssRole.HUB),
                Arguments.of(HttpMethod.post, "/hub/sync/storeCredentials", IssRole.HUB),
                Arguments.of(HttpMethod.post, "/hub/sync/setHubDetails", IssRole.HUB),
                Arguments.of(HttpMethod.get, "/hub/managerinfo", IssRole.HUB),
                Arguments.of(HttpMethod.post, "/hub/storeReportDbCredentials", IssRole.HUB),
                Arguments.of(HttpMethod.post, "/hub/removeReportDbCredentials", IssRole.HUB),
                Arguments.of(HttpMethod.get, "/hub/listAllPeripheralOrgs", IssRole.PERIPHERAL),
                Arguments.of(HttpMethod.get, "/hub/listAllPeripheralChannels", IssRole.PERIPHERAL),
                Arguments.of(HttpMethod.post, "/hub/addVendorChannels", IssRole.PERIPHERAL),
                Arguments.of(HttpMethod.post, "/hub/addCustomChannels", IssRole.PERIPHERAL)
        );
    }

    private static Stream<Arguments> onlyGetApis() {
        return allApiEndpoints().filter(e -> (HttpMethod.get == e.get()[0]));
    }

    private static Stream<Arguments> onlyPostApis() {
        return allApiEndpoints().filter(e -> (HttpMethod.post == e.get()[0]));
    }

    private static Stream<Arguments> onlyHubApis() {
        return allApiEndpoints().filter(e -> (IssRole.HUB == e.get()[2]));
    }

    private static Stream<Arguments> onlyPeripheralApis() {
        return allApiEndpoints().filter(e -> (IssRole.PERIPHERAL == e.get()[2]));
    }

    @ParameterizedTest
    @MethodSource("onlyGetApis")
    public void ensureGetApisNotWorkingWithPost(HttpMethod apiMethod, String apiEndpoint, IssRole apiRole) {
        assertThrows(IllegalStateException.class, () ->
                        testUtils.withServerFqdn(DUMMY_SERVER_FQDN)
                                .withApiEndpoint(apiEndpoint)
                                .withHttpMethod(HttpMethod.post)
                                .withRole(apiRole)
                                .withBearerTokenInHeaders()
                                .simulateControllerApiCall(),
                apiEndpoint + " get API not failing when called with post method");
    }

    @ParameterizedTest
    @MethodSource("onlyPostApis")
    public void ensurePostApisNotWorkingWithGet(HttpMethod apiMethod, String apiEndpoint, IssRole apiRole) {
        assertThrows(IllegalStateException.class, () ->
                        testUtils.withServerFqdn(DUMMY_SERVER_FQDN)
                                .withApiEndpoint(apiEndpoint)
                                .withHttpMethod(HttpMethod.get)
                                .withRole(apiRole)
                                .withBearerTokenInHeaders()
                                .simulateControllerApiCall(),
                apiEndpoint + " post API not failing when called with get method");
    }

    @ParameterizedTest
    @MethodSource("onlyHubApis")
    public void ensureHubApisNotWorkingWithPeripheral(HttpMethod apiMethod, String apiEndpoint, IssRole apiRole)
            throws Exception {
        String answerKO = (String) testUtils.withServerFqdn(DUMMY_SERVER_FQDN)
                .withApiEndpoint(apiEndpoint)
                .withHttpMethod(apiMethod)
                .withRole(IssRole.PERIPHERAL)
                .withBearerTokenInHeaders()
                .simulateControllerApiCall();

        ResultJson<?> resultKO = Json.GSON.fromJson(answerKO, ResultJson.class);
        assertFalse(resultKO.isSuccess(), apiEndpoint + " hub API not failing with peripheral server");
        assertEquals("Token does not allow access to this resource", resultKO.getMessages().get(0));
    }

    @ParameterizedTest
    @MethodSource("onlyPeripheralApis")
    public void ensurePeripheralApisNotWorkingWithHub(HttpMethod apiMethod, String apiEndpoint, IssRole apiRole)
            throws Exception {
        String answerKO = (String) testUtils.withServerFqdn(DUMMY_SERVER_FQDN)
                .withApiEndpoint(apiEndpoint)
                .withHttpMethod(apiMethod)
                .withRole(IssRole.HUB)
                .withBearerTokenInHeaders()
                .simulateControllerApiCall();

        ResultJson<?> resultKO = Json.GSON.fromJson(answerKO, ResultJson.class);
        assertFalse(resultKO.isSuccess(), apiEndpoint + " peripheral API not failing with hub server");
        assertEquals("Token does not allow access to this resource", resultKO.getMessages().get(0));
    }

    @ParameterizedTest
    @MethodSource("allApiEndpoints")
    public void ensureNotWorkingWithoutToken(HttpMethod apiMethod, String apiEndpoint, IssRole apiRole)
            throws Exception {
        try {
            testUtils.withServerFqdn(DUMMY_SERVER_FQDN)
                    .withApiEndpoint(apiEndpoint)
                    .withHttpMethod(apiMethod)
                    .withRole(apiRole)
                    .simulateControllerApiCall();

            fail(apiEndpoint + " API call should have failed without token");
        }
        catch (spark.HaltException ex) {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.statusCode());
        }
    }

    @Test
    public void checkPingApiEndpoint() throws Exception {
        String apiUnderTest = "/hub/ping";

        String answer = (String) testUtils.withServerFqdn(DUMMY_SERVER_FQDN)
                .withApiEndpoint(apiUnderTest)
                .withHttpMethod(HttpMethod.post)
                .withBearerTokenInHeaders()
                .simulateControllerApiCall();
        JsonObject jsonObj = Json.GSON.fromJson(answer, JsonObject.class);

        assertTrue(jsonObj.get("message").getAsString().startsWith("Pinged from"), "Unexpected ping message");
    }

    @Test
    public void checkRegisterHubApiEndpoint() throws Exception {
        String apiUnderTest = "/hub/sync/registerHub";

        Token dummyToken = new IssTokenBuilder(ConfigDefaults.get().getHostname()).usingServerSecret().build();
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("rootCA", "----- BEGIN TEST ROOTCA ----");
        bodyMap.put("token", dummyToken.getSerializedForm());

        String answer = (String) testUtils.withServerFqdn(ConfigDefaults.get().getHostname())
                .withApiEndpoint(apiUnderTest)
                .withHttpMethod(HttpMethod.post)
                .withBearerTokenInHeaders()
                .withBody(bodyMap)
                .simulateControllerApiCall();
        JsonObject jsonObj = Json.GSON.fromJson(answer, JsonObject.class);

        //taskomatic is not running!
        assertFalse(jsonObj.get("success").getAsBoolean());
        assertEquals("Unable to schedule root CA certificate update",
                jsonObj.get("messages").getAsJsonArray().get(0).getAsString(), "Unexpected error message");
    }

    @Test
    public void checkStoreCredentialsApiEndpoint() throws Exception {
        String apiUnderTest = "/hub/sync/storeCredentials";

        String testUserName = testUtils.createTestUserName();
        String testPassword = testUtils.createTestPassword();
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("username", testUserName);
        bodyMap.put("password", testPassword);

        String answer = (String) testUtils.withServerFqdn(ConfigDefaults.get().getHostname())
                .withApiEndpoint(apiUnderTest)
                .withHttpMethod(HttpMethod.post)
                .withRole(IssRole.HUB)
                .withBearerTokenInHeaders()
                .withBody(bodyMap)
                .simulateControllerApiCall();
        JsonObject jsonObj = Json.GSON.fromJson(answer, JsonObject.class);

        assertTrue(jsonObj.get("success").getAsBoolean(), apiUnderTest + " API call is failing");
    }

    @Test
    public void checkManagerinfoApiEndpoint() throws Exception {
        String apiUnderTest = "/hub/managerinfo";

        String answer = (String) testUtils.withServerFqdn(DUMMY_SERVER_FQDN)
                .withApiEndpoint(apiUnderTest)
                .withHttpMethod(HttpMethod.get)
                .withRole(IssRole.HUB)
                .withBearerTokenInHeaders()
                .simulateControllerApiCall();
        ManagerInfoJson mgrInfo = Json.GSON.fromJson(answer, ManagerInfoJson.class);

        assertFalse(mgrInfo.getVersion().isBlank(), "ManagerInfo version is blank");
        assertTrue(mgrInfo.hasReportDb(), "ManagerInfo has missing report db");
        assertFalse(mgrInfo.getReportDbName().isBlank(), "ManagerInfo database name is blank");
        assertFalse(mgrInfo.getReportDbHost().isBlank(), "ManagerInfo database host is blank");
        assertEquals(5432, mgrInfo.getReportDbPort(), "ManagerInfo database port is not 5432");
    }

    @Test
    public void checkStoreReportDbCredentialsApiEndpoint() throws Exception {
        String apiUnderTest = "/hub/storeReportDbCredentials";

        String testReportDbUserName = testUtils.createTestUserName();
        String testReportDbPassword = testUtils.createTestPassword();
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("username", testReportDbUserName);
        bodyMap.put("password", testReportDbPassword);

        //check there is no user with that username
        assertFalse(testUtils.existsReportDbUser(testReportDbUserName));

        String answer = (String) testUtils.withServerFqdn(DUMMY_SERVER_FQDN)
                .withApiEndpoint(apiUnderTest)
                .withHttpMethod(HttpMethod.post)
                .withRole(IssRole.HUB)
                .withBearerTokenInHeaders()
                .withBody(bodyMap)
                .simulateControllerApiCall();
        JsonObject jsonObj = Json.GSON.fromJson(answer, JsonObject.class);

        assertTrue(jsonObj.get("success").getAsBoolean(), apiUnderTest + " API call is failing");

        //check there is one user with that username
        assertTrue(testUtils.existsReportDbUser(testReportDbUserName),
                apiUnderTest + " API reports no user " + testReportDbUserName);

        //cleanup
        testUtils.cleanupReportDbUser(testReportDbUserName);
        assertFalse(testUtils.existsReportDbUser(testReportDbUserName),
                "cleanup of user not working for user " + testReportDbUserName);
    }

    @Test
    public void checkRemoveReportDbCredentialsApiEndpoint() throws Exception {
        String apiUnderTest = "/hub/removeReportDbCredentials";

        String testReportDbUserName = testUtils.createTestUserName();
        String testReportDbPassword = "testPassword" + TestUtils.randomString();
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("username", testReportDbUserName);

        //create a user
        testUtils.createReportDbUser(testReportDbUserName, testReportDbPassword);
        assertTrue(testUtils.existsReportDbUser(testReportDbUserName),
                "failed creation of user " + testReportDbUserName);

        String answer = (String) testUtils.withServerFqdn(DUMMY_SERVER_FQDN)
                .withApiEndpoint(apiUnderTest)
                .withHttpMethod(HttpMethod.post)
                .withRole(IssRole.HUB)
                .withBearerTokenInHeaders()
                .withBody(bodyMap)
                .simulateControllerApiCall();
        JsonObject jsonObj = Json.GSON.fromJson(answer, JsonObject.class);

        assertTrue(jsonObj.get("success").getAsBoolean(), apiUnderTest + " API call is failing");
        //check the user is gone
        assertFalse(testUtils.existsReportDbUser(testReportDbUserName),
                apiUnderTest + " API call fails to remove user " + testReportDbUserName);
    }

    @Test
    public void checkApiListAllPeripheralOrgs() throws Exception {
        String apiUnderTest = "/hub/listAllPeripheralOrgs";

        Org org1 = UserTestUtils.findNewOrg("org1");
        Org org2 = UserTestUtils.findNewOrg("org2");
        Org org3 = UserTestUtils.findNewOrg("org3");

        String answer = (String) testUtils.withServerFqdn(DUMMY_SERVER_FQDN)
                .withApiEndpoint(apiUnderTest)
                .withHttpMethod(HttpMethod.get)
                .withRole(IssRole.PERIPHERAL)
                .withBearerTokenInHeaders()
                .simulateControllerApiCall();
        List<OrgInfoJson> allOrgs = Arrays.asList(Json.GSON.fromJson(answer, OrgInfoJson[].class));

        assertTrue(allOrgs.size() >= 3, "All 3 test test orgs are not listed");
        assertTrue(allOrgs.stream()
                        .anyMatch(e -> (e.getOrgId() == org1.getId()) && (e.getOrgName().startsWith("org1"))),
                apiUnderTest + " API call not listing test organization [org1]");
        assertTrue(allOrgs.stream()
                        .anyMatch(e -> (e.getOrgId() == org2.getId()) && (e.getOrgName().startsWith("org2"))),
                apiUnderTest + " API call not listing test organization [org2]");
        assertTrue(allOrgs.stream()
                        .anyMatch(e -> (e.getOrgId() == org3.getId()) && (e.getOrgName().startsWith("org3"))),
                apiUnderTest + " API call not listing test organization [org3]");
    }

    @Test
    public void checkApilistAllPeripheralChannels() throws Exception {
        String apiUnderTest = "/hub/listAllPeripheralChannels";

        user.addPermanentRole(RoleFactory.CHANNEL_ADMIN);
        Channel testBaseChannel = ChannelTestUtils.createBaseChannel(user);
        Channel testChildChannel = ChannelTestUtils.createChildChannel(user, testBaseChannel);

        String answer = (String) testUtils.withServerFqdn(DUMMY_SERVER_FQDN)
                .withApiEndpoint(apiUnderTest)
                .withHttpMethod(HttpMethod.get)
                .withRole(IssRole.PERIPHERAL)
                .withBearerTokenInHeaders()
                .simulateControllerApiCall();
        List<ChannelInfoJson> allChannels = Arrays.asList(Json.GSON.fromJson(answer, ChannelInfoJson[].class));

        Optional<ChannelInfoJson> testBaseChannelInfo = allChannels.stream()
                .filter(e -> e.getName().equals(testBaseChannel.getName()))
                .findAny();
        assertTrue(testBaseChannelInfo.isPresent(),
                apiUnderTest + " API call not listing channel " + testBaseChannel);
        assertEquals(testBaseChannel.getName(), testBaseChannelInfo.get().getName());
        assertEquals(testBaseChannel.getLabel(), testBaseChannelInfo.get().getLabel());
        assertEquals(user.getOrg().getId(), testBaseChannelInfo.get().getOrgId());
        assertNull(testBaseChannelInfo.get().getParentChannelId());

        Optional<ChannelInfoJson> testChildChannelInfo = allChannels.stream()
                .filter(e -> e.getName().equals(testChildChannel.getName()))
                .findAny();
        assertTrue(testChildChannelInfo.isPresent(),
                apiUnderTest + " API call not listing channel " + testChildChannel);
        assertEquals(testChildChannel.getName(), testChildChannelInfo.get().getName());
        assertEquals(testChildChannel.getLabel(), testChildChannelInfo.get().getLabel());
        assertEquals(user.getOrg().getId(), testChildChannelInfo.get().getOrgId());
        assertEquals(testBaseChannel.getId(), testChildChannelInfo.get().getParentChannelId());
    }

    @Test
    public void checkApiThrowingRuntimeException() throws Exception {
        String apiUnderTest = "/hub/testThrowsRuntimeException";

        ControllerTestUtils utils = new ControllerTestUtils();
        assertDoesNotThrow(() -> utils.withServerFqdn(DUMMY_SERVER_FQDN)
                .withApiEndpoint(apiUnderTest)
                .withHttpMethod(HttpMethod.post)
                .withRole(IssRole.PERIPHERAL)
                .withBearerTokenInHeaders()
                .simulateControllerApiCall());

        String answer = (String) utils.withServerFqdn(DUMMY_SERVER_FQDN)
                .withApiEndpoint(apiUnderTest)
                .withHttpMethod(HttpMethod.post)
                .withRole(IssRole.PERIPHERAL)
                .withBearerTokenInHeaders()
                .simulateControllerApiCall();
        JsonObject jsonObj = Json.GSON.fromJson(answer, JsonObject.class);

        assertFalse(jsonObj.get("success").getAsBoolean(), apiUnderTest +
                " API throwing a runtime exception should fail");
        assertEquals("Internal Server Error", jsonObj.get("messages").getAsJsonArray().get(0).getAsString());
        assertEquals(TEST_ERROR_MESSAGE, jsonObj.get("messages").getAsJsonArray().get(1).getAsString());
    }

    private Channel utilityCreateVendorBaseChannel(String name, String label) throws Exception {
        Org nullOrg = null;
        ChannelFamily cfam = ChannelFamilyFactoryTest.createNullOrgTestChannelFamily();
        String query = "ChannelArch.findById";
        ChannelArch arch = (ChannelArch) TestUtils.lookupFromCacheById(500L, query);
        return ChannelFactoryTest.createTestChannel(name, label, nullOrg, arch, cfam);
    }

    private Channel utilityCreateVendorChannel(String name, String label, Channel vendorBaseChannel) throws Exception {
        Channel vendorChannel = utilityCreateVendorBaseChannel(name, label);
        vendorChannel.setParentChannel(vendorBaseChannel);
        vendorChannel.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha512"));
        ChannelFactory.save(vendorChannel);
        return vendorChannel;
    }

    private static Stream<Arguments> allBaseAndVendorChannelAlreadyPresentCombinations() {
        return Stream.of(Arguments.of(false, false),
                Arguments.of(true, false),
                Arguments.of(true, true)
        );
    }

    @ParameterizedTest
    @MethodSource("allBaseAndVendorChannelAlreadyPresentCombinations")
    public void checkApiAddVendorChannel(boolean baseChannelAlreadyPresentInPeripheral,
                                         boolean channelAlreadyPresentInPeripheral) throws Exception {
        String apiUnderTest = "/hub/addVendorChannels";

        //SUSE Linux Enterprise Server 11 SP3 x86_64
        String vendorBaseChannelTemplateName = "SLES11-SP3-Pool for x86_64";
        String vendorBaseChannelTemplateLabel = "sles11-sp3-pool-x86_64";
        String vendorChannelTemplateName = "SLES11-SP3-Updates for x86_64";
        String vendorChannelTemplateLabel = "sles11-sp3-updates-x86_64";

        String answer = testUtils.createTestVendorChannels(user, DUMMY_SERVER_FQDN,
                vendorBaseChannelTemplateName, vendorBaseChannelTemplateLabel,
                baseChannelAlreadyPresentInPeripheral,
                vendorChannelTemplateName, vendorChannelTemplateLabel,
                channelAlreadyPresentInPeripheral);

        int expectedNumOfPeripheralCreatedChannels = 2;
        if (baseChannelAlreadyPresentInPeripheral) {
            expectedNumOfPeripheralCreatedChannels--;
        }
        if (channelAlreadyPresentInPeripheral) {
            expectedNumOfPeripheralCreatedChannels--;
        }

        List<ChannelInfoJson> peripheralVendorCreatedChannelsInfo =
                Arrays.asList(Json.GSON.fromJson(answer, ChannelInfoJson[].class));

        assertEquals(expectedNumOfPeripheralCreatedChannels, peripheralVendorCreatedChannelsInfo.size());

        Optional<ChannelInfoJson> peripheralVendorBaseChannelInfo =
                peripheralVendorCreatedChannelsInfo
                        .stream()
                        .filter(e -> e.getLabel().equals(vendorBaseChannelTemplateLabel))
                        .findAny();
        if (baseChannelAlreadyPresentInPeripheral) {
            assertTrue(peripheralVendorBaseChannelInfo.isEmpty(),
                    String.format("%s API call mistakenly creating base channel %s",
                            apiUnderTest, vendorBaseChannelTemplateLabel));
        }
        else {
            assertTrue(peripheralVendorBaseChannelInfo.isPresent(),
                    String.format("%s API call mistakenly NOT creating base channel %s",
                            apiUnderTest, vendorBaseChannelTemplateLabel));
            assertEquals(vendorBaseChannelTemplateName, peripheralVendorBaseChannelInfo.get().getName());
            assertEquals(vendorBaseChannelTemplateLabel, peripheralVendorBaseChannelInfo.get().getLabel());
            assertNull(peripheralVendorBaseChannelInfo.get().getOrgId());
            assertNull(peripheralVendorBaseChannelInfo.get().getParentChannelId());
        }

        Optional<ChannelInfoJson> testChildPeriphChInfo =
                peripheralVendorCreatedChannelsInfo
                        .stream()
                        .filter(e -> e.getLabel().equals(vendorChannelTemplateLabel))
                        .findAny();
        if (channelAlreadyPresentInPeripheral) {
            assertTrue(testChildPeriphChInfo.isEmpty(),
                    String.format("%s API call mistakenly creating vendor channel %s",
                            apiUnderTest, vendorChannelTemplateLabel));
        }
        else {
            assertTrue(testChildPeriphChInfo.isPresent(),
                    String.format("%s API call mistakenly NOT creating vendor channel %s",
                            apiUnderTest, vendorChannelTemplateLabel));

            assertEquals(vendorChannelTemplateName, testChildPeriphChInfo.get().getName());
            assertEquals(vendorChannelTemplateLabel, testChildPeriphChInfo.get().getLabel());
            assertNull(testChildPeriphChInfo.get().getOrgId());
            if (baseChannelAlreadyPresentInPeripheral) {
                assertTrue(testChildPeriphChInfo.get().getParentChannelId() > 0,
                        "child channel not having valid parent channel id");
            }
            else {
                assertEquals(peripheralVendorBaseChannelInfo.get().getId(),
                        testChildPeriphChInfo.get().getParentChannelId());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void checkApiAddCustomChannel(boolean testIncludeTestChannelInChain) throws Exception {
        boolean testIsGpgCheck = true;
        boolean testIssInstallerUpdates = true;
        String testArchLabel = "channel-s390";
        String testChecksumLabel = "sha256";

        Date endOfLifeDate = testUtils.createDateUtil(2096, 10, 22);

        User testPeripheralUser = UserTestUtils.findNewUser("peripheral_user_", "peripheral_org_", true);
        Org testPeripheralOrg = testPeripheralUser.getOrg();
        Long testPeripheralOrgId = testPeripheralOrg.getId();

        // vendorBaseCh -> cloneBaseCh
        //  ^                ^
        // vendorCh -> cloneDevelCh -> cloneTestCh -> cloneProdCh

        // prepare inputs
        String vendorBaseChannelTemplateLabel = "sles11-sp3-pool-x86_64"; //SUSE Linux Enterprise Server 11 SP3 x86_64
        String vendorChannelTemplateLabel = "sles11-sp3-updates-x86_64";

        CustomChannelInfoJson cloneBaseChInfo = testUtils.createCustomChannelInfoJson(testPeripheralOrgId,
                "cloneBaseCh", "", vendorBaseChannelTemplateLabel,
                testIsGpgCheck, testIssInstallerUpdates, testArchLabel, testChecksumLabel, endOfLifeDate);

        CustomChannelInfoJson cloneDevelChInfo = testUtils.createCustomChannelInfoJson(testPeripheralOrgId,
                "cloneDevelCh", "cloneBaseCh", vendorChannelTemplateLabel,
                testIsGpgCheck, testIssInstallerUpdates, testArchLabel, testChecksumLabel, endOfLifeDate);

        CustomChannelInfoJson cloneTestChInfo = null;
        String originalOfProdCh = "cloneDevelCh";
        if (testIncludeTestChannelInChain) {
            cloneTestChInfo = testUtils.createCustomChannelInfoJson(testPeripheralOrgId,
                    "cloneTestCh", "", "cloneDevelCh",
                    testIsGpgCheck, testIssInstallerUpdates, testArchLabel, testChecksumLabel, endOfLifeDate);
            originalOfProdCh = "cloneTestCh";
        }

        CustomChannelInfoJson cloneProdChInfo = testUtils.createCustomChannelInfoJson(testPeripheralOrgId,
                "cloneProdCh", "", originalOfProdCh,
                testIsGpgCheck, testIssInstallerUpdates, testArchLabel, testChecksumLabel, endOfLifeDate);

        testUtils.createTestVendorChannels(user, DUMMY_SERVER_FQDN);

        Channel vendorBaseCh = ChannelFactory.lookupByLabel(vendorBaseChannelTemplateLabel);
        assertNotNull(vendorBaseCh);
        Channel vendorCh = ChannelFactory.lookupByLabel(vendorChannelTemplateLabel);
        assertNotNull(vendorCh);

        //create peripheral vendorCh custom cloned channels
        List<CustomChannelInfoJson> customChannelInfoListIn = new ArrayList<>();
        customChannelInfoListIn.add(cloneBaseChInfo);
        customChannelInfoListIn.add(cloneDevelChInfo);
        if (testIncludeTestChannelInChain) {
            customChannelInfoListIn.add(cloneTestChInfo);
        }
        customChannelInfoListIn.add(cloneProdChInfo);

        String answer = (String) testUtils.testAddCustomChannelsApiCall(DUMMY_SERVER_FQDN, customChannelInfoListIn);
        List<ChannelInfoJson> peripheralCreatedCustomChInfo =
                Arrays.asList(Json.GSON.fromJson(answer, ChannelInfoJson[].class));

        if (testIncludeTestChannelInChain) {
            assertEquals(4, peripheralCreatedCustomChInfo.size());
        }
        else {
            assertEquals(3, peripheralCreatedCustomChInfo.size());
        }
        assertTrue(peripheralCreatedCustomChInfo.stream()
                .anyMatch(e -> e.getLabel().equals("cloneBaseCh")));
        assertTrue(peripheralCreatedCustomChInfo.stream()
                .anyMatch(e -> e.getLabel().equals("cloneDevelCh")));
        if (testIncludeTestChannelInChain) {
            assertTrue(peripheralCreatedCustomChInfo.stream()
                    .anyMatch(e -> e.getLabel().equals("cloneTestCh")));
        }
        assertTrue(peripheralCreatedCustomChInfo.stream()
                .anyMatch(e -> e.getLabel().equals("cloneProdCh")));

        Channel cloneBaseCh = ChannelFactory.lookupByLabel("cloneBaseCh");
        assertNotNull(cloneBaseCh);
        Channel cloneDevelCh = ChannelFactory.lookupByLabel("cloneDevelCh");
        assertNotNull(cloneDevelCh);
        Channel cloneTestCh = ChannelFactory.lookupByLabel("cloneTestCh");
        if (testIncludeTestChannelInChain) {
            assertNotNull(cloneTestCh);
        }
        Channel cloneProdCh = ChannelFactory.lookupByLabel("cloneProdCh");
        assertNotNull(cloneProdCh);

        // tests
        // vendorBaseCh -> cloneBaseCh
        //  ^                ^
        // vendorCh -> cloneDevelCh -> cloneTestCh -> cloneProdCh

        assertNull(vendorBaseCh.getParentChannel());
        assertNull(cloneBaseCh.getParentChannel());
        assertEquals(vendorBaseCh, vendorCh.getParentChannel());
        assertEquals(cloneBaseCh, cloneDevelCh.getParentChannel());
        if (testIncludeTestChannelInChain) {
            assertNull(cloneTestCh.getParentChannel());
        }
        assertNull(cloneProdCh.getParentChannel());

        assertTrue(vendorBaseCh.asCloned().isEmpty(), "vendorBaseCh should not be a cloned channel");
        assertTrue(cloneBaseCh.asCloned().isPresent(), "cloneBaseCh should be a cloned channel");
        assertTrue(vendorCh.asCloned().isEmpty(), "vendorCh should not be a cloned channel");
        assertTrue(cloneDevelCh.asCloned().isPresent(), "cloneDevelCh should be a cloned channel");
        if (testIncludeTestChannelInChain) {
            assertTrue(cloneTestCh.asCloned().isPresent(), "cloneTestCh should be a cloned channel");
        }
        assertTrue(cloneProdCh.asCloned().isPresent(), "cloneProdCh should be a cloned channel");

        assertEquals(vendorBaseCh, cloneBaseCh.asCloned().get().getOriginal());
        assertEquals(vendorCh, cloneDevelCh.asCloned().get().getOriginal());
        if (testIncludeTestChannelInChain) {
            assertEquals(cloneDevelCh, cloneTestCh.asCloned().get().getOriginal());
            assertEquals(cloneTestCh, cloneProdCh.asCloned().get().getOriginal());
        }
        else {
            assertEquals(cloneDevelCh, cloneProdCh.asCloned().get().getOriginal());
        }

        ArrayList<Channel> channelsToTest = new ArrayList<>();
        channelsToTest.add(cloneBaseCh);
        channelsToTest.add(cloneDevelCh);
        channelsToTest.add(cloneProdCh);
        if (testIncludeTestChannelInChain) {
            channelsToTest.add(cloneTestCh);
        }
        for (Channel ch : channelsToTest) {
            testUtils.testCustomChannel(ch, testPeripheralOrgId,
                    testIsGpgCheck,
                    testIssInstallerUpdates,
                    testArchLabel,
                    testChecksumLabel,
                    endOfLifeDate);
        }
    }

    @Test
    public void ensureNotThrowingWhenDataIsValid() throws Exception {
        CustomChannelInfoJson customChInfo = testUtils.createValidCustomChInfo();

        testUtils.checkAddCustomChannelsApiNotThrowing(DUMMY_SERVER_FQDN, List.of(customChInfo));
    }

    @Test
    public void ensureThrowsWhenMissingPeriperhalOrg() throws Exception {
        CustomChannelInfoJson customChInfo = testUtils.createValidCustomChInfo();
        customChInfo.setPeripheralOrgId(75842L);

        testUtils.checkAddCustomChannelsApiThrows(DUMMY_SERVER_FQDN, List.of(customChInfo), "No org id");
    }

    @Test
    public void ensureThrowsWhenMissingChannelArch() throws Exception {
        CustomChannelInfoJson customChInfo = testUtils.createValidCustomChInfo();
        customChInfo.setChannelArchLabel("channel-dummy-arch");

        testUtils.checkAddCustomChannelsApiThrows(DUMMY_SERVER_FQDN, List.of(customChInfo), "No channel arch");
    }

    @Test
    public void ensureThrowsWhenMissingChecksumType() throws Exception {
        CustomChannelInfoJson customChInfo = testUtils.createValidCustomChInfo();
        customChInfo.setChecksumTypeLabel("sha123456");

        testUtils.checkAddCustomChannelsApiThrows(DUMMY_SERVER_FQDN, List.of(customChInfo), "No checksum type");
    }

    @Test
    public void ensureThrowsWhenMissingParentChannel() throws Exception {
        CustomChannelInfoJson customChInfo = testUtils.createValidCustomChInfo();
        customChInfo.setParentChannelLabel("missingParentChannelLabel");

        testUtils.checkAddCustomChannelsApiThrows(DUMMY_SERVER_FQDN, List.of(customChInfo), "No parent channel");
    }

    @Test
    public void ensureNotThrowingWhenParentChannelIsCreatedBefore() throws Exception {
        CustomChannelInfoJson customParentChInfo = testUtils.createValidCustomChInfo("parentChannel");

        CustomChannelInfoJson customChildChInfo = testUtils.createValidCustomChInfo("childChannel");
        customChildChInfo.setParentChannelLabel("parentChannel");

        testUtils.checkAddCustomChannelsApiNotThrowing(DUMMY_SERVER_FQDN,
                Arrays.asList(customParentChInfo, customChildChInfo));
    }

    @Test
    public void ensureThrowsWhenParentChannelIsCreatedAfter() throws Exception {
        CustomChannelInfoJson customParentChInfo = testUtils.createValidCustomChInfo("parentChannel");

        CustomChannelInfoJson customChildChInfo = testUtils.createValidCustomChInfo("childChannel");
        customChildChInfo.setParentChannelLabel("parentChannel");

        testUtils.checkAddCustomChannelsApiThrows(DUMMY_SERVER_FQDN,
                Arrays.asList(customChildChInfo, customParentChInfo), "No parent channel");
    }

    @Test
    public void ensureThrowsWhenMissingOriginalChannelInClonedChannels() throws Exception {
        CustomChannelInfoJson customChInfo = testUtils.createValidCustomChInfo();

        CustomChannelInfoJson clonedCustomChInfo = testUtils.createValidCustomChInfo("clonedCustomCh");
        clonedCustomChInfo.setOriginalChannelLabel(customChInfo.getLabel() + "MISSING");

        testUtils.checkAddCustomChannelsApiThrows(DUMMY_SERVER_FQDN,
                Arrays.asList(customChInfo, clonedCustomChInfo), "No original channel");
    }

    @Test
    public void ensureNotThrowingWhenOriginalChannelIsCreatedBefore() throws Exception {
        CustomChannelInfoJson customChInfo = testUtils.createValidCustomChInfo("originalCustomCh");

        CustomChannelInfoJson clonedCustomChInfo = testUtils.createValidCustomChInfo("clonedCustomCh");
        clonedCustomChInfo.setOriginalChannelLabel("originalCustomCh");

        testUtils.checkAddCustomChannelsApiNotThrowing(DUMMY_SERVER_FQDN,
                Arrays.asList(customChInfo, clonedCustomChInfo));
    }

    @Test
    public void ensureThrowsWhenOriginalChannelIsCreatedAfter() throws Exception {
        CustomChannelInfoJson customChInfo = testUtils.createValidCustomChInfo("originalCustomCh");

        CustomChannelInfoJson clonedCustomChInfo = testUtils.createValidCustomChInfo("clonedCustomCh");
        clonedCustomChInfo.setOriginalChannelLabel("originalCustomCh");

        testUtils.checkAddCustomChannelsApiThrows(DUMMY_SERVER_FQDN,
                Arrays.asList(clonedCustomChInfo, customChInfo), "No original channel");
    }

    @Test
    public void checkConversion() throws Exception {
        User localUser = UserTestUtils.findNewUser("local_user_", "local_org_", true);
        User peripheralUser = UserTestUtils.findNewUser("peripheral_user_", "peripheral_org_", true);
        ChannelSoftwareHandler channelSoftwareHandler = new ChannelSoftwareHandler(null, null);
        ProductName pn = ChannelFactoryTest.lookupOrCreateProductName(ChannelManager.RHEL_PRODUCT_NAME);
        ChannelProduct cp = ErrataTestUtils.createTestChannelProduct();

        //create vendor channels
        String vendorBaseChannelTemplateName = "SLES11-SP3-Pool for x86_64";
        String vendorBaseChannelTemplateLabel = "sles11-sp3-pool-x86_64";
        Channel vendorBaseCh = testUtils.createVendorBaseChannel(vendorBaseChannelTemplateName,
                vendorBaseChannelTemplateLabel);

        String vendorChannelTemplateName = "SLES11-SP3-Updates for x86_64";
        String vendorChannelTemplateLabel = "sles11-sp3-updates-x86_64";
        Channel vendorCh = testUtils.createVendorChannel(vendorChannelTemplateName,
                vendorChannelTemplateLabel, vendorBaseCh);
        vendorCh.setProductName(pn);
        vendorCh.setProduct(cp);
        vendorCh.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha512"));

        //test channel: clone of vendorChannel
        int testChId = channelSoftwareHandler.clone(localUser, vendorCh.getLabel(), new HashMap<>(), true);
        Channel testCh = ChannelFactory.lookupById((long) testChId);
        testCh.setProduct(vendorCh.getProduct());

        //production channel: clone of test channel
        int productionChId = channelSoftwareHandler.clone(localUser, testCh.getLabel(), new HashMap<>(), true);
        Channel productionCh = ChannelFactory.lookupById((long) productionChId);
        productionCh.setProduct(testCh.getProduct());

        CustomChannelInfoJson testChInfo = ChannelFactory.toCustomChannelInfo(testCh,
                peripheralUser.getOrg().getId(), Optional.empty());

        assertEquals(localUser.getOrg().getId(), testCh.getOrg().getId());
        assertEquals(peripheralUser.getOrg().getId(), testChInfo.getPeripheralOrgId());
        assertEquals("clone-of-sles11-sp3-updates-x86_64", testChInfo.getLabel());
        assertNull(testChInfo.getParentChannelLabel());
        assertEquals(ChannelManager.RHEL_PRODUCT_NAME, testChInfo.getProductNameLabel());
        assertEquals(vendorCh.getProduct().getProduct(), testChInfo.getChannelProductProduct());
        assertEquals(vendorCh.getProduct().getVersion(), testChInfo.getChannelProductVersion());
        assertEquals("sha512", testChInfo.getChecksumTypeLabel());
        assertEquals("sles11-sp3-updates-x86_64", testChInfo.getOriginalChannelLabel());

        CustomChannelInfoJson productionChInfo = ChannelFactory.toCustomChannelInfo(productionCh,
                peripheralUser.getOrg().getId(), Optional.empty());

        assertEquals(localUser.getOrg().getId(), productionCh.getOrg().getId());
        assertEquals(peripheralUser.getOrg().getId(), productionChInfo.getPeripheralOrgId());
        assertEquals("clone-of-clone-of-sles11-sp3-updates-x86_64", productionChInfo.getLabel());
        assertNull(productionChInfo.getParentChannelLabel());
        assertEquals(ChannelManager.RHEL_PRODUCT_NAME, productionChInfo.getProductNameLabel());
        assertEquals(vendorCh.getProduct().getProduct(), productionChInfo.getChannelProductProduct());
        assertEquals(vendorCh.getProduct().getVersion(), productionChInfo.getChannelProductVersion());
        assertEquals("sha512", productionChInfo.getChecksumTypeLabel());
        assertEquals("clone-of-sles11-sp3-updates-x86_64", productionChInfo.getOriginalChannelLabel());
    }
}
