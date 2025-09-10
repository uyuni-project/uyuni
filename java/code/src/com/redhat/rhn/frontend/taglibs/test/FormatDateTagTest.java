/*
 * Copyright (c) 2014 SUSE LLC
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

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.frontend.taglibs.FormatDateTag;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.RhnMockJspWriter;
import com.redhat.rhn.testing.TagTestHelper;
import com.redhat.rhn.testing.TagTestUtils;

import org.junit.jupiter.api.Test;

import java.util.Date;

import javax.servlet.jsp.JspException;

public class FormatDateTagTest extends RhnBaseTestCase {

    /**
     * Test tag output
     */
    @Test
    public void testTagOutput() {

        FormatDateTag ht = new FormatDateTag();
        TagTestHelper tth = TagTestUtils.setupTagTest(ht, null);
        ht.setPageContext(tth.getPageContext());

        Date now = new Date();
        ht.setValue(now);
        ht.setHumanStyle("from");

        RhnMockJspWriter out = (RhnMockJspWriter) tth.getPageContext().getOut();
        try {
            ht.doStartTag();
            ht.doEndTag();
            assertContains(out.toString(), "<time");
            ht.release();
        }
        catch (JspException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testTagOutputNullValue() {

        FormatDateTag ht = new FormatDateTag();
        TagTestHelper tth = TagTestUtils.setupTagTest(ht, null);
        ht.setPageContext(tth.getPageContext());

        ht.setValue(null);
        ht.setHumanStyle("from");

        RhnMockJspWriter out = (RhnMockJspWriter) tth.getPageContext().getOut();
        try {
            ht.doStartTag();
            ht.doEndTag();
            // no data for a null value
            assertEquals(EMPTY, out.toString());
            ht.release();
        }
        catch (JspException e) {
            fail(e.toString());
        }
    }
}
