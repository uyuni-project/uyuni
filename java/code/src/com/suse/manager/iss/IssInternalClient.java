/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.iss;

import com.redhat.rhn.domain.iss.IssRole;

import java.io.IOException;

/**
 * ISS Client to connect a remote server and invoke the private server-to-server Rest-like API
 */
public interface IssInternalClient {

    /**
     * Register a remote server with the given role
     * @param role the ISS role
     * @param token the token issued by the remote server to grant access
     * @param rootCA the root certificate, if needed
     * @throws IOException when the communication fails
     */
    void register(IssRole role, String token, String rootCA) throws IOException;
}
