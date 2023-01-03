/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.rhnpackage.ssm.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.taglibs.list.ListTagUtil;
import com.redhat.rhn.frontend.taglibs.list.TagHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.ListHelper;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 */
public class SelectVerifyPackagesActionTest extends RhnMockStrutsTestCase {

    private SsmActionTestUtils utils;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setRequestPathInfo("/ssm/PackageVerify");

        utils = new SsmActionTestUtils(user);
    }

    @Test
    public void testNonDispatch() throws Exception {
        // Setup
        utils.initSsmEnvironment();

        // Test
        actionPerform();

        // Verify
        verifyForward(RhnHelper.DEFAULT_FORWARD);

        Object pageList = getRequest().getAttribute(RequestContext.PAGE_LIST);
        assertNotNull(pageList);

        DataResult result = (DataResult)pageList;
        assertEquals(2, result.size());
    }

    @Test
    public void testDispatch() throws Exception {
        // Setup
        utils.initSsmEnvironment();
        selectAllPackagesInUI();
        addDispatchCall("ssm.package.verify.select.confirm");

        // Test
        actionPerform();

        // Verify
        verifyForward("confirm");
    }

    private void selectAllPackagesInUI() {

        String[] itemsSelected = new String[] {
            "dummy"
        };

        String listName = TagHelper.generateUniqueName(ListHelper.LIST);
        String requestParameterName = ListTagUtil.makePageItemsName(listName);
        addRequestParameter(requestParameterName, itemsSelected);

        requestParameterName = ListTagUtil.makeSelectedItemsName(listName);
        addRequestParameter(requestParameterName, itemsSelected);
    }
}
