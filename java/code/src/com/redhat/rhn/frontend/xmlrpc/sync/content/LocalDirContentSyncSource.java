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
import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCFileClient;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

public class LocalDirContentSyncSource implements ContentSyncSource {
    private final Path path;

    /**
     * Builds a local source on the specified path
     * @param pathIn the local path where the scc data is available
     */
    public LocalDirContentSyncSource(Path pathIn) {
        this.path = pathIn;
    }

    @Override
    public <T> T match(Function<SCCContentSyncSource, T> scc, Function<RMTContentSyncSource, T> rmt,
                       Function<LocalDirContentSyncSource, T> local) {
        return local.apply(this);
    }

    public Path getPath() {
        return path;
    }

    @Override
    public Optional<RemoteCredentials> getCredentials() {
        return Optional.empty();
    }

    @Override
    public SCCClient getClient(String uuid, Optional<Path> loggingDir) throws SCCClientException {
        File localFile = path.toFile();
        String localAbsolutePath = localFile.getAbsolutePath();

        if (!localFile.canRead()) {
            throw new SCCClientException(
                String.format("Unable to access resource at \"%s\" location.",
                    localAbsolutePath));
        }
        else if (!localFile.isDirectory()) {
            throw new SCCClientException(String.format("Path \"%s\" must be a directory.", localAbsolutePath));
        }

        return new SCCFileClient(new SCCConfig(localAbsolutePath));
    }
}
