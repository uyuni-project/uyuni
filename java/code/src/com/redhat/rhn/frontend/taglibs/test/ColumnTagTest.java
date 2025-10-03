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
package com.redhat.rhn.frontend.taglibs.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.frontend.taglibs.ColumnTag;
import com.redhat.rhn.frontend.taglibs.ListDisplayTag;
import com.redhat.rhn.frontend.taglibs.NavDialogMenuTag;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.RhnMockJspWriter;
import com.redhat.rhn.testing.RhnMockPageContext;
import com.redhat.rhn.testing.TagTestHelper;
import com.redhat.rhn.testing.TagTestUtils;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

/**
 * ColumnTagTest
 */
public class ColumnTagTest extends RhnBaseTestCase {

    @Test
    public void testConstructor() {
        ColumnTag ct = new ColumnTag();
        assertNotNull(ct);
        assertNull(ct.getHeader());
        assertNotNull(ct.getStyle());
        assertNull(ct.getCssClass());
        assertNull(ct.getUrl());
        assertNull(ct.getWidth());
        assertNull(ct.getParent());
        assertNull(ct.getNowrap());
        assertTrue(ct.isRenderUrl());
        assertNull(ct.getArg0());
    }

    @Test
    public void testCopyConstructor() {
        ColumnTag ct = new ColumnTag();
        ct.setHeader("header");
        ct.setStyle("text-align: center;");
        ct.setCssClass("first-column");
        ct.setUrl("http://www.hostname.com");
        ct.setWidth("10%");
        ct.setNowrap("false");
        ct.setRenderUrl(false);
        ct.setArg0("system");
        ColumnTag copy = new ColumnTag(ct);
        assertEquals(ct, copy);
    }

    @Test
    public void testEquals() {
        ColumnTag ct = new ColumnTag();
        ct.setHeader("header");
        ct.setStyle("text-align: center;");
        ct.setCssClass("first-column");
        ct.setUrl("http://www.hostname.com");
        ct.setWidth("10%");
        ct.setNowrap("false");

        ColumnTag ct1 = new ColumnTag();
        ct1.setHeader("header");
        ct1.setStyle("text-align: center;");
        ct1.setCssClass("first-column");
        ct1.setUrl("http://www.hostname.com");
        ct1.setWidth("10%");
        ct1.setNowrap("false");

        assertEquals(ct, ct1);
        assertEquals(ct1, ct);

        ct1.setUrl("http://www.hostname.com?sgid=1234");
        assertEquals(ct, ct1);
        assertEquals(ct1, ct);
    }

    @Test
    public void testSettersGetters() {
        ColumnTag ct = new ColumnTag();
        ct.setHeader("header");
        ct.setStyle("text-align: center;");
        ct.setCssClass("first-column");
        ct.setUrl("http://www.hostname.com");
        ct.setWidth("10%");
        ct.setRenderUrl(true);
        ct.setNowrap("true");
        ct.setArg0("foo");

        assertEquals("header", ct.getHeader());
        assertEquals("foo", ct.getArg0());
        assertEquals("text-align: center;", ct.getStyle());
        assertEquals("first-column", ct.getCssClass());
        assertEquals("http://www.hostname.com", ct.getUrl());
        assertEquals("10%", ct.getWidth());
        assertEquals("true", ct.getNowrap());
        assertTrue(ct.isRenderUrl());
        assertNull(ct.getParent());
    }

    @Test
    public void testFindListDisplay() {
        ColumnTag ct = new ColumnTag();
        ct.setParent(new ListDisplayTag());
        assertNotNull(ct.findListDisplay());

        ColumnTag ct2 = new ColumnTag();
        NavDialogMenuTag middle = new NavDialogMenuTag();
        middle.setParent(new ListDisplayTag());
        ct2.setParent(middle);
        assertNotNull(ct2.findListDisplay());

        ColumnTag ct3 = new ColumnTag();
        ct3.setParent(new NavDialogMenuTag());
        assertNull(ct3.findListDisplay());
    }

    @Test
    public void testDoStartTag() throws JspException {
        TestUtils.disableLocalizationLogging();
        ListDisplayTag ldt = new ListDisplayTag();
        ColumnTag ct = new ColumnTag();
        ct.setSortProperty("sortProp");
        assertNull(ct.getParent());

        ct.setParent(ldt);
        ct.setHeader("headervalue");
        assertEquals(ldt, ct.getParent());

        TagTestHelper tth = TagTestUtils.setupTagTest(ct, null);
        RhnMockHttpServletRequest mockRequest = (RhnMockHttpServletRequest)
                tth.getPageContext().getRequest();
        mockRequest.addParameter("order", "asc");

        RhnMockPageContext mpc = tth.getPageContext();
        mpc.setAttribute("current", new Object());
        ct.setPageContext(mpc);
        tth.assertDoStartTag(Tag.SKIP_BODY);
        tth.assertDoEndTag(Tag.EVAL_BODY_INCLUDE);
        //TODO: verify if this test is needed, followup with bug 458688
        RhnMockJspWriter out = (RhnMockJspWriter)tth.getPageContext().getOut();
        String expected = String.format("<th><a class=\"js-spa\" title=\"Sort By This Column\" " +
                        "href=\"?order=desc&sort=sortProp&uid=%d\">**headervalue**</a></th>",
                Integer.parseInt(mockRequest.getParameterMap().get("uid")[0]));
        assertEquals(expected, out.toString());
        TestUtils.enableLocalizationLogging();
    }

}
