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
package com.redhat.rhn.frontend.servlets.ajax.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.session.WebSession;
import com.redhat.rhn.domain.session.WebSessionFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.servlets.ajax.AjaxHandlerServlet;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.UserTestUtils;

import com.mockobjects.servlet.MockRequestDispatcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

public class AjaxHandlerServletTest extends AjaxHandlerServlet {

    private RhnMockHttpServletRequest request;
    private HttpServletResponse response;
    private AjaxHandlerServlet servlet;
    private User user;

    @BeforeEach
    public void setUp() throws Exception {
        user = UserTestUtils.findNewUser("testUser", "testOrg" +
                this.getClass().getSimpleName());
        WebSession session = WebSessionFactory.createSession();
        session.setWebUserId(user.getId());
        request = new RhnMockHttpServletRequest();

        request.addAttribute("session", session);
        request.setupGetRequestDispatcher(new MockRequestDispatcher());
        response = new RhnMockHttpServletResponse();
        servlet = new AjaxHandlerServlet();
    }

    /**
     * Test handlers which set parentUrl according to the request URI when processing it
     */
    @Test
    public void testListTagHelperHandlers() {
        Set<String> uris = Set.of(
                "/rhn/ajax/systems-groups",
                "/rhn/ajax/critical-systems",
                "/rhn/ajax/recent-systems"
        );
        for (String uri : uris) {
            // setup request URI
            request.setupGetRequestURI(uri);

            // parentURL is not set before handling the request
            assertNotEquals(uri, request.getAttribute("parentUrl"));

            servlet.doPost(request, response);

            // parentURL is set according to the URI after handling the request
            assertEquals(uri, request.getAttribute("parentUrl"));
        }
    }

    @Test
    public void testInactiveSystems() {
        // setup request URI
        String uri = "/rhn/ajax/inactive-systems";
        request.setupGetRequestURI(uri);

        // inactiveSystemsClass is not set before handling the request
        assertNull(request.getAttribute("inactiveSystemsClass"));

        servlet.doPost(request, response);

        // either inactiveSystemList or inactiveSystemsEmpty is set after handling the request
        boolean renderedSomething =
                request.getAttribute("inactiveSystemList") != null ||
                        request.getAttribute("inactiveSystemsEmpty") != null;
        assertTrue(renderedSomething);
    }

    @Test
    public void testPendingActions() {
        // setup request URI
        String uri = "/rhn/ajax/pending-actions";
        request.setupGetRequestURI(uri);

        // showPendingActions is not set before handling the request
        assertNull(request.getAttribute("showPendingActions"));

        servlet.doPost(request, response);

        // showPendingActions is properly set after handling the request
        assertTrue((Boolean) request.getAttribute("showPendingActions"));
    }

    @Test
    public void testLatestErrata() {
        // setup request URI
        String uri = "/rhn/ajax/latest-errata";
        request.setupGetRequestURI(uri);

        // showErrata is not set before handling the request
        assertNull(request.getAttribute("showErrata"));

        servlet.doPost(request, response);

        // showErrata is properly set after handling the request
        assertTrue((Boolean) request.getAttribute("showErrata"));
    }

    /**
     * Test a scenario where the request body needs to be parsed in order to handle the request.
     */
    @Test
    public void testHandlerWithRequestBody() throws IOException {
        // setup request URI
        String uri = "/rhn/ajax/action-chain-entries";
        request.setupGetRequestURI(uri);

        // setup data
        Integer sortOrder = 13;
        ActionChain a = ActionChainFactory.createActionChain("test-label", user);

        // setup request body
        Reader input = new StringReader("{ \"actionChainId\":" + a.getId() + ", \"sortOrder\": 13 }");
        request.setupGetReader(new BufferedReader(input));

        // sortOrder is not set before handling the request
        assertNull(request.getAttribute("sortOrder"));
        servlet.doPost(request, response);

        // sortOrder is set according to request body
        assertEquals(sortOrder, request.getAttribute("sortOrder"));
    }
}
