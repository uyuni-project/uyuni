/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.hub;

import java.io.IOException;

/**
 * Hub Inter-Server-Sync Client to connect to a remote server and invoke the public XMLRPC/Rest-like APIs.
 */
public interface HubExternalClient extends AutoCloseable {

    /**
     * Issue a token for the given remote server
     * @param fqdn the FQDN of the remote server
     * @return the generated token
     * @throws IOException when the remote communication fails
     */
    String generateAccessToken(String fqdn) throws IOException;

    /**
     * Stores the given token associating it to the specified server
     * @param fqdn the FQDN of the remote server
     * @param token the access token
     * @throws IOException when the remote communication fails
     */
    void storeAccessToken(String fqdn, String token) throws IOException;

    /**
     * {@inheritDoc}
     * @throws IOException when the remote communication fails
     */
    void close() throws IOException;

}
