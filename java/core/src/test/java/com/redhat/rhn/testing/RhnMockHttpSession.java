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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * RhnMockHttpSession is a mock implementation of the HttpSession interface
 */
public class RhnMockHttpSession implements HttpSession {

    private final Map<String, Object> attributes = new HashMap<>();
    private final String id = UUID.randomUUID().toString();
    private final long creationTime = System.currentTimeMillis();
    private long lastAccessedTime = creationTime;
    private int maxInactiveInterval = 1800; // 30 minutes default
    private boolean invalidated = false;
    private boolean isNew = true;
    private MockServletContext servletContext;

    /**
     * Gets the attribute value associated with the specified name
     *
     * @param name the name of the attribute
     * @return the attribute value, or null if not found
     */
    public Object getAttribute(String name) {
        checkValid();
        return attributes.get(name);
    }

    /**
     * Gets an enumeration of all attribute names in this session
     *
     * @return an enumeration of attribute names
     */
    public Enumeration<String> getAttributeNames() {
        checkValid();
        return Collections.enumeration(attributes.keySet());
    }

    /**
     * Gets the time when this session was created
     *
     * @return the creation time in milliseconds since epoch
     */
    public long getCreationTime() {
        checkValid();
        return creationTime;
    }

    /**
     * Gets the unique identifier for this session
     *
     * @return the session ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the time when this session was last accessed
     *
     * @return the last accessed time in milliseconds since epoch
     */
    public long getLastAccessedTime() {
        checkValid();
        return lastAccessedTime;
    }

    /**
     * Gets the maximum time interval (in seconds) that this session can remain inactive
     *
     * @return the maximum inactive interval in seconds
     */
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    /**
     * Sets the maximum time interval (in seconds) that this session can remain inactive
     *
     * @param interval the maximum inactive interval in seconds
     */
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    /**
     * Gets the servlet context associated with this session
     *
     * @return null (not implemented in this mock)
     */
    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(MockServletContext servletContextIn) {
        servletContext = servletContextIn;
    }

    /**
     * Gets the session context (deprecated method)
     *
     * @return null (not implemented in this mock)
     *
     */
    public HttpSessionContext getSessionContext() {
        return null;
    }

    /**
     * Get attribute value
     *
     * @param name attribute name
     * @return attribute value
     */
    public Object getValue(String name) {
        return getAttribute(name);
    }

    /**
     * Get attribute names
     *
     * @return attribute names
     */
    public String[] getValueNames() {
        checkValid();
        return attributes.keySet().toArray(new String[0]);
    }

    /**
     * Invalidate this session
     */
    public void invalidate() {
        checkValid();
        invalidated = true;
        attributes.clear();
    }

    /**
     * Check if this session is new
     *
     * @return true if this session is new
     */
    public boolean isNew() {
        checkValid();
        return isNew;
    }

    /**
     * Stores an attribute value (deprecated method)
     *
     * @param name  the attribute name
     * @param value the attribute value
     */
    public void putValue(String name, Object value) {
        setAttribute(name, value);
    }

    /**
     * Removes the attribute with the specified name from this session
     *
     * @param name the name of the attribute to remove
     */
    public void removeAttribute(String name) {
        checkValid();
        attributes.remove(name);
    }

    /**
     * Removes an attribute value (deprecated method)
     *
     * @param name the attribute name to remove
     */
    public void removeValue(String name) {
        removeAttribute(name);
    }

    /**
     * Sets an attribute in this session
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute (null removes the attribute)
     */
    public void setAttribute(String name, Object value) {
        checkValid();
        if (value == null) {
            removeAttribute(name);
        }
        else {
            attributes.put(name, value);
        }
    }

    /**
     * Mark this session as not new (used internally)
     */
    public void markNotNew() {
        this.isNew = false;
    }

    /**
     * Update the last accessed time (used internally)
     */
    public void updateLastAccessedTime() {
        this.lastAccessedTime = System.currentTimeMillis();
    }

    /**
     * Checks if this session is still valid and throws an exception if invalidated
     *
     * @throws IllegalStateException if the session has been invalidated
     */
    private void checkValid() {
        if (invalidated) {
            throw new IllegalStateException("Session has been invalidated");
        }
    }
}
