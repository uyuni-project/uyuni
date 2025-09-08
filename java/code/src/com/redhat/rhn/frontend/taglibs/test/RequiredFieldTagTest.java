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

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.frontend.taglibs.RequiredFieldTag;
import com.redhat.rhn.testing.MockHttpServletRequest;
import com.redhat.rhn.testing.MockJspWriter;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TagTestHelper;
import com.redhat.rhn.testing.TagTestUtils;

import org.junit.jupiter.api.Test;

import java.net.URL;

import javax.servlet.jsp.tagext.Tag;


/**
 * RequiredFieldTagTest
 */
public class RequiredFieldTagTest extends RhnBaseTestCase {
    @Test
    public void testRender() throws Exception {
        RequiredFieldTag tag = new RequiredFieldTag();
        MockHttpServletRequest request = new MockHttpServletRequest();
        TagTestHelper tth = TagTestUtils.setupTagTest(tag,
                                        new URL("http://localhost"),
                                        request);
        tag.setPageContext(tth.getPageContext());
        String key = "getMessage";
        tag.setKey(key);
        // ok let's test the tag
        tth.assertDoStartTag(Tag.EVAL_BODY_INCLUDE);
        tth.assertDoEndTag(Tag.SKIP_BODY);

        MockJspWriter rout = (MockJspWriter) tth.getPageContext().getOut();
        assertTrue(rout.toString().contains("<span class"));
        assertTrue(rout.toString().contains("</span>"));
        assertTrue(rout.toString().contains("*"));
        assertTrue(rout.toString().contains("\"" +
                RequiredFieldTag.REQUIRED_FIELD_CSS + "\""));
        LocalizationService ls = LocalizationService.getInstance();
        assertTrue(rout.toString().startsWith(ls.getMessage(key)));
    }
}
