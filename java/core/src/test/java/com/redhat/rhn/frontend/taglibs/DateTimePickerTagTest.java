/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.redhat.rhn.frontend.taglibs;

import com.redhat.rhn.common.util.DatePicker;
import com.redhat.rhn.testing.BaseTestCase;
import com.redhat.rhn.testing.RhnMockJspWriter;
import com.redhat.rhn.testing.TagTestHelper;
import com.redhat.rhn.testing.TagTestUtils;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import jakarta.servlet.jsp.JspException;

class DateTimePickerTagTest extends BaseTestCase {

    private TimeZone originalTimeZone;

    @BeforeEach
    void setUpTimeZone() {
        originalTimeZone = TimeZone.getDefault();
    }

    @AfterEach
    void restoreTimeZone() {
        TimeZone.setDefault(originalTimeZone);
    }

    @Test
    void testDataValueUsesServerTimeZone() throws JspException {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        DatePicker picker = new DatePicker("date", TimeZone.getTimeZone("Asia/Kolkata"), Locale.US,
                DatePicker.YEAR_RANGE_POSITIVE);
        picker.setDate(Date.from(Instant.parse("2024-03-05T07:08:09Z")));

        DateTimePickerTag tag = new DateTimePickerTag();
        TagTestHelper tth = TagTestUtils.setupTagTest(tag, null);
        tag.setPageContext(tth.getPageContext());
        tag.setData(picker);
        tag.doEndTag();

        String html = ((RhnMockJspWriter) tth.getPageContext().getOut()).toString();
        TestUtils.assertContains(html, "data-value=\"2024-03-05T12:38+0530\"");
    }
}
