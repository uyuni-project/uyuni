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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.credentials.RemoteCredentials;
import com.redhat.rhn.domain.credentials.SCCCredentials;

import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCWebClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

public class SCCContentSyncSource implements ContentSyncSource {

    private final SCCCredentials credentials;

    /**
     * Builds an SCC content source
     * @param credentialsIn the credentials used to access SCC
     */
    public SCCContentSyncSource(SCCCredentials credentialsIn) {
        this.credentials = credentialsIn;
    }

    @Override
    public <T> T match(Function<SCCContentSyncSource, T> scc, Function<RMTContentSyncSource, T> rmt,
                       Function<LocalDirContentSyncSource, T> local) {
        return scc.apply(this);
    }

    @Override
    public Optional<RemoteCredentials> getCredentials() {
        return Optional.of(credentials);
    }

    @Override
    public SCCClient getClient(String uuid, Optional<Path> loggingDir) throws ContentSyncSourceException {
        try {
            URI url = new URI(Config.get().getString(ConfigDefaults.SCC_URL));

            String username = credentials.getUsername();
            String password = credentials.getPassword();

            SCCConfig config = loggingDir
                .map(path -> path.toAbsolutePath().toString())
                .map(path -> new SCCConfig(url, username, password, uuid, null, path, false))
                .orElseGet(() -> new SCCConfig(url, username, password, uuid));

            return new SCCWebClient(config);
        }
        catch (URISyntaxException e) {
            throw new ContentSyncSourceException(e);
        }
    }
}
