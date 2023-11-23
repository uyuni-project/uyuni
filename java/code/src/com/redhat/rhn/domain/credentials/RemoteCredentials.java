/*
 * Copyright (c) 2023 SUSE LLC
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

package com.redhat.rhn.domain.credentials;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Base class for all the credentials that can be used by {@link com.redhat.rhn.manager.content.ContentSyncManager}
 */
@MappedSuperclass
public abstract class RemoteCredentials extends PasswordBasedCredentials {

    private String url;

    /**
     * Return the URL.
     * @return url
     */
    @Column(name = "url")
    public String getUrl() {
        return url;
    }

    /**
     * Set the url.
     * @param urlIn url
     */
    public void setUrl(String urlIn) {
        this.url = urlIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof RemoteCredentials)) {
            return false;
        }

        RemoteCredentials that = (RemoteCredentials) o;

        return new EqualsBuilder()
            .appendSuper(super.equals(o))
            .append(url, that.url)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .appendSuper(super.hashCode())
            .append(url)
            .toHashCode();
    }

}
