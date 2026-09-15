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

package com.suse.manager.model.hub;

/**
 * Common interface for ISS servers
 */
public interface IssServer {

    /**
     * The role, either HUB or PERIPHERAL
     * @return the server {@link IssRole}
     */
    IssRole getRole();

    /**
     * Gets the ID
     * @return the server ID
     */
    Long getId();

    /**
     * Gets the FQDN
     * @return the FQDN
     */
    String getFqdn();

    /**
     * Gets the root certificate
     * @return the root certificate, if specified
     */
    String getRootCa();

    /**
     * Sets the FQDN
     * @param fqdnIn the new FQDN
     */
    void setFqdn(String fqdnIn);

    /**
     * Sets the root certificate
     * @param rootCaIn the new root certificate, or null if not needed
     */
    void setRootCa(String rootCaIn);
}
