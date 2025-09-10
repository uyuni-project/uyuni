/*
 * Copyright (c) 2015--2025 SUSE LLC
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
package com.redhat.rhn.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

/**
 * RhnMockHttpServletRequest is a mock implementation of the
 * HttpServletRequest which provides a simple mock for testing purposes.
 */
public class RhnMockHttpServletRequest implements HttpServletRequest {

    private static final String LOCALHOST = "localhost";

    private String requestURL;
    private String requestURI;
    private String serverName;
    private String contextPath;
    private String servletPath;
    private String pathInfo;
    private String queryString;
    private String method;
    private String encoding;
    private String contentType;
    private int contentLength = -1;
    private Map<String, Object> attributes;
    private Map<String, String> headers;
    private Map<String, String[]> parameters;
    private List<Locale> locales;
    private int port;
    private boolean secure;
    private List<Cookie> cookies;
    private HttpSession session;
    private String remoteAddr;
    private String remoteHost;
    private int remotePort;
    private String localAddr;
    private String localName;
    private int localPort;
    private String protocol = "HTTP/1.1";
    private String scheme = "http";
    private String requestDispatcherURI;
    private RequestDispatcher requestDispatcher;
    private BufferedReader reader;
    private ServletInputStream inputStream;

    /**
     * Default constructor
     */
    public RhnMockHttpServletRequest() {
        this.attributes = new HashMap<>();
        this.headers = new HashMap<>();
        this.locales = new ArrayList<>();
        this.parameters = new HashMap<>();
        this.cookies = new ArrayList<>();
        this.serverName = "host.mlm.suse.com";
        this.requestURI = "/mlm/network/somepage.do";
        this.requestURL = "http://host.mlm.suse.com/mlm/network/somepage.do";
        this.contextPath = "";
        this.servletPath = "";
        this.pathInfo = null;
        this.queryString = null;
        this.session = new RhnMockHttpSession();
        this.method = "POST";
        this.port = 80;
        this.remoteAddr = "127.0.0.1";
        this.remoteHost = LOCALHOST;
        this.remotePort = 12345;
        this.localAddr = "127.0.0.1";
        this.localName = LOCALHOST;
        this.localPort = 8080;
    }

    /**
     * Get the request URL
     *
     * @return StringBuffer request URL
     */
    public StringBuffer getRequestURL() {
        return new StringBuffer(requestURL);
    }

    /**
     * Added the ability to specify a context path for testing.
     * @param pathIn Request url path.
     */
    public void setRequestURL(String pathIn) {
        this.requestURL = pathIn;
    }

    /**
     * Returns the attribute bound to the given name.
     * @param name Name of attribute whose value is sought.
     * @return Object value of attribute with given name.
     */
    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * Adds a new attribute to the Request.
     * @param name attribute name
     * @param value attribute value
     */
    public void addAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    /**
     * Sets an attribute to the Request.
     * @param name attribute name
     * @param value attribute value
     */
    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    /** {@inheritDoc} */
    @Override
    public String getHeader(String name) {
        return headers.get(name);
    }

    /**
     * Add custom header and value
     * @param name header
     * @param value header value
     */
    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    /** {@inheritDoc} */
    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headers.keySet());
    }


    /** {@inheritDoc} */
    @Override
    public String getParameter(String paramName) {
        String[] values = getParameterValues(paramName);
        if (values == null) {
            return null;
        }

        return values[0];
    }

    /**
     * Returns the current values of a request parameter while consuming the first entry
     * @param name parameter name
     * @return the array of values associated with the parameter
     */
    @Override
    public String[] getParameterValues(String name) {
        String[] existing = parameters.get(name);
        if (existing == null || existing.length == 0) {
            return null;
        }

        String[] reduced = Arrays.copyOfRange(existing, 1, existing.length);
        parameters.put(name, reduced);
        return existing;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, String[]> getParameterMap() {
        return parameters;
    }

    public void setParameters(Map<String, String[]> map) {
        parameters = map;
    }

    /**
     * Add a parameter to the request
     *
     * @param name  parameter name
     * @param value parameter value
     */
    public void addParameter(String name, String value) {
        addParameter(name, new String[]{value});
    }

    /**
     * Add a parameter array to the request
     *
     * @param name   parameter name
     * @param values parameter values
     */
    public void addParameter(String name, String[] values) {
        String[] existing = parameters.get(name);
        if (existing == null || existing.length == 0 || (existing.length == 1 && existing[0] == null)) {
            parameters.put(name, values);
        }
        else {
            String[] merged = Arrays.copyOf(existing, existing.length + values.length);
            System.arraycopy(values, 0, merged, existing.length, values.length);
            parameters.put(name, merged);
        }
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    /**
     * Set parameter names with empty values
     *
     * @param names parameter names
     */
    public void setParameterNames(Enumeration<String> names) {
        while (names.hasMoreElements()) {
            parameters.put(names.nextElement(), new String[]{""});
        }
    }

    /** {@inheritDoc} */
    @Override
    public Locale getLocale() {
        return this.locales.isEmpty() ? Locale.getDefault() : this.locales.get(0);
    }

    /**
     * Set the primary locale of this Request.
     * @param lcl The primary Local of this Request.
     */
    public void setLocale(Locale lcl) {
        if (this.locales.isEmpty()) {
            this.locales.add(lcl);
        }
        else {
            this.locales.set(0, lcl);
        }
    }

    /**
     * Set the list of Locales.
     * @param lcls List of Locales.
     */
    public void setLocales(List<Locale> lcls) {
        this.locales = lcls;
    }

    /** {@inheritDoc} */
    @Override
    public Enumeration<Locale> getLocales() {
        return Collections.enumeration(this.locales);
    }

    /**
     * Allows you to add a Cookie to the request to simulate receiving
     * a cookie from the browser.
     * @param cookie Cookie to added.
     */
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    /** {@inheritDoc} */
    @Override
    public Cookie[] getCookies() {
        return cookies.toArray(new Cookie[0]);
    }

    /** {@inheritDoc} */
    @Override
    public int getServerPort() {
        return port;
    }

    /**
     * Sets the server port for this request.
     * @param p Port
     */
    public void setServerPort(int p) {
        port = p;
    }


    /**
     * Configures whether this request is secure.
     * @param s Flag indicating whether request is secure.
     */
    public void setIsSecure(boolean s) {
        secure = s;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSecure() {
        return secure;
    }

    /** {@inheritDoc} */
    @Override
    public String getCharacterEncoding() {
        return encoding;
    }

    /** {@inheritDoc} */
    @Override
    public void setCharacterEncoding(String encodingIn) {
        this.encoding = encodingIn;
    }

    /**
     * @return Returns the method.
     */
    @Override
    public String getMethod() {
        return method;
    }

    /**
     * @param methodIn The method to set.
     */
    public void setMethod(String methodIn) {
        this.method = methodIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRemoteUser() {
        return null;
    }

    /**
     * Set the server name for this request.
     *
     * @param serverNameIn server name
     */
    public void setServerName(String serverNameIn) {
        this.serverName = serverNameIn;
    }



    @Override
    public String getAuthType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContextPath() {
        return contextPath != null ? contextPath : "";
    }

    @Override
    public String getPathInfo() {
        return pathInfo;
    }

    public void setPathInfo(String pathInfoIn) {
        this.pathInfo = pathInfoIn;
    }

    @Override
    public String getPathTranslated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getQueryString() {
        return queryString;
    }


    public void setQueryString(String queryStringIn) {
        this.queryString = queryStringIn;
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    /**
     * Set the request URI for this request.
     *
     * @param requestURIIn request URI
     */
    public void setRequestURI(String requestURIIn) {
        this.requestURI = requestURIIn;
    }

    @Override
    public String getRequestedSessionId() {
        return session != null ? session.getId() : null;
    }

    @Override
    public String getServletPath() {
        return servletPath != null ? servletPath : "";
    }

    @Override
    public HttpSession getSession() {
        return session;
    }

    /**
     * Set the session for this request.
     *
     * @param sessionIn HTTP session
     */
    public void setSession(HttpSession sessionIn) {
        this.session = sessionIn;
    }

    @Override
    public HttpSession getSession(boolean create) {
        if (session == null && create) {
            session = new RhnMockHttpSession();
        }
        return session;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return session != null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public String changeSessionId() {
        return session != null ? session.getId() : null;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {
        // Mock implementation - do nothing
    }

    @Override
    public void logout() throws ServletException {
        // Mock implementation - do nothing
    }

    @Override
    public Collection<Part> getParts() {
        return Collections.emptyList();
    }

    @Override
    public Part getPart(String name)  {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> httpUpgradeHandlerClass)
            throws IOException, ServletException {
        return null;
    }

    @Override
    public boolean isTrailerFieldsReady() {
        return HttpServletRequest.super.isTrailerFieldsReady();
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        String value = headers.get(name);
        return value != null ?
                Collections.enumeration(Collections.singletonList(value)) :
                Collections.emptyEnumeration();
    }

    @Override
    public int getIntHeader(String name) {
        String value = headers.get(name);
        return value != null ? Integer.parseInt(value) : -1;
    }

    @Override
    public long getDateHeader(String name) {
        return -1;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }
    @Override
    public int getContentLength() {
        return contentLength;
    }

    @Override
    public long getContentLengthLong() {
        return contentLength >= 0 ? contentLength : -1L;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return inputStream;
    }

    public void setInputStream(ServletInputStream inputStreamIn) {
        this.inputStream = inputStreamIn;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public String getServerName() {
        return serverName != null ? serverName : LOCALHOST;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return reader;
    }

    public void setReader(BufferedReader readerIn) {
        this.reader = readerIn;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddrIn) {
        remoteAddr = remoteAddrIn;
    }

    public String getRemoteHost() {
        return remoteHost;
    }


    public void setAttributes(Map<String, Object> map) {
        this.attributes = map;
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    /**
     * Set a RequestDispatcher to be returned when getRequestDispatcher is called.
     *
     * @param requestDispatcherIn the RequestDispatcher to return
     */
    public void setRequestDispatcher(RequestDispatcher requestDispatcherIn) {
        this.requestDispatcher = requestDispatcherIn;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String uri) {
        this.requestDispatcherURI = uri;
        return requestDispatcher;
    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return remotePort;
    }

    @Override
    public String getLocalName() {
        return localName;
    }

    @Override
    public String getLocalAddr() {
        return localAddr;
    }

    @Override
    public int getLocalPort() {
        return localPort;
    }

    @Override
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
            throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAsyncStarted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAsyncSupported() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncContext getAsyncContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DispatcherType getDispatcherType() {
        return DispatcherType.REQUEST;
    }
}
