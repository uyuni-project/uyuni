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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.taglibs;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.testing.BaseTestCase;
import com.redhat.rhn.testing.RhnMockJspWriter;
import com.redhat.rhn.testing.TagTestHelper;
import com.redhat.rhn.testing.TagTestUtils;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import jakarta.servlet.jsp.JspException;

public class FormatDateTagTest extends BaseTestCase {

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
            assertTrue(out.toString().contains("<time"));
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

    @Test
    public void testDefaultPatternInContextTimeZone() throws JspException {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            Context.getCurrentContext().setTimezone(TimeZone.getTimeZone("Europe/Berlin"));

            assertEquals("2010-04-01 15:04:24 CEST", renderBody(new FormatDateTag()));
        }
        finally {
            Locale.setDefault(originalLocale);
            Context.freeCurrentContext();
        }
    }

    @Test
    public void testDefaultPatternWithTypeUsesTagLocale() throws JspException {
        try {
            Context.getCurrentContext().setTimezone(TimeZone.getTimeZone("Europe/Berlin"));
            FormatDateTag tag = new FormatDateTag();
            tag.setType("both");
            tag.setLocale(Locale.GERMAN);

            // the default pattern wins over the date and time styles
            assertEquals("2010-04-01 15:04:24 MESZ", renderBody(tag));
        }
        finally {
            Context.freeCurrentContext();
        }
    }

    private static String renderBody(FormatDateTag tag) throws JspException {
        TagTestHelper tth = TagTestUtils.setupTagTest(tag, null);
        tag.setPageContext(tth.getPageContext());
        tag.setValue(Date.from(Instant.parse("2010-04-01T13:04:24Z")));
        tag.doStartTag();
        String out = tth.getPageContext().getOut().toString();
        return out.substring(out.indexOf('>') + 1, out.lastIndexOf("  </time>"));
    }
}
