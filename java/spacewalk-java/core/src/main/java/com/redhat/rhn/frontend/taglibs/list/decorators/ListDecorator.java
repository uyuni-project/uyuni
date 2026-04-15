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
package com.redhat.rhn.frontend.taglibs.list.decorators;

import com.redhat.rhn.frontend.taglibs.list.ListSetTag;
import com.redhat.rhn.frontend.taglibs.list.ListTag;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;

/**
 * Decorator for a given list
 *
 * Decorators receive callbacks corresponding to events which occur during the
 * rendering of list. These callbacks can be used to modify the appearance of the
 * list widget on the page.
 *
 *
 */
public interface ListDecorator {

    /**
     * Notifies the decorator that a column has been detected
     */
    void addColumn();

    /**
     * Notifies the decorator that list rendering is about to begin
     * @throws JspException something bad happened
     */
    void beforeList() throws JspException;

    /**
     * Notifies the decorator that top pagination controls have been rendered
     * @throws JspException something bad happened
     */
    void afterTopPagination() throws JspException;

    /**
     * Notifies the decorator that bottom pagination controls have been rendered
     * @throws JspException something bad happened
     */
    void afterBottomPagination() throws JspException;

    /**
     * Notifies the decorator that list rendering is complete
     * @throws JspException something bad happened
     */
    void afterList() throws JspException;

    /**
     * Sets up the runtime environment for the decorator
     * @param ctx current JSP PageContext
     * @param parent enclosing ListSetTag
     * @param listName name of list
     */
    void setEnvironment(PageContext ctx, ListSetTag parent, String listName);

    /**
     * Sets a refernce to the currently rendering list tag
     * @param currentList the current list tag
     */
    void setCurrentList(ListTag currentList);

    /**
     * Use this hook to insert content before the top pagination
     *
     */
    void beforeTopPagination();

    /**
     * With this hook one can insert content before the list
     *
     */
    void onTopExtraContent();

    /**
     * With this hook one can insert content in the heading
     *
     */
    void onHeadExtraContent();

    /**
     * With this hook one can insert content in the footer
     *
     */
    void onFooterExtraContent();

    /**
     * With this hook one can insert content after the list
     *
     */
    void onBottomExtraContent();

    /**
     * With this hook one can insert extra addons in the before the list
     *
     */
    void onTopExtraAddons();

    /**
     * With this hook one can insert extra addons in the heading
     *
     * @throws JspException
     *             something bad happened
     */
    void onHeadExtraAddons() throws JspException;

    /**
     * With this hook one can insert extra addons in the footer
     *
     * @throws JspException
     *             something bad happened
     */
    void onFooterExtraAddons() throws JspException;

    /**
     * With this hook one can insert extra addons after the list
     *
     */
    void onBottomExtraAddons();
}
