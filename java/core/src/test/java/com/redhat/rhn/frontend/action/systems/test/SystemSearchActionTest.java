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
package com.redhat.rhn.frontend.action.systems.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.action.BaseSearchAction;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;

import org.apache.struts.action.DynaActionForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * SystemSearchActionTest
 */
public class SystemSearchActionTest extends RhnMockStrutsTestCase {

    private Server s;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setRequestPathInfo("/systems/Search");
        s = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());

    }

    /**
     * This test tests multiple search results. The system search page, if
     * only one result is found, will forward you directly to that
     * system's SDC page instead of showing a list with one member
     * on the system search page. This test is expecting multiple systems to be found
     * and the user to be forwarded to the system search page with a list of systems
     * shown.
     */
    public void skipTestQueryWithResults() {
       /**
        * SystemSearch now talks to a Lucene search server.  This creates issues
        * for testing...you can't use a test util to create a system put it in the
        * DB and expect the search server to have the data indexed and ready to go.
        *
        * Will be marking this test to be skipped till a suitable test is implemented
        */
        s = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        addRequestParameter(RhnAction.SUBMITTED, Boolean.TRUE.toString());
        addRequestParameter(BaseSearchAction.SEARCH_STR, "redhat");
        addRequestParameter(BaseSearchAction.WHERE_TO_SEARCH, "all");
        addRequestParameter(BaseSearchAction.VIEW_MODE,
        "systemsearch_name_and_description");
        actionPerform();
        verifyForward(RhnHelper.DEFAULT_FORWARD);
        DataResult dr = (DataResult) request.getAttribute(RequestContext.PAGE_LIST);
        assertNotNull(dr);
        assertFalse(dr.isEmpty());
        assertNotNull(request.getAttribute(BaseSearchAction.VIEW_MODE));
        assertNotNull(request.getAttribute(BaseSearchAction.WHERE_TO_SEARCH));
        assertNotNull(request.getAttribute(BaseSearchAction.SEARCH_STR));
    }

    /**
     * This test is the case where only one system is found. It verfies
     * that the user is redirected to that system's SDC page.
     */
    public void skipTestQueryWithOneResult() {
        /**
         * SystemSearch now talks to a Lucene search server.  This creates issues
         * for testing...you can't use a test util to create a system put it in the
         * DB and expect the search server to have the data indexed and ready to go.
         *
         * Will be marking this test to be skipped till a suitable test is implemented
         */
        addRequestParameter(RhnAction.SUBMITTED, Boolean.TRUE.toString());
        addRequestParameter(BaseSearchAction.SEARCH_STR, s.getName());
        addRequestParameter(BaseSearchAction.WHERE_TO_SEARCH, "all");
        addRequestParameter(BaseSearchAction.VIEW_MODE,
        "systemsearch_name_and_description");
        actionPerform();
        System.err.println("getMockResponse() = " + getMockResponse());
        System.err.println("getMockResponse().getStatusCode() = " +
                getMockResponse().getStatusCode());
        assertEquals(302, getMockResponse().getStatusCode());
    }

    @Test
    public void testQueryWithoutResults() {
    }

    /**
     * This test verfies that if a bad view mode is passed in by the user,
     * the system search handles and catches any underlying exceptions
     * that might be caused by this, instead of allowing the exception to escalate
     * beyond the SystemSearchAction.
     */
    @Test
    public void testQueryWithBadParameter() {
        addRequestParameter(RhnAction.SUBMITTED, Boolean.TRUE.toString());
        addRequestParameter(BaseSearchAction.SEARCH_STR, s.getName());
        addRequestParameter(BaseSearchAction.WHERE_TO_SEARCH, "all");
        addRequestParameter(BaseSearchAction.VIEW_MODE,
        "all_your_systems_are_belong_to_us");
        actionPerform();
    }

    @Test
    public void testNoSubmit() {
        actionPerform();
        DynaActionForm formIn = (DynaActionForm) getActionForm();
        assertNotNull(formIn.get(BaseSearchAction.WHERE_TO_SEARCH));
        assertNotNull(request.getAttribute(BaseSearchAction.VIEW_MODE));
    }

    @Test
    public void testAlphaSubmitForNumericField() {
        addRequestParameter(RhnAction.SUBMITTED, Boolean.TRUE.toString());
        addRequestParameter(BaseSearchAction.SEARCH_STR, "abc");
        addRequestParameter(BaseSearchAction.WHERE_TO_SEARCH, "all");
        addRequestParameter(BaseSearchAction.VIEW_MODE,
                            "systemsearch_cpu_mhz_lt");
        actionPerform();
        verifyActionErrors(new String[] { "systemsearch.errors.numeric",
                "packages.search.connection_error" });
    }

    @Test
    public void testSmallAlphaSubmitForNumericField() {
        addRequestParameter(RhnAction.SUBMITTED, Boolean.TRUE.toString());
        addRequestParameter(BaseSearchAction.SEARCH_STR, "a");
        addRequestParameter(BaseSearchAction.WHERE_TO_SEARCH, "all");
        addRequestParameter(BaseSearchAction.VIEW_MODE,
                            "systemsearch_cpu_mhz_lt");
        actionPerform();
        verifyActionErrors(new String[] { "systemsearch.errors.numeric",
                "packages.search.connection_error" });
    }
}
