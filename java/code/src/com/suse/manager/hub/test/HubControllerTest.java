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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.ConnectionManager;
import com.redhat.rhn.common.hibernate.ConnectionManagerFactory;
import com.redhat.rhn.common.hibernate.ReportDbHibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.channel.test.ChannelFamilyFactoryTest;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.taskomatic.task.ReportDBHelper;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.hub.HubController;
import com.suse.manager.model.hub.ChannelInfoJson;
import com.suse.manager.model.hub.IssRole;
import com.suse.manager.model.hub.ManagerInfoJson;
import com.suse.manager.model.hub.OrgInfoJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.token.IssTokenBuilder;
import com.suse.manager.webui.utils.token.Token;
import com.suse.utils.Json;

import com.google.gson.JsonObject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import spark.route.HttpMethod;

public class HubControllerTest extends JMockBaseTestCaseWithUser {

    private static final String DUMMY_SERVER_FQDN = "dummy-server.unit-test.local";

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        HubController dummyHubController = new HubController();
        dummyHubController.initRoutes();
    }

    private String createTestUserName() {
        return "testUser" + TestUtils.randomString();
    }

    private String createTestPassword() {
        return "testPassword" + TestUtils.randomString();
    }

    private void createReportDbUser(String testReportDbUserName, String testReportDbPassword) {
        String dbname = Config.get().getString(ConfigDefaults.REPORT_DB_NAME, "");
        ConnectionManager localRcm = ConnectionManagerFactory.localReportingConnectionManager();
        ReportDbHibernateFactory localRh = new ReportDbHibernateFactory(localRcm);
        ReportDBHelper dbHelper = ReportDBHelper.INSTANCE;

        dbHelper.createDBUser(localRh.getSession(), dbname, testReportDbUserName, testReportDbPassword);
        localRcm.commitTransaction();
    }

    private boolean existsReportDbUser(String testReportDbUserName) {
        ConnectionManager localRcm = ConnectionManagerFactory.localReportingConnectionManager();
        ReportDbHibernateFactory localRh = new ReportDbHibernateFactory(localRcm);
        ReportDBHelper dbHelper = ReportDBHelper.INSTANCE;

        return dbHelper.hasDBUser(localRh.getSession(), testReportDbUserName);
    }

    private void cleanupReportDbUser(String testReportDbUserName) {
        ConnectionManager localRcm = ConnectionManagerFactory.localReportingConnectionManager();
        ReportDbHibernateFactory localRh = new ReportDbHibernateFactory(localRcm);
        ReportDBHelper dbHelper = ReportDBHelper.INSTANCE;

        dbHelper.dropDBUser(localRh.getSession(), testReportDbUserName);
        localRcm.commitTransaction();
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
                Arguments.of(HttpMethod.post, "/hub/addVendorChannels", IssRole.PERIPHERAL)
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
        ControllerTestUtils utils = new ControllerTestUtils();

        assertThrows(IllegalStateException.class, () ->
                        utils.withServerFqdn(DUMMY_SERVER_FQDN)
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
        ControllerTestUtils utils = new ControllerTestUtils();

        assertThrows(IllegalStateException.class, () ->
                        utils.withServerFqdn(DUMMY_SERVER_FQDN)
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
        ControllerTestUtils utils = new ControllerTestUtils();

        String answerKO = (String) utils.withServerFqdn(DUMMY_SERVER_FQDN)
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
        ControllerTestUtils utils = new ControllerTestUtils();

        String answerKO = (String) utils.withServerFqdn(DUMMY_SERVER_FQDN)
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
        ControllerTestUtils utils = new ControllerTestUtils();

        try {
            utils.withServerFqdn(DUMMY_SERVER_FQDN)
                    .withApiEndpoint(apiEndpoint)
                    .withHttpMethod(apiMethod)
                    .withRole(apiRole)
                    .simulateControllerApiCall();

            Assertions.fail(apiEndpoint + " API call should have failed without token");
        }
        catch (spark.HaltException ex) {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.statusCode());
        }
    }

    @Test
    public void checkPingApiEndpoint() throws Exception {
        String apiUnderTest = "/hub/ping";

        ControllerTestUtils utils = new ControllerTestUtils();
        String answer = (String) utils.withServerFqdn(DUMMY_SERVER_FQDN)
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

        ControllerTestUtils utils = new ControllerTestUtils();
        String answer = (String) utils.withServerFqdn(ConfigDefaults.get().getHostname())
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

        String testUserName = createTestUserName();
        String testPassword = createTestPassword();
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("username", testUserName);
        bodyMap.put("password", testPassword);

        ControllerTestUtils utils = new ControllerTestUtils();
        String answer = (String) utils.withServerFqdn(ConfigDefaults.get().getHostname())
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

        ControllerTestUtils utils = new ControllerTestUtils();
        String answer = (String) utils.withServerFqdn(DUMMY_SERVER_FQDN)
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

        String testReportDbUserName = createTestUserName();
        String testReportDbPassword = createTestPassword();
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("username", testReportDbUserName);
        bodyMap.put("password", testReportDbPassword);

        //check there is no user with that username
        assertFalse(existsReportDbUser(testReportDbUserName));

        ControllerTestUtils utils = new ControllerTestUtils();
        String answer = (String) utils.withServerFqdn(DUMMY_SERVER_FQDN)
                .withApiEndpoint(apiUnderTest)
                .withHttpMethod(HttpMethod.post)
                .withRole(IssRole.HUB)
                .withBearerTokenInHeaders()
                .withBody(bodyMap)
                .simulateControllerApiCall();
        JsonObject jsonObj = Json.GSON.fromJson(answer, JsonObject.class);

        assertTrue(jsonObj.get("success").getAsBoolean(), apiUnderTest + " API call is failing");

        //check there is one user with that username
        assertTrue(existsReportDbUser(testReportDbUserName),
                apiUnderTest + " API reports no user " + testReportDbUserName);

        //cleanup
        cleanupReportDbUser(testReportDbUserName);
        assertFalse(existsReportDbUser(testReportDbUserName),
                "cleanup of user not working for user " + testReportDbUserName);
    }

    @Test
    public void checkRemoveReportDbCredentialsApiEndpoint() throws Exception {
        String apiUnderTest = "/hub/removeReportDbCredentials";

        String testReportDbUserName = createTestUserName();
        String testReportDbPassword = "testPassword" + TestUtils.randomString();
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("username", testReportDbUserName);

        //create a user
        createReportDbUser(testReportDbUserName, testReportDbPassword);
        assertTrue(existsReportDbUser(testReportDbUserName),
                "failed creation of user " + testReportDbUserName);

        ControllerTestUtils utils = new ControllerTestUtils();
        String answer = (String) utils.withServerFqdn(DUMMY_SERVER_FQDN)
                .withApiEndpoint(apiUnderTest)
                .withHttpMethod(HttpMethod.post)
                .withRole(IssRole.HUB)
                .withBearerTokenInHeaders()
                .withBody(bodyMap)
                .simulateControllerApiCall();
        JsonObject jsonObj = Json.GSON.fromJson(answer, JsonObject.class);

        assertTrue(jsonObj.get("success").getAsBoolean(), apiUnderTest + " API call is failing");
        //check the user is gone
        assertFalse(existsReportDbUser(testReportDbUserName),
                apiUnderTest + " API call fails to remove user " + testReportDbUserName);
    }

    @Test
    public void checkApiListAllPeripheralOrgs() throws Exception {
        String apiUnderTest = "/hub/listAllPeripheralOrgs";

        Org org1 = UserTestUtils.findNewOrg("org1");
        Org org2 = UserTestUtils.findNewOrg("org2");
        Org org3 = UserTestUtils.findNewOrg("org3");

        ControllerTestUtils utils = new ControllerTestUtils();
        String answer = (String) utils.withServerFqdn(DUMMY_SERVER_FQDN)
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

        ControllerTestUtils utils = new ControllerTestUtils();
        String answer = (String) utils.withServerFqdn(DUMMY_SERVER_FQDN)
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

    private Channel utilityCreateVendorBaseChannel(String name, String label) throws Exception {
        Org nullOrg = null;
        ChannelFamily cfam = ChannelFamilyFactoryTest.createNullOrgTestChannelFamily();
        String query = "ChannelArch.findById";
        ChannelArch arch = (ChannelArch) TestUtils.lookupFromCacheById(500L, query);
        return ChannelFactoryTest.createTestChannel(name, label, nullOrg, arch, cfam);
    }

    private void utilityCreateVendorChannel(String name, String label, Channel vendorBaseChannel) throws Exception {
        Channel vendorChannel = utilityCreateVendorBaseChannel(name, label);
        vendorChannel.setParentChannel(vendorBaseChannel);
        ChannelFactory.save(vendorChannel);
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

        //SUSE Linux Enterprise Server 11 SP3 x86_64
        String vendorChannelTemplateName = "SLES11-SP3-Updates for x86_64";
        String vendorChannelTemplateLabel = "sles11-sp3-updates-x86_64";

        SUSEProductTestUtils.createVendorSUSEProductEnvironment(user, null, true);

        int expectedNumOfPeripheralCreatedChannels = 2;
        Channel vendorBaseChannel = null;
        if (baseChannelAlreadyPresentInPeripheral) {
            vendorBaseChannel = utilityCreateVendorBaseChannel(vendorBaseChannelTemplateName,
                    vendorBaseChannelTemplateLabel);
            expectedNumOfPeripheralCreatedChannels--;
        }
        if (channelAlreadyPresentInPeripheral) {
            utilityCreateVendorChannel(vendorChannelTemplateName, vendorChannelTemplateLabel, vendorBaseChannel);
            expectedNumOfPeripheralCreatedChannels--;
        }

        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("vendorchannellabellist", Json.GSON.toJson(List.of(vendorChannelTemplateLabel)));

        ControllerTestUtils utils = new ControllerTestUtils();
        String answer = (String) utils.withServerFqdn(DUMMY_SERVER_FQDN)
                .withApiEndpoint(apiUnderTest)
                .withHttpMethod(HttpMethod.post)
                .withRole(IssRole.PERIPHERAL)
                .withBearerTokenInHeaders()
                .withBody(bodyMap)
                .simulateControllerApiCall();
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
}
