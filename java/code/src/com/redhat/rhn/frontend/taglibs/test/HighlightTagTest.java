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
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.frontend.taglibs.HighlightTag;
import com.redhat.rhn.testing.MockBodyContent;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.RhnMockJspWriter;
import com.redhat.rhn.testing.TagTestHelper;
import com.redhat.rhn.testing.TagTestUtils;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

/**
 * HighlightTagTest
 */
public class HighlightTagTest extends RhnBaseTestCase {

    private HighlightTag ht;
    private TagTestHelper tth;
    private RhnMockJspWriter out;


    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        ht = new HighlightTag();
        tth = TagTestUtils.setupTagTest(ht, null);
        ht.setPageContext(tth.getPageContext());

        MockBodyContent bc = new MockBodyContent("some test text");
        ht.setBodyContent(bc);

        out = (RhnMockJspWriter) tth.getPageContext().getOut();
    }

    @Test
    public void testDoEndWithRegularTag() {
        /*
         * <rhn:highlight tag="foo" text="test">
         *     some test text
         * </rhn:highlight>
         */
        ht.setTag("foo");
        ht.setText("test");

        try {
            tth.assertDoEndTag(Tag.EVAL_PAGE);
        }
        catch (JspException e) {
            fail(e.toString());
        }

        assertEquals("some <foo>test</foo> text\n", out.toString());
    }

    @Test
    public void testDoEndWithCustomTags() {
        /*
         * <rhn:highlight tag="foo" startTag="<foo bar=1>" text="test">
         *     some test text
         * </rhn:highlight>
         */
        ht.setTag("foo");
        ht.setText("test");
        ht.setStartTag("<foo bar=1>");
        try {
            tth.assertDoEndTag(Tag.EVAL_PAGE);
        }
        catch (JspException e) {
            fail(e.toString());
        }
        assertEquals("some <foo bar=1>test</foo> text\n", out.toString());
    }

    @Test
    public void testDoEndWithOnlyCustomTags() {
        /*
         * <rhn:highlight startTag="<foo>" endTag="</foo>" text="test">
         *     some test text
         * </rhn:highlight>
         */
        ht.setTag(null);
        ht.setStartTag("<foo>");
        ht.setEndTag("</foo>");
//        out.setExpectedData("some <foo>test</foo> text");
        try {
            tth.assertDoEndTag(Tag.EVAL_PAGE);
        }
        catch (JspException e) {
            fail(e.toString());
        }

        assertEquals("some test text\n", out.toString());
    }

    @Test
    public void testDoEndFailures() {
        /*
         * <rhn:highlight endTag="</foo>" text="test">
         * -- missing startTag or tag
         */
        ht.setTag(null);
        ht.setStartTag(null);
        try {
            tth.assertDoEndTag(Tag.EVAL_PAGE);
            fail(); //Shouldn't get here
        }
        catch (JspException e) {
            //Success
        }
    }

    @Test
    public void testDoEndWithNoBodyContent() {
        /*
         * <rhn:highlight tag="foo" text="test"></rhn:highlight>
         */
        ht.setBodyContent(null);
        ht.setTag("foo");
        try {
            tth.assertDoEndTag(Tag.SKIP_BODY);
        }
        catch (JspException e) {
            fail(e.toString());
        }

        assertEquals(StringUtils.EMPTY, out.toString());
    }

    @Test
    public void testDoEndWithMultipleOccurrences() {
        ht.setBodyContent(new MockBodyContent("some test text to Test in a TEST"));
        ht.setTag("foo");
        ht.setText("test");
        try {
            tth.assertDoEndTag(Tag.EVAL_PAGE);
        }
        catch (JspException e) {
            fail(e.toString());
        }
        assertEquals("some <foo>test</foo> text to <foo>Test</foo> in a <foo>TEST</foo>\n", out.toString());
    }
}
