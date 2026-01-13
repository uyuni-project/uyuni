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
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * A web session
 */
@Entity
@Table(name = "PXTSessions")
@NamedQuery(
        name = "WebSession.deleteByUserId",
        query = "DELETE FROM WebSessionImpl w WHERE w.webUserId = :user_id")
public class WebSessionImpl implements WebSession {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "expires", nullable = false)
    private long expires;
    @Column(name = "web_user_id")
    private Long webUserId;

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
        return " ";
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

        return id + "x" + SessionManager.generateSessionKey(id.toString());
    }

}
