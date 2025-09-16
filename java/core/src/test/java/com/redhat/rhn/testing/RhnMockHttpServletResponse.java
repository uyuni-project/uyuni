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
package com.redhat.rhn.testing;

import com.mockobjects.ExpectationValue;
import com.mockobjects.servlet.MockHttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

/**
 * RhnMockHttpServletResponse is a mock implementation of the
 * HttpServletResponse which fixes deficiencies in the MockObjects'
 * implementation of MockHttpServletResponse.
 */
public class RhnMockHttpServletResponse extends MockHttpServletResponse {
    private final Map<String, Cookie> cookies = new HashMap<>();
    private final Map<String, String> header = new HashMap<>();
    private String redirect;
    private String encoding;
    private final ExpectationValue myStatus = new ExpectationValue("RhnMockHttpServletResponse.setStatus");

    /** {@inheritDoc} */
    @Override
    public void addCookie(Cookie cookie) {
        cookies.put(cookie.getName(), cookie);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void addHeader(String key, String value) {
        header.put(key, value);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void setHeader(String key, String value) {
        header.put(key, value);
    }

    /**
     * Returns the String value matching the given key
     * @param key the header name
     * @return header value or null...
     */
    @Override
    public String getHeader(String key) {
        return header.get(key);
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
        return false;
    }

    /**
     * Set the expected response status
     * @param status the response status
     */
    public void setExpectedStatus(int status) {
        myStatus.setExpected(status);
    }

    @Override
    public void setStatus(int status) {
        super.setStatus(status);
        myStatus.setActual(status);
    }
}
