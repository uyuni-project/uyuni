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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.util.test.CSVWriterTest;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.taglibs.ListDisplayTag;
import com.redhat.rhn.frontend.taglibs.ListTag;
import com.redhat.rhn.testing.MockObjectTestCase;
import com.redhat.rhn.testing.RhnMockJspWriter;
import com.redhat.rhn.testing.TestUtils;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

/**
 * ColumnTagTest
 */
public class ListDisplayTagTest extends MockObjectTestCase {

    private ListDisplayTag ldt;

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

        ldt = new ListDisplayTag();
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
            atLeast(1).of(pageContext).pushBody(with(any(Writer.class)));

            atLeast(1).of(request).getParameter(RequestContext.LIST_SORT);
            will(returnValue(null));
        } });

        ldt.setTitle("Inactive Systems");
        int tagval = ldt.doStartTag();
        assertEquals(Tag.EVAL_BODY_INCLUDE, tagval);
        tagval = ldt.doEndTag();
        ldt.release();
        assertEquals(Tag.EVAL_PAGE, tagval);
        String htmlOut = writer.toString();
        assertPaginationControls(htmlOut);
    }

    /**
     * @param htmlOut the html output
     */
    private void assertPaginationControls(String htmlOut) {
        for (RequestContext.Pagination pagination : RequestContext.Pagination.values()) {
            String att = pagination.getLowerAttributeName();
            assertTrue(htmlOut.contains("name=\"" + att));
        }
        assertTrue(htmlOut.contains("name=\"lower"));
    }

    /**
     * {@inheritDoc}
     */
    @AfterEach
    public void tearDown() {
        TestUtils.enableLocalizationLogging();
    }

    @Test
    public void testTag() throws JspException {
        context().checking(new Expectations() { {
            atLeast(1).of(pageContext).popBody();
            atLeast(1).of(pageContext).pushBody();
            atLeast(1).of(pageContext).pushBody(with(any(Writer.class)));

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
        String htmlOut = writer.toString();
        assertPaginationControls(htmlOut);
    }
}
