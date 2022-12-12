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

package com.redhat.rhn.frontend.taglibs.list;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.frontend.html.HtmlTag;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Implements one column of a displayed data list
 */
public class ColumnTag extends BodyTagSupport {
    private static final Logger LOG = LogManager.getLogger(ColumnTag.class);

    private static final long serialVersionUID = -1139212563984660282L;

    protected String styleClass;

    protected String attributeName;
    protected String sortAttribute;
    protected boolean isBound;
    protected String headerKey;
    protected String headerText;
    protected String headerStyle;
    protected String headerClass;
    protected boolean sortable;
    private String filterAttr;
    private String filterMessage;
    private String width;

    /**
     * @param widthIn The width to set.
     */
    public void setWidth(String widthIn) {
        this.width = widthIn;
    }

    /**
     * @param filterMessageIn The filterMessage to set.
     */
    public void setFiltermessage(String filterMessageIn) {
        filterMessage = filterMessageIn;
    }

    /**
     * Sets sortable attribute
     * True values: true, t, yes, y, 1
     * False values: Everything else
     * @param sortableIn flag
     */
    public void setSortable(String sortableIn) {
        sortable = ListTagUtil.toBoolean(sortableIn);
    }

    /**
     * Sets the attribute to sort on. This will override the attribute, if any,
     * bound to the column
     * @param sortAttr sort attribute
     */
    public void setSortattr(String sortAttr) {
        sortAttribute = sortAttr;
        sortable = true;
    }

    /**
     * Sets the resource bundle key used to display the column's header
     * @param key resource bundle key
     */
    public void setHeaderkey(String key) {
        headerKey = key;
    }

    /**
     * Sets the text to display in the column's header.  "header text" may be used
     * instead of a header key in cases where there is no resource bundle needed.
     * For example, if column heading was a system name and didn't need translation.
     * @param text text
     */
    public void setHeadertext(String text) {
        headerText = text;
    }

    /**
     * Sets the CSS class for the header
     * @param styleIn CSS class name
     */
    public void setHeaderclass(String styleIn) {
        headerStyle = styleIn;
    }

    /**
     * Sets the data bean attribute to use for this column
     * @param attr data bean attribute
     */
    public void setAttr(String attr) {
        attributeName = attr;
    }

    /**
     * Sets the CSS class to use for the data cells
     * @param styleIn CSS class name
     */
    public void setStyleclass(String styleIn) {
        styleClass = styleIn;
    }


    /**
     * Is this column bound or not
     * @param bound bound flag
     */
    public void setBound(String bound) {
        isBound = ListTagUtil.toBoolean(bound);
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public int doStartTag() throws JspException {
        ListCommand command = ListTagUtil.getCurrentCommand(this, pageContext);
        ListTag parent = (ListTag) TagSupport.findAncestorWithClass(this,
                ListTag.class);
        int retval = Tag.SKIP_BODY;

        if (command.equals(ListCommand.ENUMERATE)) {
            parent.addColumn();
            retval = Tag.EVAL_PAGE;
            if (isSortable()) {
                parent.setSortable(true);
            }
        }
        else if (command.equals(ListCommand.COL_HEADER)) {
            renderHeader();
            retval = Tag.EVAL_PAGE;
        }
        else if (command.equals(ListCommand.RENDER)) {
            if (isBound) {
                renderBound();
                retval = Tag.SKIP_BODY;
            }
            else {
                renderUnbound();
                retval = Tag.EVAL_BODY_INCLUDE;
            }
        }
        return retval;
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public int doEndTag() throws JspException {
        if (sortable && attributeName == null && sortAttribute == null) {
            throw new JspException("Sortable columns must use either attr or sortAttr");
        }
        checkForBoundsAndAttrs();
        ListCommand command = ListTagUtil.getCurrentCommand(this, pageContext);
        if (command.equals(ListCommand.RENDER)) {
            ListTagUtil.write(pageContext, "</td>");
        }
        else if (command.equals(ListCommand.ENUMERATE) &&
                            !StringUtils.isBlank(filterAttr)) {
            setupColumnFilter();
        }

        return Tag.EVAL_PAGE;
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public void release() {
        width = null;
        styleClass = null;
        attributeName = null;
        isBound = false;
        headerKey = null;
        headerText = null;
        headerStyle = null;
        headerClass = null;
        filterAttr = null;
        sortable = false;
        filterMessage = null;
    }

    protected void renderHeader() throws JspException {
        if ((headerKey == null) && (headerText == null)) {
            return;
        }
        ListTagUtil.write(pageContext, "<th");

        if (headerClass != null) {
            ListTagUtil.write(pageContext, " class=\"");
            ListTagUtil.write(pageContext, headerClass);
            ListTagUtil.write(pageContext, "\" ");
        }

        if (headerStyle != null || isCurrColumnSorted()) {
            ListTagUtil.write(pageContext, " class=\"");

            if (headerStyle != null) {
                ListTagUtil.write(pageContext, headerStyle);
                ListTagUtil.write(pageContext, " ");
            }

            ListTagUtil.write(pageContext, getCurrentSortDir() + "Sort");

            ListTagUtil.write(pageContext, "\"");
        }


        ListTagUtil.write(pageContext, ">");

        if (filterAttr != null) {
            HtmlTag filterClass = new HtmlTag("input");
            filterClass.setAttribute("type", "hidden");
            filterClass.setAttribute("name",
                    ListTagUtil.makeFilterAttributeByLabel(getListName()));
            filterClass.setAttribute("value", filterAttr);
            ListTagUtil.write(pageContext, filterClass.render());
        }

        if (isSortable()) {
            writeSortLink();
        }
        else {
            writeColumnName();
        }
        ListTagUtil.write(pageContext, "</th>");
    }

    private boolean isCurrColumnSorted() {
        return !StringUtils.isEmpty(getCurrentSortDir());
    }

    private void writeSortLink() throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        String sortBy = getSortName();
        String sortDir = getCurrentSortDir();
        String jsurl = ListTagUtil.makeColumnSortLink(request, getListName(),
                sortBy, sortDir);
        String href = "<a href=\"javascript:%s\">";
        ListTagUtil.write(pageContext, String.format(href, jsurl));
        writeColumnName();
        ListTagUtil.write(pageContext, "</a>");

    }

    /**
     * @throws JspException
     */
    private void writeColumnName() throws JspException {
        LocalizationService ls = LocalizationService.getInstance();
        if (headerKey != null) {
            ListTagUtil.write(pageContext, ls.getMessage(headerKey));
        }
        else {
            ListTagUtil.write(pageContext, headerText);
        }
    }

    protected void renderUnbound() throws JspException {
        ListTag parent = (ListTag)
            TagSupport.findAncestorWithClass(this, ListTag.class);
        if (attributeName != null) {
            Object bean = parent.getCurrentObject();
            String value = ListTagUtil.getBeanValue(bean, attributeName);
            pageContext.setAttribute("beanValue", value, PageContext.PAGE_SCOPE);
        }
        writeStartingTd();
    }

    protected void renderBound() throws JspException {
        ListTag parent = (ListTag)
            TagSupport.findAncestorWithClass(this, ListTag.class);
        Object bean = parent.getCurrentObject();
        writeStartingTd();
        ListTagUtil.write(pageContext, ListTagUtil.
                                        getBeanValue(bean, attributeName));
    }

    /**
     *
     */
    private void checkForBoundsAndAttrs() {
        if (isBound && StringUtils.isBlank(attributeName)) {
            String msg = String.format("Error Rendering column - [%s]. " +
                        "You are probably using bound=true without an attr." +
                        " Either have both bound = true with " +
                            "an attr OR set bound=false.", headerKey);
            throw new RuntimeException(msg);
        }
    }

    protected String getListName() {
        ListTag parent = (ListTag)
            TagSupport.findAncestorWithClass(this, ListTag.class);
        return parent.getUniqueName();
    }

    protected void writeStartingTd() throws JspException {
        ListTagUtil.write(pageContext, "<td");

        ListCommand command = ListTagUtil.getCurrentCommand(this, pageContext);

        if (styleClass != null ||
                (isCurrColumnSorted() && command != ListCommand.COL_HEADER)) {
            ListTagUtil.write(pageContext, " class=\"");
            if (styleClass != null) {
                ListTagUtil.write(pageContext, styleClass);
                ListTagUtil.write(pageContext, " ");
            }
            if (isCurrColumnSorted()) {
                ListTagUtil.write(pageContext, "sortedCol");
            }
            ListTagUtil.write(pageContext, "\"");
        }
        if (!StringUtils.isBlank(width)) {
            ListTagUtil.write(pageContext, " width=\"");
            ListTagUtil.write(pageContext, width);
            ListTagUtil.write(pageContext, "\"");
        }
        ListTagUtil.write(pageContext, ">");
    }

    private boolean isSortable() {
        ListTag parent = (ListTag)
            TagSupport.findAncestorWithClass(this, ListTag.class);
        return sortable && parent.getPageRowCount() > 0;
    }

    /**
     * Sets up this column as the defualt for sorting...
     * @param sortDir the sort direction... asc/desc
     */
    public void setDefaultsort(String sortDir) {
        String sortName = getSortName();
        if (!StringUtils.isBlank(sortName)) {
            ListTag parent = (ListTag) TagSupport.findAncestorWithClass(this,
                    ListTag.class);

            if (StringUtils.isBlank(parent.getDefaultSortAttr())) {
                parent.setDefaultsortattr(sortName);
                parent.setDefaultsortdir(sortDir);
            }
            else if (!parent.getDefaultSortAttr().equals(sortName)) {
                String msg = "Trying to set  column [{}] as the default sort." +
                "The default sort column has already been set for [{}]." +
                        " Can't reset it to [{}].";

                LOG.warn(msg, sortName, parent.getDefaultSortAttr(), sortName);
            }
        }
        else {
            String msg = "Can't set a default sort value for a " +
                        "column that does not have a sortattr or attr tags set. ";
            LOG.warn(msg);
        }
    }

    /**
     * Gets current sort direction for this column, or empty string if the list is not
     * currently sorted on this column
     *
     * @return Current sort direction for the column
     */
    private String getCurrentSortDir() {
        ListTag parent = (ListTag) TagSupport.findAncestorWithClass(this,
                ListTag.class);

        String currSortAttr = parent.getCurrentSortAttr();
        String currSortDir = parent.getCurrentSortDir();

        return currSortAttr.equals(getSortName()) ? currSortDir : "";
    }

    /**
     * @return the sort by column name
     */
    private String getSortName() {
        if (sortAttribute != null) {
           return sortAttribute;
        }
        return attributeName;
    }

    /**
     * @return Returns the headerClass.
     */
    public String getHeaderClass() {
        return headerClass;
    }

    /**
     * @param headerClassIn The headerClass to set.
     */
    public void setHeaderClass(String headerClassIn) {
        this.headerClass = headerClassIn;
    }

    /**
     * Sets the filter attribute
     * @param attribute the name of the filter attribute
     */
    public void setFilterattr(String attribute) {
        this.filterAttr = attribute;
    }

    private void setupColumnFilter() throws JspException {
        ListTag parent = (ListTag)
                TagSupport.findAncestorWithClass(this, ListTag.class);
        String key = headerKey;
        if (!StringUtils.isBlank(filterMessage)) {
            key = filterMessage;
        }
        ColumnFilter f = new ColumnFilter(key, filterAttr);
        parent.setColumnFilter(f);
    }
}
