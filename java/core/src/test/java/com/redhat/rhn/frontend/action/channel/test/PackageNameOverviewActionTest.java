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
package com.redhat.rhn.frontend.action.channel.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.testing.RhnMockStrutsTestCase;

import org.junit.jupiter.api.Test;


/**
 * PackageNameOverviewActionTest
 */
public class PackageNameOverviewActionTest extends RhnMockStrutsTestCase {

    @Test
    public void testInChannels() {
        String[] arches = {"channel-ia32", "channel-ia64"};
        setRequestPathInfo("/software/packages/NameOverview");
        addRequestParameter("channel_arch", arches);
        addRequestParameter("search_subscribed_channels", "");
        addRequestParameter("package_name", "kernel");
        actionPerform();
        assertNotNull(getActualForward());
        assertTrue(getActualForward().startsWith(
                "/WEB-INF/pages/software/packages/packagenameoverview.jsp"));
    }

    @Test
    public void testSubscribedChannels() {

    }

}
