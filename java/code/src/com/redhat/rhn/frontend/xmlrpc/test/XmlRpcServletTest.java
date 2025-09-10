/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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

package com.redhat.rhn.frontend.xmlrpc.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.XmlRpcServlet;
import com.redhat.rhn.frontend.xmlrpc.serializer.SerializerFactory;
import com.redhat.rhn.testing.MockObjectTestCase;
import com.redhat.rhn.testing.MockServletInputStream;
import com.redhat.rhn.testing.UserTestUtils;

import org.jmock.Expectations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class XmlRpcServletTest extends MockObjectTestCase {

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
        HibernateFactory.closeSession();
    }

    public void doTest(String request, String expectedResponse)
        throws Exception {

        StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);

        final MockServletInputStream input = new MockServletInputStream();
        input.setupRead(request.getBytes());

        final HttpServletRequest mockreq = this.mock(HttpServletRequest.class);
        final HttpServletResponse mockresp = this.mock(HttpServletResponse.class);

        context().checking(new Expectations() { {
            atLeast(1).of(mockreq).getHeader("SOAPAction");
            will(returnValue(null));
            atLeast(1).of(mockreq).getInputStream();
            will(returnValue(input));
            atLeast(1).of(mockreq).getRemoteAddr();
            will(returnValue("porsche.devel.redhat.com"));
            atLeast(1).of(mockreq).getLocalName();
            will(returnValue("foo.devel.redhat.com"));
            atLeast(1).of(mockreq).getProtocol();
            will(returnValue("http"));
            atLeast(1).of(mockresp).getWriter();
            will(returnValue(pw));
            atLeast(1).of(mockresp).setContentType("text/xml");
        } });

        // ok run servlet
        XmlRpcServlet xrs = new XmlRpcServlet(XmlRpcTestUtils.getTestHandlerFactory(), new SerializerFactory());
        xrs.init();
        xrs.doPost(mockreq, mockresp);

        assertEquals(expectedResponse, sw.toString());
    }

    @Test
    public void testStringReturn() throws Exception {
        doTest("<?xml version=\"1.0\"?> <methodCall> " +
               "<methodName>registration.privacyStatement</methodName>" +
               " <params> </params> </methodCall>",
               "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<methodResponse><params><param><value><string>This is " +
               "a privacy statement!</string></value></param></params>" +
               "</methodResponse>");
    }

    @Test
    public void testHashReturn() throws Exception {
        doTest("<?xml version=\"1.0\"?> <methodCall> " +
               "<methodName>unittest.login</methodName> <params> " +
               "</params> </methodCall>",
               "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<methodResponse><params><param><value><struct><member>" +
               "<name>X-RHN-Server-Id</name><value><string>foo" +
               "</string></value></member><member><name>" +
               "X-RHN-Auth-Server-Time</name><value><string>foo" +
               "</string></value></member><member><name>X-RHN-Auth" +
               "</name><value><string>foo</string></value></member>" +
               "<member><name>X-RHN-Auth-Channels</name><value><string>" +
               "foo</string></value></member><member><name>" +
               "X-RHN-Auth-Expire-Offset</name><value><string>foo" +
               "</string></value></member><member><name>" +
               "X-RHN-Auth-User-Id</name><value><string>foo</string>" +
               "</value></member></struct></value></param></params>" +
               "</methodResponse>");
    }

    @Test
    public void testWrongNumParams() throws Exception {
        Random rand = new Random();
        int param1 = rand.nextInt();
        doTest("<?xml version=\"1.0\"?> <methodCall> " +
               "<methodName>unittest.add</methodName> <params> " +
               "<param><value><i4>" + param1 + "</i4></value></param>" +
               "</params>" +
               "</methodCall>",

               "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<methodResponse><fault><value><struct><member><name>" +
               "faultCode</name><value><int>-1</int></value></member>" +
               "<member><name>faultString</name><value><string>" +
               "redstone.xmlrpc.XmlRpcFault: Could not find method: add in class: " +
               "com.redhat.rhn.frontend.xmlrpc.test.UnitTestHandler with params: " +
               "[java.lang.Integer]</string></value></member></struct></value></fault>" +
               "</methodResponse>");
    }

    @Test
    public void testWithParam() throws Exception {
        Random rand = new Random();
        int param1 = rand.nextInt();
        int param2 = rand.nextInt();
        doTest("<?xml version=\"1.0\"?> <methodCall> " +
               "<methodName>unittest.add</methodName> <params> " +
               "<param><value><i4>" + param1 + "</i4></value></param>" +
               "<param><value><i4>" + param2 + "</i4></value></param>" +
               "</params>" +
               "</methodCall>",

               "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<methodResponse><params><param><value><i4>" +
               (param1 + param2) + "</i4>" +
               "</value></param></params>" +
               "</methodResponse>");
    }

    @Test
    public void testFault() throws Exception {
        doTest("<?xml version=\"1.0\"?> <methodCall> " +
               "<methodName>unittest.throwFault</methodName> <params> " +
               "</params> </methodCall>",

               "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<methodResponse><fault><value><struct><member><name>" +
               "faultCode</name><value><int>1</int></value></member>" +
               "<member><name>faultString</name><value><string>" +
               "redstone.xmlrpc.XmlRpcFault: " +
               "This does not appear to be a valid username.</string>" +
               "</value></member></struct></value></fault>" +
               "</methodResponse>");
    }

    @Test
    public void testTranslation() throws Exception {
        User user = UserTestUtils.findNewUser("testuser", "testorg");
        doTest("<?xml version=\"1.0\"?> <methodCall> " +
               "<methodName>unittest.getUserLogin</methodName> <params> " +
               "<param><value><i4>" + user.getId() + "</i4></value></param>" +
               "</params>" +
               "</methodCall>",

               "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<methodResponse><params><param><value><string>" +
               user.getLogin() +
               "</string></value></param></params>" +
               "</methodResponse>");
    }

    @Test
    public void testCtor() {
        // this test makes sure we always have a default ctor
        XmlRpcServlet xrs = new XmlRpcServlet();
        assertNotNull(xrs);
    }
}
