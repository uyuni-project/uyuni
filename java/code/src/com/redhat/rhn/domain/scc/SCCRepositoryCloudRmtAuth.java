/*
 * Copyright (c) 2021 SUSE LLC
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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * This is a SUSE repository loaded from a PAYG instance and linked to a cloud rmt server
 */
@Entity(name = "CloudRmt")
@DiscriminatorValue("cloudrmt")
public class SCCRepositoryCloudRmtAuth extends SCCRepositoryAuth {

    // Logger instance
    private static Logger log = LogManager.getLogger(SCCRepositoryCloudRmtAuth.class);

    private static final String MIRRCRED_QUERY = "credentials=mirrcred_";

    /**
     * Default Constructor
     */
    public SCCRepositoryCloudRmtAuth() { }

    /**
     * @return the URL including authentication info
     */
    @Override
    @Transient
    public String getUrl() {
        try {
            URI url = new URI(getRepo().getUrl());
            if (!url.getHost().startsWith(MgrSyncUtils.OFFICIAL_SUSE_UPDATE_HOST)) {
                /*
                SCC data contain repositories which point to external server and are free to access.
                Examples are openSUSE and nVidia repositories. These repos are not available on the RMT servers
                and the URLs shoudl not be re-written. Creating them at type {@link SCCRepositoryNoAuth}
                requires to get the json definition of the repo somehow as input into
                {@link ContentSyncManager#refreshRepositoriesAuthentication}. Otherwise the repo will be removed again.
                Just returning the original URL here is better to understand.
                */
                return getRepo().getUrl();
            }

            URI credUrl = new URI(getOptionalCredentials().orElseThrow().getUrl());
            List<String> sourceParams = new ArrayList<>(Arrays.asList(
                    StringUtils.split(Optional.ofNullable(url.getQuery()).orElse(""), '&')));
            sourceParams.add(MIRRCRED_QUERY + getOptionalCredentials().orElseThrow().getId());
            String newQuery = StringUtils.join(sourceParams, "&");

            URI newURI = new URI(credUrl.getScheme(), url.getUserInfo(), credUrl.getHost(), credUrl.getPort(),
                    mergeUrls(credUrl, url), newQuery, credUrl.getFragment());
            return newURI.toString();
        }
        catch (URISyntaxException ex) {
            log.error("Unable to parse URL: {}", getUrl());
        }
        return null;
    }

    private static String mergeUrls(URI credentialUrl, URI repositoryUrl) {
        // If the paths start in the same way we have a clashing folder to remove.
        // This happens when the credential url is https://host/repo/ and the repo path is /repo/whatever/dir/
        // We DON'T want to end up with https://host/repo/repo/whatever/dir/
        if (repositoryUrl.getPath().startsWith(credentialUrl.getPath())) {
            return credentialUrl.resolve(repositoryUrl.getPath()).getPath();
        }

        // Otherwise Just combine the two paths
        return credentialUrl.getPath() + repositoryUrl.getPath();
    }

    @Override
    public <T> T fold(
            Function<SCCRepositoryBasicAuth, ? extends T> basicAuth,
            Function<SCCRepositoryNoAuth, ? extends T> noAuth,
            Function<SCCRepositoryTokenAuth, ? extends T> tokenAuth,
            Function<SCCRepositoryCloudRmtAuth, ? extends T> cloudRmtAuth) {
        return cloudRmtAuth.apply(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("authType", "rmtAuth")
                .toString();
    }
}
