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
package com.redhat.rhn.frontend.taglibs.list.decorators;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringUtils;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.frontend.html.HtmlTag;
import com.redhat.rhn.frontend.taglibs.ListDisplayTag;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.frontend.taglibs.list.ListTagUtil;
import com.redhat.rhn.frontend.taglibs.list.SelectableColumnTag;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;

/**
 * Handles selectable lists, such as lists backed by RhnSet
 *
 * @version $Rev $
 */
public class SelectableDecorator extends BaseListDecorator {
    private static final String NULL_SELECTION = "0";
    private static final String JAVASCRIPT_TAG =
                    "<script type=\"text/javascript\">%s</script>";

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeList() throws JspException {
        ListTagUtil.write(pageContext, "<input type=\"hidden\" name=\"list_" +
                listName + "_all\" value=\"false\" id=\"" + "list_" + listName +
                "_all\" />");
        ListTagUtil.write(pageContext, "<input type=\"hidden\" name=\"list_" +
                listName + "_none\" value=\"false\" id=\"" + "list_" + listName +
                "_none\" />");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterTopPagination() throws JspException {
        renderSelectedCaption(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterBottomPagination() throws JspException {
        renderSelectedCaption(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFooterExtraAddons() throws JspException {
        renderSelectButtons();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterList() throws JspException {
        String script = SelectableColumnTag.
                        getPostScript(listName, pageContext.getRequest());
        if (!StringUtils.isBlank(script)) {
            ListTagUtil.write(pageContext, String.format(JAVASCRIPT_TAG, script));
        }
    }

    private void renderSelectedCaption(boolean isHeader) throws JspException {
        if (!currentList.isEmpty()) {
            String selectedName = ListTagUtil.makeSelectedAmountName(listName);
            String selected = (String) pageContext.getRequest().getAttribute(selectedName);
            if (selected == null) {
                selected = NULL_SELECTION;
            }
            LocalizationService ls = LocalizationService.getInstance();
            Object[] args = new Object[1];
            args[0] = selected;
            //
            //
            String setName = ListTagHelper.lookupSetDeclFor(listName,
                    pageContext.getRequest());
            if (!RhnSetDecl.SYSTEMS.getLabel().equals(setName)) {
                String msg = ls.getMessage("message.numselected", args);
                ListTagUtil.write(pageContext, "<span id=\"");
                if (isHeader) {
                    ListTagUtil.write(pageContext, "pagination_selcount_top");
                }
                else {
                    ListTagUtil.write(pageContext, "pagination_selcount_bottom");
                }
                ListTagUtil.write(pageContext, "\">");
                ListTagUtil.write(pageContext, msg);
                ListTagUtil.write(pageContext, "</span>");
            }
        }
    }

    private void renderSelectButtons() throws JspException {
        if (!currentList.isEmpty()) {
            StringBuilder buf = new StringBuilder();
            buf.append("<span class=\"spacewalk-list-selection-btns\">");
            String buttonName = ListTagUtil.makeSelectActionName(listName);
            LocalizationService ls = LocalizationService.getInstance();
            HtmlTag tag = new HtmlTag("button");
            tag.setAttribute("class", "btn btn-default");
            tag.setAttribute("type", "submit");
            tag.setAttribute("name", buttonName);
            tag.setAttribute("value",
                    ls.getMessage(ListDisplayTag.UPDATE_LIST_KEY));
            tag.setAttribute("id", "update_list_key_id");
            tag.setBody(ls.getMessage(ListDisplayTag.UPDATE_LIST_KEY));
            buf.append(tag.render()).append("&nbsp;");

            tag.setAttribute("value",
                    ls.getMessage(ListDisplayTag.SELECT_ALL_KEY));
            tag.setBody(ls.getMessage(ListDisplayTag.SELECT_ALL_KEY));
            buf.append(tag.render()).append("&nbsp;");

            String selectedName = ListTagUtil.makeSelectedAmountName(listName);
            String selected = (String) pageContext.getRequest().getAttribute(selectedName);
            if (!NULL_SELECTION.equals(selected) &&  selected != null) {
                tag.setAttribute("value",
                        ls.getMessage(ListDisplayTag.UNSELECT_ALL_KEY));
                tag.setBody(ls.getMessage(ListDisplayTag.UNSELECT_ALL_KEY));
                buf.append(tag.render()).append("\n");
            }
            buf.append("</span>");
            ListTagUtil.write(pageContext, buf.toString());
        }
    }
}
