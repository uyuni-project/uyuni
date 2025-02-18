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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.taskomatic.task.ReportDBHelper;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.SparkTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.model.hub.CustomChannelInfoJson;
import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssHub;
import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.model.hub.IssRole;
import com.suse.manager.model.hub.TokenType;
import com.suse.manager.webui.utils.token.IssTokenBuilder;
import com.suse.manager.webui.utils.token.Token;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.manager.webui.utils.token.TokenParsingException;
import com.suse.utils.Json;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import spark.Request;
import spark.RequestResponseFactory;
import spark.Response;
import spark.RouteImpl;
import spark.route.HttpMethod;
import spark.routematch.RouteMatch;

public class ControllerTestUtils {

    private String apiEndpoint;
    private HttpMethod httpMethod;
    private String serverFqdn;
    private String authBearerToken;
    private Instant authBearerTokenExpiration;
    private IssRole role;
    private boolean addBearerTokenToHeaders;
    private Map<String, String> bodyMap;

    public ControllerTestUtils() {
        apiEndpoint = null;
        httpMethod = null;
        serverFqdn = null;
        authBearerToken = null;
        authBearerTokenExpiration = null;
        role = null;
        addBearerTokenToHeaders = false;
        bodyMap = null;
    }

    public ControllerTestUtils withServerFqdn(String serverFqdnIn)
            throws TokenBuildingException, TokenParsingException {
        serverFqdn = serverFqdnIn;
        Token dummyServerToken = new IssTokenBuilder(serverFqdn).usingServerSecret().build();
        authBearerToken = dummyServerToken.getSerializedForm();
        authBearerTokenExpiration = dummyServerToken.getExpirationTime();
        return this;
    }

    public ControllerTestUtils withApiEndpoint(String apiEndpointIn) {
        apiEndpoint = apiEndpointIn;
        return this;
    }

    public ControllerTestUtils withHttpMethod(HttpMethod httpMethodIn) {
        httpMethod = httpMethodIn;
        return this;
    }

    public ControllerTestUtils withRole(IssRole roleIn) {
        role = roleIn;
        return this;
    }

    public ControllerTestUtils withBearerTokenInHeaders() {
        addBearerTokenToHeaders = true;
        return this;
    }

    public ControllerTestUtils withBody(Map<String, String> bodyMapIn) {
        bodyMap = bodyMapIn;
        return this;
    }

    public Object simulateControllerApiCall() throws Exception {
        HubFactory hubFactory = new HubFactory();
        hubFactory.saveToken(serverFqdn, authBearerToken, TokenType.ISSUED, authBearerTokenExpiration);

        if (null != role) {
            switch (role) {
                case HUB:
                    Optional<IssHub> hub = hubFactory.lookupIssHubByFqdn(serverFqdn);
                    if (hub.isEmpty()) {
                        hubFactory.save(new IssHub(serverFqdn, ""));
                    }
                    break;
                case PERIPHERAL:
                    Optional<IssPeripheral> peripheral = hubFactory.lookupIssPeripheralByFqdn(serverFqdn);
                    if (peripheral.isEmpty()) {
                        hubFactory.save(new IssPeripheral(serverFqdn, ""));
                    }
                    break;
                default:
                    throw new IllegalArgumentException("unsupported role " + role.getLabel());
            }
        }

        String bodyString = (null == bodyMap) ? null : Json.GSON.toJson(bodyMap, Map.class);

        return simulateApiEndpointCall(apiEndpoint, httpMethod,
                addBearerTokenToHeaders ? authBearerToken : null, bodyString);
    }

    public static Object simulateApiEndpointCall(String apiEndpoint, HttpMethod httpMethod,
                                                 String authBearerToken, String body) throws Exception {
        Optional<RouteMatch> routeMatch = spark.Spark.routes()
                .stream()
                .filter(e -> apiEndpoint.equals(e.getMatchUri()))
                .filter(e -> httpMethod.equals(e.getHttpMethod()))
                .findAny();

        if (routeMatch.isEmpty()) {
            throw new IllegalStateException("route not found for " + apiEndpoint);
        }

        RouteImpl routeImpl = (RouteImpl) routeMatch.get().getTarget();

        Map<String, String> httpHeaders = (null == authBearerToken) ?
                new HashMap<>() :
                Map.of("Authorization", "Bearer " + authBearerToken);

        Request dummyTestRequest = (null == body) ?
                SparkTestUtils.createMockRequestWithParams(apiEndpoint, new HashMap<>(), httpHeaders) :
                SparkTestUtils.createMockRequestWithBody(apiEndpoint, httpHeaders, body);

        Response dummyTestResponse = RequestResponseFactory.create(new RhnMockHttpServletResponse());
        return routeImpl.handle(dummyTestRequest, dummyTestResponse);
    }

    public String createTestUserName() {
        return "testUser" + TestUtils.randomString();
    }

    public String createTestPassword() {
        return "testPassword" + TestUtils.randomString();
    }

    public void createReportDbUser(String testReportDbUserName, String testReportDbPassword) {
        String dbname = Config.get().getString(ConfigDefaults.REPORT_DB_NAME, "");
        ConnectionManager localRcm = ConnectionManagerFactory.localReportingConnectionManager();
        ReportDbHibernateFactory localRh = new ReportDbHibernateFactory(localRcm);
        ReportDBHelper dbHelper = ReportDBHelper.INSTANCE;

        dbHelper.createDBUser(localRh.getSession(), dbname, testReportDbUserName, testReportDbPassword);
        localRcm.commitTransaction();
    }

    public boolean existsReportDbUser(String testReportDbUserName) {
        ConnectionManager localRcm = ConnectionManagerFactory.localReportingConnectionManager();
        ReportDbHibernateFactory localRh = new ReportDbHibernateFactory(localRcm);
        ReportDBHelper dbHelper = ReportDBHelper.INSTANCE;

        return dbHelper.hasDBUser(localRh.getSession(), testReportDbUserName);
    }

    public void cleanupReportDbUser(String testReportDbUserName) {
        ConnectionManager localRcm = ConnectionManagerFactory.localReportingConnectionManager();
        ReportDbHibernateFactory localRh = new ReportDbHibernateFactory(localRcm);
        ReportDBHelper dbHelper = ReportDBHelper.INSTANCE;

        dbHelper.dropDBUser(localRh.getSession(), testReportDbUserName);
        localRcm.commitTransaction();
    }

    public Channel createVendorBaseChannel(String name, String label) throws Exception {
        Org nullOrg = null;
        ChannelFamily cfam = ChannelFamilyFactoryTest.createNullOrgTestChannelFamily();
        String query = "ChannelArch.findById";
        ChannelArch arch = (ChannelArch) TestUtils.lookupFromCacheById(500L, query);
        return ChannelFactoryTest.createTestChannel(name, label, nullOrg, arch, cfam);
    }

    public Channel createVendorChannel(String name, String label, Channel vendorBaseChannel) throws Exception {
        Channel vendorChannel = createVendorBaseChannel(name, label);
        vendorChannel.setParentChannel(vendorBaseChannel);
        vendorChannel.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha512"));
        ChannelFactory.save(vendorChannel);
        return vendorChannel;
    }

    public Date createDateUtil(int year, int month, int dayOfMonth) {
        GregorianCalendar cal = new GregorianCalendar(year, month, dayOfMonth);
        return cal.getTime();
    }

    public boolean isNowUtil(Date dateIn) {
        GregorianCalendar cal = new GregorianCalendar();
        Date nowDate = createDateUtil(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        return (dateIn.getTime() - nowDate.getTime() < 24L * 60L * 60L * 1000L);
    }

    public CustomChannelInfoJson createCustomChannelInfoJson(Long orgId,
                                                             String channelLabel,
                                                             String parentChannelLabel,
                                                             String originalChannelLabel,
                                                             boolean isGpgCheck,
                                                             boolean isInstallerUpdates,
                                                             String archLabel,
                                                             String checksumLabel,
                                                             Date endOfLifeDate,
                                                             Date lastSyncedDate) {
        CustomChannelInfoJson info = new CustomChannelInfoJson();

        info.setPeripheralOrgId(orgId);
        info.setParentChannelLabel(parentChannelLabel);
        info.setChannelArchLabel(archLabel);
        info.setLabel(channelLabel);
        info.setBaseDir("baseDir_" + channelLabel);
        info.setName("name_" + channelLabel);
        info.setSummary("summary_" + channelLabel);
        info.setDescription("description_" + channelLabel);
        info.setProductNameLabel("productNameLabel");
        info.setGpgCheck(isGpgCheck);
        info.setGpgKeyUrl("gpgKeyUrl_" + channelLabel);
        info.setGpgKeyId("gpgKeyId");
        info.setGpgKeyFp("gpgKeyFp_" + channelLabel);
        info.setEndOfLifeDate(endOfLifeDate);
        info.setChecksumTypeLabel(checksumLabel);
        info.setLastSyncedDate(lastSyncedDate);
        info.setChannelProductProduct("channelProductProduct");
        info.setChannelProductVersion("channelProductVersion");
        info.setChannelAccess("chAccess"); // max 10
        info.setMaintainerName("maintainerName_" + channelLabel);
        info.setMaintainerEmail("maintainerEmail_" + channelLabel);
        info.setMaintainerPhone("maintainerPhone_" + channelLabel);
        info.setSupportPolicy("supportPolicy_" + channelLabel);
        info.setUpdateTag("updateTag_" + channelLabel);
        info.setInstallerUpdates(isInstallerUpdates);

        info.setOriginalChannelLabel(originalChannelLabel);

        return info;
    }

    public void testCustomChannel(Channel ch, Long peripheralOrgId,
                                  boolean isGpgCheck,
                                  boolean isInstallerUpdates,
                                  String archLabel,
                                  String checksumLabel,
                                  Date endOfLifeDate,
                                  Date lastSyncedDate) {
        ChannelArch testChannelArch = ChannelFactory.findArchByLabel(archLabel);
        String channelLabel = ch.getLabel();

        if (null != peripheralOrgId) {
            assertEquals(peripheralOrgId, ch.getOrg().getId());
        }
        else {
            assertNull(ch.getOrg());
        }

        assertEquals(testChannelArch, ch.getChannelArch());
        assertEquals("baseDir_" + channelLabel, ch.getBaseDir());
        assertEquals("name_" + channelLabel, ch.getName());
        assertEquals("summary_" + channelLabel, ch.getSummary());
        assertEquals("description_" + channelLabel, ch.getDescription());
        assertEquals(isGpgCheck, ch.isGPGCheck());
        assertEquals("gpgKeyUrl_" + channelLabel, ch.getGPGKeyUrl());
        assertEquals("gpgKeyId", ch.getGPGKeyId());
        assertEquals("gpgKeyFp_" + channelLabel, ch.getGPGKeyFp());

        assertEquals(endOfLifeDate, ch.getEndOfLife());
        assertEquals(checksumLabel, ch.getChecksumType().getLabel());
        assertTrue(isNowUtil(ch.getLastModified()));
        assertEquals(lastSyncedDate, ch.getLastSynced());

        assertEquals(lastSyncedDate, ch.getLastSynced());
        assertEquals("chAccess", ch.getAccess());

        assertEquals("maintainerName_" + channelLabel, ch.getMaintainerName());
        assertEquals("maintainerEmail_" + channelLabel, ch.getMaintainerEmail());
        assertEquals("maintainerPhone_" + channelLabel, ch.getMaintainerPhone());

        assertEquals("supportPolicy_" + channelLabel, ch.getSupportPolicy());
        assertEquals("updateTag_" + channelLabel, ch.getUpdateTag());
        assertEquals(isInstallerUpdates, ch.isInstallerUpdates());

        assertEquals("productNameLabel", ch.getProductName().getLabel());
        assertEquals("channelProductProduct", ch.getProduct().getProduct());
        assertEquals("channelProductVersion", ch.getProduct().getVersion());
    }

    public String createTestVendorChannels(User userIn, String serverFqdnIn) throws Exception {

        //SUSE Linux Enterprise Server 11 SP3 x86_64
        return createTestVendorChannels(userIn, serverFqdnIn,
                "SLES11-SP3-Pool for x86_64", "sles11-sp3-pool-x86_64", true,
                "SLES11-SP3-Updates for x86_64", "sles11-sp3-updates-x86_64", true);

    }

    public String createTestVendorChannels(User userIn, String serverFqdnIn,
                                           String vendorBaseChannelTemplateNameIn,
                                           String vendorBaseChannelTemplateLabelIn,
                                           boolean createBaseChannelIn,
                                           String vendorChannelTemplateNameIn,
                                           String vendorChannelTemplateLabelIn,
                                           boolean createChildChannelIn) throws Exception {

        SUSEProductTestUtils.createVendorSUSEProductEnvironment(userIn, null, true);

        Channel vendorBaseChannel = null;
        if (createBaseChannelIn) {
            vendorBaseChannel = createVendorBaseChannel(vendorBaseChannelTemplateNameIn,
                    vendorBaseChannelTemplateLabelIn);
        }
        if (createChildChannelIn) {
            createVendorChannel(vendorChannelTemplateNameIn, vendorChannelTemplateLabelIn, vendorBaseChannel);
        }

        Map<String, String> bodyMapIn = new HashMap<>();
        bodyMapIn.put("vendorchannellabellist", Json.GSON.toJson(List.of(vendorChannelTemplateLabelIn)));

        return (String) withServerFqdn(serverFqdnIn)
                .withApiEndpoint("/hub/addVendorChannels")
                .withHttpMethod(HttpMethod.post)
                .withRole(IssRole.PERIPHERAL)
                .withBearerTokenInHeaders()
                .withBody(bodyMapIn)
                .simulateControllerApiCall();
    }

    public CustomChannelInfoJson createValidCustomChInfo() {
        return createValidCustomChInfo("customCh");
    }

    public CustomChannelInfoJson createValidCustomChInfo(String channelLabel) {
        User testPeripheralUser = UserTestUtils.findNewUser("peripheral_user_", "peripheral_org_", true);
        return createCustomChannelInfoJson(testPeripheralUser.getOrg().getId(),
                channelLabel, "", "",
                true, true, "channel-s390", "sha256",
                createDateUtil(2096, 10, 22), createDateUtil(2025, 4, 30));
    }

    public Object testAddCustomChannelsApiCall(String serverFqdnIn,
                                               List<CustomChannelInfoJson> customChannelInfoListIn) throws Exception {
        String apiUnderTest = "/hub/addCustomChannels";

        Map<String, String> bodyMapIn = new HashMap<>();
        bodyMapIn.put("customchannellist", Json.GSON.toJson(customChannelInfoListIn));

        return withServerFqdn(serverFqdnIn)
                .withApiEndpoint(apiUnderTest)
                .withHttpMethod(HttpMethod.post)
                .withRole(IssRole.PERIPHERAL)
                .withBearerTokenInHeaders()
                .withBody(bodyMapIn)
                .simulateControllerApiCall();
    }

    public void checkAddCustomChannelsApiNotThrowing(String serverFqdnIn,
                                                     List<CustomChannelInfoJson> customChannelInfoListIn)
            throws Exception {

        try {
            testAddCustomChannelsApiCall(serverFqdnIn, customChannelInfoListIn);
            assertTrue(true);
        }
        catch (IllegalArgumentException e) {
            fail("addCustomChannels API not failing when creating peripheral channel ");
        }
    }

    public void checkAddCustomChannelsApiThrows(String serverFqdnIn,
                                                List<CustomChannelInfoJson> customChannelInfoListIn,
                                                String errorStartsWith) throws Exception {
        try {
            testAddCustomChannelsApiCall(serverFqdnIn, customChannelInfoListIn);
            fail("addCustomChannels API not failing when creating peripheral channel with " + errorStartsWith);
        }
        catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith(errorStartsWith),
                    "Wrong expected start of error message: [" + e.getMessage() + "]");
        }
    }
}
