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

package com.suse.manager.hub;

import com.suse.manager.model.hub.ManagerInfoJson;

import java.io.IOException;

/**
 * Hub Inter-Server-Sync Client to connect a remote server and invoke the private server-to-server Rest-like API
 */
public interface HubInternalClient {

    /**
     * Register a remote server as a hub
     * @param token the token issued by the remote server to grant access
     * @param rootCA the root certificate, if needed
     * @throws IOException when the communication fails
     */
    void registerHub(String token, String rootCA) throws IOException;

    /**
     * Store the SCC credentials on the remote peripheral server
     * @param username the username
     * @param password the password
     * @throws IOException when the communication fails
     */
    void storeCredentials(String username, String password) throws IOException;

    /**
     * Query Manager information from the peripheral server
     * @return return {@link ManagerInfoJson} from peripheral server
     * @throws IOException when communication fails
     */
    ManagerInfoJson getManagerInfo() throws IOException;

    /**
     * Store Report DB credentials on the remote peripheral server
     * @param username the username
     * @param password the password
     * @throws IOException when communication fails
     */
    void storeReportDbCredentials(String username, String password) throws IOException;

}
