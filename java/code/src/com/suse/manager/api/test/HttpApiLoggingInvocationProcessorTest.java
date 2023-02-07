/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.manager.api.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.legacy.UserImpl;

import com.suse.manager.api.HttpApiLoggingInvocationProcessor;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class HttpApiLoggingInvocationProcessorTest {

    private final HttpApiLoggingInvocationProcessor processor = new HttpApiLoggingInvocationProcessor();

    @Test
    public void testGetHandlerAndMethodNames() {
        String url = "system/provisioning/powermanagement/listTypes";
        String[] urlTokens = url.split("/");
        String handlerName = processor.getHandlerName(urlTokens);
        assertEquals("system.provisioning.powermanagement", handlerName);
    }

    @Test
    public void testGetCallerLogin() {
        String login = "userLoginTest";
        User user = new UserImpl();
        user.setLogin(login);
        assertEquals("none", processor.getCallerLogin(null));
        assertEquals(login, processor.getCallerLogin(user));
    }

    @Test
    public void getParamValueToLog() {
        String value = "testValue";
        String valueToLog = processor.getParamValueToLog("system", "login", "username", value);
        assertEquals(value, valueToLog);

        valueToLog = processor.getParamValueToLog("auth", "login", "password", value);
        assertEquals("******", valueToLog);
    }

    @Test
    public void testParseBody() {
        String body = "[\"not object json body\"]";
        Map<String, String> parsed = processor.parseBody(body);
        assertTrue(parsed.isEmpty());

        body = "{ \"var1\": \"value1\", \"var2\": \"value2\" }";
        parsed = processor.parseBody(body);
        assertTrue(parsed.containsKey("var1"));
        assertTrue(parsed.containsKey("var2"));
        assertEquals("\"value1\"", parsed.get("var1"));
        assertEquals("\"value2\"", parsed.get("var2"));
    }

    @Test
    public void testProcessParams() {
        Map<String, String> params = new HashMap<>();
        params.put("sid", "100001");
        params.put("user", "userTest");
        StringBuilder result = processor.processParams(
            params, "system.provisioning.powermanagement", "getDetails"
        );
        assertEquals("user=userTest, sid=100001", result.toString());
    }
}
