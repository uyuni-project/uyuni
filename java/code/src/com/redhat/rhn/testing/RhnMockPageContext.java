/*
 * Copyright (c) 2011--2025 SUSE LLC
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

package com.redhat.rhn.testing;

import java.io.IOException;
import java.util.Enumeration;

import javax.el.ELContext;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

/**
 * RhnMockPageContext
 */
public class RhnMockPageContext extends PageContext {
    private JspWriter jspWriter;
    private ServletRequest request;
    private HttpSession httpSession;
    private ServletContext servletContext;

    /**
     * Releases any resources associated with this PageContext.
     * This is a mock implementation that does nothing.
     */
    public void release() {
    }

    /**
     * Gets the JspWriter object for this page context.
     *
     * @return the JspWriter object
     */
    public JspWriter getOut() {
        return jspWriter;
    }

    @Override
    public ExpressionEvaluator getExpressionEvaluator() {
        return null;
    }

    @Override
    public ELContext getELContext() {
        return null;
    }

    @Override
    public VariableResolver getVariableResolver() {
        return null;
    }

    /**
     * Sets the JspWriter object for this page context.
     *
     * @param jspWriterIn the JspWriter to set
     */
    public void setJspWriter(JspWriter jspWriterIn) {
        this.jspWriter = jspWriterIn;
    }

    /**
     * Handles a page exception.
     * This is a mock implementation that does nothing.
     *
     * @param e the exception to handle
     */
    public void handlePageException(Exception e) {
        // Mock implementation - no exception handling
    }

    /**
     * Gets the ServletContext for this page context.
     *
     * @return the ServletContext
     */
    public ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * Sets the ServletContext for this page context.
     *
     * @param servletContextIn the ServletContext to set
     */
    public void setServletContext(ServletContext servletContextIn) {
        this.servletContext = servletContextIn;
    }

    /**
     * Gets the scope of the attribute with the specified name.
     * This is a mock implementation that always returns -1.
     *
     * @param s the name of the attribute
     * @return always returns -1 in this mock implementation
     */
    public int getAttributesScope(String s) {
        return -1;
    }

    /**
     * Includes the content of the specified resource.
     * This is a mock implementation that does nothing.
     *
     * @param s the resource path to include
     */
    public void include(String s) {
        // Mock implementation - no content to include
    }

    /**
     * Includes the content of the specified resource.
     * This is a mock implementation that does nothing.
     *
     * @param sIn the resource path to include
     * @param bIn whether to flush the output buffer
     * @throws ServletException if an error occurs during inclusion
     * @throws IOException      if an I/O error occurs
     */
    @Override
    public void include(String sIn, boolean bIn) throws ServletException, IOException {
        // Mock implementation - no content to include
    }

    /**
     * Removes an attribute from the specified scope.
     * This is a mock implementation that does nothing.
     *
     * @param s the name of the attribute to remove
     * @param i the scope from which to remove the attribute
     */
    public void removeAttribute(String s, int i) {
        // Mock implementation - no attributes to remove
    }

    /**
     * Gets an enumeration of attribute names in the specified scope.
     * This is a mock implementation that always returns null.
     *
     * @param i the scope to get attribute names from
     * @return always returns null in this mock implementation
     */
    public Enumeration getAttributeNamesInScope(int i) {
        return null;
    }

    /**
     * Forwards the request to the specified resource.
     * This is a mock implementation that does nothing.
     *
     * @param s the resource path to forward to
     */
    public void forward(String s) {
        // Mock implementation - no forwarding performed
    }

    /**
     * Gets the servlet instance that is associated with this PageContext.
     * This is a mock implementation that always returns null.
     *
     * @return always returns null in this mock implementation
     */
    public Object getPage() {
        return null;
    }

    /**
     * Handles a page exception represented by a Throwable.
     * This is a mock implementation that does nothing.
     *
     * @param t the throwable to handle
     */
    public void handlePageException(Throwable t) {
        // Mock implementation - no exception handling
    }

    /**
     * Gets the current ServletRequest.
     *
     * @return the ServletRequest object
     */
    public ServletRequest getRequest() {
        return request;
    }

    /**
     * Sets the ServletRequest for this page context.
     *
     * @param servletRequest the ServletRequest to set
     */
    public void setRequest(ServletRequest servletRequest) {
        this.request = servletRequest;
    }

    /**
     * Gets the current ServletResponse.
     * This is a mock implementation that always returns null.
     *
     * @return always returns null in this mock implementation
     */
    public ServletResponse getResponse() {
        return null;
    }

    /**
     * Removes an attribute from page scope.
     * This is a mock implementation that does nothing.
     *
     * @param s the name of the attribute to remove
     */
    public void removeAttribute(String s) {
        // Mock implementation - no attributes to remove
    }

    /**
     * Gets an attribute value from the specified scope.
     * This is a mock implementation that always returns null.
     *
     * @param s the name of the attribute
     * @param i the scope to search in
     * @return always returns null in this mock implementation
     */
    public Object getAttribute(String s, int i) {
        return null;
    }

    /**
     * Gets the ServletConfig associated with this PageContext.
     * This is a mock implementation that always returns null.
     *
     * @return always returns null in this mock implementation
     */
    public ServletConfig getServletConfig() {
        return null;
    }

    /**
     * Initializes this PageContext with the specified parameters.
     * This is a mock implementation that does nothing.
     *
     * @param servlet         the servlet
     * @param servletRequest  the servlet request
     * @param servletResponse the servlet response
     * @param s               the error page URL
     * @param b               whether the session is needed
     * @param i               the buffer size
     * @param b2              whether to auto-flush
     */
    public void initialize(Servlet servlet, ServletRequest servletRequest, ServletResponse servletResponse, String s,
                           boolean b, int i, boolean b2) {
        // Mock implementation - no initialization performed
    }

    /**
     * Searches for the named attribute in all scopes and returns its value.
     * This is a mock implementation that always returns null.
     *
     * @param s the name of the attribute to find
     * @return always returns null in this mock implementation
     */
    public Object findAttribute(String s) {
        return null;
    }

    /**
     * Gets the session associated with this PageContext.
     *
     * @return the HttpSession object
     */
    public HttpSession getSession() {
        return httpSession;
    }

    /**
     * Sets the session for this page context.
     *
     * @param httpSessionIn the HttpSession to set
     */
    public void setSession(HttpSession httpSessionIn) {
        this.httpSession = httpSessionIn;
    }

    /**
     * Sets an attribute in page scope.
     * This is a mock implementation that does nothing.
     *
     * @param s the name of the attribute
     * @param o the value of the attribute
     */
    public void setAttribute(String s, Object o) {
        // Mock implementation - no attributes stored
    }

    /**
     * Sets an attribute in the specified scope.
     * This is a mock implementation that does nothing.
     *
     * @param s the name of the attribute
     * @param o the value of the attribute
     * @param i the scope in which to set the attribute
     */
    public void setAttribute(String s, Object o, int i) {
        // Mock implementation - no attributes stored
    }

    /**
     * Gets an attribute from page scope.
     * This is a mock implementation that always returns null.
     *
     * @param s the name of the attribute
     * @return always returns null in this mock implementation
     */
    public Object getAttribute(String s) {
        return null;
    }

    /**
     * Gets the current exception being processed.
     * This is a mock implementation that always returns null.
     *
     * @return always returns null in this mock implementation
     */
    public Exception getException() {
        return null;
    }
}
