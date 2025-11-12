/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2012 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.session;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.session.SessionManager;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * A web session
 */
@Entity
@Table(name = "PXTSessions")
public class WebSessionImpl implements WebSession {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pxt_seq")
    @SequenceGenerator(name = "pxt_seq", sequenceName = "pxt_id_seq", allocationSize = 1)
    private Long id;
    @Column
    private long expires;
    @Column(name = "web_user_id")
    private Long webUserId;
    @Column
    private String value;

    /**
     * Protected Constructor
     */
    protected WebSessionImpl() {
        // keep Hibernate & perl from blowing chunks
        value = " ";
    }

    /** {@inheritDoc} */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * Sets the value of id to new value
     * @param idIn New value for id
     */
    protected void setId(Long idIn) {
        id = idIn;
    }

    /** {@inheritDoc} */
    @Override
    public Long getWebUserId() {
        return webUserId;
    }

    /** {@inheritDoc} */
    @Override
    public void setWebUserId(Long idIn) {
        if (idIn != null && idIn == 0) {
            throw new IllegalArgumentException("user id must be null or non-zero");
        }
        webUserId = idIn;
    }

    /** {@inheritDoc} */
    @Override
    public User getUser() {
        if (webUserId != null) {
            return UserFactory.lookupById(webUserId);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public long getExpires() {
        return expires;
    }

    /** {@inheritDoc} */
    @Override
    public void setExpires(long expIn) {
        expires = expIn;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExpired() {
        long expireTime = getExpires();
        //if the expire time is less than the allowable values, it shouldn't be
        return expireTime < -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue() {
        return value;
    }

    private void setValue(String val) {
        value = val;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        if (id == null || id < 0) {
            throw new InvalidSessionIdException("Attempted to get key for session with " +
                                                "an invalid id");
        }

        return id.toString() + "x" + SessionManager.generateSessionKey(id.toString());
    }

}
