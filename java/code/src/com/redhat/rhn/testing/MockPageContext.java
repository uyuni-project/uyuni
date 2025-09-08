/*
 * Copyright (c) 2025 SUSE LLC
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

public class MockPageContext extends PageContext {
    private JspWriter jspWriter;
    private ServletRequest request;
    private HttpSession httpSession;
    private ServletContext servletContext;


    public void release() {
    }

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

    public void setJspWriter(JspWriter jspWriter) {
        this.jspWriter = jspWriter;
    }

    public void handlePageException(Exception e) {
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public int getAttributesScope(String s) {
        return -1;
    }

    public void include(String s) {
    }

    @Override
    public void include(String sIn, boolean bIn) throws ServletException, IOException {

    }

    public void removeAttribute(String s, int i) {
    }

    public Enumeration getAttributeNamesInScope(int i) {
        return null;
    }

    public void forward(String s) {
    }

    public Object getPage() {
        return null;
    }

    public void handlePageException(Throwable t) {
    }

    public ServletRequest getRequest() {
        return request;
    }

    public void setRequest(ServletRequest servletRequest) {
        this.request = servletRequest;
    }

    public ServletResponse getResponse() {
        return null;
    }

    public void removeAttribute(String s) {
    }

    public Object getAttribute(String s, int i) {
        return null;
    }

    public ServletConfig getServletConfig() {
        return null;
    }

    public void initialize(Servlet servlet, ServletRequest servletRequest, ServletResponse servletResponse, String s, boolean b, int i, boolean b2) {
    }

    public Object findAttribute(String s) {
        return null;
    }

    public HttpSession getSession() {
        return httpSession;
    }

    public void setSession(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    public void setAttribute(String s, Object o) {
    }

    public void setAttribute(String s, Object o, int i) {
    }

    public Object getAttribute(String s) {
        return null;
    }

    public Exception getException() {
        return null;
    }

    public void verify() {
    }
}
