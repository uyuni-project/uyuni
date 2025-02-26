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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.taskomatic.task.ReportDBHelper;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.SparkTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.model.hub.ChannelInfoDetailsJson;
import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssHub;
import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.model.hub.IssRole;
import com.suse.manager.model.hub.TokenType;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.token.IssTokenBuilder;
import com.suse.manager.webui.utils.token.Token;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.manager.webui.utils.token.TokenParsingException;
import com.suse.scc.SCCEndpoints;
import com.suse.scc.model.SCCRepositoryJson;
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
    private String bodyString;

    public ControllerTestUtils() {
        apiEndpoint = null;
        httpMethod = null;
        serverFqdn = null;
        authBearerToken = null;
        authBearerTokenExpiration = null;
        role = null;
        addBearerTokenToHeaders = false;
        bodyString = null;
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

    public ControllerTestUtils withBody(String bodyIn) {
        bodyString = bodyIn;
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
        ChannelArch arch = ChannelFactory.lookupArchByLabel("channel-x86_64");
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

    public ChannelInfoDetailsJson createChannelInfoDetailsJson(Long orgId,
                                                               String channelLabel,
                                                               String parentChannelLabel,
                                                               String originalChannelLabel,
                                                               boolean isGpgCheck,
                                                               boolean isInstallerUpdates,
                                                               String archLabel,
                                                               String checksumLabel,
                                                               Date endOfLifeDate) {
        ChannelInfoDetailsJson info = new ChannelInfoDetailsJson(channelLabel);

        info.setPeripheralOrgId(orgId);
        info.setParentChannelLabel(parentChannelLabel);
        info.setChannelArchLabel(archLabel);
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

        SCCRepositoryJson repo = SCCEndpoints.buildCustomRepoJson(channelLabel, "hub.domain.top", "123456789abcdef");
        info.setRepositoryInfo(repo);

        return info;
    }

    public void testCustomChannel(Channel ch, Long peripheralOrgId,
                                  boolean isGpgCheck,
                                  boolean isInstallerUpdates,
                                  String archLabel,
                                  String checksumLabel,
                                  Date endOfLifeDate) {
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

    public ChannelInfoDetailsJson createValidCustomChInfo() {
        return createValidCustomChInfo("customCh");
    }

    public ChannelInfoDetailsJson createValidCustomChInfo(String channelLabel) {
        User testPeripheralUser = UserTestUtils.findNewUser("peripheral_user_", "peripheral_org_", true);
        return createChannelInfoDetailsJson(testPeripheralUser.getOrg().getId(),
                channelLabel, "", "",
                true, true, "channel-s390", "sha256",
                createDateUtil(2096, 10, 22));
    }

    public Object testSyncChannelsApiCall(String serverFqdnIn,
                                          List<ChannelInfoDetailsJson> channelInfoListIn) throws Exception {
        String apiUnderTest = "/hub/syncChannels";

        return withServerFqdn(serverFqdnIn)
                .withApiEndpoint(apiUnderTest)
                .withHttpMethod(HttpMethod.post)
                .withRole(IssRole.HUB)
                .withBearerTokenInHeaders()
                .withBody(Json.GSON.toJson(channelInfoListIn))
                .simulateControllerApiCall();
    }

    public void checkSyncChannelsApiNotThrowing(String serverFqdnIn,
                                                List<ChannelInfoDetailsJson> channelInfoListIn)
            throws Exception {

        try {
            String answer = (String) testSyncChannelsApiCall(serverFqdnIn, channelInfoListIn);

            ResultJson<?> result = Json.GSON.fromJson(answer, ResultJson.class);
            assertTrue(result.isSuccess(), "Failed: " + answer);
        }
        catch (IllegalArgumentException e) {
            fail("syncChannels API should not throw");
        }
    }

    public void checkSyncChannelsApiThrows(String serverFqdnIn,
                                           List<ChannelInfoDetailsJson> channelInfoListIn,
                                           String errorStartsWith) throws Exception {
        try {
            String answer = (String) testSyncChannelsApiCall(serverFqdnIn, channelInfoListIn);

            ResultJson<?> result = Json.GSON.fromJson(answer, ResultJson.class);
            assertFalse(result.isSuccess(),
                    "syncChannels API not failing when creating peripheral channel with " +
                            errorStartsWith);
            assertTrue(result.getMessages().get(0).startsWith(errorStartsWith),
                    "Wrong expected start of error message: [" + result.getMessages().get(0) + "]");
        }
        catch (IllegalArgumentException e) {
            fail("syncChannels API should not throw");
        }
    }

    private static void checkEqualIfModified(Object modified, Object pristine) {
        if (null != modified) {
            assertEquals(modified, pristine);
        }
    }

    private static void checkDifferentIfModified(Object modified, Object pristine) {
        if (null != modified) {
            assertNotEquals(modified, pristine);
        }
    }

    @FunctionalInterface
    private interface CheckMethod {
        void checkCompatible(Object modified, Object pristine);
    }

    public void checkEqualModifications(ChannelInfoDetailsJson modifyInfo, Channel ch) {
        checkModifications(modifyInfo, ch, ControllerTestUtils::checkEqualIfModified);
    }

    public void checkDifferentModifications(ChannelInfoDetailsJson modifyInfo, Channel ch) {
        checkModifications(modifyInfo, ch, ControllerTestUtils::checkDifferentIfModified);
    }

    private void checkModifications(ChannelInfoDetailsJson modifyInfo, Channel ch, CheckMethod checkMethod) {
        assertEquals(modifyInfo.getLabel(), ch.getLabel());

        if (null != modifyInfo.getPeripheralOrgId()) {
            checkMethod.checkCompatible(modifyInfo.getPeripheralOrgId(), ch.getOrg().getId());
        }

        checkMethod.checkCompatible(modifyInfo.getOriginalChannelLabel(), ch.getOriginal().getLabel());

        checkMethod.checkCompatible(modifyInfo.getBaseDir(), ch.getBaseDir());
        checkMethod.checkCompatible(modifyInfo.getName(), ch.getName());
        checkMethod.checkCompatible(modifyInfo.getSummary(), ch.getSummary());
        checkMethod.checkCompatible(modifyInfo.getDescription(), ch.getDescription());
        checkMethod.checkCompatible(modifyInfo.getProductNameLabel(), ch.getProductName().getLabel());
        checkMethod.checkCompatible(modifyInfo.isGpgCheck(), ch.isGPGCheck());
        checkMethod.checkCompatible(modifyInfo.getGpgKeyUrl(), ch.getGPGKeyUrl());
        checkMethod.checkCompatible(modifyInfo.getGpgKeyId(), ch.getGPGKeyId());
        checkMethod.checkCompatible(modifyInfo.getGpgKeyFp(), ch.getGPGKeyFp());
        checkMethod.checkCompatible(modifyInfo.getEndOfLifeDate().toString(), ch.getEndOfLife().toString());

        checkMethod.checkCompatible(modifyInfo.getChannelProductProduct(), ch.getProduct().getProduct());
        checkMethod.checkCompatible(modifyInfo.getChannelProductVersion(), ch.getProduct().getVersion());
        checkMethod.checkCompatible(modifyInfo.getChannelAccess(), ch.getAccess());
        checkMethod.checkCompatible(modifyInfo.getMaintainerName(), ch.getMaintainerName());
        checkMethod.checkCompatible(modifyInfo.getMaintainerEmail(), ch.getMaintainerEmail());
        checkMethod.checkCompatible(modifyInfo.getMaintainerPhone(), ch.getMaintainerPhone());
        checkMethod.checkCompatible(modifyInfo.getSupportPolicy(), ch.getSupportPolicy());
        checkMethod.checkCompatible(modifyInfo.getUpdateTag(), ch.getUpdateTag());
        checkMethod.checkCompatible(modifyInfo.isInstallerUpdates(), ch.isInstallerUpdates());
    }

    public void createTestChannel(ChannelInfoDetailsJson modifyInfo, User userIn) throws Exception {
        ChannelFamily cfam = ChannelFamilyFactoryTest.createTestChannelFamily();
        String query = "ChannelArch.findById";
        ChannelArch arch = (ChannelArch) TestUtils.lookupFromCacheById(500L, query);
        ChannelFactoryTest.createTestChannel(modifyInfo.getName(), modifyInfo.getLabel(), userIn.getOrg(), arch, cfam);
    }
}
