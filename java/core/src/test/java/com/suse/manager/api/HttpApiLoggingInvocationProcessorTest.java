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
package com.suse.manager.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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

}
