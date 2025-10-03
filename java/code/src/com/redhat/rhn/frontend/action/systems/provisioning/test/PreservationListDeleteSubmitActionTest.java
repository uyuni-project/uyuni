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
package com.redhat.rhn.frontend.action.systems.provisioning.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.domain.common.CommonFactory;
import com.redhat.rhn.domain.common.FileList;
import com.redhat.rhn.domain.common.test.FileListTest;
import com.redhat.rhn.frontend.action.systems.provisioning.PreservationListDeleteSubmitAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.testing.ActionHelper;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForward;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;


/**
 * PreservationListDeleteSubmitActionTest
 */
public class PreservationListDeleteSubmitActionTest extends RhnBaseTestCase {
    private Action action = null;

    @Override
    @BeforeEach
    public void setUp() {
        action = new PreservationListDeleteSubmitAction();
    }

    /**
     * Test that we forward to confirm
     * @throws Exception if test fails
     */
    @Test
    public void testForwardToConfirm() throws Exception {
        ActionHelper ah = new ActionHelper();
        ah.setUpAction(action, "delete");
        ah.getRequest().setRequestURL("");
        ah.getRequest().addParameter("newset", (String)null);
        ah.getRequest().addParameter("items_on_page", (String)null);
        List ids = new LinkedList<>();

        // give list some FileLists
        for (int i = 0; i < 5; i++) {
            FileList fl = FileListTest.createTestFileList(
                                                   ah.getUser().getOrg());
            CommonFactory.saveFileList(fl);
            ids.add(fl.getId().toString());

            // clean up
            CommonFactory.removeFileList(fl);
        }

        ah.getRequest().addParameter("items_selected",
                (String[]) ids.toArray(new String[0]));
        ah.setupClampListBounds();
        ActionForward testforward = ah.executeAction("forwardToConfirm");
        assertEquals("path?lower=10", testforward.getPath());
        assertEquals("delete", testforward.getName());
    }

    /**
     * Test that we forward to confirm
     * @throws Exception if test fails
     */
    @Test
    public void testNothingSeleted() throws Exception {
        ActionHelper ah = new ActionHelper();
        ah.setUpAction(action, RhnHelper.DEFAULT_FORWARD);
        ah.getRequest().setRequestURL("");
        ah.getRequest().addParameter("newset", (String)null);
        ah.getRequest().addParameter("items_on_page", (String)null);
        ah.getRequest().addParameter("items_selected", (String[]) null);
        ah.setupClampListBounds();
        ActionForward testforward = ah.executeAction("forwardToConfirm");
        assertEquals("path?lower=10", testforward.getPath());
        assertEquals(RhnHelper.DEFAULT_FORWARD, testforward.getName());
    }

}
