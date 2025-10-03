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
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.frontend.taglibs.LocalizedSubmitTag;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.RhnMockJspWriter;
import com.redhat.rhn.testing.TagTestHelper;
import com.redhat.rhn.testing.TagTestUtils;

import org.junit.jupiter.api.Test;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

/**
 * LocalizedSubmitTagTest
 */
public class LocalizedSubmitTagTest extends RhnBaseTestCase {

    @Test
    public void testTagOutputDefault() {
        LocalizedSubmitTag ltag = new LocalizedSubmitTag();
        ltag.setValueKey("none.message");
        ltag.setTabindex("3");
        try {
            TagTestHelper tth = TagTestUtils.setupTagTest(ltag, null);
            tth.getPageContext().getRequest();
            // setup mock objects
            RhnMockJspWriter out = (RhnMockJspWriter)tth.getPageContext().getOut();
            // ok let's test the tag
            tth.assertDoStartTag(Tag.SKIP_BODY);
            tth.assertDoEndTag(Tag.EVAL_PAGE);
            assertEquals("<input type=\"submit\"" +
                    " tabindex=\"3\" value=\"(none)\" class=\"btn btn-primary\">", out.toString());
        }
        catch (JspException e) {
            fail(e.toString());
        }
        catch (Exception e1) {
            e1.printStackTrace();
            fail(e1.toString());
        }
    }

    @Test
    public void testTagOutputWithStyle() {
        LocalizedSubmitTag ltag = new LocalizedSubmitTag();
        ltag.setValueKey("none.message");
        ltag.setTabindex("3");
        ltag.setStyleClass("foo btn btn-danger");
        try {
            TagTestHelper tth = TagTestUtils.setupTagTest(ltag, null);
            tth.getPageContext().getRequest();
            // setup mock objects
            RhnMockJspWriter out = (RhnMockJspWriter)tth.getPageContext().getOut();
            // ok let's test the tag
            tth.assertDoStartTag(Tag.SKIP_BODY);
            tth.assertDoEndTag(Tag.EVAL_PAGE);
            assertEquals("<input type=\"submit\"" +
                    " tabindex=\"3\" value=\"(none)\" class=\"foo btn btn-danger\">", out.toString());
        }
        catch (JspException e) {
            fail(e.toString());
        }
        catch (Exception e1) {
            e1.printStackTrace();
            fail(e1.toString());
        }
    }
}
