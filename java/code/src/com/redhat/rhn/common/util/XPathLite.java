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
package com.redhat.rhn.common.util;

import org.jdom.Document;
import org.jdom.Element;

import java.util.Collections;
import java.util.List;

/**
 * Simple implementation of XPath-like selectors
 * without XPath
 *
 */
public class XPathLite {

    private String[] names;

    /**
     * Constructor
     * @param expr XPath expression to use
     */
    public XPathLite(String expr) {
        if (expr.startsWith("//")) {
            expr = expr.substring(2);
        }
        else if (expr.startsWith("/")) {
            expr = expr.substring(1);
        }
        names = expr.split("/");
    }


    /**
     * Select a single node based on expression
     * @param doc XML doc to search
     * @return node if found, otherwise null
     */
    public Element selectNode(Document doc) {
        Element current = doc.getRootElement();
        for (String nameIn : names) {
            current = findChild(current, nameIn);
            if (current == null) {
                break;
            }
        }
        return current;
    }

    /**
     * Select all children of a node based on expression
     * @param doc XML doc to search
     * @return node if found, otherwise null
     */
    @SuppressWarnings("unchecked")
    public List<Element> selectChildren(Document doc) {
        Element current = selectNode(doc);
        if (current != null) {
            return current.getChildren();
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private Element findChild(Element current, String name) {
        List<Element> children = current.getChildren();
        for (Element child : children) {
            if (child.getName().equals(name)) {
                current = child;
                break;
            }
            current = null;
        }
        return current;
    }
}
