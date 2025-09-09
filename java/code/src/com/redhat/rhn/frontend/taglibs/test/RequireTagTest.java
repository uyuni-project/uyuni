/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.common.security.acl.AclHandler;
import com.redhat.rhn.frontend.taglibs.RequireTag;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TagTestHelper;
import com.redhat.rhn.testing.TagTestUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

/**
 * RequireTagTest
 */
public class RequireTagTest extends RhnBaseTestCase {

    private RequireTag rt;
    private TagTestHelper tth;

    @Override
    @BeforeEach
    public void setUp() {
        rt = new RequireTag();
        tth = TagTestUtils.setupTagTest(rt, null);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        rt = null;
        tth = null;
    }

    @Test
    public void testInvalidAcl() {
        // set test condition
        rt.setAcl("is(foo)");

        assertDoesNotThrow(() -> tth.assertDoStartTag(Tag.SKIP_BODY));
    }

    @Test
    public void testNullAcl() {
        // set test condition
        rt.setAcl(null);
        // we don't expect this to work
        assertThrows(JspException.class, () -> tth.assertDoStartTag(-1));
    }

    @Test
    public void testEmptyAcl() {
        // set test condition
        rt.setAcl("");
        // we don't expect this to work
        assertThrows(JspException.class, () -> tth.assertDoStartTag(-1));
    }

    @Test
    public void testMixin() {
        rt.setAcl("true_test()");
        rt.setMixins("throws.class.not.found.exception," +
                BooleanAclHandler.class.getName());

        assertThrows(JspException.class, () -> tth.assertDoStartTag(Tag.EVAL_BODY_INCLUDE));
    }

    @Test
    public void testMultipleMixinsMultipleAcls() {
        rt.setMixins(MockOneAclHandler.class.getName() + "," +
                MockTwoAclHandler.class.getName());
        rt.setAcl("first_true_acl(); second_true_acl(); is_foo(foo)");

        assertDoesNotThrow(() -> tth.assertDoStartTag(Tag.EVAL_BODY_INCLUDE));
    }

    @Test
    public void testMultipleAclsSingleMixin() {
        rt.setAcl("first_true_acl(); second_true_acl()");
        rt.setMixins(MockOneAclHandler.class.getName());

        assertDoesNotThrow(() -> tth.assertDoStartTag(Tag.EVAL_BODY_INCLUDE));
    }

    @Test
    public void testValidAclInvalidMixin() {
        rt.setAcl("true_test()");
        rt.setMixins("throws.class.not.found.exception," +
                BooleanAclHandler.class.getName());

        assertThrows(JspException.class, () -> tth.assertDoStartTag(Tag.EVAL_BODY_INCLUDE));
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
