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

package com.redhat.rhn.frontend.struts.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.util.test.CSVWriterTest;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.struts.BaseSetListAction;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.testing.ActionHelper;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * RhnListActionTest - test RhnListAction code.
 */
public class BaseSetListActionTest extends RhnBaseTestCase {

    private TestSetupListAction tla;
    private ActionHelper sah;

    /**
     * {@inheritDoc}
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        tla = new TestSetupListAction();
        sah = new ActionHelper();
        sah.setUpAction(tla);
        sah.setupClampListBounds();
        sah.getRequest().addParameter("submitted", "false");
    }

    /**
     * Test to make sure we check for the right filter value string
     * @throws Exception something bad happened
     */
    @Test
    public void testExecute() throws Exception {
        sah.executeAction();
        assertNotNull(sah.getRequest().getAttribute("pageList"));
        DataResult dr = (DataResult) sah.getRequest().getAttribute("pageList");
        assertEquals(11, dr.getStart());
        assertEquals(200, dr.getTotalSize());
    }

    public class TestSetupListAction extends BaseSetListAction {

        @Override
        protected DataResult<Map<String, String>> getDataResult(RequestContext rctx, PageControl pc) {

            List<Map<String, String>> values = new LinkedList<>();
            for (int i = 0; i < 20; i++) {
                values.addAll(CSVWriterTest.getTestListOfMaps());
            }
            DataResult<Map<String, String>> dr = new DataResult<>(values);
            if (pc != null) {
                dr = dr.subList(pc.getStart(), pc.getEnd());
            }
            return dr;
        }

        @Override
        public RhnSetDecl getSetDecl() {
            return RhnSetDecl.TEST;
        }
    }
}
