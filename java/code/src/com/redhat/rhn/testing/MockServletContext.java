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

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

/**
 * Mock implementation of ServletContext for testing purposes.
 * This class provides stub implementations of all ServletContext methods.
 */
public class MockServletContext implements ServletContext {

    private Map<String, Object> attributes = new HashMap<>();
    private Set<String> resourcePaths;
    private List<String> realPaths = new ArrayList<>();
    private int realPathIndex = 0;
    private URL resource;
    private Map<String, String> initParameters = new HashMap<>();
    private RequestDispatcher requestDispatcher;

    /**
     * Gets all servlets registered in this context.
     * @return empty enumeration for mock implementation
     */
    public Enumeration<Servlet> getServlets() {
        return Collections.emptyEnumeration();
    }


    /**
     * Sets the resource to be returned by getResource().
     * @param resourceIn the URL resource to set
     */
    public void setResource(URL resourceIn) {
        this.resource = resourceIn;
    }

    /**
     * Gets a URL to the resource mapped to the specified path.
     * @param string the path to the resource
     * @return the resource URL
     */
    public URL getResource(String string) {
        return resource;
    }

    /**
     * Sets the resource paths to be returned by getResourcePaths().
     * @param resourcePathsIn the set of resource paths to set
     */
    public void setResourcePaths(Set<String> resourcePathsIn) {
        this.resourcePaths = resourcePathsIn;
    }

    /**
     * Gets a directory-like listing of all the paths to resources within the web application.
     * @param string the partial path used to match the resources
     * @return the set of resource paths
     */
    public Set<String> getResourcePaths(String string) {
        return resourcePaths;
    }

    @Override
    /**
     * Gets the context path of the web application.
     * @return empty string for mock implementation
     */
    public String getContextPath() {
        return "";
    }

    /**
     * Gets the ServletContext object that corresponds to a specified URL on the server.
     * @param string a String specifying the context path of another web application
     * @return null for mock implementation
     */
    public ServletContext getContext(String string) {
        return null;
    }

    /**
     * Gets the minor version of the Servlet API that this servlet container supports.
     * @return -1 for mock implementation
     */
    public int getMinorVersion() {
        return -1;
    }

    /**
     * Removes the attribute with the given name from the servlet context.
     * @param string the name of the attribute to remove
     */
    public void removeAttribute(String string) {
        attributes.remove(string);
    }

    /**
     * Adds a real path to the list of paths to be returned by getRealPath().
     * @param realPath the real path to add
     */
    public void addRealPath(String realPath) {
        this.realPaths.add(realPath);
    }

    /**
     * Gets the real path corresponding to the given virtual path.
     * @param string the virtual path to be translated to a real path
     * @return the real path, or null if the translation cannot be performed
     */
    public String getRealPath(String string) {
        if (realPaths.isEmpty()) {
            return null;
        }
        if (realPathIndex < realPaths.size()) {
            return realPaths.get(realPathIndex++);
        }
        return realPaths.get(realPaths.size() - 1);
    }

    /**
     * Gets an enumeration of the servlet names known to this context.
     * @return empty enumeration for mock implementation
     */
    public Enumeration<String> getServletNames() {
        return Collections.emptyEnumeration();
    }


    /**
     * Gets the servlet with the specified name.
     * @param string the name of the servlet to get
     * @return null for mock implementation
     */
    public Servlet getServlet(String string) {
        return null;
    }

    /**
     * Logs an exception and an explanatory message to the servlet log file.
     * @param exception the exception to log
     * @param string the explanatory message
     */
    @Override
    public void log(Exception exception, String string) {
        // This method is intentionally empty for mock implementation
    }

    /**
     * Logs an exception and an explanatory message to the servlet log file.
     * @param sIn the explanatory message
     * @param throwableIn the exception to log
     */
    @Override
    public void log(String sIn, Throwable throwableIn) {
        // This method is intentionally empty for mock implementation
    }

    /**
     * Logs an exception and an explanatory message to the servlet log file.
     * @param sIn the explanatory message
     */
    @Override
    public void log(String sIn) {
        // This method is intentionally empty for mock implementation
    }

    /**
     * Gets the name and version of the servlet container on which the servlet is running.
     * @return null for mock implementation
     */
    public String getServerInfo() {
        return null;
    }

    /**
     * Sets the request dispatcher to be returned by named dispatcher requests.
     * @param requestDispatcherIn the request dispatcher to set
     */
    public void setRequestDispatcher(RequestDispatcher requestDispatcherIn) {
        this.requestDispatcher = requestDispatcherIn;
    }

    /**
     * Gets the major version of the Servlet API that this servlet container supports.
     * @return -1 for mock implementation
     */
    public int getMajorVersion() {
        return -1;
    }

    /**
     * Gets the resource paths for all resources.
     * @return empty set for mock implementation
     */
    public Set<String> getResourcePaths() {
        return Collections.emptySet();
    }

    /**
     * Adds an attribute to the servlet context.
     * @param string the name of the attribute
     * @param object the value of the attribute
     */
    public void addAttribute(String string, Object object) {
        attributes.put(string, object);
    }

    /**
     * Gets the MIME type of the specified file.
     * @param string the file name whose MIME type is required
     * @return null for mock implementation
     */
    public String getMimeType(String string) {
        return null;
    }

    /**
     * Gets a RequestDispatcher object that acts as a wrapper for the named servlet.
     * @param string the name of the servlet for which a RequestDispatcher is requested
     * @return null for mock implementation
     */
    public RequestDispatcher getNamedDispatcher(String string) {
        return null;
    }

    /**
     * Gets the value of the initialization parameter with the given name.
     * @param paramName the name of the initialization parameter
     * @return the value of the initialization parameter
     */
    public String getInitParameter(String paramName) {
        return initParameters.get(paramName);
    }

    /**
     * Sets the initialization parameter with the given name and value.
     * @param paramName the name of the initialization parameter
     * @param paramValue the value of the initialization parameter
     * @return true if the parameter was set successfully
     */
    public boolean setInitParameter(String paramName, String paramValue) {
        initParameters.put(paramName, paramValue);
        return true; // Mock implementation always succeeds
    }

    /**
     * Gets the servlet context attribute with the given name.
     * @param string the name of the attribute
     * @return the attribute value
     */
    public Object getAttribute(String string) {
        return attributes.get(string);
    }

    /**
     * Gets an enumeration of the attribute names available within this servlet context.
     * @return empty enumeration for mock implementation
     */
    public Enumeration<String> getAttributeNames() {
        return Collections.emptyEnumeration();
    }

    /**
     * Gets the name of this web application as specified in the deployment descriptor.
     * @return null for mock implementation
     */
    public String getServletContextName() {
        return null;
    }

    @Override
    /**
     * Adds a servlet with the given name and class name to this servlet context.
     * @param sIn the servlet name
     * @param sIn1 the servlet class name
     * @return null for mock implementation
     */
    public ServletRegistration.Dynamic addServlet(String sIn, String sIn1) {
        return null;
    }

    @Override
    /**
     * Adds a servlet with the given name and servlet instance to this servlet context.
     * @param sIn the servlet name
     * @param servletIn the servlet instance
     * @return null for mock implementation
     */
    public ServletRegistration.Dynamic addServlet(String sIn, Servlet servletIn) {
        return null;
    }

    @Override
    /**
     * Adds a servlet with the given name and servlet class to this servlet context.
     * @param sIn the servlet name
     * @param classIn the servlet class
     * @return null for mock implementation
     */
    public ServletRegistration.Dynamic addServlet(String sIn, Class<? extends Servlet> classIn) {
        return null;
    }

    @Override
    /**
     * Adds a JSP file with the given name and file path to this servlet context.
     * @param sIn the servlet name
     * @param sIn1 the JSP file path
     * @return null for mock implementation
     */
    public ServletRegistration.Dynamic addJspFile(String sIn, String sIn1) {
        return null;
    }

    @Override
    /**
     * Creates a servlet of the given class.
     * @param classIn the servlet class
     * @param <T> the servlet type
     * @return null for mock implementation
     * @throws ServletException if servlet creation fails
     */
    public <T extends Servlet> T createServlet(Class<T> classIn) throws ServletException {
        return null;
    }

    @Override
    /**
     * Gets the servlet registration for the servlet with the given servlet name.
     * @param sIn the servlet name
     * @return null for mock implementation
     */
    public ServletRegistration getServletRegistration(String sIn) {
        return null;
    }

    @Override
    /**
     * Gets a map containing the servlet registrations for all servlets.
     * @return empty map for mock implementation
     */
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return Map.of();
    }

    @Override
    /**
     * Adds a filter with the given name and class name to this servlet context.
     * @param sIn the filter name
     * @param sIn1 the filter class name
     * @return null for mock implementation
     */
    public FilterRegistration.Dynamic addFilter(String sIn, String sIn1) {
        return null;
    }

    @Override
    /**
     * Adds a filter with the given name and filter instance to this servlet context.
     * @param sIn the filter name
     * @param filterIn the filter instance
     * @return null for mock implementation
     */
    public FilterRegistration.Dynamic addFilter(String sIn, Filter filterIn) {
        return null;
    }

    @Override
    /**
     * Adds a filter with the given name and filter class to this servlet context.
     * @param sIn the filter name
     * @param classIn the filter class
     * @return null for mock implementation
     */
    public FilterRegistration.Dynamic addFilter(String sIn, Class<? extends Filter> classIn) {
        return null;
    }

    @Override
    /**
     * Creates a filter of the given class.
     * @param classIn the filter class
     * @param <T> the filter type
     * @return null for mock implementation
     * @throws ServletException if filter creation fails
     */
    public <T extends Filter> T createFilter(Class<T> classIn) throws ServletException {
        return null;
    }

    @Override
    /**
     * Gets the filter registration for the filter with the given filter name.
     * @param sIn the filter name
     * @return null for mock implementation
     */
    public FilterRegistration getFilterRegistration(String sIn) {
        return null;
    }

    @Override
    /**
     * Gets a map containing the filter registrations for all filters.
     * @return empty map for mock implementation
     */
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return Map.of();
    }

    @Override
    /**
     * Gets the session cookie configuration for this servlet context.
     * @return null for mock implementation
     */
    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }

    @Override
    /**
     * Sets the session tracking modes for this servlet context.
     * @param setIn the set of session tracking modes
     */
    public void setSessionTrackingModes(Set<SessionTrackingMode> setIn) {
        // Mock implementation does nothing
    }

    @Override
    /**
     * Gets the default session tracking modes for this servlet context.
     * @return empty set for mock implementation
     */
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return Set.of();
    }

    @Override
    /**
     * Gets the effective session tracking modes for this servlet context.
     * @return empty set for mock implementation
     */
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return Set.of();
    }

    @Override
    /**
     * Adds a listener with the given class name to this servlet context.
     * @param sIn the listener class name
     */
    public void addListener(String sIn) {
        // Mock implementation does nothing
    }

    @Override
    /**
     * Adds a listener instance to this servlet context.
     * @param tIn the listener instance
     * @param <T> the listener type
     */
    public <T extends EventListener> void addListener(T tIn) {
        // Mock implementation does nothing
    }

    @Override
    /**
     * Adds a listener with the given listener class to this servlet context.
     * @param classIn the listener class
     */
    public void addListener(Class<? extends EventListener> classIn) {
        // Mock implementation does nothing
        // Mock implementation does nothing
    }

    @Override
    /**
     * Creates a listener of the given class.
     * @param classIn the listener class
     * @param <T> the listener type
     * @return null for mock implementation
     * @throws ServletException if listener creation fails
     */
    public <T extends EventListener> T createListener(Class<T> classIn) throws ServletException {
        return null;
    }

    @Override
    /**
     * Gets the JSP configuration descriptor for this servlet context.
     * @return null for mock implementation
     */
    public JspConfigDescriptor getJspConfigDescriptor() {
        return null;
    }

    /**
     * Gets the resource located at the named path as an InputStream object.
     * @param string the path to the resource
     * @return null for mock implementation
     */
    public InputStream getResourceAsStream(String string) {
        return null;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String sIn) {
        return null;
    }

    /**
     * Gets the names of the context's initialization parameters.
     * @return empty enumeration for mock implementation
     */
    public Enumeration<String> getInitParameterNames() {
        return Collections.emptyEnumeration();
    }

    /**
     * Binds an object to a given attribute name in this servlet context.
     * @param key the attribute name
     * @param value the object to be bound
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    // Servlet 4.0 required methods
    /**
     * Sets the response character encoding for this servlet context.
     * @param encoding the character encoding
     */
    public void setResponseCharacterEncoding(String encoding) {
        // Mock implementation - does nothing
    }

    /**
     * Gets the response character encoding for this servlet context.
     * @return null for mock implementation
     */
    public String getResponseCharacterEncoding() {
        // Mock implementation - returns null
        return null;
    }

    /**
     * Sets the request character encoding for this servlet context.
     * @param encoding the character encoding
     */
    public void setRequestCharacterEncoding(String encoding) {
        // Mock implementation - does nothing
    }

    /**
     * Gets the request character encoding for this servlet context.
     * @return null for mock implementation
     */
    public String getRequestCharacterEncoding() {
        // Mock implementation - returns null
        return null;
    }

    /**
     * Sets the session timeout for this servlet context.
     * @param sessionTimeout the session timeout in minutes
     */
    public void setSessionTimeout(int sessionTimeout) {
        // Mock implementation - does nothing
    }

    /**
     * Gets the session timeout for this servlet context.
     * @return 30 minutes default timeout for mock implementation
     */
    public int getSessionTimeout() {
        // Mock implementation - return default timeout
        return 30; // 30 minutes default
    }

    /**
     * Gets the primary name of the virtual server on which this ServletContext is deployed.
     * @return null for mock implementation
     */
    public String getVirtualServerName() {
        // Mock implementation - returns null
        return null;
    }

    /**
     * Declares role names that are referenced by the application.
     * @param roleNames the role names to declare
     */
    public void declareRoles(String... roleNames) {
        // Mock implementation - does nothing
    }

    /**
     * Gets the class loader of the web application represented by this ServletContext.
     * @return the current thread context class loader
     */
    public ClassLoader getClassLoader() {
        // Mock implementation - return current thread context class loader
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Gets the effective major version of the Servlet specification that this application is based on.
     * @return 4 for mock implementation
     */
    public int getEffectiveMajorVersion() {
        return 4;
    }

    /**
     * Gets the effective minor version of the Servlet specification that this application is based on.
     * @return 0 for mock implementation
     */
    public int getEffectiveMinorVersion() {
        return 0;
    }
}
