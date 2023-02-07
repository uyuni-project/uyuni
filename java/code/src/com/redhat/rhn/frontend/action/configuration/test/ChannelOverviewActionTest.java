/*
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
package com.redhat.rhn.frontend.action.configuration.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigChannelType;
import com.redhat.rhn.domain.config.ConfigFile;
import com.redhat.rhn.domain.config.ConfigurationFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.manager.configuration.ChannelSummary;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.Test;

import servletunit.HttpServletRequestSimulator;

public class ChannelOverviewActionTest extends RhnMockStrutsTestCase {

    public static final String FWD_SUCCESS = "/configuration/ChannelOverview.do?ccid=";
    public static final String FWD_ERROR = "/WEB-INF/pages/configuration/channel/channelcreate.jsp";

    @Test
    public void testExecuteNoFiles() {
        UserTestUtils.addUserRole(user, RoleFactory.CONFIG_ADMIN);

        ConfigChannel cc = ConfigTestUtils.createConfigChannel(user.getOrg());
        ConfigFile cf = ConfigTestUtils.createConfigFile(cc);

        long ccid = cc.getId();
        setRequestPathInfo("/configuration/ChannelOverview");
        addRequestParameter("ccid", "" + ccid);
        actionPerform();
        assertNotNull(request.getAttribute("ccid"));
        assertTrue(request.getAttribute("ccid") instanceof Long);
        assertEquals(ccid, ((Long)request.getAttribute("ccid")).longValue());
        assertNotNull(request.getAttribute("channel"));
        assertNotNull(request.getAttribute("summary"));
        assertTrue(request.getAttribute("channel") instanceof ConfigChannel);
        assertTrue(request.getAttribute("summary") instanceof ChannelSummary);
    }

    @Test
    public void testCreateChannelWithValidLabel() {
        ConfigChannel channel = doCreateChannelAction("channel1");
        assertNotNull(channel);

        String forwardPath = getActualForward();
        assertTrue(forwardPath.startsWith(FWD_SUCCESS));
    }

    @Test
    public void testCreateChannelWithDotsInLabel() {
        ConfigChannel channel = doCreateChannelAction("channel.with.dots");
        assertNull(channel);

        String forwardPath = getActualForward();
        assertTrue(forwardPath.startsWith(FWD_ERROR));
    }

    @Test
    public void testCreateChannelWithSpacesInLabel() {
        ConfigChannel channel = doCreateChannelAction("invalid_channel_123 with spaces");
        assertNull(channel);

        String forwardPath = getActualForward();
        assertTrue(forwardPath.startsWith(FWD_ERROR));
    }

    private ConfigChannel doCreateChannelAction(String channelLabel) {
        // create a config channel via action and return it
        request.setMethod(HttpServletRequestSimulator.POST);
        setRequestPathInfo("/configuration/ChannelCreate");
        addRequestParameter(RhnAction.SUBMITTED, Boolean.TRUE.toString());
        addRequestParameter("created", Boolean.TRUE.toString());
        addRequestParameter("type", ConfigChannelType.normal().getLabel());
        addRequestParameter("cofLabel", channelLabel);
        addRequestParameter("cofName", "Conf channel name");
        addRequestParameter("cofDescription", "This is an awesome channel!");

        actionPerform();

        return ConfigurationFactory.lookupConfigChannelByLabel(channelLabel, user.getOrg(),
                ConfigChannelType.normal());
    }
}
