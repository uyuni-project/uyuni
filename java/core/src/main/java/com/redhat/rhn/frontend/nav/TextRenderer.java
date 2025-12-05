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

package com.redhat.rhn.frontend.nav;

import java.util.Map;

/**
 * TextRenderer renders each node in the Navigation Tree as a
 * formatted text string.<p>Each level is indented two (2)
 * spaces for each level. For example, a tree with three
 * levels will be indented by six (6) spaces at the third
 * level.  This is a great renderer for test cases.
 * <pre>
 *     (*) Node 'name': url [acl: acl] "
 * </pre>
 */

public class TextRenderer extends Renderable {
    /**
     * Public constructor
     */
    public TextRenderer() {
        // empty
    }

    /** {@inheritDoc} */
    @Override
    public void preNavLevel(StringBuilder sb, int depth) {
        //do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void preNavNode(StringBuilder sb, int depth) {
        if (!canRender(null, depth)) {
            return;
        }

        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void navNodeActive(StringBuilder sb,
                              NavNode node,
                              NavTreeIndex treeIndex,
                              Map parameters,
                              int depth) {
        if (!canRender(node, depth)) {
            return;
        }

        sb.append("(*) Node '" + node.getName() + "': " + node.getPrimaryURL());
        if (node.getAcl() != null) {
            sb.append(" [acl: " + node.getAcl() + "]");
        }
        sb.append(" " + node.hashCode());

        sb.append("\n");
    }

    /** {@inheritDoc} */
    @Override
    public void navNodeInactive(StringBuilder sb,
                                NavNode node,
                                NavTreeIndex treeIndex,
                                Map parameters,
                                int depth) {
        if (!canRender(node, depth)) {
            return;
        }

        sb.append("( ) Node '" + node.getName() + "': " + node.getPrimaryURL());
        if (node.getAcl() != null) {
            sb.append(" [acl: " + node.getAcl() + "]");
        }
        sb.append(" " + node.hashCode());

        sb.append("\n");
    }

    /** {@inheritDoc} */
    @Override
    public void postNavNode(StringBuilder sb, int depth) {
    }

    /** {@inheritDoc} */
    @Override
    public void postNavLevel(StringBuilder sb, int depth) {
    }

    /** {@inheritDoc} */
    @Override
    public boolean nodeRenderInline(int depth) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void preNav(StringBuilder sb) {
    }

    /** {@inheritDoc} */
    @Override
    public void postNav(StringBuilder sb) {
    }
}


