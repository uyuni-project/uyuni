/*
 * Copyright (c) 2009--2016 Red Hat, Inc.
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

import com.redhat.rhn.frontend.html.HtmlTag;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

/**
 * DialognavRenderer - renders a navigation bar
 *
 * Renders the navigation inside the content, which is implemented
 * as rows of Twitter Bootstrap tabs (nav-tabs)
 *
 * The navigation is enclosed in a div styled with class
 * 'spacewalk-content-nav' and the individual rows can be styled by
 * ul:nth-child selectors.
 *
 */

public class DialognavRenderer extends Renderable {
    private final StringBuilder titleBuf;
    /**
     * Public constructor
     */
    public DialognavRenderer() {
         // empty
        titleBuf = new StringBuilder();
    }

    /** {@inheritDoc} */
    @Override
    public void preNav(StringBuilder sb) {
        HtmlTag div = new HtmlTag("div");
        div.setAttribute("class", "spacewalk-content-nav");
        sb.append(div.renderOpenTag());
    }

    /** {@inheritDoc} */
    @Override
    public void preNavLevel(StringBuilder sb, int depth) {
        if (!canRender(null, depth)) {
            return;
        }

        HtmlTag ul = new HtmlTag("ul");
        if (depth == 0) {
            ul.setAttribute("class", "nav nav-tabs");
        }
        else {
            ul.setAttribute("class", "nav nav-tabs nav-tabs-pf");
        }
        sb.append(ul.renderOpenTag());
    }

    /** {@inheritDoc} */
    @Override
    public void preNavNode(StringBuilder sb, int depth) {
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

        titleBuf.append(" - " + node.getName());

        renderNode(sb, node, treeIndex, parameters,
                   "active");
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

        renderNode(sb, node, treeIndex, parameters, "");
    }

    private void renderNode(StringBuilder sb, NavNode node,
                            NavTreeIndex treeIndex, Map parameters,
                            String cssClass) {
        HtmlTag li = new HtmlTag("li");

        if (!cssClass.equals("")) {
            li.setAttribute("class", cssClass);
        }

        String href = node.getPrimaryURL();
        String hrefNew = href;
        if (parameters != null) {
            StrSubstitutor substitutor = new StrSubstitutor(
                    ((Map<String, String[]>)parameters).entrySet().stream()
                        .filter(entry -> entry.getValue() != null && entry.getValue().length > 0)
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()[0])));
            hrefNew = substitutor.replace(href);
        }
        String allowedFormVars = treeIndex.getTree().getFormvar();
        if (!href.equals(hrefNew)) {
            href = hrefNew;
        }
        else if (allowedFormVars != null) {
            StringBuilder formVars;
            if (href.indexOf('?') == -1) {
                formVars = new StringBuilder("?");
            }
            else {
                formVars = new StringBuilder("&");
            }

            StringTokenizer st = new StringTokenizer(allowedFormVars);
            while (st.hasMoreTokens()) {
                if (formVars.length() > 1) {
                    formVars.append("&amp;");
                }
                String currentVar = st.nextToken();
                String[] values = (String[])parameters.get(currentVar);

                // if currentVar is null, values will be null too, so we can
                // just check values.
                if (values != null) {
                    formVars.append(currentVar + "=" +
                             StringEscapeUtils.escapeHtml4(values[0]));
                }
            }
            href += formVars.toString();
        }

        li.addBody(aHref(href, node.getName(), node.getTarget()));
        sb.append(li.render());
        sb.append("\n");
    }

    /** {@inheritDoc} */
    @Override
    public void postNavNode(StringBuilder sb, int depth) {
    }

    /** {@inheritDoc} */
    @Override
    public void postNavLevel(StringBuilder sb, int depth) {
        if (!canRender(null, depth)) {
            return;
        }

        HtmlTag ul = new HtmlTag("ul");
        sb.append(ul.renderCloseTag());
        sb.append("\n");
    }

    /** {@inheritDoc} */
    @Override
    public void postNav(StringBuilder sb) {
        HtmlTag div = new HtmlTag("div");
        sb.append(div.renderCloseTag());
        sb.append("\n");
    }

    /** {@inheritDoc} */
    @Override
    public boolean nodeRenderInline(int depth) {
        return false;
    }

    private static String aHref(String url, String text, String target) {
        HtmlTag a = new HtmlTag("a");

        if (target != null && !target.equals("")) {
            a.setAttribute("target", target);
        }

        a.setAttribute("href", url);
        a.setAttribute("class", "js-spa");
        a.addBody(text);
        return a.render();
    }
}


