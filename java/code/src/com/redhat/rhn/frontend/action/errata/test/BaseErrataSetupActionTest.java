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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.errata.BaseErrataSetupAction;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.testing.MockHttpServletRequest;
import com.redhat.rhn.testing.MockTestUtils;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.jmock.Mockery;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletResponse;

/**
 * BaseErrataSetupActionTest
 */
public class BaseErrataSetupActionTest extends RhnBaseTestCase {

    private final Mockery context = new Mockery();

    @Test
    public void testExecute() throws Exception {
        BaseErrataSetupAction action = new BaseErrataSetupAction();

        ActionMapping mapping = new ActionMapping();
        ActionForward def = new ActionForward(RhnHelper.DEFAULT_FORWARD, "path", false);
        DynaActionForm form = new DynaActionForm();
        MockHttpServletRequest request = MockTestUtils.getRequestWithSessionAndUser();
        HttpServletResponse response = context.mock(HttpServletResponse.class);
        mapping.addForwardConfig(def);

        RequestContext requestContext = new RequestContext(request);

        User user = requestContext.getCurrentUser();
        Errata published = ErrataFactoryTest.createTestErrata(user.getOrg().getId());

        //test lookup exception
        request.setupAddParameter("eid", Long.valueOf(-92861).toString());
        try {
            action.execute(mapping, form, request, response);
            fail();
        }
        catch (LookupException e) {
            //Success!!!
        }

        //test default case
        request.setupAddParameter("eid", published.getId().toString());
        ActionForward result = action.execute(mapping, form, request, response);
        assertEquals(RhnHelper.DEFAULT_FORWARD, result.getName());
        assertNotNull(request.getAttribute("advisory"));
    }
}
