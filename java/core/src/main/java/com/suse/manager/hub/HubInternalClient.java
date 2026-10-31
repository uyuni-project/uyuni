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

import com.suse.manager.model.hub.ChannelInfoDetailsJson;
import com.suse.manager.model.hub.ChannelInfoJson;
import com.suse.manager.model.hub.ManagerInfoJson;
import com.suse.manager.model.hub.OrgInfoJson;
import com.suse.manager.model.hub.ServerInfoJson;

import java.io.IOException;
import java.util.List;

/**
 * Hub Inter-Server-Sync Client to connect a remote server and invoke the private server-to-server Rest-like API
 */
public interface HubInternalClient {

    /**
     * Register a remote server as a hub
     * @param token the token issued by the remote server to grant access
     * @param rootCA the root certificate, if needed
     * @param gpgKey the gpg key, if needed
     * @throws IOException when the communication fails
     */
    void registerHub(String token, String rootCA, String gpgKey) throws IOException;

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

    /**
     * De-register the calling server from the remote side
     *
     * @throws IOException when the communication fails
     */
    void deregister() throws IOException;

    /**
     * Return all the peripheral organizations
     * @return the organizations
     * @throws IOException
     */
    List<OrgInfoJson> getAllPeripheralOrgs() throws IOException;

    /**
     * Return all the peripheral channels
     * @return the channels
     * @throws IOException
     */
    List<ChannelInfoJson> getAllPeripheralChannels() throws IOException;

    /**
     * Replace the hub token on the remote peripheral server and get a new peripheral token back
     * @param newHubToken the new hub token
     * @return return the new peripheral token
     * @throws IOException when the communication fails
     */
    String replaceTokens(String newHubToken) throws IOException;

    /**
     * Schedule a product refresh on the remote peripheral server
     * @throws IOException when the communication fails
     */
    void scheduleProductRefresh() throws IOException;

    /**
     * Synchronizes all channels on the remote peripheral server
     *
     * @param channelInfo a list of {@link ChannelInfoDetailsJson} objects
     * @throws IOException when the communication fails
     */
    void syncChannels(List<ChannelInfoDetailsJson> channelInfo) throws IOException;

    /**
     * Deletes the ISSv1 Master
     */
    void deleteIssV1Master() throws IOException;

    /**
     * checks if the server can be registered as peripheral
     *
     * @return information about the server
     * @throws IOException when the communication fails
     */
    ServerInfoJson getServerInfo() throws IOException;
}
