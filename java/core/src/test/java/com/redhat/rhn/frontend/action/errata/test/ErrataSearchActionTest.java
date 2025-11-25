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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.action.errata.test;

import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.frontend.action.BaseSearchAction;
import com.redhat.rhn.frontend.action.errata.ErrataSearchAction;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.testing.ActionHelper;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.Test;

/**
 * ErrataSearchActionTest
 */
public class ErrataSearchActionTest extends RhnBaseTestCase {

    @Test
    public void testExecute() throws Exception {

        ErrataSearchAction action = new ErrataSearchAction();
        ActionHelper ah = new ActionHelper();
        Errata e = ErrataFactoryTest.createTestErrata(UserTestUtils.createOrg(this).getId());
        String name = e.getAdvisory();

        ah.setUpAction(action, RhnHelper.DEFAULT_FORWARD);
        ah.getForm().set(BaseSearchAction.VIEW_MODE, BaseSearchAction.OPT_ADVISORY);
        ah.getForm().set(RhnAction.SUBMITTED, Boolean.TRUE);
        RhnMockHttpServletRequest request = ah.getRequest();
        request.addParameter(BaseSearchAction.SEARCH_STR, name);
        request.addParameter(BaseSearchAction.VIEW_MODE, BaseSearchAction.OPT_ADVISORY);
        request.addParameter(BaseSearchAction.FINE_GRAINED, "on");
        request.addParameter(BaseSearchAction.SUBMITTED, "true");

        ah.setupClampListBounds();

        ah.executeAction();
    }
}

