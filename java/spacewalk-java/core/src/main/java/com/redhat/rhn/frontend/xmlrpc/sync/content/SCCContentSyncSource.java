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

import com.suse.manager.model.hub.IssHub;
import com.suse.scc.client.SCCConfigBuilder;
import com.suse.scc.client.SCCWebClient;
import com.suse.utils.CertificateUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.cert.CertificateException;
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
    public SCCWebClient getClient(String uuid, Path loggingDir, boolean skipOwner) throws ContentSyncSourceException {
        try {
            URI url = new URI(Config.get().getString(ConfigDefaults.SCC_URL));

            String username = credentials.getUsername();
            String password = credentials.getPassword();

            IssHub issHub = credentials.getIssHub();
            if (issHub != null) {
                String rootCa = issHub.getRootCa();
                URI uri = new URI("https://%1$s/rhn/hub/scc/".formatted(issHub.getFqdn()));
                var cfg = new SCCConfigBuilder()
                        .setUrl(uri)
                        .setCertificates(CertificateUtils.parse(rootCa).stream().toList())
                        .setUsername(username)
                        .setPassword(password)
                        .setUuid(uuid)
                        .setLoggingDir(loggingDir.toAbsolutePath().toString())
                        .setSkipOwner(skipOwner)
                        .createSCCConfig();
                return new SCCWebClient(cfg);
            }

            var config = new SCCConfigBuilder()
                    .setUrl(url)
                    .setUsername(username)
                    .setPassword(password)
                    .setUuid(uuid)
                    .setLoggingDir(loggingDir.toAbsolutePath().toString())
                    .setSkipOwner(skipOwner)
                    .createSCCConfig();

            return new SCCWebClient(config);
        }
        catch (URISyntaxException | CertificateException e) {
            throw new ContentSyncSourceException(e);
        }
    }
}
