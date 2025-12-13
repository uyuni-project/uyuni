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
package com.redhat.rhn.frontend.taglibs.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.frontend.taglibs.ListTag;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.RhnMockJspWriter;
import com.redhat.rhn.testing.RhnMockPageContext;
import com.redhat.rhn.testing.TagTestHelper;
import com.redhat.rhn.testing.TagTestUtils;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

/**
 * ColumnTagTest
 */
public class ListTagTest extends RhnBaseTestCase {

    @Test
    public void testConstructor() {
        ListTag lt = new ListTag();
        assertNotNull(lt);
        assertNull(lt.getPageList());
    }

    @Test
    public void testLegends() throws Exception {
        ListTag lt = new ListTag();

        TagTestHelper tth = TagTestUtils.setupTagTest(lt, new URL("http://localhost/"));
        RhnMockPageContext pc = tth.getPageContext();
        RhnMockHttpServletRequest request = new RhnMockHttpServletRequest();

        pc.setRequest(request);
        lt.setPageContext(pc);

        //test null
        lt.setLegend(null);
        lt.setNoDataText("none.message");
        pc.getRequest().setAttribute("legends", null);
        lt.doStartTag();
        assertNull(pc.getRequest().getAttribute("legends"));

        //test 1
        lt.setLegend("yankee");
        lt.setNoDataText("none.message");
        pc.getRequest().setAttribute("legends", "");
        lt.doStartTag();
        assertEquals("yankee", pc.getRequest().getAttribute("legends"));

        //test > 1
        lt.setLegend("foxtrot");
        lt.setNoDataText("none.message");
        pc.getRequest().setAttribute("legends", "yankee,hotel");
        lt.doStartTag();
        assertEquals("yankee,hotel,foxtrot", pc.getRequest().getAttribute("legends"));
    }

    @Test
    public void testTagNoOutput() throws Exception {
        ListTag lt = new ListTag();
        DataResult dr = new DataResult(new ArrayList<>());

        lt.setPageList(dr);
        lt.setNoDataText("cant have spaces");
        TagTestHelper tth = TagTestUtils.setupTagTest(lt, new URL("http://localhost/"));

        try {
            // setup mock objects
            RhnMockJspWriter out = (RhnMockJspWriter) tth.getPageContext().getOut();

            // ok let's test the tag
            tth.assertDoStartTag(Tag.SKIP_BODY);
            assertEquals("<div class=\"list-empty-message\">do spaces work?</div>\n", out.toString());
        }
        catch (JspException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testTagOutput() throws Exception {
        ListTag lt = new ListTag();

        // Invent some data so we have a non-empty list
        String[] vals = {"one", "two", "three"};
        List<String> results = new ArrayList<>();
        for (String s : vals) {
            results.add(s);
        }

        DataResult dr = new DataResult(results);

        lt.setPageList(dr);
        lt.setNoDataText("No Data.");
        TagTestHelper tth = TagTestUtils.setupTagTest(lt, new URL("http://localhost/"));

        try {
            // setup mock objects
            RhnMockJspWriter out = (RhnMockJspWriter) tth.getPageContext().getOut();

            // ok let's test the tag
            tth.assertDoStartTag(Tag.EVAL_BODY_INCLUDE);
            assertEquals(StringUtils.EMPTY, out.toString());
        }
        catch (JspException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testNullPageList() throws Exception {
        ListTag lt = new ListTag();

        lt.setPageList(null);
        lt.setNoDataText("No Data.");
        TagTestHelper tth = TagTestUtils.setupTagTest(lt, new URL("http://localhost/"));

        try {
            // setup mock objects
            RhnMockJspWriter out = (RhnMockJspWriter) tth.getPageContext().getOut();

            // ok let's test the tag
            tth.assertDoStartTag(Tag.SKIP_BODY);
            assertEquals("<div class=\"list-empty-message\">**No Data.**</div>\n", out.toString());
        }
        catch (JspException e) {
            fail(e.toString());
        }
    }
}
