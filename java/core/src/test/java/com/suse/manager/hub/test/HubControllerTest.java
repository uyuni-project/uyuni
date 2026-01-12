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
import com.redhat.rhn.domain.access.AccessGroupFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.channel.ClonedChannel;
import com.redhat.rhn.domain.channel.ProductName;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.domain.iss.IssMaster;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.ErrataTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.hub.HubController;
import com.suse.manager.hub.HubManager;
import com.suse.manager.model.hub.ChannelInfoDetailsJson;
import com.suse.manager.model.hub.ChannelInfoJson;
import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssAccessToken;
import com.suse.manager.model.hub.IssHub;
import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.model.hub.IssPeripheralChannels;
import com.suse.manager.model.hub.IssRole;
import com.suse.manager.model.hub.ManagerInfoJson;
import com.suse.manager.model.hub.OrgInfoJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.token.IssTokenBuilder;
import com.suse.manager.webui.utils.token.Token;
import com.suse.utils.Json;

import com.google.gson.JsonObject;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
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

import jakarta.servlet.http.HttpServletResponse;
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
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        context().checking(new Expectations() {{
            allowing(taskomaticMock).scheduleProductRefresh(with(any(Date.class)), with(false));
        }});

        HubController dummyHubController = new HubController(new HubManager(), taskomaticMock);
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
                Arguments.of(HttpMethod.post, "/hub/scheduleProductRefresh", IssRole.HUB),
                Arguments.of(HttpMethod.get, "/hub/managerinfo", IssRole.HUB),
                Arguments.of(HttpMethod.post, "/hub/storeReportDbCredentials", IssRole.HUB),
                Arguments.of(HttpMethod.post, "/hub/removeReportDbCredentials", IssRole.HUB),
                Arguments.of(HttpMethod.get, "/hub/listAllPeripheralOrgs", IssRole.HUB),
                Arguments.of(HttpMethod.get, "/hub/listAllPeripheralChannels", IssRole.HUB),
                Arguments.of(HttpMethod.post, "/hub/syncChannels", IssRole.HUB),
                Arguments.of(HttpMethod.post, "/hub/sync/channelfamilies", IssRole.HUB),
                Arguments.of(HttpMethod.post, "/hub/sync/products", IssRole.HUB),
                Arguments.of(HttpMethod.post, "/hub/sync/repositories", IssRole.HUB),
                Arguments.of(HttpMethod.post, "/hub/sync/subscriptions", IssRole.HUB),
                Arguments.of(HttpMethod.post, "/hub/sync/migrate/v1/deleteMaster", IssRole.HUB)
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

    private static boolean noHubApis() {
        return onlyHubApis().findAny().isEmpty();
    }

    private static Stream<Arguments> onlyPeripheralApis() {
        return allApiEndpoints().filter(e -> (IssRole.PERIPHERAL == e.get()[2]));
    }

    private static boolean noPeripheralApis() {
        return onlyPeripheralApis().findAny().isEmpty();
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
    @DisabledIf(value = "noHubApis", disabledReason = "No API can be called only from a Hub")
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
    @DisabledIf(value = "noPeripheralApis", disabledReason = "No API can be called only from a Peripheral")
    public void ensurePeripheralApisNotWorkingWithHub(HttpMethod apiMethod, String apiEndpoint/*, IssRole apiRole*/)
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
                .withBody(Json.GSON.toJson(bodyMap, Map.class))
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
                .withBody(Json.GSON.toJson(bodyMap, Map.class))
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
                .withBody(Json.GSON.toJson(bodyMap, Map.class))
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
    public void checkScheduleProductRefreshEndpoint() throws Exception {
        String apiUnderTest = "/hub/scheduleProductRefresh";

        String answer = (String) testUtils.withServerFqdn(DUMMY_SERVER_FQDN)
                .withApiEndpoint(apiUnderTest)
                .withHttpMethod(HttpMethod.post)
                .withRole(IssRole.HUB)
                .withBearerTokenInHeaders()
                .simulateControllerApiCall();
        JsonObject jsonObj = Json.GSON.fromJson(answer, JsonObject.class);

        assertTrue(jsonObj.get("success").getAsBoolean(), apiUnderTest + " API call is failing");
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
                .withBody(Json.GSON.toJson(bodyMap, Map.class))
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

        Org org1 = UserTestUtils.createOrg("org1");
        Org org2 = UserTestUtils.createOrg("org2");
        Org org3 = UserTestUtils.createOrg("org3");

        String answer = (String) testUtils.withServerFqdn(DUMMY_SERVER_FQDN)
                .withApiEndpoint(apiUnderTest)
                .withHttpMethod(HttpMethod.get)
                .withRole(IssRole.HUB)
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

        user.addToGroup(AccessGroupFactory.CHANNEL_ADMIN);
        Channel testBaseChannel = ChannelTestUtils.createBaseChannel(user);
        Channel testChildChannel = ChannelTestUtils.createChildChannel(user, testBaseChannel);

        String answer = (String) testUtils.withServerFqdn(DUMMY_SERVER_FQDN)
                .withApiEndpoint(apiUnderTest)
                .withHttpMethod(HttpMethod.get)
                .withRole(IssRole.HUB)
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

    private static Stream<Arguments> allBaseAndVendorChannelAlreadyPresentCombinations() {
        return Stream.of(Arguments.of(false, false),
                Arguments.of(true, false),
                Arguments.of(true, true)
        );
    }

    @ParameterizedTest
    @MethodSource("allBaseAndVendorChannelAlreadyPresentCombinations")
    public void checkApiSyncChannel(boolean baseChannelAlreadyPresentInPeripheral,
                                    boolean channelAlreadyPresentInPeripheral) throws Exception {
        //SUSE Linux Enterprise Server 15 SP7 x86_64
        String vendorBaseChannelTemplateName = "SLES15-SP7-Pool for x86_64";
        String vendorBaseChannelTemplateLabel = "sles15-sp7-pool-x86_64";
        String vendorChannelTemplateName = "SLES15-SP7-Updates for x86_64";
        String vendorChannelTemplateLabel = "sles15-sp7-updates-x86_64";
        String peripheralFQDN = "peripheral.example.com";
        boolean testIsGpgCheck = true;
        boolean testIssInstallerUpdates = false;
        String testArchLabel = "channel-x86_64";
        String testChecksumLabel = "sha256";

        Date endOfLifeDate = testUtils.createDateUtil(2096, 10, 22);

        HubFactory hubFactory = new HubFactory();
        IssPeripheral peripheral = new IssPeripheral(peripheralFQDN, "");
        hubFactory.save(peripheral);

        //create peripheral vendor Channels
        List<ChannelInfoDetailsJson> vendorChannelInfoListIn = new ArrayList<>();
        Channel vendorCh = null;
        if (baseChannelAlreadyPresentInPeripheral) {
            vendorCh = testUtils.createVendorBaseChannel(vendorBaseChannelTemplateName, vendorBaseChannelTemplateLabel);
            IssPeripheralChannels issCh = new IssPeripheralChannels(peripheral, vendorCh);
            hubFactory.save(issCh);
        }
        else {
            ChannelInfoDetailsJson vendorBaseChInfo = testUtils.createChannelInfoDetailsJson(null,
                    vendorBaseChannelTemplateLabel, "", "",
                    testIsGpgCheck, testIssInstallerUpdates, testArchLabel, testChecksumLabel, endOfLifeDate);
            vendorBaseChInfo.setName(vendorBaseChannelTemplateName);
            vendorChannelInfoListIn.add(vendorBaseChInfo);
        }
        if (channelAlreadyPresentInPeripheral) {
            Channel ch = testUtils.createVendorChannel(vendorChannelTemplateName, vendorChannelTemplateLabel, vendorCh);
            IssPeripheralChannels issCh = new IssPeripheralChannels(peripheral, ch);
            hubFactory.save(issCh);
        }
        else {
            ChannelInfoDetailsJson vendorChInfo = testUtils.createChannelInfoDetailsJson(null,
                    vendorChannelTemplateLabel, vendorBaseChannelTemplateLabel, "",
                    testIsGpgCheck, testIssInstallerUpdates, testArchLabel, testChecksumLabel, endOfLifeDate);
            vendorChInfo.setName(vendorChannelTemplateName);
            vendorChannelInfoListIn.add(vendorChInfo);
        }
        vendorChannelInfoListIn.addAll(hubFactory.listChannelInfoForPeripheral(peripheral));

        String answer = (String) testUtils.testSyncChannelsApiCall(DUMMY_SERVER_FQDN, vendorChannelInfoListIn);
        ResultJson<?> result = Json.GSON.fromJson(answer, ResultJson.class);
        assertTrue(result.isSuccess(), "Failed: " + answer);

        Channel baseChannel = ChannelFactory.lookupByLabel(vendorBaseChannelTemplateLabel);
        assertNotNull(baseChannel);
        Channel childChannel = ChannelFactory.lookupByLabel(vendorChannelTemplateLabel);
        assertNotNull(childChannel);

        assertEquals(1, baseChannel.getSources().size());
        assertEquals(1, childChannel.getSources().size());

        if (!baseChannel.getSources().stream().allMatch(cs -> cs.getSourceUrl().contains("?"))) {
            fail("Content Source URL is missing a token for Channel " + baseChannel);
        }
        if (!childChannel.getSources().stream().allMatch(cs -> cs.getSourceUrl().contains("?"))) {
            fail("Content Source URL is missing a token for Channel " + childChannel);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void checkApiSyncCustomChannel(boolean testIncludeTestChannelInChain) throws Exception {
        boolean testIsGpgCheck = true;
        boolean testIssInstallerUpdates = true;
        String testArchLabel = "channel-x86_64";
        String testChecksumLabel = "sha512";

        Date endOfLifeDate = testUtils.createDateUtil(2096, 10, 22);

        User testPeripheralUser = new UserTestUtils.UserBuilder()
                .userName("peripheral_user_")
                .orgName("peripheral_org_")
                .orgAdmin(true)
                .build();
        Org testPeripheralOrg = testPeripheralUser.getOrg();
        Long testPeripheralOrgId = testPeripheralOrg.getId();

        // vendorBaseCh -> cloneBaseCh
        //  ^                ^
        // vendorCh -> cloneDevelCh -> cloneTestCh -> cloneProdCh

        // prepare inputs
        String vendorBaseChannelTemplateLabel = "sles11-sp3-pool-x86_64"; //SUSE Linux Enterprise Server 11 SP3 x86_64
        String vendorChannelTemplateLabel = "sles11-sp3-updates-x86_64";

        ChannelInfoDetailsJson vendorBaseChInfo = testUtils.createChannelInfoDetailsJson(null,
                vendorBaseChannelTemplateLabel, "", "",
                testIsGpgCheck, testIssInstallerUpdates, testArchLabel, testChecksumLabel, endOfLifeDate);

        ChannelInfoDetailsJson vendorChInfo = testUtils.createChannelInfoDetailsJson(null,
                vendorChannelTemplateLabel, vendorBaseChannelTemplateLabel, "",
                testIsGpgCheck, testIssInstallerUpdates, testArchLabel, testChecksumLabel, endOfLifeDate);


        //create peripheral vendor Channels
        List<ChannelInfoDetailsJson> vendorChannelInfoListIn = new ArrayList<>();
        vendorChannelInfoListIn.add(vendorBaseChInfo);
        vendorChannelInfoListIn.add(vendorChInfo);

        String answer = (String) testUtils.testSyncChannelsApiCall(DUMMY_SERVER_FQDN, vendorChannelInfoListIn);
        ResultJson<?> result = Json.GSON.fromJson(answer, ResultJson.class);
        assertTrue(result.isSuccess(), "Failed: " + answer);

        Channel vendorBaseCh = ChannelFactory.lookupByLabel(vendorBaseChannelTemplateLabel);
        assertNotNull(vendorBaseCh);
        Channel vendorCh = ChannelFactory.lookupByLabel(vendorChannelTemplateLabel);
        assertNotNull(vendorCh);


        ChannelInfoDetailsJson cloneBaseChInfo = testUtils.createChannelInfoDetailsJson(testPeripheralOrgId,
                "cloneBaseCh", "", vendorBaseChannelTemplateLabel,
                testIsGpgCheck, testIssInstallerUpdates, testArchLabel, testChecksumLabel, endOfLifeDate);

        ChannelInfoDetailsJson cloneDevelChInfo = testUtils.createChannelInfoDetailsJson(testPeripheralOrgId,
                "cloneDevelCh", "cloneBaseCh", vendorChannelTemplateLabel,
                testIsGpgCheck, testIssInstallerUpdates, testArchLabel, testChecksumLabel, endOfLifeDate);

        ChannelInfoDetailsJson cloneTestChInfo = null;
        String originalOfProdCh = "cloneDevelCh";
        if (testIncludeTestChannelInChain) {
            cloneTestChInfo = testUtils.createChannelInfoDetailsJson(testPeripheralOrgId,
                    "cloneTestCh", "", "cloneDevelCh",
                    testIsGpgCheck, testIssInstallerUpdates, testArchLabel, testChecksumLabel, endOfLifeDate);
            originalOfProdCh = "cloneTestCh";
        }

        ChannelInfoDetailsJson cloneProdChInfo = testUtils.createChannelInfoDetailsJson(testPeripheralOrgId,
                "cloneProdCh", "", originalOfProdCh,
                testIsGpgCheck, testIssInstallerUpdates, testArchLabel, testChecksumLabel, endOfLifeDate);



        //create peripheral vendorCh custom cloned channels
        List<ChannelInfoDetailsJson> customChannelInfoListIn = new ArrayList<>(vendorChannelInfoListIn);
        customChannelInfoListIn.add(cloneBaseChInfo);
        customChannelInfoListIn.add(cloneDevelChInfo);
        if (testIncludeTestChannelInChain) {
            customChannelInfoListIn.add(cloneTestChInfo);
        }
        customChannelInfoListIn.add(cloneProdChInfo);

        answer = (String) testUtils.testSyncChannelsApiCall(DUMMY_SERVER_FQDN, customChannelInfoListIn);
        result = Json.GSON.fromJson(answer, ResultJson.class);
        assertTrue(result.isSuccess(), "Failed: " + answer);

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

        assertEquals(vendorBaseCh, cloneBaseCh.asCloned().map(ClonedChannel::getOriginal).orElseThrow());
        assertEquals(vendorCh, cloneDevelCh.asCloned().map(ClonedChannel::getOriginal).orElseThrow());
        if (testIncludeTestChannelInChain) {
            assertEquals(cloneDevelCh, cloneTestCh.asCloned().map(ClonedChannel::getOriginal).orElseThrow());
            assertEquals(cloneTestCh, cloneProdCh.asCloned().map(ClonedChannel::getOriginal).orElseThrow());
        }
        else {
            assertEquals(cloneDevelCh, cloneProdCh.asCloned().map(ClonedChannel::getOriginal).orElseThrow());
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
        ChannelInfoDetailsJson customChInfo = testUtils.createValidCustomChInfo();

        testUtils.checkSyncChannelsApiNotThrowing(DUMMY_SERVER_FQDN, List.of(customChInfo));
    }

    @Test
    public void ensureThrowsWhenMissingPeriperhalOrg() throws Exception {
        ChannelInfoDetailsJson customChInfo = testUtils.createValidCustomChInfo();
        customChInfo.setPeripheralOrgId(75842L);

        testUtils.checkSyncChannelsApiThrows(DUMMY_SERVER_FQDN, List.of(customChInfo), "No org id");
    }

    @Test
    public void ensureThrowsWhenMissingChannelArch() throws Exception {
        ChannelInfoDetailsJson customChInfo = testUtils.createValidCustomChInfo();
        customChInfo.setChannelArchLabel("channel-dummy-arch");

        testUtils.checkSyncChannelsApiThrows(DUMMY_SERVER_FQDN, List.of(customChInfo), "No channel arch");
    }

    @Test
    public void ensureThrowsWhenMissingChecksumType() throws Exception {
        ChannelInfoDetailsJson customChInfo = testUtils.createValidCustomChInfo();
        customChInfo.setChecksumTypeLabel("sha123456");

        testUtils.checkSyncChannelsApiThrows(DUMMY_SERVER_FQDN, List.of(customChInfo), "No checksum type");
    }

    @Test
    public void ensureThrowsWhenMissingParentChannel() throws Exception {
        ChannelInfoDetailsJson customChInfo = testUtils.createValidCustomChInfo();
        customChInfo.setParentChannelLabel("missingParentChannelLabel");

        testUtils.checkSyncChannelsApiThrows(DUMMY_SERVER_FQDN, List.of(customChInfo),
                "Information about the parent channel");
    }

    @Test
    public void ensureNotThrowingWhenParentChannelIsCreatedBefore() throws Exception {
        ChannelInfoDetailsJson customParentChInfo = testUtils.createValidCustomChInfo("parentChannel");

        ChannelInfoDetailsJson customChildChInfo = testUtils.createValidCustomChInfo("childChannel");
        customChildChInfo.setParentChannelLabel("parentChannel");

        testUtils.checkSyncChannelsApiNotThrowing(DUMMY_SERVER_FQDN,
                Arrays.asList(customParentChInfo, customChildChInfo));
    }

    @Test
    public void ensureNotThrowingWhenParentChannelIsCreatedAfter() throws Exception {
        ChannelInfoDetailsJson customParentChInfo = testUtils.createValidCustomChInfo("parentChannel");

        ChannelInfoDetailsJson customChildChInfo = testUtils.createValidCustomChInfo("childChannel");
        customChildChInfo.setParentChannelLabel("parentChannel");

        testUtils.checkSyncChannelsApiNotThrowing(DUMMY_SERVER_FQDN,
                Arrays.asList(customChildChInfo, customParentChInfo));
    }

    @Test
    public void ensureThrowsWhenMissingOriginalChannelInClonedChannels() throws Exception {
        ChannelInfoDetailsJson customChInfo = testUtils.createValidCustomChInfo();

        ChannelInfoDetailsJson clonedCustomChInfo = testUtils.createValidCustomChInfo("clonedCustomCh");
        clonedCustomChInfo.setOriginalChannelLabel(customChInfo.getLabel() + "MISSING");

        testUtils.checkSyncChannelsApiThrows(DUMMY_SERVER_FQDN,
                Arrays.asList(customChInfo, clonedCustomChInfo), "Information about the original channel");
    }

    @Test
    public void ensureNotThrowingWhenOriginalChannelIsCreatedBefore() throws Exception {
        ChannelInfoDetailsJson customChInfo = testUtils.createValidCustomChInfo("originalCustomCh");

        ChannelInfoDetailsJson clonedCustomChInfo = testUtils.createValidCustomChInfo("clonedCustomCh");
        clonedCustomChInfo.setOriginalChannelLabel("originalCustomCh");

        testUtils.checkSyncChannelsApiNotThrowing(DUMMY_SERVER_FQDN,
                Arrays.asList(customChInfo, clonedCustomChInfo));
    }

    @Test
    public void ensureNotThrowingWhenOriginalChannelIsCreatedAfter() throws Exception {
        ChannelInfoDetailsJson customChInfo = testUtils.createValidCustomChInfo("originalCustomCh");

        ChannelInfoDetailsJson clonedCustomChInfo = testUtils.createValidCustomChInfo("clonedCustomCh");
        clonedCustomChInfo.setOriginalChannelLabel("originalCustomCh");

        testUtils.checkSyncChannelsApiNotThrowing(DUMMY_SERVER_FQDN,
                Arrays.asList(clonedCustomChInfo, customChInfo));
    }

    @Test
    public void checkConversion() throws Exception {
        User localUser = new UserTestUtils.UserBuilder()
                .userName("local_user_")
                .orgName("local_org_")
                .orgAdmin(true)
                .build();
        User peripheralUser = new UserTestUtils.UserBuilder()
                .userName("peripheral_user_")
                .orgName("peripheral_org_")
                .orgAdmin(true)
                .build();
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

        ChannelInfoDetailsJson testChInfo = ChannelFactory.toChannelInfo(testCh,
                peripheralUser.getOrg().getId(), Optional.empty());

        assertEquals(localUser.getOrg().getId(), testCh.getOrg().getId());
        assertEquals(peripheralUser.getOrg().getId(), testChInfo.getPeripheralOrgId());
        assertEquals("clone-of-sles11-sp3-updates-x86_64", testChInfo.getLabel());
        assertNull(testChInfo.getParentChannelLabel());
        assertEquals(ChannelManager.RHEL_PRODUCT_NAME, testChInfo.getProductNameLabel());
        assertEquals(vendorCh.getProduct().getProduct(), testChInfo.getChannelProductProduct());
        assertEquals(vendorCh.getProduct().getVersion(), testChInfo.getChannelProductVersion());
        assertEquals("sha512", testChInfo.getChecksumTypeLabel());
        assertNull(testChInfo.getOriginalChannelLabel());

        ChannelInfoDetailsJson productionChInfo = ChannelFactory.toChannelInfo(productionCh,
                peripheralUser.getOrg().getId(), Optional.of("sles11-sp3-updates-x86_64"));

        assertEquals(localUser.getOrg().getId(), productionCh.getOrg().getId());
        assertEquals(peripheralUser.getOrg().getId(), productionChInfo.getPeripheralOrgId());
        assertEquals("clone-of-clone-of-sles11-sp3-updates-x86_64", productionChInfo.getLabel());
        assertNull(productionChInfo.getParentChannelLabel());
        assertEquals(ChannelManager.RHEL_PRODUCT_NAME, productionChInfo.getProductNameLabel());
        assertEquals(vendorCh.getProduct().getProduct(), productionChInfo.getChannelProductProduct());
        assertEquals(vendorCh.getProduct().getVersion(), productionChInfo.getChannelProductVersion());
        assertEquals("sha512", productionChInfo.getChecksumTypeLabel());
        assertEquals("sles11-sp3-updates-x86_64", productionChInfo.getOriginalChannelLabel());
    }

    @Test
    public void checkModifyCustomChannels() throws Exception {
        // cloneDevelCh -> cloneTestCh -> cloneProdCh
        ChannelInfoDetailsJson cloneDevelChInfo = testUtils.createValidCustomChInfo("cloneDevelCh");

        ChannelInfoDetailsJson cloneTestChInfo = testUtils.createValidCustomChInfo("cloneTestCh");
        cloneTestChInfo.setOriginalChannelLabel("cloneDevelCh");

        ChannelInfoDetailsJson cloneProdChInfo = testUtils.createValidCustomChInfo("cloneProdCh");
        cloneProdChInfo.setOriginalChannelLabel("cloneTestCh");

        testUtils.checkSyncChannelsApiNotThrowing(DUMMY_SERVER_FQDN,
                Arrays.asList(cloneDevelChInfo, cloneTestChInfo, cloneProdChInfo));

        Channel cloneDevelCh = ChannelFactory.lookupByLabel("cloneDevelCh");
        assertNotNull(cloneDevelCh);
        Channel cloneTestCh = ChannelFactory.lookupByLabel("cloneTestCh");
        assertNotNull(cloneTestCh);
        Channel cloneProdCh = ChannelFactory.lookupByLabel("cloneProdCh");
        assertNotNull(cloneProdCh);


        Date anotherEndOfLifeDate = testUtils.createDateUtil(2042, 4, 2);

        ChannelInfoDetailsJson modifyInfo = ChannelFactory.toChannelInfo(
                cloneProdCh, cloneProdChInfo.getPeripheralOrgId(), Optional.of("cloneDevelCh"));

        modifyInfo.setBaseDir("baseDir_diff");
        modifyInfo.setName("name_diff");
        modifyInfo.setSummary("summary_diff");
        modifyInfo.setDescription("description_diff");
        modifyInfo.setProductNameLabel("productNameLabel_diff");
        modifyInfo.setGpgCheck(!cloneProdCh.isGPGCheck());
        modifyInfo.setGpgKeyUrl("gpgKeyUrl_diff");
        modifyInfo.setGpgKeyId("gpgKeyId_diff");
        modifyInfo.setGpgKeyFp("gpgKeyFp_diff");
        modifyInfo.setEndOfLifeDate(anotherEndOfLifeDate);

        modifyInfo.setChannelProductProduct("channelProductProduct_diff");
        modifyInfo.setChannelProductVersion("channelProductVersion_diff");
        modifyInfo.setMaintainerName("maintainerName__diff");
        modifyInfo.setMaintainerEmail("maintainerEmail_diff");
        modifyInfo.setMaintainerPhone("maintainerPhone_diff");
        modifyInfo.setSupportPolicy("supportPolicy_diff");
        modifyInfo.setUpdateTag("updateTag_diff");
        modifyInfo.setInstallerUpdates(!cloneProdCh.isInstallerUpdates());

        testUtils.checkDifferentModifications(modifyInfo, cloneProdCh);

        String answer = (String) testUtils.testSyncChannelsApiCall(DUMMY_SERVER_FQDN,
                List.of(cloneDevelChInfo, modifyInfo, cloneTestChInfo));
        ResultJson<?> result = Json.GSON.fromJson(answer, ResultJson.class);
        assertTrue(result.isSuccess(), "Failed: " + answer);

        testUtils.checkEqualModifications(modifyInfo, cloneProdCh);
    }

    @Test
    public void ensureNotThrowingWhenModifyingDataIsValid() throws Exception {
        ChannelInfoDetailsJson modifyInfo = testUtils.createValidCustomChInfo("customCh");
        Org org = OrgFactory.lookupById(modifyInfo.getPeripheralOrgId());
        testUtils.createTestChannel(modifyInfo, org);

        testUtils.checkSyncChannelsApiNotThrowing(DUMMY_SERVER_FQDN, List.of(modifyInfo));
    }

    @Test
    public void ensureThrowsWhenModifyingMissingPeriperhalOrg() throws Exception {
        ChannelInfoDetailsJson modifyInfo = testUtils.createValidCustomChInfo("customCh");
        Org org = OrgFactory.lookupById(modifyInfo.getPeripheralOrgId());
        testUtils.createTestChannel(modifyInfo, org);

        modifyInfo.setPeripheralOrgId(75842L);

        testUtils.checkSyncChannelsApiThrows(DUMMY_SERVER_FQDN, List.of(modifyInfo), "No org id");
    }

    @Test
    public void ensureThrowsWhenModifyingMissingOriginalChannelInClonedChannels() throws Exception {
        ChannelInfoDetailsJson modifyInfo = testUtils.createValidCustomChInfo("customCh");
        Org org = OrgFactory.lookupById(modifyInfo.getPeripheralOrgId());
        testUtils.createTestChannel(modifyInfo, org);

        modifyInfo.setOriginalChannelLabel(modifyInfo.getLabel() + "MISSING");

        testUtils.checkSyncChannelsApiThrows(DUMMY_SERVER_FQDN, List.of(modifyInfo),
                "Information about the original channel");
    }

    @Test
    public void checkDeleteMaster() throws Exception {
        String apiUnderTest = "/hub/sync/migrate/v1/deleteMaster";

        IssMaster master = new IssMaster();
        master.setLabel(DUMMY_SERVER_FQDN);
        master.makeDefaultMaster();
        master.setCaCert("/etc/pki/trust/anchors/master.pem");

        IssFactory.save(master);
        TestUtils.flushAndEvict(master);

        HubFactory hubFactory = new HubFactory();
        IssHub hub = new IssHub(DUMMY_SERVER_FQDN, "");
        hubFactory.save(hub);

        String answer = (String) testUtils.withServerFqdn(DUMMY_SERVER_FQDN)
            .withApiEndpoint(apiUnderTest)
            .withHttpMethod(HttpMethod.post)
            .withRole(IssRole.HUB)
            .withBearerTokenInHeaders()
            .simulateControllerApiCall();

        ResultJson<?> result = Json.GSON.fromJson(answer, ResultJson.class);
        assertTrue(result.isSuccess(), "Failed: " + answer);

        assertNull(IssFactory.lookupMasterByLabel(DUMMY_SERVER_FQDN));
        assertNull(IssFactory.getCurrentMaster());
    }

    @Test
    public void ensureThrowsWhenMasterIsNotFound() throws Exception {
        String apiUnderTest = "/hub/sync/migrate/v1/deleteMaster";

        IssMaster master = new IssMaster();
        // label not matching the hub fqdn
        master.setLabel("iss-master.unit-test.local");
        master.makeDefaultMaster();
        master.setCaCert("/etc/pki/trust/anchors/master.pem");

        IssFactory.save(master);
        TestUtils.flushAndEvict(master);

        String answer = (String) testUtils.withServerFqdn(DUMMY_SERVER_FQDN)
            .withApiEndpoint(apiUnderTest)
            .withHttpMethod(HttpMethod.post)
            .withRole(IssRole.HUB)
            .withBearerTokenInHeaders()
            .simulateControllerApiCall();

        ResultJson<?> result = Json.GSON.fromJson(answer, ResultJson.class);
        assertFalse(result.isSuccess(), "Failed: " + answer);
        assertNotNull(result.getMessages());
        assertEquals(1, result.getMessages().size());
        assertEquals(
            "dummy-server.unit-test.local is not registered as an ISS v1 master",
            result.getMessages().get(0)
        );

        IssMaster reloaded = IssFactory.getCurrentMaster();
        assertNotNull(reloaded);
        assertEquals("iss-master.unit-test.local", reloaded.getLabel());
        assertTrue(reloaded.isDefaultMaster());
        assertEquals("/etc/pki/trust/anchors/master.pem", reloaded.getCaCert());
    }
}
