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

import com.redhat.rhn.common.security.acl.AclHandler;
import com.redhat.rhn.frontend.taglibs.ToolbarTag;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.RhnMockJspWriter;
import com.redhat.rhn.testing.TagTestHelper;
import com.redhat.rhn.testing.TagTestUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

/**
 * BaseTestToolbarTag
 */
public abstract class BaseTestToolbarTag extends RhnBaseTestCase {
    protected URL url = null;
    protected TagTestHelper tth;
    protected ToolbarTag tt;
    protected RhnMockJspWriter out;

    @Override
    @BeforeEach
    public void setUp() {
        tt = new ToolbarTag();
        tth = TagTestUtils.setupTagTest(tt, null);
        out = (RhnMockJspWriter) tth.getPageContext().getOut();
        RhnMockHttpServletRequest req = tth.getRequest();
        req.setAttributes(new HashMap<>());
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        tt = null;
        tth = null;
        out = null;
    }

    public void verifyTag(String output) throws JspException {
        tth.assertDoStartTag(Tag.EVAL_BODY_INCLUDE);
        tth.assertDoEndTag(Tag.EVAL_PAGE);
        assertEquals(output, out.toString());
    }

    public static class MockTwoAclHandler implements AclHandler {

        public MockTwoAclHandler() {
            super();
        }

        public boolean aclIsFoo(Map<String, Object> ctx, String[] params) {
            return (params[0].equals("foo"));
        }
    }

    public static class MockOneAclHandler implements AclHandler {

        public MockOneAclHandler() {
            super();
        }

        public boolean aclFirstTrueAcl(Map<String, Object> ctx, String[] params) {
            return true;
        }

        public boolean aclFirstFalseAcl(Map<String, Object> ctx, String[] params) {
            return false;
        }

        public boolean aclSecondFalseAcl(Map<String, Object> ctx, String[] params) {
            return false;
        }

        public boolean aclSecondTrueAcl(Map<String, Object> ctx, String[] params) {
            return true;
        }
    }

    public static class BooleanAclHandler implements AclHandler {
        /**
         * Always returns true.
         * @param ctx ignored
         * @param params ignored
         * @return true
         */
        public boolean aclTrueTest(Map<String, Object> ctx, String[] params) {
            return true;
        }

        /**
         * Always returns false.
         * @param ctx ignored
         * @param params ignored
         * @return false
         */
        public boolean aclFalseTest(Map<String, Object> ctx, String[] params) {
            return false;
        }
    }
}
