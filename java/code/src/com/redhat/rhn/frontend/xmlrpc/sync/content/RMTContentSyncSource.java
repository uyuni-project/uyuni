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

import com.redhat.rhn.domain.credentials.CloudRMTCredentials;
import com.redhat.rhn.domain.credentials.RemoteCredentials;

import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCWebClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class RMTContentSyncSource implements ContentSyncSource {

    private final CloudRMTCredentials credentials;

    /**
     * Builds an RMT content source
     * @param rmtCredentialsIn the cloud credentials to access RMT
     */
    public RMTContentSyncSource(CloudRMTCredentials rmtCredentialsIn) {
        this.credentials = rmtCredentialsIn;
    }

    @Override
    public <T> T match(Function<SCCContentSyncSource, T> scc, Function<RMTContentSyncSource, T> rmt,
                       Function<LocalDirContentSyncSource, T> local) {
        return rmt.apply(this);
    }

    @Override
    public Optional<RemoteCredentials> getCredentials() {
        return Optional.of(credentials);
    }

    @Override
    public SCCClient getClient(String uuid, Optional<Path> loggingDir) throws ContentSyncSourceException {
        try {
            URI uri = new URI(credentials.getUrl());
            URI url = new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), null, null, null);

            Gson gson = new GsonBuilder().create();

            @SuppressWarnings("unchecked")
            Map<String, String> headers = gson.fromJson(new String(credentials.getExtraAuthData()), Map.class);

            SCCConfig config = loggingDir
                .map(path -> path.toAbsolutePath().toString())
                .map(path -> new SCCConfig(url, null, null, uuid, null, path, false, headers))
                .orElseGet(() -> new SCCConfig(url, null, null, uuid, headers));

            return new SCCWebClient(config);
        }
        catch (URISyntaxException | JsonParseException e) {
            throw new ContentSyncSourceException(e);
        }
    }
}
