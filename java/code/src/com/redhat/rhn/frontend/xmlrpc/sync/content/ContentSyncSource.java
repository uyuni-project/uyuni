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

package com.redhat.rhn.frontend.xmlrpc.sync.content;

import com.redhat.rhn.domain.credentials.RemoteCredentials;

import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.client.SCCWebClient;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

public interface ContentSyncSource {

    /**
     * Retrieves this content source object constrained by the specified type
     * @param contentSyncSourceClass the expected content source class
     * @return an optional containing the current instance converted to the given type, or empty if the types do not
     * match
     * @param <T> an implementation of {@link ContentSyncSource}
     */
    default <T extends ContentSyncSource> Optional<T> castAs(Class<T> contentSyncSourceClass) {
        if (contentSyncSourceClass.isInstance(this)) {
            return Optional.of(contentSyncSourceClass.cast(this));
        }

        return Optional.empty();
    }

    /**
     * Get the {@link RemoteCredentials} associated with this content source, if any.
     * @return the remote credentials to be used, or empty if not supported by this source
     */
    Optional<RemoteCredentials> getCredentials();

    /**
     * Gets the instance of {@link SCCWebClient} that allows to access this content source.
     * @param uuid the unique identifier for this client for debugging purpose
     * @param loggingDir the optional logging directory
     * @return the client that can be used to connect this content source.
     * @throws ContentSyncSourceException when it's not possible to build a client
     * @throws SCCClientException when an client error happens during the initialization
     */
    SCCClient getClient(String uuid, Optional<Path> loggingDir) throws ContentSyncSourceException, SCCClientException;


    /**
     * Retrieves the credentials associated with this content source constrained to the specified type, if any.
     * @param credentialsClass the type of credentials required
     * @return the remote credentials to be used, or empty if not supported by this source
     * @param <T> a subclass of {@link RemoteCredentials}
     */
    default <T extends RemoteCredentials> Optional<T> getCredentials(Class<T> credentialsClass) {
        return getCredentials().flatMap(c -> c.castAs(credentialsClass));
    }

    /**
     * Execute the specified function on this content source, matching the source type
     * @param scc the function to apply when the source is {@link SCCContentSyncSource}
     * @param rmt the function to apply when the source is {@link RMTContentSyncSource}
     * @param local the function to apply when the source is {@link LocalDirContentSyncSource}
     * @return the result returned by the function
     * @param <T> the type of the result
     */
    <T> T match(
            Function<SCCContentSyncSource, T> scc,
            Function<RMTContentSyncSource, T> rmt,
            Function<LocalDirContentSyncSource, T> local
    );
}
