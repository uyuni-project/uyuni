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

import com.redhat.rhn.frontend.dto.SystemSearchResult;
import com.redhat.rhn.frontend.xmlrpc.system.search.SystemSearchHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class SystemSearchHandlerContractTest extends BaseOpenApiTest {

    private static final String SESSION_KEY = "fake-session";

    @Override
    protected String getApiNamespace() {
        return "system.search";
    }

    @Override
    protected Class<SystemSearchHandler> getHandlerClass() {
        return SystemSearchHandler.class;
    }

    private SystemSearchHandler handler() {
        return (SystemSearchHandler) handlerMock;
    }

    /**
     * The serializer only emits the hardware fields when the result carries hardware
     * details, so a plain result exercises the documented optional properties.
     *
     * @return a search result serialized by SystemSearchResultSerializer
     */
    private SystemSearchResult searchResult() {
        SystemSearchResult result = new SystemSearchResult();
        result.setId(1000010000L);
        result.setName("test-system.example.com");
        result.setLastCheckin("2026-08-11 09:00:00");
        result.setHostname("test-system.example.com");
        result.setIpaddr("192.168.1.10");
        return result;
    }

    /**
     * Every method in this namespace takes the same parameters and returns the same
     * structure, so one helper covers all eight endpoints.
     *
     * @param methodName the handler method to exercise
     * @param searchTerm the search term to pass
     * @throws Exception if the contract validation fails
     */
    private void assertSearchContract(String methodName, String searchTerm) throws Exception {
        validateApiContract("/system.search/" + methodName, "GET")
                .withParams(Map.of("searchTerm", new String[] {searchTerm}))
                .onHandlerMethod(methodName, String.class, String.class);
    }

    @Test
    public void testIp() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).ip(with(SESSION_KEY), with("192.168.1.10"));
            will(returnValue(List.of(searchResult())));
        }});

        assertSearchContract("ip", "192.168.1.10");
    }

    @Test
    public void testHostname() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).hostname(with(SESSION_KEY), with("test-system"));
            will(returnValue(List.of(searchResult())));
        }});

        assertSearchContract("hostname", "test-system");
    }

    @Test
    public void testDeviceVendorId() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).deviceVendorId(with(SESSION_KEY), with("0x8086"));
            will(returnValue(List.of(searchResult())));
        }});

        assertSearchContract("deviceVendorId", "0x8086");
    }

    @Test
    public void testDeviceId() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).deviceId(with(SESSION_KEY), with("0x1234"));
            will(returnValue(List.of(searchResult())));
        }});

        assertSearchContract("deviceId", "0x1234");
    }

    @Test
    public void testDeviceDriver() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).deviceDriver(with(SESSION_KEY), with("e1000e"));
            will(returnValue(List.of(searchResult())));
        }});

        assertSearchContract("deviceDriver", "e1000e");
    }

    @Test
    public void testDeviceDescription() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).deviceDescription(with(SESSION_KEY), with("Ethernet"));
            will(returnValue(List.of(searchResult())));
        }});

        assertSearchContract("deviceDescription", "Ethernet");
    }

    @Test
    public void testNameAndDescription() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).nameAndDescription(with(SESSION_KEY), with("test"));
            will(returnValue(List.of(searchResult())));
        }});

        assertSearchContract("nameAndDescription", "test");
    }

    @Test
    public void testUuid() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).uuid(with(SESSION_KEY), with("1234abcd"));
            will(returnValue(List.of(searchResult())));
        }});

        assertSearchContract("uuid", "1234abcd");
    }
}
