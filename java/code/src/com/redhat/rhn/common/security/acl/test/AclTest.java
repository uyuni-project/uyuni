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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.security.acl.Acl;
import com.redhat.rhn.common.security.acl.AclHandler;
import com.redhat.rhn.testing.RhnBaseTestCase;

import com.mockobjects.ExpectationValue;
import com.mockobjects.Verifiable;

import org.junit.jupiter.api.AfterEach;
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
public class AclTest extends RhnBaseTestCase {

    private Acl acl = null;
    private Map<String, Object> context = null;
    private MockAclHandler handler = null;

    /** Sets up the acl, handler, and context objects. */
    @Override
    @BeforeEach
    public void setUp() {
        acl = new Acl();
        context = new HashMap<>();
        handler = new MockAclHandler();

        acl.registerHandler(handler);
    }

    /** Tears down the acl, handler, and context objects. */
    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        acl = null;
        context = null;
        handler = null;
    }

    /* Test single-statement acls.
     * Tests the following, to make sure parser is behaving:
     * <ul>
     *   <li>"handler_zero()"
     *   <li>"handler_zero(true)"
     *   <li>"handler_zero(true,false)"
     *   <li>"handler_zero(true ,false)"
     *   <li>"handler_zero(true , false)"
     *   <li>"handler_zero(true, false)"
     *   <li>"not handler_zero(true)"
     * </ul>
     */
    @Test
    public void testSimpleAcl() {

        // test parsing with no params. should be false
        handler.setExpected("handler_zero", new String[0]);
        assertFalse(acl.evalAcl(context, "handler_zero()"));
        handler.verify();

        // test parsing with no 1 param. should be true
        handler.setExpected("handler_zero", new String[]{"true"});
        assertTrue(acl.evalAcl(context, "handler_zero(true)"));
        handler.verify();

        // test 2 params with diff spacings
        handler.setExpected("handler_zero",
                new String[]{"true", "false"});
        assertTrue(acl.evalAcl(context, "handler_zero(true,false)"));
        handler.verify();

        handler.setExpected("handler_zero", new String[]{"true", "false"});
        assertTrue(acl.evalAcl(context, "handler_zero(true ,false)"));
        handler.verify();

        handler.setExpected("handler_zero", new String[]{"true", "false"});
        assertTrue(acl.evalAcl(context, "handler_zero(true , false)"));
        handler.verify();

        handler.setExpected("handler_zero", new String[]{"true", "false"});
        assertTrue(acl.evalAcl(context, "handler_zero(true, false)"));
        handler.verify();

        // test negation
        handler.setExpected("handler_zero", new String[]{"true"});
        assertFalse(acl.evalAcl(context, "not handler_zero(true)"));
        handler.verify();
    }

    /* Test expressions connected with Or.
     * Tests the following:
     * <ul>
     *   <li>"handler_zero(false) or handler_one(true)"
     *   <li>"handler_zero(true) or handler_one(false)"
     * </ul>
     */
    @Test
    public void testMultipleOrStatementsAcl() {
        handler.setExpected("handler_zero", new String[]{"false"});
        handler.setExpected("handler_one", new String[]{"true"});
        assertTrue(acl.evalAcl(context,
                    "handler_zero(false) or handler_one(true)"));
        handler.verify();

        handler.setExpected("handler_zero", new String[]{"true"});
        // handler_one, even though we give it false in evalAcl, is not expected
        // to have an expectation value. handler_one won't get called
        // because handler_zero will return true
        handler.setExpected("handler_one", null);
        assertTrue(acl.evalAcl(context,
                    "handler_zero(true) or handler_one(false)"));
        handler.verify();
    }

    /* Test statements connected with And.
     * Tests the following:
     * <ul>
     *   <li>"handler_zero(false) ; handler_one(true)"
     *   <li>"handler_zero(true,false) ; handler_one(true)"
     * </ul>
     */
    @Test
    public void testMultipleAndStatementsAcl() {
        handler.setExpected("handler_zero", new String[]{"false"});
        // handler_one, even though we give it false in evalAcl, is not expected
        // to have an expectation value. handler_one won't get called
        // because handler_zero will return true
        handler.setExpected("handler_one", null);
        assertFalse(acl.evalAcl(context,
                    "handler_zero(false) ; handler_one(true)"));
        handler.verify();


        handler.setExpected("handler_zero", new String[]{"true", "false"});
        handler.setExpected("handler_one", new String[]{"true"});
        assertTrue(acl.evalAcl(context,
                    "handler_zero(true,false) ; handler_one(true)"));
        handler.verify();
    }

    /* Test statements connected with And and Or.
     * Tests the following:
     * <ul>
     *   <li>"handler_zero(true) or handler_one(false) ; handler_two(true)"
     * </ul>
     */
    @Test
    public void testCompoundAcl() {

        handler.setExpected("handler_zero", new String[]{"true"});
        // handler_one, even though we give it false in evalAcl, is not expected
        // to have an expectation value. handler_one won't get called
        // because handler_zero will return true
        handler.setExpected("handler_one", null);
        handler.setExpected("handler_two", new String[]{"true"});

        assertTrue(acl.evalAcl(context,
            "handler_zero(true) or handler_one(false) ; handler_two(true)"));

        handler.verify();
    }


    /* Test bad handler.
     */
    @Test
    public void testBadHandler() {
        try {
            acl.evalAcl(null, "handler_does_not_exist(true)");
            fail("expected to fail");
        }
        catch (IllegalArgumentException e) {
            // good
        }
    }

    /* Test bad syntax.
     */
    @Test
    public void testBadSyntax() {
        try {
            acl.evalAcl(null, "handler_zero(true) and handler_zero(true)");
            fail("expected to fail");
        }
        catch (IllegalArgumentException e) {
            // good
        }
    }

    /* Test bad syntax.
     */
    @Test
    public void test() {
        try {
            acl.evalAcl(null, null);
            fail("expected to fail");
        }
        catch (IllegalArgumentException e) {
            // good
        }
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
    public void testStringArrayConstructor() {
        Acl localAcl = new Acl(new String[]{MockAclHandler.class.getName(),
            MockAclHandlerWithFunkyNames.class.getName()});

        // make sure we can call an acl handler from each class
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


    // HELPER CLASSES

    /* Mock AclHandler that can be used to check that the Acl class
     * is parsing parameters correctly.
     * If no parameters are given to this AclHandler, its
     * {@link #handleAcl} method returns false. If the
     * first parameter equals "true", then handleAcl() returns true.
     */
   public static class MockAclHandler implements AclHandler, Verifiable {
       private Map<String, Object> expected = null;

       public MockAclHandler() {
           reset();
       }
       private void reset() {
           expected = new HashMap<>();
           expected.put("handler_zero",
                   new ExpectationValue("handler_zero params"));
           expected.put("handler_one",
                   new ExpectationValue("handler_one params"));
           expected.put("handler_two",
                   new ExpectationValue("handler_two params"));

           // defer verifying until verify() is called
           // otherwise, calling setActual() might throw an Exception,
           // which we don't want because then we won't get our
           // assert exceptions
           for (Object expectedValueIn : expected.values()) {
               ExpectationValue exp = (ExpectationValue) expectedValueIn;
               exp.setFailOnVerify();
           }
       }
       /**
        * Set the parameters expected to be given a handler upon
        * a call to evalAcl. These get reset with {@link #verify}
        * is called.
        * @param handlerName the handler name
        * @param params the params
        */
       public void setExpected(String handlerName, String[] params) {
           if (params != null) {
               ExpectationValue exp =
                   (ExpectationValue)expected.get(handlerName);
               exp.setExpected(Arrays.asList(params));
           }
       }
       public boolean aclHandlerZero(Map<String, Object> ctx, String[] params) {
           return handlerDelegate("handler_zero", ctx, params);
       }
       public boolean aclHandlerOne(Map<String, Object> ctx, String[] params) {
           return handlerDelegate("handler_one", ctx, params);
       }
       public boolean aclHandlerTwo(Map<String, Object> ctx, String[] params) {
           return handlerDelegate("handler_two", ctx, params);
       }

       private boolean handlerDelegate(
               String name, Map<String, Object> ctx, String[] params) {
           ExpectationValue exp = (ExpectationValue)expected.get(name);
           exp.setActual(Arrays.asList(params));

           if (params.length == 0) {
               return false;
           }

           return params[0].equals("true");

       }

       /** Call to verify that the expected parameters match
        * the parameters given to the handler when Acl calls handleAcl.
        * The expectation values get reset when this is called.
        */
       @Override
       public void verify() {
           for (Object expectedValueIn : expected.values()) {
               ExpectationValue exp = (ExpectationValue) expectedValueIn;
               exp.verify();
           }
           reset();
       }
   }

   /** A handler class with a variety of names to test that method names
    *  get converted to acl names correctly.
    */
   public static class MockAclHandlerWithFunkyNames implements AclHandler {
       public boolean aclTheQuickBrownFoxJumpedOverTheLazyDog(
               Map<String, Object> ctx, String[] params) {
           return true;
       }
       public boolean aclTestXMLFile(Map<String, Object> ctx, String[] params) {
           return true;
       }
       public boolean aclTestX(Map<String, Object> ctx, String[] params) {
           return true;
       }
       public boolean aclTestXML(Map<String, Object> ctx, String[] params) {
           return true;
       }
       public boolean aclXMLTest(Map<String, Object> ctx, String[] params) {
           return true;
       }
   }
}
