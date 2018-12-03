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

import org.apache.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * This is a SUSE repository as parsed from JSON coming in from SCC.
 */
@Entity(name = "TokenAuth")
@DiscriminatorValue("tokenauth")
public class SCCRepositoryTokenAuth extends SCCRepositoryAuth {

    // Logger instance
    private static Logger log = Logger.getLogger(SCCRepositoryTokenAuth.class);

    private String auth;

    /**
     * Default Constructor
     */
    public SCCRepositoryTokenAuth() { }

    /**
     * Constructor
     * @param authIn e.g. token
     */
    public SCCRepositoryTokenAuth(String authIn) {
        auth = authIn;
    }

    /**
     * @return the auth string
     */
    @Column
    public String getAuth() {
        return auth;
    }

    /**
     * @param authIn the auth string
     */
    public void setAuth(String authIn) {
        auth = authIn;
    }

    /**
     * @return the URL including authentication info
     */
    @Transient
    public String getUrl() {
        try {
            URI url = new URI(getRepository().getUrl());
            URI newURI = new URI(url.getScheme(), url.getUserInfo(), url.getHost(), url.getPort(),
                    url.getPath(), getAuth(), url.getFragment());
            return newURI.toString();
        }
        catch (URISyntaxException ex) {
            log.error("Unable to parse URL: " + getUrl());
        }
        return null;
    }

    @Override
    public <T> T fold(
            Function<SCCRepositoryBasicAuth, ? extends T> basicAuth,
            Function<SCCRepositoryNoAuth, ? extends T> noAuth,
            Function<SCCRepositoryTokenAuth, ? extends T> tokenAuth) {
        return tokenAuth.apply(this);
    }

}
