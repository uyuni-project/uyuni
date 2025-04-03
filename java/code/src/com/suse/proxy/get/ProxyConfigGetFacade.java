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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.proxy.get;

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;

import com.suse.proxy.model.ProxyConfig;

import java.util.List;
import java.util.Map;

/**
 * Interface for operations that retrieve proxy configuration data.
 */
public interface ProxyConfigGetFacade {

    /**
     * Get the proxy configuration
     * @param server the server
     * @return the proxy configuration
     */
    ProxyConfig getProxyConfig(Server server);

    /**
     * Get the data to be rendered in the form
     * @param user the user
     * @param server the server
     * @return the form data
     */
    Map<String, Object> getFormData(User user, Server server);

    /**
     * Retrieves the tags from a given registry URL.
     * @param registryUrl the registry URL
     * @param isExact flag indicating if the URL is exact (in opposition to a base url)
     * @return the tags retrieved from registry
     */
    List<String> getRegistryUrlTags(String registryUrl, boolean isExact);

}
