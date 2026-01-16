/*
 * Copyright (c) 2011--2025 SUSE LLC
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.frontend.servlets;

import static com.redhat.rhn.frontend.servlets.RhnHttpServletRequest.ACTIVE_LANG_ATTR;
import static com.redhat.rhn.testing.RhnMockHttpServletRequest.DEFAULT_SERVER_NAME;
import static com.redhat.rhn.testing.RhnMockHttpServletRequest.LOCALHOST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.redhat.rhn.testing.RhnMockHttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Unit tests for RhnHttpServletRequest.
 */
public class RhnHttpServletRequestTest {

    private RhnMockHttpServletRequest baseRequest;
    private RhnHttpServletRequest request;

    @Before
    public void setUp() {
        baseRequest = new RhnMockHttpServletRequest();
        request = new RhnHttpServletRequest(baseRequest);
    }

    @Test
    public void testDefaultConstructor() {
        assertAll(
                "Default constructor values",
                () -> assertEquals(
                        Collections.singletonList(ACTIVE_LANG_ATTR),
                        Collections.list(request.getAttributeNames())
                ),
                () -> assertFalse(request.getHeaderNames().hasMoreElements()),
                () -> assertFalse(request.getLocales().hasMoreElements()),
                () -> assertTrue(request.getParameterMap().isEmpty()),
                () -> assertEquals(0, request.getCookies().length),
                () -> assertEquals(DEFAULT_SERVER_NAME, request.getServerName()),
                () -> assertEquals("/mlm/network/somepage.do", request.getRequestURI()),
                () -> assertEquals(
                        "http://host.mlm.suse.com/mlm/network/somepage.do",
                        request.getRequestURL().toString()
                ),
                () -> assertEquals(StringUtils.EMPTY, request.getContextPath()),
                () -> assertEquals(StringUtils.EMPTY, request.getServletPath()),
                () -> assertNull(request.getPathInfo()),
                () -> assertNull(request.getQueryString()),
                () -> assertNotNull(request.getSession()),
                () -> assertEquals("POST", request.getMethod()),
                () -> assertEquals(80, request.getServerPort()),
                () -> assertEquals("127.0.0.1", request.getRemoteAddr()),
                () -> assertEquals(LOCALHOST, request.getRemoteHost()),
                () -> assertEquals(12345, request.getRemotePort()),
                () -> assertEquals("127.0.0.1", request.getLocalAddr()),
                () -> assertEquals(LOCALHOST, request.getLocalName()),
                () -> assertEquals(8080, request.getLocalPort()),
                () -> assertEquals("http", request.getProtocol()),
                () -> assertEquals("http", request.getScheme()),
                () -> assertEquals(-1, request.getContentLength()),
                () -> assertFalse(request.isSecure())
        );
    }

    @Test
    public void testOverrideServerName() {
        assertEquals(DEFAULT_SERVER_NAME, request.getServerName());
        baseRequest.setHeader("X-Server-Hostname", "otherhost.mlm.suse.com");
        assertEquals("otherhost.mlm.suse.com", request.getServerName());
    }

    @Test
    public void testSetAndGetProtocol() {
        assertFalse(baseRequest.isSecure());
        assertEquals("http", request.getProtocol());

        baseRequest.setSecure(true);
        assertTrue(baseRequest.isSecure());
        assertEquals("https", request.getProtocol());
    }

    @Test
    public void testAttributes() {
        String attributeKey = "foo";

        assertEquals(Collections.singletonList(ACTIVE_LANG_ATTR), Collections.list(request.getAttributeNames()));

        // simple attribute set
        baseRequest.setAttribute(attributeKey, "bar");
        assertEquals("bar", request.getAttribute(attributeKey));

        // attribute override test
        request.setAttribute(attributeKey, "overridden");
        assertEquals("overridden", request.getAttribute(attributeKey));

        // attribute removal test
        request.removeAttribute(attributeKey);
        assertNull(request.getAttribute(attributeKey));
    }

    @Test
    public void testSetAndGetLocales() {
        List<Locale> locales = Arrays.asList(Locale.US, Locale.FRANCE);

        assertFalse(request.getLocales().hasMoreElements());

        baseRequest.setLocales(locales);
        Enumeration<Locale> browserLocales = request.getBrowserLocales();
        List<Locale> browserLocaleList = Collections.list(browserLocales);
        assertTrue(browserLocaleList.contains(Locale.US));
        assertTrue(browserLocaleList.contains(Locale.FRANCE));
    }

    @Test
    public void testHeaders() {
        String headerA = "header_A_key";
        String headerB = "header_B_key";
        String expectedHeaderA = "header_a_original_value";
        String expectedHeaderB = "header_b_overriden_value";

        assertFalse(request.getHeaderNames().hasMoreElements());

        baseRequest.setHeader(headerA, expectedHeaderA);
        baseRequest.setHeader(headerB, "header_b_original_value");
        baseRequest.setHeader(headerB, expectedHeaderB);

        assertEquals(2, Collections.list(request.getHeaderNames()).size());
        assertEquals(expectedHeaderA, request.getHeader(headerA));
        assertEquals(expectedHeaderB, request.getHeader(headerB));
    }

    @Test
    public void testParameters() {
        String paramAKey = "paramAKey";
        String paramAValue1 = "param_a_value_1";
        List<String> expectedParamAValues = Collections.singletonList(paramAValue1);

        String paramBKey = "paramBKey";
        String paramBValue1 = "param_b_value_1";
        String paramBValue2 = "param_b_value_2";
        List<String> expectedParamBValues = Arrays.asList(paramBValue1, paramBValue2);

        assertTrue(request.getParameterMap().isEmpty());

        baseRequest.addParameter(paramAKey, paramAValue1);
        baseRequest.addParameter(paramBKey, paramBValue1);
        baseRequest.addParameter(paramBKey, paramBValue2);

        Map<String, String[]> parameterMap = request.getParameterMap();
        assertEquals(2, parameterMap.size());
        assertEquals(expectedParamAValues, Arrays.asList(parameterMap.get(paramAKey)));
        assertEquals(expectedParamBValues, Arrays.asList(parameterMap.get(paramBKey)));

        // test parameter consumption and it's order
        assertEquals(paramBValue1, request.getParameter(paramBKey));
        assertEquals(paramBValue2, request.getParameter(paramBKey));
        assertNull(request.getParameter(paramBKey));
    }
}
