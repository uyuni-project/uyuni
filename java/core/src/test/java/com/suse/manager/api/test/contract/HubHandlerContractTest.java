/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.api.test.contract;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.model.hub.ChannelInfoJson;
import com.suse.manager.model.hub.ManagerInfoJson;
import com.suse.manager.model.hub.OrgInfoJson;
import com.suse.manager.model.hub.migration.MigrationMessageLevel;
import com.suse.manager.model.hub.migration.MigrationResult;
import com.suse.manager.xmlrpc.iss.HubHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HubHandlerContractTest extends BaseOpenApiTest {

    private static final String FQDN = "peripheral.example.com";
    private static final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.header.signature";
    private static final String ROOT_CA = "-----BEGIN CERTIFICATE-----";

    @Override
    protected String getApiNamespace() {
        return "sync.hub";
    }

    @Override
    protected Class<HubHandler> getHandlerClass() {
        return HubHandler.class;
    }

    private HubHandler handler() {
        return (HubHandler) handlerMock;
    }

    private MigrationResult migrationResult() {
        MigrationResult result = new MigrationResult();
        result.addMessage(MigrationMessageLevel.INFO, "Peripheral migrated successfully");
        return result;
    }

    private Map<String, Object> peripheralServer() {
        Map<String, Object> server = new LinkedHashMap<>();
        server.put("fqdn", FQDN);
        server.put("id", 1000010000);
        server.put("root_ca", ROOT_CA);
        return server;
    }

    private List<Map<String, String>> migrationData() {
        Map<String, String> entry = new LinkedHashMap<>();
        entry.put("fqdn", FQDN);
        entry.put("token", TOKEN);
        entry.put("root_ca", ROOT_CA);
        return List.of(entry);
    }

    @Test
    public void testGenerateAccessToken() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).generateAccessToken(with(mockUser), with(FQDN));
            will(returnValue(TOKEN));
        }});

        validateApiContract("/sync.hub/generateAccessToken", "POST")
                .withBody(Map.of("fqdn", FQDN))
                .onHandlerMethod("generateAccessToken", User.class, String.class);
    }

    @Test
    public void testStoreAccessToken() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("fqdn", FQDN);
        body.put("token", TOKEN);

        context.checking(new Expectations() {{
            oneOf(handler()).storeAccessToken(with(mockUser), with(FQDN), with(TOKEN));
            will(returnValue(1));
        }});

        validateApiContract("/sync.hub/storeAccessToken", "POST")
                .withBody(body)
                .onHandlerMethod("storeAccessToken", User.class, String.class, String.class);
    }

    @Test
    public void testReplaceTokens() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).replaceTokens(with(mockUser), with(FQDN));
            will(returnValue(1));
        }});

        validateApiContract("/sync.hub/replaceTokens", "POST")
                .withBody(Map.of("fqdn", FQDN))
                .onHandlerMethod("replaceTokens", User.class, String.class);
    }

    @Test
    public void testRegisterPeripheral() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("fqdn", FQDN);
        body.put("username", "admin");
        body.put("password", "secret");
        body.put("rootCA", ROOT_CA);

        context.checking(new Expectations() {{
            oneOf(handler()).registerPeripheral(with(mockUser), with(FQDN), with("admin"), with("secret"),
                    with(ROOT_CA));
            will(returnValue(1));
        }});

        validateApiContract("/sync.hub/registerPeripheral", "POST")
                .withBody(body)
                .onHandlerMethod("registerPeripheral", User.class, String.class, String.class, String.class,
                        String.class);
    }

    @Test
    public void testRegisterPeripheralWithToken() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("fqdn", FQDN);
        body.put("token", TOKEN);
        body.put("rootCA", ROOT_CA);

        context.checking(new Expectations() {{
            oneOf(handler()).registerPeripheralWithToken(with(mockUser), with(FQDN), with(TOKEN), with(ROOT_CA));
            will(returnValue(1));
        }});

        validateApiContract("/sync.hub/registerPeripheralWithToken", "POST")
                .withBody(body)
                .onHandlerMethod("registerPeripheralWithToken", User.class, String.class, String.class, String.class);
    }

    @Test
    public void testDeregister() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("fqdn", FQDN);
        body.put("onlyLocal", true);

        context.checking(new Expectations() {{
            oneOf(handler()).deregister(with(mockUser), with(FQDN), with(true));
            will(returnValue(1));
        }});

        validateApiContract("/sync.hub/deregister", "POST")
                .withBody(body)
                .onHandlerMethod("deregister", User.class, String.class, boolean.class);
    }

    @Test
    public void testSetDetails() throws Exception {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("root_ca", ROOT_CA);
        data.put("gpg_key", "-----BEGIN PGP PUBLIC KEY BLOCK-----");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("fqdn", FQDN);
        body.put("role", "PERIPHERAL");
        body.put("data", data);

        context.checking(new Expectations() {{
            oneOf(handler()).setDetails(with(mockUser), with(FQDN), with("PERIPHERAL"), with(any(Map.class)));
            will(returnValue(1));
        }});

        validateApiContract("/sync.hub/setDetails", "POST")
                .withBody(body)
                .onHandlerMethod("setDetails", User.class, String.class, String.class, Map.class);
    }

    @Test
    public void testGetManagerInfo() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getManagerInfo(with(mockUser));
            will(returnValue(new ManagerInfoJson("2026.08", true, "reportdb", "reportdb.example.com", 5432)));
        }});

        validateApiContract("/sync.hub/getManagerInfo", "GET")
                .onHandlerMethod("getManagerInfo", User.class);
    }

    @Test
    public void testGetAllPeripheralOrgs() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getAllPeripheralOrgs(with(mockUser), with(FQDN));
            will(returnValue(List.of(new OrgInfoJson(1L, "Default Organization"))));
        }});

        validateApiContract("/sync.hub/getAllPeripheralOrgs", "GET")
                .withParams(Map.of("fqdn", new String[] {FQDN}))
                .onHandlerMethod("getAllPeripheralOrgs", User.class, String.class);
    }

    @Test
    public void testGetAllPeripheralChannels() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getAllPeripheralChannels(with(mockUser), with(FQDN));
            will(returnValue(List.of(new ChannelInfoJson(101L, "Basesystem Module", "basesystem-module",
                    "Basesystem Module for SUSE Linux Enterprise 15", 1L, 100L))));
        }});

        validateApiContract("/sync.hub/getAllPeripheralChannels", "GET")
                .withParams(Map.of("fqdn", new String[] {FQDN}))
                .onHandlerMethod("getAllPeripheralChannels", User.class, String.class);
    }

    @Test
    public void testListPeripheralServers() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listPeripheralServers(with(mockUser));
            will(returnValue(List.of(peripheralServer())));
        }});

        validateApiContract("/sync.hub/listPeripheralServers", "GET")
                .onHandlerMethod("listPeripheralServers", User.class);
    }

    @Test
    public void testAddPeripheralChannelsToSync() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("fqdn", FQDN);
        body.put("channelLabels", List.of("basesystem-module", "desktop-module"));
        body.put("peripheralOrgIdWhenCustomChannel", 1);

        context.checking(new Expectations() {{
            oneOf(handler()).addPeripheralChannelsToSync(with(mockUser), with(FQDN), with(any(List.class)),
                    with(1));
            will(returnValue(1));
        }});

        validateApiContract("/sync.hub/addPeripheralChannelsToSync", "POST")
                .withBody(body)
                .onHandlerMethod("addPeripheralChannelsToSync", User.class, String.class, List.class, Integer.class);
    }

    @Test
    public void testRemovePeripheralChannelsToSync() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("fqdn", FQDN);
        body.put("channelLabels", List.of("basesystem-module"));

        context.checking(new Expectations() {{
            oneOf(handler()).removePeripheralChannelsToSync(with(mockUser), with(FQDN), with(any(List.class)));
            will(returnValue(1));
        }});

        validateApiContract("/sync.hub/removePeripheralChannelsToSync", "POST")
                .withBody(body)
                .onHandlerMethod("removePeripheralChannelsToSync", User.class, String.class, List.class);
    }

    @Test
    public void testListPeripheralChannelsToSync() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listPeripheralChannelsToSync(with(mockUser), with(FQDN));
            will(returnValue(List.of("basesystem-module", "desktop-module")));
        }});

        validateApiContract("/sync.hub/listPeripheralChannelsToSync", "GET")
                .withParams(Map.of("fqdn", new String[] {FQDN}))
                .onHandlerMethod("listPeripheralChannelsToSync", User.class, String.class);
    }

    @Test
    public void testSyncPeripheralChannels() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).syncPeripheralChannels(with(mockUser), with(FQDN));
            will(returnValue(1));
        }});

        validateApiContract("/sync.hub/syncPeripheralChannels", "POST")
                .withBody(Map.of("fqdn", FQDN))
                .onHandlerMethod("syncPeripheralChannels", User.class, String.class);
    }

    @Test
    public void testRegenerateSCCCredentials() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).regenerateSCCCredentials(with(mockUser), with(FQDN));
            will(returnValue(1));
        }});

        validateApiContract("/sync.hub/regenerateSCCCredentials", "POST")
                .withBody(Map.of("fqdn", FQDN))
                .onHandlerMethod("regenerateSCCCredentials", User.class, String.class);
    }

    @Test
    public void testMigrateFromISSv1() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).migrateFromISSv1(with(mockUser), with(any(List.class)));
            will(returnValue(migrationResult()));
        }});

        validateApiContract("/sync.hub/migrateFromISSv1", "POST")
                .withBody(Map.of("migrationData", migrationData()))
                .onHandlerMethod("migrateFromISSv1", User.class, List.class);
    }

    @Test
    public void testMigrateFromISSv2() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).migrateFromISSv2(with(mockUser), with(any(List.class)));
            will(returnValue(migrationResult()));
        }});

        validateApiContract("/sync.hub/migrateFromISSv2", "POST")
                .withBody(Map.of("migrationData", migrationData()))
                .onHandlerMethod("migrateFromISSv2", User.class, List.class);
    }

    @Test
    public void testIsISSPeripheral() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).isISSPeripheral(with(mockUser));
            will(returnValue(true));
        }});

        validateApiContract("/sync.hub/isISSPeripheral", "GET")
                .onHandlerMethod("isISSPeripheral", User.class);
    }

    @Test
    public void testScheduleUpdateTask() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("earliest", "2026-08-16T10:00:00Z");
        body.put("withReposync", true);

        context.checking(new Expectations() {{
            oneOf(handler()).scheduleUpdateTask(with(mockUser), with(any(Date.class)), with(true));
            will(returnValue(1));
        }});

        validateApiContract("/sync.hub/scheduleUpdateTask", "POST")
                .withBody(body)
                .onHandlerMethod("scheduleUpdateTask", User.class, Date.class, boolean.class);
    }
}
