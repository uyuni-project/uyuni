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

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.errata.NotifyAction;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.RhnMockDynaActionForm;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.TestUtils;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.junit.jupiter.api.Test;

/**
 * NotifyActionTest
 */
public class NotifyActionTest extends RhnBaseTestCase {

    @Test
    public void testNotifyAction() throws Exception {
        NotifyAction action = new NotifyAction();

        RhnMockHttpServletRequest request = TestUtils.getRequestWithSessionAndUser();
        ActionMapping mapping = new ActionMapping();
        ActionForward def = new ActionForward(RhnHelper.DEFAULT_FORWARD, "path", false);
        RhnMockDynaActionForm form = new RhnMockDynaActionForm();
        RhnMockHttpServletResponse response = new RhnMockHttpServletResponse();
        mapping.addForwardConfig(def);

        RequestContext requestContext = new RequestContext(request);

        User user = requestContext.getCurrentUser();
        Errata published = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        Channel c = ChannelFactoryTest.createBaseChannel(user);
        published.addChannel(c);

        //test default case
        request.addParameter("eid", published.getId().toString());
        ActionForward result = action.execute(mapping, form, request, response);
        assertEquals(RhnHelper.DEFAULT_FORWARD, result.getName());

        Long id = published.getId();
        flushAndEvict(published);
        Errata errata = ErrataManager.lookupErrata(id, user);
        assertEquals(1, errata.getNotificationQueue().size());
    }
}
