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
package com.redhat.rhn.frontend.taglibs.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.util.test.CSVWriterTest;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.taglibs.ListTag;
import com.redhat.rhn.frontend.taglibs.UnpagedListDisplayTag;
import com.redhat.rhn.testing.MockObjectTestCase;
import com.redhat.rhn.testing.RhnMockJspWriter;
import com.redhat.rhn.testing.TestUtils;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

/**
 * UnpagedListDisplayTagTest
 */
public class UnpagedListDisplayTagTest extends MockObjectTestCase {
    private UnpagedListDisplayTag ldt;
    private HttpServletRequest request;
    private PageContext pageContext;
    private RhnMockJspWriter writer;

    @BeforeEach
    public void setUp() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        TestUtils.disableLocalizationLogging();

        request = mock(HttpServletRequest.class);
        pageContext = mock(PageContext.class);
        writer = new RhnMockJspWriter();

        ldt = new UnpagedListDisplayTag();
        ListTag lt = new ListTag();
        ldt.setPageContext(pageContext);
        ldt.setParent(lt);

        lt.setPageList(new DataResult<>(CSVWriterTest.getTestListOfMaps()));

        context().checking(new Expectations() { {
            atLeast(1).of(pageContext).getOut();
            will(returnValue(writer));
            atLeast(1).of(pageContext).getRequest();
            will(returnValue(request));
            atLeast(1).of(pageContext).setAttribute("current", null);
        } });
    }

    @Test
    public void testTitle() throws JspException {
        context().checking(new Expectations() { {
            atLeast(1).of(pageContext).popBody();
            atLeast(1).of(pageContext).pushBody();
            atLeast(1).of(request).getParameter(RequestContext.LIST_SORT);
            will(returnValue(null));
        } });


        ldt.setTitle("Inactive Systems");
        int tagval = ldt.doStartTag();
        assertEquals(Tag.EVAL_BODY_INCLUDE, tagval);
        tagval = ldt.doEndTag();
        ldt.release();
        assertEquals(Tag.EVAL_PAGE, tagval);
        assertEquals(EXPECTED_HTML_OUT_WITH_TITLE, writer.toString());
    }

    @AfterEach
    public void tearDown() {
        TestUtils.enableLocalizationLogging();
    }

    @Test
    public void testTag() throws JspException {
        context().checking(new Expectations() { {
            atLeast(1).of(pageContext).popBody();
            atLeast(1).of(pageContext).pushBody();
            atLeast(1).of(request).getParameter(RequestContext.LIST_SORT);
            will(returnValue("column2"));
            atLeast(1).of(request).getParameter(RequestContext.SORT_ORDER);
            will(returnValue(RequestContext.SORT_ASC));

        } });
        int tagval = ldt.doStartTag();
        assertEquals(tagval, Tag.EVAL_BODY_INCLUDE);
        tagval = ldt.doEndTag();
        ldt.release();
        assertEquals(tagval, Tag.EVAL_PAGE);
        assertEquals(EXPECTED_HTML_OUT, writer.toString());
    }

    private static final String EXPECTED_HTML_OUT =
        "<div class=\"spacewalk-list\"><div class=\"panel panel-default\">" +
        "<table class=\"table\">\n<thead>\n<tr>\n\n</tbody>\n</table>\n\n" +
        "</div>\n\n" +
        "</div>\n\n";

    private static final String EXPECTED_HTML_OUT_WITH_TITLE =
        "<div class=\"spacewalk-list\"><div class=\"panel panel-default\">" +
        "<div class=\"panel-heading\">\n<h4 class=\"panel-title\">**Inactive Systems**</h4>\n" +
        "<div class=\"spacewalk-list-head-addons\">\n\n" +
        "<div class=\"spacewalk-list-head-addons-extra\">\n\n</div>\n" +
        "</div>\n</div>\n<table class=\"table\">\n<thead>\n<tr>\n\n</tbody>\n</table>\n\n" +
        "</div>\n\n" +
        "</div>\n\n";
}
