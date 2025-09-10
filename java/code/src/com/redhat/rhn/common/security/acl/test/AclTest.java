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

package com.redhat.rhn.common.security.acl.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.redhat.rhn.common.security.acl.Acl;
import com.redhat.rhn.common.security.acl.AclHandler;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/*
 * Test for {@link Acl}
 *
 */
public class AclTest {

    private Mockery jmock;
    private Acl acl;
    private Map<String, Object> context;

    @BeforeEach
    void setUp() {
        jmock = new Mockery() {{
            setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        }};
        acl = new Acl();
        context = new HashMap<>();
    }

    /* -------------------------
       Test helpers / matchers
       ------------------------- */

    /** Hamcrest matcher that asserts String[] equality by value (Arrays.equals). */
    private static org.hamcrest.Matcher<String[]> params(String... expected) {
        return new TypeSafeDiagnosingMatcher<String[]>() {
            @Override
            protected boolean matchesSafely(String[] actual, Description mismatchDescription) {
                if (!Arrays.equals(actual, expected)) {
                    mismatchDescription.appendText("was ").appendValue(Arrays.toString(actual));
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("params ").appendValue(Arrays.toString(expected));
            }
        };
    }

    /* -------------------------
       A testable handler to mock
       ------------------------- */

    /**
     * Concrete class with the reflective methods Acl will invoke.
     * We mock THIS class (not the AclHandler interface) so we can set expectations
     * on aclHandlerZero/One/Two. We also provide a no-op reset().
     */
    public static class TestableHandler implements AclHandler {
        public void reset() { /* no-op for tests */ }

        public boolean aclHandlerZero(Map<String, Object> context, String[] params) {
            return false;
        }
        public boolean aclHandlerOne(Map<String, Object> context, String[] params) {
            return false;
        }
        public boolean aclHandlerTwo(Map<String, Object> context, String[] params) {
            return false;
        }
    }

    /* -------------------------
       Core parsing & logic tests
       ------------------------- */

    @Test
    void testSimpleAcl() {
        TestableHandler h = jmock.mock(TestableHandler.class);
        acl.registerHandler(h);

        // handler_zero() -> false
        jmock.checking(new Expectations() {{
            oneOf(h).aclHandlerZero(with(context), with(params()));
            will(returnValue(false));
        }});
        assertFalse(acl.evalAcl(context, "handler_zero()"));
        jmock.assertIsSatisfied();

        // handler_zero(true) -> true
        jmock.checking(new Expectations() {{
            oneOf(h).aclHandlerZero(with(context), with(params("true")));
            will(returnValue(true));
        }});
        assertTrue(acl.evalAcl(context, "handler_zero(true)"));
        jmock.assertIsSatisfied();

        // handler_zero(true,false) variants -> true
        String[] expressions = {
                "handler_zero(true,false)",
                "handler_zero(true ,false)",
                "handler_zero(true , false)",
                "handler_zero(true, false)"
        };
        for (String expr : expressions) {
            jmock.checking(new Expectations() {{
                oneOf(h).aclHandlerZero(with(context), with(params("true", "false")));
                will(returnValue(true));
            }});
            assertTrue(acl.evalAcl(context, expr));
            jmock.assertIsSatisfied();
        }

        // not handler_zero(true) -> false (zero(true) returns true, then negated)
        jmock.checking(new Expectations() {{
            oneOf(h).aclHandlerZero(with(context), with(params("true")));
            will(returnValue(true));
        }});
        assertFalse(acl.evalAcl(context, "not handler_zero(true)"));
        jmock.assertIsSatisfied();
    }

    /* Test expressions connected with Or.
     * Tests the following:
     * <ul>
     *   <li>"handler_zero(false) or handler_one(true)"
     *   <li>"handler_zero(true) or handler_one(false)"
     * </ul>
     */
    @Test
    void testMultipleOrStatementsAcl() {
        TestableHandler h = jmock.mock(TestableHandler.class);
        acl.registerHandler(h);

        // handler_zero(false) or handler_one(true) -> true
        jmock.checking(new Expectations() {{
            oneOf(h).aclHandlerZero(with(context), with(params("false")));
            will(returnValue(false));
            oneOf(h).aclHandlerOne(with(context), with(params("true")));
            will(returnValue(true));
        }});
        assertTrue(acl.evalAcl(context, "handler_zero(false) or handler_one(true)"));
        jmock.assertIsSatisfied();

        // handler_zero(true) or handler_one(false) -> short-circuit true, handler_one not called
        jmock.checking(new Expectations() {{
            oneOf(h).aclHandlerZero(with(context), with(params("true"))); will(returnValue(true));
            never(h).aclHandlerOne(with(any(Map.class)), with(any(String[].class)));
        }});
        assertTrue(acl.evalAcl(context, "handler_zero(true) or handler_one(false)"));
        jmock.assertIsSatisfied();
    }

    /* Test statements connected with And.
     * Tests the following:
     * <ul>
     *   <li>"handler_zero(false) ; handler_one(true)"
     *   <li>"handler_zero(true,false) ; handler_one(true)"
     * </ul>
     */
    @Test
    void testMultipleAndStatementsAcl() {
        TestableHandler h = jmock.mock(TestableHandler.class);
        acl.registerHandler(h);

        // handler_zero(false) ; handler_one(true) -> short-circuit false, handler_one not called
        jmock.checking(new Expectations() {{
            oneOf(h).aclHandlerZero(with(context), with(params("false")));
            will(returnValue(false));
            never(h).aclHandlerOne(with(any(Map.class)), with(any(String[].class)));
        }});
        assertFalse(acl.evalAcl(context, "handler_zero(false) ; handler_one(true)"));
        jmock.assertIsSatisfied();

        // handler_zero(true,false) ; handler_one(true) -> true && true => true
        jmock.checking(new Expectations() {{
            oneOf(h).aclHandlerZero(with(context), with(params("true", "false")));
            will(returnValue(true));
            oneOf(h).aclHandlerOne(with(context), with(params("true")));
            will(returnValue(true));
        }});
        assertTrue(acl.evalAcl(context, "handler_zero(true,false) ; handler_one(true)"));
        jmock.assertIsSatisfied();
    }

    /* Test statements connected with And and Or.
     * Tests the following:
     * <ul>
     *   <li>"handler_zero(true) or handler_one(false) ; handler_two(true)"
     * </ul>
     */
    @Test
    void testCompoundAcl() {
        TestableHandler h = jmock.mock(TestableHandler.class);
        acl.registerHandler(h);

        // handler_zero(true) or handler_one(false) ; handler_two(true)
        // Expect: handler_zero(true) -> true (OR short-circuits, so handler_one not called)
        // Then AND handler_two(true) must still be evaluated (depending on your Acl precedence).
        jmock.checking(new Expectations() {{
            oneOf(h).aclHandlerZero(with(context), with(params("true"))); will(returnValue(true));
            never(h).aclHandlerOne(with(any(Map.class)), with(any(String[].class)));
            oneOf(h).aclHandlerTwo(with(context), with(params("true")));  will(returnValue(true));
        }});
        assertTrue(acl.evalAcl(context, "handler_zero(true) or handler_one(false) ; handler_two(true)"));
        jmock.assertIsSatisfied();
    }


    /* Test bad handler.
     */
    @Test
    void testBadHandler() {
        assertThrows(IllegalArgumentException.class,
                () -> acl.evalAcl(null, "handler_does_not_exist(true)"));
    }

    /* Test bad syntax.
     */
    @Test
    void testBadSyntax() {
        // Assuming "and" is invalid per original test
        assertThrows(IllegalArgumentException.class,
                () -> acl.evalAcl(null, "handler_zero(true) and handler_zero(true)"));
    }

    /* Test bad syntax.
     */
    @Test
    void testNullExpression() {
        assertThrows(IllegalArgumentException.class,
                () -> acl.evalAcl(null, null));
    }

    /** Makes sure that method names are properly converted to acl handler
     *  names.
     *  Tests the following:
     *  <table>
     *  <caption>things to test</caption>
     *  <tr>
     *      <td>method name</td><td>acl handler name</td>
     *  </tr>
     *  <tr>
     *      <td>aclTheQuickBrownFoxJumpedOverTheLazyDog</td>
     *      <td>the_quick_brown_fox_jumped_over_the_lazy_dog</td>
     *  </tr>
     *  <tr>
     *      <td>aclTestXMLFile</td><td>test_xml_file</td>
     *  </tr>
     *  <tr>
     *      <td>aclTestX</td><td>test_x</td>
     *  </tr>
     *  <tr>
     *      <td>aclTestXML</td><td>test_xml</td>
     *  </tr>
     *  </table>
     */
    @Test
    public void testMethodNameToAclName() {
        acl.registerHandler(new MockAclHandlerWithFunkyNames());

        /** Each of the following should call the expected method
         *  from MockAclHandlerWithFunkyNames and return true */
        assertTrue(acl.evalAcl(context,
                    "the_quick_brown_fox_jumped_over_the_lazy_dog()"));
        assertTrue(acl.evalAcl(context, "test_xml_file()"));
        assertTrue(acl.evalAcl(context, "test_x()"));
        assertTrue(acl.evalAcl(context, "test_xml()"));
        assertTrue(acl.evalAcl(context, "xml_test()"));
    }

    @Test
    public void testRegisterByClass() {
        acl.registerHandler(MockAclHandlerWithFunkyNames.class);
        assertTrue(acl.evalAcl(context, "xml_test()"));
    }

    @Test
    public void testRegisterByString() {
        acl.registerHandler(MockAclHandlerWithFunkyNames.class.getName());
        assertTrue(acl.evalAcl(context, "xml_test()"));
    }

    @Test
    public void testBadRegisterByString() {
        try {
            acl.registerHandler("Bubba");
            fail("Expected call to fail");
        }
        catch (IllegalArgumentException e) {
            // good.
        }
    }

    @Test
    void testStringArrayConstructor() {
        // For constructor-based registration, we must use real classes with default ctors.
        Acl localAcl = new Acl(new String[]{
                MockAclHandler.class.getName(),
                MockAclHandlerWithFunkyNames.class.getName()
        });
        assertTrue(localAcl.evalAcl(context, "handler_zero(true)"));
        assertTrue(localAcl.evalAcl(context, "xml_test()"));
    }

    @Test
    public void testGetAclHandlerNames() {
        Acl localAcl = new Acl();
        localAcl.registerHandler(MockAclHandler.class.getName());
        Set<String> ts = localAcl.getAclHandlerNames();
        ts.contains("handler_zero");
        ts.contains("handler_one");
        ts.contains("handle_two");
    }


    /** Real class used by the string-array constructor test. */
    public static class MockAclHandler implements AclHandler {
        public void reset() { /* no-op */ }
        public boolean aclHandlerZero(Map<String, Object> c, String[] p) {
            return p.length > 0 && "true".equals(p[0]);
        }
        public boolean aclHandlerOne(Map<String, Object> c, String[] p) {
            return p.length > 0 && "true".equals(p[0]);
        }
        public boolean aclHandlerTwo(Map<String, Object> c, String[] p) {
            return p.length > 0 && "true".equals(p[0]);
        }
    }

    /** Real class to verify method-name → ACL-name conversion. */
    public static class MockAclHandlerWithFunkyNames implements AclHandler {
        public void reset() {
            /* no-op */
        }
        public boolean aclTheQuickBrownFoxJumpedOverTheLazyDog(Map<String, Object> context, String[] params) {
            return true;
        }
        public boolean aclTestXMLFile(Map<String, Object> context, String[] params) {
            return true;
        }
        public boolean aclTestX(Map<String, Object> context, String[] params) {
            return true;
        }
        public boolean aclTestXML(Map<String, Object> context, String[] params) {
            return true;
        }
        // Depending on your Acl's camelCase rules, this may need to be aclXmlTest instead.
        public boolean aclXMLTest(Map<String, Object> context, String[] params) {
            return true;
        }
    }
}
