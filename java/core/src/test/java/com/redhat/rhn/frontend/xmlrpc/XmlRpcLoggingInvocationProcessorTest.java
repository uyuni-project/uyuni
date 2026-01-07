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
package com.redhat.rhn.frontend.xmlrpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.session.WebSession;
import com.redhat.rhn.domain.session.WebSessionFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Arrays;

import redstone.xmlrpc.XmlRpcInvocation;

/**
 * LoggingInvocationProcessorTest
 */
public class XmlRpcLoggingInvocationProcessorTest extends RhnBaseTestCase {

    private XmlRpcLoggingInvocationProcessor lip;
    private Writer writer;

    @Override
    @BeforeEach
    public void setUp() {
        lip = new XmlRpcLoggingInvocationProcessor();
        writer = new StringWriter();
    }

    private class TestHandler extends BaseHandler {

        public Method getMethodArg1() throws NoSuchMethodException {
            return testHandler.getClass().getMethod("testMethod1Arg", String.class);
        }

        public Method getMethodArg2() throws NoSuchMethodException {
            return testHandler.getClass().getMethod("testMethod2Arg", String.class, String.class);
        }

        public void testMethod1Arg(String arg1) {
            //test method
        }

        public void testMethod2Arg(String arg1, String arg2) {
            //test method
        }
    }

    private TestHandler testHandler = new TestHandler();

    @Test
    public void testPreProcess() {
        String[] args = {"username", "password"};

        boolean rc = lip.before(new XmlRpcInvocation(10, "handler",
                "method", null, Arrays.asList(args), writer));

        assertTrue(rc);
    }

    @Test
    public void testPreProcessWithXmlArg() {
        String[] args = {"<?xml version=\"1.0\"?><somestuff>foo</somestuff>",
                "password"};

        boolean rc = lip.before(new XmlRpcInvocation(10, "handler",
                "method", null, Arrays.asList(args), writer));

        assertTrue(rc);
    }

    @Test
    public void testPreProcessWithValidSession() {
        // create a web session indicating a logged in user.
        WebSession s = WebSessionFactory.createSession();
        assertNotNull(s);
        WebSessionFactory.save(s);
        assertNotNull(s.getId());

        String[] args = {s.getKey()};

        boolean rc = lip.before(new XmlRpcInvocation(10, "handler",
                "method", null, Arrays.asList(args), writer));

        assertTrue(rc);
    }

    @Test
    public void testPreProcessWithInvalidSession() {
        String[] args = {"12312312xFFFFFABABABFFFCD01"};

        boolean rc = lip.before(new XmlRpcInvocation(10, "handler",
                    "method", null, Arrays.asList(args), writer));
        assertTrue(rc);
    }

    @Test
    public void testPostProcess() throws NoSuchMethodException {
        String[] args = {"<?xml version=\"1.0\"?><somestuff>foo</somestuff>",
                "password"};

        XmlRpcLoggingInvocationProcessor.setCalledMethod(testHandler.getMethodArg2());
        Object rc = lip.after(new XmlRpcInvocation(10, "handler", "method",
                null, Arrays.asList(args), writer), "returnthis");
        assertEquals("returnthis", rc);
        assertEquals("", writer.toString());
    }

    @Test
    public void testPostProcessValidSession() throws NoSuchMethodException {
        User user = UserTestUtils.createUser(this);
        // create a web session indicating a logged in user.
        WebSession s = WebSessionFactory.createSession();
        s.setWebUserId(user.getId());
        assertNotNull(s);
        WebSessionFactory.save(s);
        assertNotNull(s.getId());

        String[] args = {s.getKey()};
        lip.before(new XmlRpcInvocation(10, "handler", "method",
                null, Arrays.asList(args), writer));
        XmlRpcLoggingInvocationProcessor.setCalledMethod(testHandler.getMethodArg1());
        Object rc = lip.after(new XmlRpcInvocation(10, "handler", "method",
                null, Arrays.asList(args), writer), "returnthis");
        assertEquals("returnthis", rc);
        assertEquals("", writer.toString());
    }



    @Test
    public void testPostProcessInvalidSession() throws NoSuchMethodException {
        String[] args = {"12312312xFFFFFABABABFFFCD01"};

        lip.before(new XmlRpcInvocation(10, "handler", "method",
                null, Arrays.asList(args), writer));
        XmlRpcLoggingInvocationProcessor.setCalledMethod(testHandler.getMethodArg1());
        Object rc = lip.after(new XmlRpcInvocation(10, "handler", "method",
                null, Arrays.asList(args), writer), "returnthis");
        assertEquals("returnthis", rc);
        assertEquals("", writer.toString());
    }

    @Test
    public void testPostProcessWhereFirstArgHasNoX() throws NoSuchMethodException {
        String[] args = {"abcdefghijklmnopqrstuvwyz", "password"};

        XmlRpcLoggingInvocationProcessor.setCalledMethod(testHandler.getMethodArg2());
        Object rc = lip.after(new XmlRpcInvocation(10, "handler", "method",
                null, Arrays.asList(args), writer), "returnthis");
        assertEquals("returnthis", rc);
        assertEquals("", writer.toString());
    }

    @Test
    public void testAuthLogin() throws NoSuchMethodException {
        String[] args = {"user", "password"};

        XmlRpcLoggingInvocationProcessor.setCalledMethod(testHandler.getMethodArg2());
        Object rc = lip.after(new XmlRpcInvocation(10, "auth", "login",
                null, Arrays.asList(args), writer), "returnthis");
        assertEquals("returnthis", rc);
        assertEquals("", writer.toString());
    }
}
