/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.frontend.xmlrpc.admin.ssh;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.docs.ApiEndpointDoc;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * API contract for {@link AdminSshHandler}.
 */
@Tag(name = "admin.ssh", description = "Provides methods to manage SSH data.")
public interface AdminSshHandlerApi {

    /**
     * Removes a host from the list of known hosts.
     *
     * @param loggedInUser current user
     * @param hostname the hostname or IP of the host to remove
     * @param port the port of the host to remove
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Remove a host from the list of known hosts.",
        requestClass = RemoveKnownHostRequest.class,
        isIntegerResponse = true
    )
    int removeKnownHost(User loggedInUser, String hostname, Integer port);

    @Schema(name = "RemoveKnownHostRequest")
    @JsonPropertyOrder({"hostname", "port"})
    interface RemoveKnownHostRequest {

        /**
         * @return the hostname or IP of the host to remove
         */
        @Schema(description = "hostname or IP of the host to remove", requiredMode = Schema.RequiredMode.REQUIRED)
        String getHostname();

        /**
         * @return the port of the host to remove
         */
        @Schema(description = "port of the host to remove", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getPort();
    }
}
