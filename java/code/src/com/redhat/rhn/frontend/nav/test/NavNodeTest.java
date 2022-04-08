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
package com.redhat.rhn.frontend.nav.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.frontend.nav.NavNode;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.apache.commons.beanutils.MethodUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

/**
 * NavNodeTest
 */
public class NavNodeTest extends RhnBaseTestCase {

    private NavNode node;

    @BeforeEach
    public void setUp() throws Exception {
        node = new NavNode();
        TestUtils.disableLocalizationLogging();
    }

    @Test
    public void testAddNode() {
        for (int i = 0; i < 10; i++) {
            NavNode n = new NavNode();
            n.setName(Integer.toString(i));
            node.addNode(n);
        }

        List list = node.getNodes();
        for (int i = 0; i < 10; i++) {
            NavNode n = (NavNode) list.get(i);
            assertEquals(Integer.toString(i), n.getName());
        }
    }

    // Some reflection trickery here to verify that we set the
    // localized key at the right time.
    @Test
    public void testLocalizedName() throws Exception {
        NavNode n1 = new NavNode();
        String randName = TestUtils.randomString();
        n1.setName(randName);
        node.addNode(n1);

        NavNode n2 = node.getNodes().get(0);
        assertEquals("**" + randName + "**", n2.getName());
        Class c = n2.getClass();
        Field[] fields = c.getDeclaredFields();
        String privateValue = null;
        for (Field fieldIn : fields) {
            if (fieldIn.getName().equals("name")) {
                fieldIn.setAccessible(true);
                privateValue = (String) fieldIn.get(n1);
            }
        }
        assertEquals(randName, privateValue);

    }

    @Test
    public void testEscapedName() {
        NavNode theNode = new NavNode();
        String random = TestUtils.randomString();
        String name = random + "&you";
        String escapedName = random + "&amp;amp;you";
        //it localizes the name too
        //LocalizationService escapes "key-not-found" strings as a general rule -
        //and so does getName(). So fail-string will be escaped TWICE
        String expected = "**" + escapedName + "**";

        theNode.setName(name);
        assertEquals(expected, theNode.getName());
    }


    @Test
    public void testAddUrls() {
        for (int i = 0; i < 10; i++) {
            node.addURL(Integer.toString(i));
        }

        List list = node.getURLs();
        for (int i = 0; i < 10; i++) {
            String n = (String) list.get(i);
            assertEquals(Integer.toString(i), n);
        }
    }

    @Test
    public void testExceptionCase() {
        boolean flag = false;
        try {
            node.getPrimaryURL();
            flag = true;
        }
        catch (IndexOutOfBoundsException ioobe) {
            assertFalse(flag);
        }
    }

    @Test
    public void testToString() {
        assertNotNull(node.toString());
    }

    @Test
    public void testStringSetters()
        throws Exception {
        String[] methods = { "Label", "Name", "Acl",
                "PermFailRedirect", "OnClick",
                "DynamicChildren" };

        for (String methodIn : methods) {
            verifyStringSetterMethod(methodIn);
        }
    }

    @Test
    public void testBooleanSetters()
        throws Exception {
        String[] methods = { "Dominant", "Invisible", "OverrideSidenav",
                "ShowChildrenIfActive" };

        for (String methodIn : methods) {
            verifyBooleanSetterMethod(methodIn);
        }
    }

    private void verifyStringSetterMethod(String methodname)
        throws Exception {
        Object[] args = { "value" };
        MethodUtils.invokeMethod(node, "set" + methodname, args);
        String rc = (String) MethodUtils.invokeMethod(node, "get" + methodname,
                null);
        assertEquals("value", rc);
    }

    private void verifyBooleanSetterMethod(String methodname)
        throws Exception {
        Object[] args = { Boolean.TRUE };
        MethodUtils.invokeMethod(node, "set" + methodname, args);
        Boolean rc = (Boolean) MethodUtils.invokeMethod(node, "get" +
                methodname, null);
        assertTrue(rc);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        node = null;
    }
}
