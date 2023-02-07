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
package com.redhat.rhn.frontend.action.kickstart.cobbler.test;

import com.redhat.rhn.testing.RhnMockStrutsTestCase;

import org.junit.jupiter.api.Test;

/**
 * CobblerSnippetListSetupTest
 */
public class CobblerSnippetEditActionTest extends RhnMockStrutsTestCase {

    @Test
    public void testExecute() {
        setRequestPathInfo("/kickstart/cobbler/CobblerSnippetEdit");
        addRequestParameter("name", "redhat_register");
        actionPerform();
    }

    /**
     * TODO: Right now this blows up with a permission denied.
    @Test
    public void testSubmitExecute() throws Exception {
        setRequestPathInfo("/kickstart/cobbler/CobblerSnippetEdit");
        addRequestParameter("name", "redhat_register");
        addSubmitted();
        actionPerform();
    }


    @Test
    public void testDelete() throws Exception {
        setRequestPathInfo("/kickstart/cobbler/CobblerSnippetDelete");
        addRequestParameter("name", "pre_install_network_config.rpmnew");
        addSubmitted();
        actionPerform();
        verifyActionMessage("cobbler.snippet.couldnotdelete.message");
    }*/

}

