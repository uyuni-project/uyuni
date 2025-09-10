/*
 * Copyright (c) 2015--2025 SUSE LLC
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * RhnMockHttpServletResponse is a mock implementation of the
 * HttpServletResponse which provides a simple mock for testing purposes.
 */
public class RhnMockHttpServletResponse implements HttpServletResponse {
    private final Map<String, Cookie> cookies = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();
    private String redirect;
    private String encoding = StandardCharsets.UTF_8.name();;
    private String contentType;
    private Locale locale = Locale.getDefault();
    private int bufferSize = 8192;
    private boolean committed = false;
    private int status = SC_OK;
    private StringWriter stringWriter = new StringWriter();
    private PrintWriter writer = new PrintWriter(stringWriter);
    private MockServletOutputStream outputStream = new MockServletOutputStream();

    /** {@inheritDoc} */
    @Override
    public void addCookie(Cookie cookie) {
        cookies.put(cookie.getName(), cookie);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    /**
     * Returns the String value matching the given key
     * @param key the header name
     * @return header value or null...
     */
    @Override
    public String getHeader(String key) {
        return headers.get(key);
    }

    /**
     * Returns a Cookie matching the given name, null otherwise.
     * @param name cookie name
     * @return a Cookie matching the given name, null otherwise.
     */
    public Cookie getCookie(String name) {
        return cookies.get(name);
    }

    /**
     * Saves the url sent through a redirect so we can test it.
     * @param aURL The URL for this redirect
     */
    @Override
    public void sendRedirect(String aURL) {
        redirect = aURL;
        committed = true;
    }

    /**
     * Gets the redirect instance variable
     * @return the redirect instance variable
     */
    public String getRedirect() {
        return redirect;
    }

    /**
     * Sets the redirect to null.
     */
    public void clearRedirect() {
        redirect = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCharacterEncoding(String encodingIn) {
        this.encoding = encodingIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCharacterEncoding() {
        return this.encoding;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCommitted() {
        return committed;
    }

    @Override
    public void setStatus(int statusIn) {
        this.status = statusIn;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    @Override
    public String encodeURL(String url) {
        return url;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return url;
    }

    @Override
    public String encodeUrl(String url) {
        return url;
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return url;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.status = sc;
        committed = true;
    }

    @Override
    public void sendError(int sc) throws IOException {
        this.status = sc;
        committed = true;
    }

    @Override
    public void setDateHeader(String name, long date) {
        headers.put(name, String.valueOf(date));
    }

    @Override
    public void addDateHeader(String name, long date) {
        headers.put(name, String.valueOf(date));
    }

    @Override
    public void setIntHeader(String name, int value) {
        headers.put(name, String.valueOf(value));
    }

    @Override
    public void addIntHeader(String name, int value) {
        headers.put(name, String.valueOf(value));
    }

    @Override
    public void setStatus(int sc, String sm) {
        this.status = sc;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return headers.containsKey(name) ?
            Collections.singletonList(headers.get(name)) :
            Collections.emptyList();
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    // ServletResponse implementation

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(String type) {
        this.contentType = type;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return writer;
    }

    @Override
    public void setContentLength(int len) {
        headers.put("Content-Length", String.valueOf(len));
    }

    @Override
    public void setContentLengthLong(long len) {
        headers.put("Content-Length", String.valueOf(len));
    }

    @Override
    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    public void setBufferSize(int size) {
        this.bufferSize = size;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale loc) {
        this.locale = loc;
    }

    @Override
    public void flushBuffer() throws IOException {
        committed = true;
        writer.flush();
        outputStream.flush();
    }

    @Override
    public void resetBuffer() {
        if (!committed) {
            stringWriter = new StringWriter();
            writer = new PrintWriter(stringWriter);
            outputStream = new MockServletOutputStream();
        }
    }

    @Override
    public void reset() {
        if (!committed) {
            headers.clear();
            resetBuffer();
            status = SC_OK;
            contentType = null;
            encoding = "UTF-8";
        }
    }

}
