/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.manager.webui.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.SparkTestUtils;

import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.utils.PagedSqlQueryBuilder.FilterWithValue;
import com.suse.manager.webui.services.TestSaltApi;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.utils.gson.SubscribeChannelsJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.StringUtils;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import spark.ModelAndView;
import spark.Request;

public class SystemsControllerTest extends BaseControllerTestCase {

    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<String, List<String>> SERVER_NAMES_BY_HOST_SERVER_NAME_NAME_MAP = Map.of(
            "hostA", Arrays.asList("hostA_VmA", "hostA_VmB", "hostA_VmC"),
            "hostB", Arrays.asList("hostB_VmA", "hostB_VmB")
    );
    private static final String BASE_URI = "http://localhost:8080/rhn/manager/systems/list/virtual";
    private static final String KEY_HOST_SERVER_NAME = "host_server_name";
    private static final String KEY_SERVER_NAME = "server_name";
    private static final String PROPERTY_HOST_SERVER_NAME = "hostServerName";
    private static final String PROPERTY_SERVER_NAME = "name";

    //
    private Map<String, Server> serversByHostServerName;
    private SystemsController systemsController;

    @BeforeEach
    public void setUp() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);

        SaltApi saltApi = new TestSaltApi();
        systemsController = new SystemsController(saltApi);
        SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(saltApi), new SystemEntitler(saltApi)
        );

        serversByHostServerName = new HashMap<>();
        for (Map.Entry<String, List<String>> e : SERVER_NAMES_BY_HOST_SERVER_NAME_NAME_MAP.entrySet()) {

            // Set up the host
            String hostName = e.getKey();
            Server host = ServerTestUtils.createVirtHostWithGuests(user, e.getValue().size(), true,
                    systemEntitlementManager);
            host.setName(hostName);
            serversByHostServerName.put(hostName, host);

            // Set up the host's vms
            Iterator<String> vmNamesIterator = e.getValue().iterator();
            Iterator<VirtualInstance> guestIterator = host.getGuests().iterator();
            while (guestIterator.hasNext() && vmNamesIterator.hasNext()) {
                VirtualInstance guest = guestIterator.next();
                String vmName = vmNamesIterator.next();
                guest.setName(vmName);
            }

        }

        // Assert all vms are created
        for (Server host : serversByHostServerName.values()) {
            int expectedVmsSize = SERVER_NAMES_BY_HOST_SERVER_NAME_NAME_MAP.get(host.getName()).size();
            int actualVmsSize = SystemManager.virtualGuestsForHostList(user, host.getId(), null).size();
            assertEquals(expectedVmsSize, actualVmsSize,
                    "Expected to find " + expectedVmsSize + " guests for host " + host.getName() +
                            " but got " + actualVmsSize);
        }
    }

    /**
     * Test that all virtual systems are returned when no filter is provided
     */
    @Test
    public void testVirtualSystemsReturnAllSystemsWhenNoFilter() {
        Request request = SparkTestUtils.createMockRequest(BASE_URI);
        Object virtualSystemsObject = systemsController.virtualSystems(request, response, user);
        assertNotNull(virtualSystemsObject);
        Map<String, Object> virtualSystemsAsMap = new Gson().fromJson(virtualSystemsObject.toString(), Map.class);
        assertEquals(5, ((Collection) virtualSystemsAsMap.get("items")).size());
    }

    /**
     * Test that all virtual systems are returned when blank params are provided
     */
    @Test
    public void testVirtualSystemsReturnAllSystemsWhenBlankParams() {
        List<Map<String, Object>> items = getVirtualSystemsItems(StringUtils.EMPTY, StringUtils.EMPTY);
        assertEquals(5, items.size());
    }

    /**
     * Test that virtual systems are returned when filtered exact host or vm names
     */
    @Test
    public void testVirtualSystemsReturnAllHostVMsWhenFilterByExactName() {
        SERVER_NAMES_BY_HOST_SERVER_NAME_NAME_MAP.entrySet().forEach(e -> {
            String hostName = e.getKey();
            List<String> hostVmNames = e.getValue();

            List<Map<String, Object>> hostFilteredItems = getVirtualSystemsItems(KEY_HOST_SERVER_NAME, hostName);
            assertEquals(hostVmNames.size(), hostFilteredItems.size(),
                    "Expected host  " + hostName + " to return " + hostVmNames.size() +
                            " but got " + hostFilteredItems.size());
            assertTrue(hostFilteredItems.stream().allMatch(
                            obj -> obj.get(PROPERTY_HOST_SERVER_NAME).equals(hostName)),
                    "Querying for " + hostName + " should return only \"+hostName+\"'s vms"
            );

            hostVmNames.forEach(vmName -> {
                List<Map<String, Object>> vmFilteredItems = getVirtualSystemsItems(KEY_SERVER_NAME, vmName);
                assertEquals(1, vmFilteredItems.size());
                assertEquals(vmName, vmFilteredItems.get(0).get(PROPERTY_SERVER_NAME));
            });
        });
    }

    /**
     * Test virtual systems are returned when filtered by common/partial name
     */
    @Test
    public void testVirtualSystemsReturnAllWhenFilterByPartialName() {
        List<Map<String, Object>> items = getVirtualSystemsItems(KEY_HOST_SERVER_NAME, "host");
        assertEquals(5, items.size());
        assertTrue(items.stream().allMatch(obj -> obj.get(PROPERTY_HOST_SERVER_NAME).toString().contains("host")));

        assertEquals(2, getVirtualSystemsItems(KEY_SERVER_NAME, "VmA").size());
        assertEquals(2, getVirtualSystemsItems(KEY_SERVER_NAME, "VmB").size());
        assertEquals(1, getVirtualSystemsItems(KEY_SERVER_NAME, "VmC").size());
        assertEquals(5, getVirtualSystemsItems(KEY_SERVER_NAME, "Vm").size());
    }

    /**
     * Test no virtual systems are returned when filtered by a non-existent host name
     */
    @Test
    public void testVirtualSystemsReturnsNoneWhenNameMatches() {
        assertEquals(0, getVirtualSystemsItems(KEY_HOST_SERVER_NAME, "hostY").size());
        assertEquals(0, getVirtualSystemsItems(KEY_SERVER_NAME, "VmY").size());
    }


    /**
     * Sets up the query parameters and executes @link{SystemsController#virtualSystems}
     *
     * @param queryColumn the name of the column to filter by
     * @param query       the value to filter by
     * @return filtered virtual systems
     */
    private List<Map<String, Object>> getVirtualSystemsItems(String queryColumn, String query) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("qc", queryColumn);
        queryParams.put("q", query);
        Request request = SparkTestUtils.createMockRequestWithParams(BASE_URI, queryParams);

        //
        Object virtualSystemsObject = systemsController.virtualSystems(request, response, user);

        //
        assertNotNull(virtualSystemsObject);
        Map<String, Object> virtualSystemsAsMap = GSON.fromJson(virtualSystemsObject.toString(), Map.class);

        return (List<Map<String, Object>>) virtualSystemsAsMap.get("items");
    }

    @Test
    public void testVirtualListPageQueryParamsConversion() {
        final String paramQC = "dummy_param_qc";
        final String paramQ = "dummy_param_q";

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("qc", paramQC);
        queryParams.put("q", paramQ);
        Request request = SparkTestUtils.createMockRequestWithParams(BASE_URI, queryParams);
        ModelAndView virtualSystemsModelAndView = systemsController.virtualListPage(request, response, user);

        assertNotNull(virtualSystemsModelAndView);
        assertNotNull(virtualSystemsModelAndView.getModel());

        HashMap model = (HashMap) virtualSystemsModelAndView.getModel();
        assertAll(
                () -> assertTrue(model.containsKey("queryColumn")),
                () -> assertEquals(String.format("\"%s\"", paramQC), model.get("queryColumn"))
        );
        assertAll(
                () -> assertTrue(model.containsKey("query")),
                () -> assertEquals(String.format("\"%s\"", paramQ), model.get("query"))
        );
    }

    @Test
    public void testSubscribeChannelsJsonDeserialization() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
                .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
                .create();
        String json = "{\"earliest\":\"2026-09-11T12:00:00\"," +
                "\"actionDescription\":\"My description for schedule\",\"actionChain\":null}";
        SubscribeChannelsJson parsed = gson.fromJson(json, SubscribeChannelsJson.class);
        assertNotNull(parsed);
        assertTrue(parsed.getActionDescription().isPresent());
        assertEquals("My description for schedule", parsed.getActionDescription().get());
    }

    private static Stream<Arguments> provideValuesForFilterParser() {
        return Stream.of(
                Arguments.of("proxy", "proxy =", "true"),
                Arguments.of("=proxy", "proxy =", "true"),
                Arguments.of("!=proxy", "proxy !=", "true"),
                Arguments.of("!=proxy", "proxy !=", "true"),
                Arguments.of("mgr_server", "mgr_server =", "true"),
                Arguments.of("= mgr_server", "mgr_server =", "true"),
                Arguments.of("!= mgr_server", "mgr_server !=", "true"),
                Arguments.of("virtual_host", "virtual_host =", "true"),
                Arguments.of("= virtual_host", "virtual_host =", "true"),
                Arguments.of("!= virtual_host", "virtual_host !=", "true"),
                Arguments.of("virtual_guest", "virtual_guest =", "true"),
                Arguments.of("= virtual_guest", "virtual_guest =", "true"),
                Arguments.of("!= virtual_guest", "virtual_guest !=", "true"),
                Arguments.of("physical", "virtual_guest =", "false"),
                Arguments.of("= physical", "virtual_guest =", "false"),
                Arguments.of("!= physical", "virtual_guest !=", "false")
        );
    }

    @ParameterizedTest
    @MethodSource("provideValuesForFilterParser")
    public void testFilterParserIsProxy(String value, String expectedFilter, String expectedValue) {
        PageControl pc = new PageControl(0, 25, "name", true, "system_kind", value);
        var parser = SystemsController.getFilterParser(pc, false);
        var actualFilter = parser.apply(Optional.of(pc));

        assertEquals(String.format(" %s CAST(:filter_value AS BOOLEAN) ", expectedFilter), actualFilter.getFilter());
        assertEquals(expectedValue, actualFilter.getValue());
    }

    private static FilterWithValue parseFilter(String column, String data) {
        return parseFilter(column, data, false);
    }

    private static FilterWithValue parseFilter(String column, String data, boolean raiseErrors) {
        PageControl pc = new PageControl(0, 25, "name", true, column, data);
        return SystemsController.getFilterParser(pc, raiseErrors).apply(Optional.of(pc));
    }

    private static Stream<Arguments> provideTextFields() {
        // SystemSerializer field, column used in the query
        return Stream.of(
                Arguments.of("name", "server_name"),
                Arguments.of("id", "id"),
                Arguments.of("channel_labels", "channel_labels"),
                Arguments.of("entitlement", "entitlement"),
                Arguments.of("creator_name", "creator_name"),
                Arguments.of("status_type", "status_type")
        );
    }

    @ParameterizedTest
    @MethodSource("provideTextFields")
    public void testFilterParserTextFields(String field, String expectedColumn) {
        FilterWithValue contains = parseFilter(field, "foo");
        assertEquals(String.format(" %s ILIKE :filter_value ", expectedColumn), contains.getFilter());
        assertEquals("%foo%", contains.getValue());

        FilterWithValue notContains = parseFilter(field, "!foo");
        assertEquals(String.format(" %s NOT ILIKE :filter_value ", expectedColumn), notContains.getFilter());
        assertEquals("%foo%", notContains.getValue());

        FilterWithValue equal = parseFilter(field, "=foo");
        assertEquals(String.format(" %s = :filter_value ", expectedColumn), equal.getFilter());
        assertEquals("foo", equal.getValue());

        FilterWithValue notEqual = parseFilter(field, "!=foo");
        assertEquals(String.format(" %s != :filter_value ", expectedColumn), notEqual.getFilter());
        assertEquals("foo", notEqual.getValue());
    }

    private static Stream<Arguments> provideNumberFields() {
        // SystemSerializer field, column used in the query
        return Stream.of(
                Arguments.of("group_count", "group_count"),
                Arguments.of("security_errata", "security_errata"),
                Arguments.of("bug_errata", "bug_errata"),
                Arguments.of("enhancement_errata", "enhancement_errata"),
                Arguments.of("outdated_pkg_count", "outdated_packages"),
                Arguments.of("config_files_with_difference", "config_files_with_difference"),
                Arguments.of("extra_pkg_count", "extra_pkg_count"),
                Arguments.of("actions_count", "actions_count"),
                Arguments.of("unscheduled_errata_count", "unscheduled_errata_count")
        );
    }

    @ParameterizedTest
    @MethodSource("provideNumberFields")
    public void testFilterParserNumberFields(String field, String expectedColumn) {
        FilterWithValue implicitEqual = parseFilter(field, "5");
        assertEquals(String.format(" %s = :filter_value ", expectedColumn), implicitEqual.getFilter());
        assertEquals(5L, implicitEqual.getValue());

        for (String operator : List.of("=", "!=", "<", "<=", ">", ">=")) {
            FilterWithValue filter = parseFilter(field, operator + " 12");
            assertEquals(String.format(" %s %s :filter_value ", expectedColumn, operator), filter.getFilter());
            assertEquals(12L, filter.getValue());
        }
    }

    @ParameterizedTest
    @MethodSource("provideNumberFields")
    public void testFilterParserNumberFieldsInvalidValue(String field) {
        FilterWithValue ignored = parseFilter(field, "abc", false);
        assertEquals("", ignored.getFilter());

        assertThrows(InvalidParameterException.class, () -> parseFilter(field, "abc", true));
    }

    @Test
    public void testFilterParserTotalErrataCount() {
        FilterWithValue filter = parseFilter("total_errata_count", ">= 3");
        assertTrue(filter.getFilter().contains("security_errata + bug_errata + enhancement_errata"),
                "Unexpected filter: " + filter.getFilter());
        assertTrue(filter.getFilter().trim().endsWith(">= :filter_value"),
                "Unexpected filter: " + filter.getFilter());
        assertEquals(3L, filter.getValue());
    }

    private static Stream<String> provideBooleanFields() {
        return Stream.of("mgr_server", "proxy", "virtual_host", "virtual_guest", "requires_reboot", "kickstarting");
    }

    @ParameterizedTest
    @MethodSource("provideBooleanFields")
    public void testFilterParserBooleanFields(String field) {
        FilterWithValue implicitEqual = parseFilter(field, "true");
        assertEquals(String.format(" %s = CAST(:filter_value AS BOOLEAN) ", field), implicitEqual.getFilter());
        assertEquals("true", implicitEqual.getValue());

        FilterWithValue equal = parseFilter(field, "= false");
        assertEquals(String.format(" %s = CAST(:filter_value AS BOOLEAN) ", field), equal.getFilter());
        assertEquals("false", equal.getValue());

        FilterWithValue notEqual = parseFilter(field, "!= true");
        assertEquals(String.format(" %s != CAST(:filter_value AS BOOLEAN) ", field), notEqual.getFilter());
        assertEquals("true", notEqual.getValue());
    }

    @ParameterizedTest
    @MethodSource("provideBooleanFields")
    public void testFilterParserBooleanFieldsInvalidValue(String field) {
        assertEquals("", parseFilter(field, "maybe", false).getFilter());

        assertThrows(InvalidParameterException.class, () -> parseFilter(field, "maybe", true));
    }

    private static Stream<String> provideDateFields() {
        return Stream.of("last_checkin", "created");
    }

    @ParameterizedTest
    @MethodSource("provideDateFields")
    public void testFilterParserDateFields(String field) {
        FilterWithValue date = parseFilter(field, ">= 2026-11-23");
        assertEquals(String.format(" CAST(%s AS DATE) >= CAST(:filter_value AS DATE) ", field), date.getFilter());
        assertEquals("2026-11-23", date.getValue());

        FilterWithValue implicitEqual = parseFilter(field, "2026-11-23");
        assertEquals(String.format(" CAST(%s AS DATE) = CAST(:filter_value AS DATE) ", field),
                implicitEqual.getFilter());

        FilterWithValue dateTime = parseFilter(field, "< 2026-11-23T14:08:00Z");
        assertEquals(String.format(" %s < CAST(:filter_value AS TIMESTAMPTZ) ", field), dateTime.getFilter());
        assertEquals("2026-11-23T14:08Z", dateTime.getValue());

        FilterWithValue dateTimeWithoutOffset = parseFilter(field, "> 2026-11-23T14:08:00");
        assertEquals(String.format(" %s > CAST(:filter_value AS TIMESTAMPTZ) ", field),
                dateTimeWithoutOffset.getFilter());
        assertEquals("2026-11-23T14:08Z", dateTimeWithoutOffset.getValue());
    }

    @ParameterizedTest
    @MethodSource("provideDateFields")
    public void testFilterParserDateFieldsInvalidValue(String field) {
        assertEquals("", parseFilter(field, "yesterday", false).getFilter());

        assertThrows(InvalidParameterException.class, () -> parseFilter(field, "yesterday", true));
    }

    private static Stream<Arguments> provideDaysFields() {
        // SystemSerializer field, date column used in the query
        return Stream.of(
                Arguments.of("last_checkin_days", "last_checkin"),
                Arguments.of("created_days", "created")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDaysFields")
    public void testFilterParserDaysFields(String field, String expectedColumn) {
        String fiveDaysAgo = LocalDate.now().minusDays(5).toString();

        // The date comparison operators are inverted compared to the number of days
        for (String[] operators : new String[][] {{"<", ">"}, {"<=", ">="}, {">", "<"}, {">=", "<="},
                {"=", "="}, {"!=", "!="}}) {
            FilterWithValue filter = parseFilter(field, operators[0] + " 5");
            assertEquals(String.format(" CAST(%s AS DATE) %s CAST(:filter_value AS DATE) ",
                    expectedColumn, operators[1]), filter.getFilter(), "Wrong filter for " + operators[0]);
            assertEquals(fiveDaysAgo, filter.getValue());
        }

        FilterWithValue implicitEqual = parseFilter(field, "5");
        assertEquals(String.format(" CAST(%s AS DATE) = CAST(:filter_value AS DATE) ", expectedColumn),
                implicitEqual.getFilter());
        assertEquals(fiveDaysAgo, implicitEqual.getValue());
    }

    @ParameterizedTest
    @MethodSource("provideDaysFields")
    public void testFilterParserDaysFieldsInvalidValue(String field) {
        assertEquals("", parseFilter(field, "abc", false).getFilter());
        assertEquals("", parseFilter(field, "abc", true).getFilter());
    }

}
