/**
 * Copyright (c) 2018 SUSE LLC
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
package com.redhat.rhn.domain.scc;

import com.redhat.rhn.manager.content.MgrSyncUtils;

import java.util.function.Function;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * This is a SUSE repository as parsed from JSON coming in from SCC.
 */
@Entity(name = "NoAuth")
@DiscriminatorValue("noauth")
public class SCCRepositoryNoAuth extends SCCRepositoryAuth {

    /**
     * Default Constructor
     */
    public SCCRepositoryNoAuth() { }

    /**
     * @return the URL including authentication info
     */
    @Transient
    public String getUrl() {
        if (getCredentials() == null) {
            return MgrSyncUtils.urlToFSPath(getRepository().getUrl(), getRepository().getName()).toString();
        }
        return getRepository().getUrl();
    }

    @Override
    public <T> T fold(
            Function<SCCRepositoryBasicAuth, ? extends T> basicAuth,
            Function<SCCRepositoryNoAuth, ? extends T> noAuth,
            Function<SCCRepositoryTokenAuth, ? extends T> tokenAuth) {
        return noAuth.apply(this);
    }
}
