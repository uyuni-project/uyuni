/*
 * Copyright (c) 2009--2017 Red Hat, Inc.
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.taglibs.SystemTimeMessageTag;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.RhnMockJspWriter;
import com.redhat.rhn.testing.RhnMockPageContext;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.Test;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * SystemTimeMessageTagTest
 */
public class SystemTimeMessageTagTest extends RhnBaseTestCase {

    @Test
    public void testDoEndTag() throws Exception {
        SystemTimeMessageTag tag = new SystemTimeMessageTag();
        CustomPageContext cpc = new CustomPageContext();
        tag.setPageContext(cpc);
        CustomWriter out = (CustomWriter) cpc.getOut();
        LocalizationService ls = LocalizationService.getInstance();

        try { //no server
            tag.doEndTag();
            fail();
        }
        catch (JspException e) {
            //should go here
        }

        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        Server server = ServerFactoryTest.createTestServer(user);
        tag.setServer(server);
        tag.doEndTag();

        String result = out.getPrinted();
        assertTrue(result.startsWith("<table border=\"0\" cellspacing=\"0\" " +
                "cellpadding=\"6\">\n  <tr><td>" + ls.getMessage("timetag.lastcheckin") +
                "</td><td>" + ls.formatDate(server.getServerInfo().getCheckin())));
        assertFalse(result.contains(ls.getMessage("timetag.awol")));
    }

    private class CustomWriter extends RhnMockJspWriter {
        private StringBuffer printed = new StringBuffer();

        CustomWriter() {
            super();
        }

        @Override
        public void print(String in) {
            printed.append(in);
        }

        public String getPrinted() {
            return printed.toString();
        }
    }

    private class CustomPageContext extends RhnMockPageContext {

        private CustomWriter writer;

        CustomPageContext() {
            writer = new CustomWriter();
        }

        /**
         * @return A custom fake writer
         */
        @Override
        public JspWriter getOut() {
            return writer;
        }

    }

}
