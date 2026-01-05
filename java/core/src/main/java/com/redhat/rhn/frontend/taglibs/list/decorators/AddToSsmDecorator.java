/*
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
package com.redhat.rhn.frontend.taglibs.list.decorators;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.frontend.html.HtmlTag;
import com.redhat.rhn.frontend.taglibs.ListDisplayTag;
import com.redhat.rhn.frontend.taglibs.list.ListTagUtil;

import javax.servlet.jsp.JspException;

/**
 * Decorator to tie into ListTag 3.0 to provide an easy means to add te selected servers
 * in a table to the SSM.
 * <p>
 * This decorator must be used in conjunction with the {@link SelectableDecorator}.
 */
public class AddToSsmDecorator extends BaseListDecorator {

    /** {@inheritDoc} */
    @Override
    public void onFooterExtraAddons() throws JspException {
        if (!currentList.isEmpty()) {

            // Collect the values needed to hook into the rest of the list tag framework
            String buttonName = ListTagUtil.makeSelectActionName(listName);

            LocalizationService ls = LocalizationService.getInstance();
            String value = ls.getMessage(ListDisplayTag.ADD_TO_SSM_KEY);

            // Generate the HTML to output
            StringBuilder buf = new StringBuilder();
            buf.append("<span class=\"spacewalk-list-selection-btns\">");

            //   Add to SSM button
            HtmlTag tag = new HtmlTag("button");
            tag.setAttribute("class", "btn btn-default");
            tag.setAttribute("type", "submit");
            tag.setAttribute("name", buttonName);
            tag.setAttribute("value", value);
            tag.setBody(value);
            buf.append(tag.render()).append("&nbsp;");

            buf.append("</span>");
            ListTagUtil.write(pageContext, buf.toString());
        }
    }
}
