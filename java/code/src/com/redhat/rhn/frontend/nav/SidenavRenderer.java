/**
 * Copyright (c) 2009--2014 Red Hat, Inc.
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

import com.google.gson.GsonBuilder;
import com.suse.manager.webui.utils.gson.JSONMenuElement;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;

/**
 * SidenavRenderer: create a JSON object with the full menu tree
 * visible to the current user
 *
 * @version $Rev$
 */

public class SidenavRenderer extends Renderable {
    /**
     * Public constructor
     */
    public SidenavRenderer() {
        // empty
    }

    /** {@inheritDoc} */
    public void preNavLevel(StringBuffer sb, int depth) {
    }

    /** {@inheritDoc} */
    public void preNavNode(StringBuffer sb, int depth) {
    }

    /** {@inheritDoc} */
    public void navNodeActive(StringBuffer sb, NavNode node,
            NavTreeIndex treeIndex, Map parameters, int depth) {
    }

    /** {@inheritDoc} */
    public void navNodeInactive(StringBuffer sb, NavNode node,
            NavTreeIndex treeIndex, Map parameters, int depth) {
    }

    /** {@inheritDoc} */
    public void postNavNode(StringBuffer sb, int depth) {
    }

    /** {@inheritDoc} */
    public void postNavLevel(StringBuffer sb, int depth) {
    }

    /** {@inheritDoc} */
    public boolean nodeRenderInline(int depth) {
        return true;
    }

    /** {@inheritDoc} */
    public void preNav(StringBuffer sb) {
    }

    /** {@inheritDoc} */
    public void postNav(StringBuffer sb) {
    }

    /**
     * Convert a {@link NavTreeIndex} to a JSON String
     *
     * @param treeIndex the NavTreeIndex to convert as for input
     * @return the JSON String as for output
     */
    public String jsonRender(NavTreeIndex treeIndex) {
        return new GsonBuilder().create().toJson(
                buildJSONTree(treeIndex, treeIndex.getTree().getNodes(), 0));
    }

    /**
     * Generate a List of {@link JSONMenuElement} as a tree slice
     * of the whole treeIndex. Build it on the nodes bunch at the depth level.
     *
     * This method is designed to be recursively self-called
     * to build any n-level depth tree.
     *
     * @param treeIndex the full tree overview
     * @param nodes the subtree to build
     * @param depth the depth of the subtree
     * @return the subtree as a List of JSONMenuElement
     */
    public List<JSONMenuElement> buildJSONTree(NavTreeIndex treeIndex,
            List<NavNode> nodes, int depth) {
        if (nodes.size() == 0) {
            return null;
        }
        return nodes.stream()
                .filter(node -> canRender(node, depth))
                .map(node -> new JSONMenuElement(
                        node.getName(), node.getPrimaryURL(),
                        treeIndex.isNodeActive(node),
                        buildJSONTree(treeIndex, node.getNodes(), depth + 1)))
                .collect(toList());
    }
}
