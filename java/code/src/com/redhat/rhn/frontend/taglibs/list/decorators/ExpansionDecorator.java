/**
 * Copyright (c) 2010--2014 Red Hat, Inc.
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
import com.redhat.rhn.frontend.taglibs.list.ListTag;
import com.redhat.rhn.frontend.taglibs.list.ListTagUtil;

import javax.servlet.jsp.JspException;


/**
 * ExpandableDecorator
 * @version $Rev$
 */
public class ExpansionDecorator extends BaseListDecorator {

    private static final String SHOW_ALL_SCRIPT =
       "<div style=\"text-align: right;\"><a href=\"javascript:showAllRows(rowHash%s);\"" +
            " style=\"cursor: pointer;\">%s</a>&nbsp;&nbsp;|&nbsp;&nbsp;" +
            "<a href=\"javascript:hideAllRows(rowHash%s);\" " +
            "style=\"cursor: pointer;\">%s</a></div>";

    private static final String NEW_VAR_SCRIPT = "<script type=\"text/javascript\">var " +
                                                       "rowHash%s = new Array();</script>";

    private static final String LOAD_SCRIPT = "<script type=\"text/javascript\">" +
                                            "onLoadStuff(%s, '%s', rowHash%s);" +
                                                "showAllRows(rowHash%s);</script>";

    private static final String IMPORT_TREE_JS =
        "<script src=\"/javascript/tree.js\" type=\"text/javascript\"></script>";
    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeList() throws JspException {
        //<script src="/javascript/tree.js" type="text/javascript"></script>
        ListTagUtil.write(pageContext, IMPORT_TREE_JS);
        ListTagUtil.write(pageContext, String.format(NEW_VAR_SCRIPT, listName));
        LocalizationService ls = LocalizationService.getInstance();
        ListTagUtil.write(pageContext, String.format(SHOW_ALL_SCRIPT, listName,
                          ls.getMessage("show.all"), listName, ls.getMessage("hide.all")));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void afterList() throws JspException {
        ListTag list = getCurrentList();
        ListTagUtil.write(pageContext, String.format(LOAD_SCRIPT,
                            list.getColumnCount(), list.getStyleId() + "_exp",
                            listName, listName));
    }
}
