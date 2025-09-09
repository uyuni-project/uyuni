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
package com.redhat.rhn.frontend.action.errata.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.SetLabels;
import com.redhat.rhn.frontend.action.common.test.RhnSetActionTest;
import com.redhat.rhn.frontend.action.errata.AffectedSystemsAction;
import com.redhat.rhn.frontend.struts.RequestContext.Pagination;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.testing.ActionHelper;
import com.redhat.rhn.testing.MockObjectTestCase;
import com.redhat.rhn.testing.RhnMockDynaActionForm;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.TestUtils;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * AffectedSystemsActionTest
 */
public class AffectedSystemsActionTest extends MockObjectTestCase {

    @BeforeEach
    public void setUp() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @Test
    public void testApply() {
        AffectedSystemsAction action = new AffectedSystemsAction();
        final ActionForward forward = new ActionForward("test", "path", true);
        RhnMockHttpServletRequest request = TestUtils.getRequestWithSessionAndUser();
        RhnMockHttpServletResponse response = new RhnMockHttpServletResponse();
        RhnMockDynaActionForm form = new RhnMockDynaActionForm();

        // No systems selected
        final ActionMapping mapping = mock(ActionMapping.class, "mapping");
        context().checking(new Expectations() { {
            oneOf(mapping).findForward(RhnHelper.DEFAULT_FORWARD);
            will(returnValue(forward));
        } });

        request.addParameter("items_selected", (String[]) null);
        request.addParameter("items_on_page", (String[]) null);
        addPagination(request);
        request.addParameter("filter_string", "");
        request.addParameter("eid", "12345");

        ActionForward sameForward = action.applyErrata(mapping, form, request, response);
        assertTrue(sameForward.getPath().startsWith("path?"));
        assertTrue(sameForward.getPath().contains("eid=12345"));
        assertTrue(sameForward.getPath().contains("lower=10"));

        // With systems selected
        context().checking(new Expectations() { {
            oneOf(mapping).findForward("confirm");
            will(returnValue(forward));
        } });

        request.addParameter("items_selected", "123456");
        request.addParameter("items_on_page", (String[]) null);
        request.addParameter("eid", "54321");

        sameForward = action.applyErrata(mapping, form, request, response);
        assertEquals("path?eid=54321", sameForward.getPath());
    }

    private void addPagination(RhnMockHttpServletRequest r) {
        r.addParameter(Pagination.FIRST.getElementName(), "someValue");
        r.addParameter(Pagination.FIRST.getLowerAttributeName(), "10");
        r.addParameter(Pagination.PREV.getElementName(), "0");
        r.addParameter(Pagination.PREV.getLowerAttributeName(), "");
        r.addParameter(Pagination.NEXT.getElementName(), "20");
        r.addParameter(Pagination.NEXT.getLowerAttributeName(), "");
        r.addParameter(Pagination.LAST.getElementName(), "");
        r.addParameter(Pagination.LAST.getLowerAttributeName(), "20");
        r.addParameter("lower", "10");
    }

    @Test
    public void testSelectAll() throws Exception {
        AffectedSystemsAction action = new AffectedSystemsAction();
        ActionHelper ah = new ActionHelper();
        ah.setUpAction(action);
        ah.setupProcessPagination();

        User user = ah.getUser();
        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        Errata errata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());

        for (int i = 0; i < 4; i++) {
            Server server = ServerFactoryTest.createTestServer(user, true);
            ErrataFactoryTest.updateNeedsErrataCache(
                    errata.getPackages().iterator().next().getId(),
                    server.getId(), errata.getId());
        }

        ah.getRequest().addParameter("eid", errata.getId().toString());
        ah.getRequest().addParameter("eid", errata.getId().toString()); // stupid mock
        ah.getRequest().addParameter("items_on_page", (String[]) null);
        ah.getRequest().addParameter("items_selected", (String[]) null);
        ah.executeAction("selectall");

        RhnSetActionTest.verifyRhnSetData(ah.getUser().getId(),
                SetLabels.AFFECTED_SYSTEMS_LIST, 4);
    }

}
