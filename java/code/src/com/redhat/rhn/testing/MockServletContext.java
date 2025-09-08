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

public class MockServletContext implements ServletContext {

    // Simple data structures to replace mockobjects functionality
    private Map<String, Object> attributes = new HashMap<>();
    private Set<String> resourcePaths;
    private List<String> realPaths = new ArrayList<>();
    private int realPathIndex = 0;
    private URL resource;
    private Map<String, String> initParameters = new HashMap<>();

    // Expected values for testing/verification
    private String expectedLogValue;
    private String actualLogValue;
    private Throwable expectedLogThrowable;
    private Throwable actualLogThrowable;
    private String expectedRequestDispatcherURI;
    private String actualRequestDispatcherURI;
    private RequestDispatcher requestDispatcher;

    // Expected attributes for verification
    private List<AttributeEntry> expectedAttributes = new ArrayList<>();
    private List<AttributeEntry> actualAttributes = new ArrayList<>();

    // Simple data class to replace MapEntry
    private static class AttributeEntry {
        private final String key;
        private final Object value;

        public AttributeEntry(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            AttributeEntry that = (AttributeEntry) obj;
            return key.equals(that.key) &&
                    (value != null ? value.equals(that.value) : that.value == null);
        }

        @Override
        public int hashCode() {
            int result = key.hashCode();
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }

    public Enumeration<Servlet> getServlets() {
        return Collections.emptyEnumeration();
    }

    public void log(String string) {
        this.actualLogValue = string;
    }

    public void setExpectedLog(String string) {
        this.expectedLogValue = string;
    }

    public boolean verifyLog() {
        return expectedLogValue != null && expectedLogValue.equals(actualLogValue);
    }

    public void setupGetResource(URL resource) {
        this.resource = resource;
    }

    public URL getResource(String string) {
        return resource;
    }

    public void setupGetResourcePaths(Set<String> resourcePaths) {
        this.resourcePaths = resourcePaths;
    }

    public Set<String> getResourcePaths(String string) {
        return resourcePaths;
    }

    @Override
    public String getContextPath() {
        return "";
    }

    public ServletContext getContext(String string) {
        return null;
    }

    public int getMinorVersion() {
        return -1;
    }

    public void removeAttribute(String string) {
        attributes.remove(string);
    }

    public void log(String string, Throwable t) {
        log(string);
        this.actualLogThrowable = t;
    }

    public void setExpectedLogThrowable(Throwable throwable) {
        this.expectedLogThrowable = throwable;
    }

    public boolean verifyLogThrowable() {
        return expectedLogThrowable != null && expectedLogThrowable.equals(actualLogThrowable);
    }

    public void addRealPath(String realPath) {
        this.realPaths.add(realPath);
    }

    public String getRealPath(String string) {
        if (realPaths.isEmpty()) {
            return null;
        }
        if (realPathIndex < realPaths.size()) {
            return realPaths.get(realPathIndex++);
        }
        return realPaths.get(realPaths.size() - 1);
    }

    public Enumeration<String> getServletNames() {
        return Collections.emptyEnumeration();
    }

    public Servlet getServlet(String string) {
        return null;
    }

    public void log(Exception exception, String string) {
        // This method is intentionally empty for mock implementation
    }

    public String getServerInfo() {
        return null;
    }

    public void setExpectedRequestDispatcherURI(String uri) {
        this.expectedRequestDispatcherURI = uri;
    }

    public void setupGetRequestDispatcher(
            RequestDispatcher requestDispatcher) {

        this.requestDispatcher = requestDispatcher;
    }

    public RequestDispatcher getRequestDispatcher(String uri) {
        this.actualRequestDispatcherURI = uri;
        return requestDispatcher;
    }

    public boolean verifyRequestDispatcherURI() {
        return expectedRequestDispatcherURI != null &&
                expectedRequestDispatcherURI.equals(actualRequestDispatcherURI);
    }

    public int getMajorVersion() {
        return -1;
    }

    public Set<String> getResourcePaths() {
        return Collections.emptySet();
    }

    public void setupGetAttribute(String string, Object object) {
        attributes.put(string, object);
    }

    public String getMimeType(String string) {
        return null;
    }

    public RequestDispatcher getNamedDispatcher(String string) {
        return null;
    }

    public String getInitParameter(String paramName) {
        return initParameters.get(paramName);
    }

    public boolean setInitParameter(String paramName, String paramValue) {
        initParameters.put(paramName, paramValue);
        return true; // Mock implementation always succeeds
    }

    public Object getAttribute(String string) {
        return attributes.get(string);
    }

    public Enumeration<String> getAttributeNames() {
        return Collections.emptyEnumeration();
    }

    public String getServletContextName() {
        return null;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String sIn, String sIn1) {
        return null;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String sIn, Servlet servletIn) {
        return null;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String sIn, Class<? extends Servlet> classIn) {
        return null;
    }

    @Override
    public ServletRegistration.Dynamic addJspFile(String sIn, String sIn1) {
        return null;
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> classIn) throws ServletException {
        return null;
    }

    @Override
    public ServletRegistration getServletRegistration(String sIn) {
        return null;
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return Map.of();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String sIn, String sIn1) {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String sIn, Filter filterIn) {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String sIn, Class<? extends Filter> classIn) {
        return null;
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> classIn) throws ServletException {
        return null;
    }

    @Override
    public FilterRegistration getFilterRegistration(String sIn) {
        return null;
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return Map.of();
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> setIn) {

    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return Set.of();
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return Set.of();
    }

    @Override
    public void addListener(String sIn) {

    }

    @Override
    public <T extends EventListener> void addListener(T tIn) {

    }

    @Override
    public void addListener(Class<? extends EventListener> classIn) {

    }

    @Override
    public <T extends EventListener> T createListener(Class<T> classIn) throws ServletException {
        return null;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return null;
    }

    public InputStream getResourceAsStream(String string) {
        return null;
    }

    public Enumeration<String> getInitParameterNames() {
        return Collections.emptyEnumeration();
    }

    public void addExpectedAttribute(String key, Object value) {
        expectedAttributes.add(new AttributeEntry(key, value));
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
        actualAttributes.add(new AttributeEntry(key, value));
    }

    public boolean verifyAttributes() {
        return expectedAttributes.equals(actualAttributes);
    }

    // Servlet 4.0 required methods
    public void setResponseCharacterEncoding(String encoding) {
        // Mock implementation - does nothing
    }

    public String getResponseCharacterEncoding() {
        // Mock implementation - returns null
        return null;
    }

    public void setRequestCharacterEncoding(String encoding) {
        // Mock implementation - does nothing
    }

    public String getRequestCharacterEncoding() {
        // Mock implementation - returns null
        return null;
    }

    public void setSessionTimeout(int sessionTimeout) {
        // Mock implementation - does nothing
    }

    public int getSessionTimeout() {
        // Mock implementation - return default timeout
        return 30; // 30 minutes default
    }

    public String getVirtualServerName() {
        // Mock implementation - returns null
        return null;
    }

    public void declareRoles(String... roleNames) {
        // Mock implementation - does nothing
    }

    public ClassLoader getClassLoader() {
        // Mock implementation - return current thread context class loader
        return Thread.currentThread().getContextClassLoader();
    }

    public int getEffectiveMajorVersion() {
        return 4;
    }

    public int getEffectiveMinorVersion() {
        return 0;
    }
}
