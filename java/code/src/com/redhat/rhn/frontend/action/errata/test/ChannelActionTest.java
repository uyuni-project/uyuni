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

import com.redhat.rhn.domain.access.AccessGroupFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.rhnset.RhnSetFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.errata.ChannelAction;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.testing.ActionHelper;
import com.redhat.rhn.testing.RhnJmockBaseTestCase;
import com.redhat.rhn.testing.RhnMockDynaActionForm;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.RhnMockHttpSession;
import com.redhat.rhn.testing.TestUtils;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.Test;

/**
 * ChannelActionTest
 */
public class ChannelActionTest extends RhnJmockBaseTestCase {

    @Test
    public void testPublish() throws Exception {
        ChannelAction action = new ChannelAction();

        ActionMapping mapping = new ActionMapping();
        ActionForward def = new ActionForward(RhnHelper.DEFAULT_FORWARD, "path", true);
        ActionForward publish = new ActionForward("publish", "path", true);
        ActionForward failure = new ActionForward("failure", "path", false);
        mapping.addForwardConfig(def);
        mapping.addForwardConfig(publish);
        mapping.addForwardConfig(failure);

        RhnMockHttpServletRequest request = TestUtils.getRequestWithSessionAndUser();
        RhnMockHttpServletResponse response = new RhnMockHttpServletResponse();
        RhnMockHttpSession session = new RhnMockHttpSession();
        request.setSession(session);
        request.setServerName("mymachine.rhndev.redhat.com");
        RhnMockDynaActionForm form = new RhnMockDynaActionForm();

        RequestContext requestContext = new RequestContext(request);

        User usr = requestContext.getCurrentUser();

        //create the errata
        Errata errata = ErrataFactoryTest.createTestErrata(usr.getOrg().getId());

        //We can't publish without selecting channels. Make sure we get an error.
        request.addParameter("eid", errata.getId().toString());
        request.addParameter("items_on_page", "");
        request.addParameter("items_selected", new String[0]);

        ActionForward result = action.publish(mapping, form, request, response);
        assertEquals("failure", result.getName());

        //now create the channel
        Channel c1 = ChannelFactoryTest.createTestChannel(usr);

        //setup the request
        request.addParameter("eid", errata.getId().toString());
        request.addParameter("items_on_page", "");
        request.addParameter("items_selected", c1.getId().toString());

        result = action.publish(mapping, form, request, response);
        assertEquals(result.getName(), "publish");
    }

    @Test
    public void testUpdateChannels() throws Exception {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);

        ChannelAction action = new ChannelAction();

        ActionMapping mapping = new ActionMapping();
        ActionForward def = new ActionForward(RhnHelper.DEFAULT_FORWARD, "path", true);
        ActionForward failure = new ActionForward("failure", "path", false);
        ActionForward push = new ActionForward("push", "path", false);
        mapping.addForwardConfig(def);
        mapping.addForwardConfig(failure);
        mapping.addForwardConfig(push);

        RhnMockHttpServletRequest request = TestUtils.getRequestWithSessionAndUser();
        RhnMockHttpServletResponse response = new RhnMockHttpServletResponse();
        RhnMockHttpSession session = new RhnMockHttpSession();
        request.setSession(session);
        request.setServerName("mymachine.rhndev.redhat.com");
        RhnMockDynaActionForm form = new RhnMockDynaActionForm();

        RequestContext requestContext = new RequestContext(request);

        User user = requestContext.getCurrentUser();
        user.addToGroup(AccessGroupFactory.CHANNEL_ADMIN);

        //create the errata
        Errata errata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        //get the id for errata and flush so things get stored to the db
        Long id = errata.getId();
        flushAndEvict(errata);

        //We can't take away all channels. make sure we get an error
        request.addParameter("eid", id.toString());
        request.addParameter("items_on_page", "");
        request.addParameter("items_selected", new String[0]);

        ActionForward result = action.publish(mapping, form, request, response);
        assertEquals("failure", result.getName());

        //make sure we can add channels
        //first create the channel family
        //now create the channels
        Channel c1 = ChannelFactoryTest.createTestChannel(user);
        Channel c2 = ChannelFactoryTest.createTestChannel(user);

        //setup the request
        String[] selected = {c1.getId().toString(), c2.getId().toString()};
        request.addParameter("eid", id.toString());
        request.addParameter("items_on_page", "");
        request.addParameter("items_selected", selected);

        result = action.updateChannels(mapping, form, request, response);
        assertEquals("push", result.getName());

        //Lookup the errata and make sure there are at least 2 channels
        Errata e2 = ErrataManager.lookupErrata(errata.getId(), user);
        int size = e2.getChannels().size();
        assertTrue(size >= 2);

        //clear the set
        RhnSetFactory.save(RhnSetDecl.CHANNELS_FOR_ERRATA.get(user));
        RhnSetDecl.CHANNELS_FOR_ERRATA.clear(user);
        RhnSetFactory.remove(RhnSetDecl.CHANNELS_FOR_ERRATA.get(user));

        //make sure we can take away channels
        //setup the request
        String[] selected2 = {c2.getId().toString()};
        request.addParameter("eid", id.toString());
        request.addParameter("items_on_page", "");
        request.addParameter("items_selected", selected2);
        result = action.updateChannels(mapping, form, request, response);

        assertEquals("push", result.getName());

        Errata e3 = ErrataManager.lookupErrata(errata.getId(), user);
        assertTrue(e3.getChannels().size() < size); //less than before
    }

    @Test
    public void testSelectAll() throws Exception {
        ChannelAction action = new ChannelAction();
        ActionHelper ah = new ActionHelper();
        ah.setUpAction(action);
        ah.setupProcessPagination();

        User user = ah.getUser();
        Errata errata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());


        for (int i = 0; i < 4; i++) {
            ChannelFactoryTest.createTestChannel(user);
        }

        ah.getRequest().addParameter("eid", errata.getId().toString());
        ah.getRequest().addParameter("items_on_page", (String[])null);
        ah.getRequest().addParameter("items_selected", (String[])null);
        ah.getRequest().addParameter("returnvisit", "false");
        ah.executeAction("selectall");

        //satellite could already have some channels
        RhnSet set = RhnSetDecl.CHANNELS_FOR_ERRATA.get(user);
        assertTrue(set.size() >= 4);
    }
}
