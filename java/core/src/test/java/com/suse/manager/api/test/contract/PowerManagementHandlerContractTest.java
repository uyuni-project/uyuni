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
import com.redhat.rhn.frontend.xmlrpc.system.provisioning.powermanagement.PowerManagementHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PowerManagementHandlerContractTest extends BaseOpenApiTest {

    private static final Integer SID = 1000010000;
    private static final String NAME = "test-system.example.com";

    @Override
    protected String getApiNamespace() {
        return "system.provisioning.powermanagement";
    }

    @Override
    protected Class<PowerManagementHandler> getHandlerClass() {
        return PowerManagementHandler.class;
    }

    private PowerManagementHandler handler() {
        return (PowerManagementHandler) handlerMock;
    }

    /**
     * @return the power management settings of a system, keyed as the handler keys them
     */
    private Map<String, String> settings() {
        Map<String, String> settings = new LinkedHashMap<>();
        settings.put("powerType", "ipmitool");
        settings.put("powerAddress", "192.168.1.100");
        settings.put("powerUsername", "admin");
        settings.put("powerPassword", "secret");
        settings.put("powerId", "node01");
        return settings;
    }

    @Test
    public void testListTypes() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listTypes(with(mockUser));
            will(returnValue(List.of("ipmitool", "redfish", "wti")));
        }});

        validateApiContract("/system.provisioning.powermanagement/listTypes", "GET")
                .onHandlerMethod("listTypes", User.class);
    }

    @Test
    public void testGetDetailsBySid() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getDetails(with(mockUser), with(SID));
            will(returnValue(settings()));
        }});

        validateApiContract("/system.provisioning.powermanagement/getDetails", "GET")
                .withParams(Map.of("sid", new String[] {SID.toString()}))
                .onHandlerMethod("getDetails", User.class, Integer.class);
    }

    @Test
    public void testGetDetailsByName() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getDetails(with(mockUser), with(NAME));
            will(returnValue(settings()));
        }});

        validateApiContract("/system.provisioning.powermanagement/getDetails", "GET")
                .withParams(Map.of("name", new String[] {NAME}))
                .onHandlerMethod("getDetails", User.class, String.class);
    }

    @Test
    public void testSetDetailsBySid() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sid", SID);
        body.put("data", settings());

        context.checking(new Expectations() {{
            oneOf(handler()).setDetails(with(mockUser), with(SID), with(settings()));
            will(returnValue(1));
        }});

        validateApiContract("/system.provisioning.powermanagement/setDetails", "POST")
                .withBody(body)
                .onHandlerMethod("setDetails", User.class, Integer.class, Map.class);
    }

    @Test
    public void testSetDetailsByName() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", NAME);
        body.put("data", settings());

        context.checking(new Expectations() {{
            oneOf(handler()).setDetails(with(mockUser), with(NAME), with(settings()));
            will(returnValue(1));
        }});

        validateApiContract("/system.provisioning.powermanagement/setDetails", "POST")
                .withBody(body)
                .onHandlerMethod("setDetails", User.class, String.class, Map.class);
    }

    @Test
    public void testPowerOnBySid() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).powerOn(with(mockUser), with(SID));
            will(returnValue(1));
        }});

        validateApiContract("/system.provisioning.powermanagement/powerOn", "POST")
                .withBody(Map.of("sid", SID))
                .onHandlerMethod("powerOn", User.class, Integer.class);
    }

    @Test
    public void testPowerOnByName() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).powerOn(with(mockUser), with(NAME));
            will(returnValue(1));
        }});

        validateApiContract("/system.provisioning.powermanagement/powerOn", "POST")
                .withBody(Map.of("name", NAME))
                .onHandlerMethod("powerOn", User.class, String.class);
    }

    @Test
    public void testPowerOffBySid() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).powerOff(with(mockUser), with(SID));
            will(returnValue(1));
        }});

        validateApiContract("/system.provisioning.powermanagement/powerOff", "POST")
                .withBody(Map.of("sid", SID))
                .onHandlerMethod("powerOff", User.class, Integer.class);
    }

    @Test
    public void testPowerOffByName() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).powerOff(with(mockUser), with(NAME));
            will(returnValue(1));
        }});

        validateApiContract("/system.provisioning.powermanagement/powerOff", "POST")
                .withBody(Map.of("name", NAME))
                .onHandlerMethod("powerOff", User.class, String.class);
    }

    @Test
    public void testRebootBySid() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).reboot(with(mockUser), with(SID));
            will(returnValue(1));
        }});

        validateApiContract("/system.provisioning.powermanagement/reboot", "POST")
                .withBody(Map.of("sid", SID))
                .onHandlerMethod("reboot", User.class, Integer.class);
    }

    @Test
    public void testRebootByName() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).reboot(with(mockUser), with(NAME));
            will(returnValue(1));
        }});

        validateApiContract("/system.provisioning.powermanagement/reboot", "POST")
                .withBody(Map.of("name", NAME))
                .onHandlerMethod("reboot", User.class, String.class);
    }

    @Test
    public void testGetStatusBySid() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getStatus(with(mockUser), with(SID));
            will(returnValue(true));
        }});

        validateApiContract("/system.provisioning.powermanagement/getStatus", "GET")
                .withParams(Map.of("sid", new String[] {SID.toString()}))
                .onHandlerMethod("getStatus", User.class, Integer.class);
    }

    @Test
    public void testGetStatusByName() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getStatus(with(mockUser), with(NAME));
            will(returnValue(false));
        }});

        validateApiContract("/system.provisioning.powermanagement/getStatus", "GET")
                .withParams(Map.of("name", new String[] {NAME}))
                .onHandlerMethod("getStatus", User.class, String.class);
    }
}
