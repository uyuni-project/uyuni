/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.servlets.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.frontend.servlets.CreateRedirectURI;
import com.redhat.rhn.testing.MockObjectTestCase;

import com.suse.manager.webui.utils.LoginHelper;

import org.apache.commons.lang3.StringUtils;
import org.jmock.Expectations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

/**
 * CreateRedirectURITest
 */
/**
 * CreateRedirectURITest
 */
/**
 * CreateRedirectURITest
 */
/**
 * CreateRedirectURITest
 */
/**
 * CreateRedirectURITest
 */
/**
 * CreateRedirectURITest
 */
/**
 * CreateRedirectURITest
 */
/**
 * CreateRedirectURITest
 */
/**
 * CreateRedirectURITest
 */
public class CreateRedirectURITest extends MockObjectTestCase {

    private HttpServletRequest mockRequest;

    @BeforeEach
    public void setUp() throws Exception {
        mockRequest = mock(HttpServletRequest.class);
    }

    private HttpServletRequest getMockRequest() {
        return mockRequest;
    }

    /**
     * @throws Exception something bad happened
     */
    @Test
    public final void testExecuteWhenRequestHasNoParams() throws Exception {
      context().checking(new Expectations() { {
          allowing(mockRequest).getParameterNames();
          will(returnValue(new Vector<String>().elements()));
          allowing(mockRequest).getRequestURI();
          will(returnValue("/YourRhn.do"));
      } });

        CreateRedirectURI command = new CreateRedirectURI();
        String redirectUrl = command.execute(getMockRequest());

        assertEquals("/YourRhn.do?", redirectUrl);
    }

    /**
     * @throws Exception something bad happened
     */
    @Test
    public final void testExecuteWhenRequestHasParams() throws Exception {
        final String paramName = "foo";
        final String paramValue = "param value = bar#$%!";

        String expected = "/YourRhn.do?foo=" + URLEncoder.encode(paramValue, "UTF-8") + "&";

        final Vector<String> paramNames = new Vector<>();
        paramNames.add(paramName);

        context().checking(new Expectations() { {
            allowing(mockRequest).getParameterNames();
            will(returnValue(paramNames.elements()));
            allowing(mockRequest).getParameter(paramName);
            will(returnValue(paramValue));
            allowing(mockRequest).getRequestURI();
            will(returnValue("/YourRhn.do"));

        } });

        CreateRedirectURI command = new CreateRedirectURI();
        String redirectURI = command.execute(getMockRequest());

        assertEquals(expected, redirectURI);
    }

    @Test
    public final void testExecuteWhenRedirectURIExceedsMaxLength() throws Exception {
        final String url = StringUtils.rightPad("/YourRhn.do",
                (int)CreateRedirectURI.MAX_URL_LENGTH + 1, "x");

        context().checking(new Expectations() { {
            allowing(mockRequest).getParameterNames();
            will(returnValue(new Vector<String>().elements()));
            allowing(mockRequest).getRequestURI();
            will(returnValue(url));
        } });

        CreateRedirectURI command = new CreateRedirectURI();
        String redirectUrl = command.execute(getMockRequest());

        assertEquals(LoginHelper.DEFAULT_URL_BOUNCE, redirectUrl);
    }

}
