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
package com.redhat.rhn.frontend.nav.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.frontend.nav.DialognavRenderer;
import com.redhat.rhn.frontend.nav.NavNode;
import com.redhat.rhn.frontend.nav.NavTree;
import com.redhat.rhn.frontend.nav.NavTreeIndex;
import com.redhat.rhn.frontend.nav.RenderGuard;
import com.redhat.rhn.frontend.nav.Renderable;
import com.redhat.rhn.frontend.nav.TextRenderer;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * RendererTest tests all Renderable classes.
 */
public class RendererTest extends RhnBaseTestCase {

    ////////////////////////////////////////////////////////////////
    // TEST: DialognavRenderer
    ////////////////////////////////////////////////////////////////

    @Test
    public void testDialognavTrue() {
        Map<String, Object> expectations = new HashMap<>();
        expectations.put("preNavLevel", "<ul class=\"nav nav-tabs nav-tabs-pf\">");
        expectations.put("preNavNode", "");
        expectations.put("navNodeActive", "<li class=\"active\">" +
            "<a href=\"http://rhn.redhat.com\" class=\"js-spa\">name</a></li>\n");
        expectations.put("navNodeInactive",
                         "<li><a href=\"http://rhn.redhat.com\" class=\"js-spa\">name</a></li>\n");
        expectations.put("postNavNode", "");
        expectations.put("postNavLevel", "</ul>\n");
        expectations.put("nodeRenderInline", Boolean.FALSE);

        // test depth > 1
        rendererTest(new DialognavRenderer(), new TrueRenderGuard(), expectations, 4);

        // test depth 1
        expectations.put("preNavLevel", "<ul class=\"nav nav-tabs nav-tabs-pf\">");
        expectations.put("postNavLevel", "</ul>\n");
        rendererTest(new DialognavRenderer(), new TrueRenderGuard(), expectations, 1);

        // test depth 0
        expectations.put("preNavLevel", "<ul class=\"nav nav-tabs\">");
        expectations.put("postNavLevel", "</ul>\n");
        rendererTest(new DialognavRenderer(), new TrueRenderGuard(), expectations, 0);
    }

    @Test
    public void testDialognavFalse() {
        Map<String, Object> expectations = new HashMap<>();
        expectations.put("preNavLevel", "");
        expectations.put("preNavNode", "");
        expectations.put("navNodeActive", "");
        expectations.put("navNodeInactive", "");
        expectations.put("postNavNode", "");
        expectations.put("postNavLevel", "");
        expectations.put("nodeRenderInline", Boolean.FALSE);

        rendererTest(new DialognavRenderer(), new FalseRenderGuard(), expectations, 0);
    }

    ////////////////////////////////////////////////////////////////
    // TEST: TextRenderer
    ////////////////////////////////////////////////////////////////

    @Test
    public void testTextTrue() {
        Map<String, Object> expectations = new HashMap<>();
        expectations.put("preNavLevel", "");
        expectations.put("preNavNode", "        ");

        NavNode node = forgeNavNode();
        expectations.put("navNodeActive",
                         "(*) Node 'name': http://rhn.redhat.com [acl: acl] " +
                         node.hashCode() + "\n");
        expectations.put("navNodeInactive",
                         "( ) Node 'name': http://rhn.redhat.com [acl: acl] " +
                         node.hashCode() + "\n");

        expectations.put("postNavNode", "");
        expectations.put("postNavLevel", "");
        expectations.put("nodeRenderInline", Boolean.TRUE);

        rendererTest(new TextRenderer(), node, new TrueRenderGuard(), expectations, 4);
    }

    @Test
    public void testTextFalse() {
        Map<String, Object> expectations = new HashMap<>();
        expectations.put("preNavLevel", "");
        expectations.put("preNavNode", "");
        expectations.put("navNodeActive", "");
        expectations.put("navNodeInactive", "");
        expectations.put("postNavNode", "");
        expectations.put("postNavLevel", "");
        expectations.put("nodeRenderInline", Boolean.TRUE);

        rendererTest(new TextRenderer(), new FalseRenderGuard(), expectations, 4);
    }

    ////////////////////////////////////////////////////////////////
    // Test methods
    ////////////////////////////////////////////////////////////////

    private void rendererTest(Renderable r, RenderGuard guard, Map<String, Object> exp,
            int depth) {
        rendererTest(r, forgeNavNode(), guard, exp, depth);
    }

    private void rendererTest(Renderable r, NavNode node, RenderGuard guard,
            Map<String, Object> exp, int depth) {

        NavTreeIndex treeIndex = forgeTreeIndex();
        rendererTest(r, node, guard, treeIndex, exp, depth);
    }

    private void rendererTest(Renderable r, NavNode node, RenderGuard guard,
            NavTreeIndex treeIndex, Map<String, Object> exp, int depth) {

        r.setRenderGuard(guard);


        // preNavLevel
        StringBuilder buf = new StringBuilder();
        r.preNavLevel(buf, depth);
        assertEquals(exp.get("preNavLevel"), buf.toString());

        // preNavNode
        buf = new StringBuilder();
        r.preNavNode(buf, depth);
        assertEquals(exp.get("preNavNode"), buf.toString());


        // navNodeActive
        buf = new StringBuilder();
        r.navNodeActive(buf, node, treeIndex, null, depth);
        assertEquals(exp.get("navNodeActive"), buf.toString());

        // navNodeInactive
        buf = new StringBuilder();
        r.navNodeInactive(buf, node, treeIndex, null, depth);
        assertEquals(exp.get("navNodeInactive"), buf.toString());

        // postNavNode
        buf = new StringBuilder();
        r.postNavNode(buf, depth);
        assertEquals(exp.get("postNavNode"), buf.toString());

        // postNavLevel
        buf = new StringBuilder();
        r.postNavLevel(buf, depth);
        assertEquals(exp.get("postNavLevel"), buf.toString());

        // nodeRenderInline
        boolean rc = r.nodeRenderInline(depth);
        Boolean v = (Boolean) exp.get("nodeRenderInline");
        assertEquals(v.booleanValue(), rc);

        // cleanup
        buf = null;
    }

    private NavTreeIndex forgeTreeIndex() {
        return new NavTreeIndex(new NavTree());
    }

    private NavNode forgeNavNode() {
        NavNode node = new NavNode();

        node.addURL("http://rhn.redhat.com");
        node.setLabel("label");
        node.setName("name");
        node.setAcl("acl");
        node.setDominant(true);
        node.setInvisible(false);
        node.setOverrideSidenav(false);
        node.setShowChildrenIfActive(true);
        node.setPermFailRedirect("permFailRedirect");
        node.setOnClick("onClick");
        node.setDynamicChildren("dynamicChildrenIn");

        return node;
    }

    ////////////////////////////////////////////////////////////////
    // INNER CLASSES
    ////////////////////////////////////////////////////////////////

    /**
     * A render guard that returns false for canRender for negative
     * testing.
     */
    public static class FalseRenderGuard implements RenderGuard {

        /**
         * method called to decide if to render
         * @param node the current NavNode
         * @param depth the current depth
         * @return boolean whether or not to render
         */
        @Override
        public boolean canRender(NavNode node, int depth) {
            return false;
        }
    }

    /**
     * A render guard that returns false for canRender for negative
     * testing.
     */
    public static class TrueRenderGuard implements RenderGuard {

        /**
         * method called to decide if to render
         * @param node the current NavNode
         * @param depth the current depth
         * @return boolean whether or not to render
         */
        @Override
        public boolean canRender(NavNode node, int depth) {
            return true;
        }
    }
}
