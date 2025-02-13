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

package com.suse.proxy;

import com.suse.manager.api.ParseException;

import java.util.List;

public interface ProxyRegistryUtils {

    /**
     * Get the tags from the registry when the URL for a specific image.
     * Eg:
     * - https://registry.opensuse.org/uyuni/proxy-httpd
     *
     * @param registryUrl the registry URL
     * @return the tags list
     */
     List<String> getTags(RegistryUrl registryUrl) throws ParseException;

    /**
     * Retrieves the list of repositories from the registry.
     *
     * @param registryUrl the registry URL
     * @return the list of repositories
     */
    List<String> getRepositories(RegistryUrl registryUrl) throws ParseException;

}
