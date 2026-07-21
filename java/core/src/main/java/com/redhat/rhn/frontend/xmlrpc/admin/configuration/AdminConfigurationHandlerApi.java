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
package com.redhat.rhn.frontend.xmlrpc.admin.configuration;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocParam;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * API contract for {@link AdminConfigurationHandler}.
 */
@Tag(name = "admin.configuration", description = "Provides methods to configure the Uyuni server.")
public interface AdminConfigurationHandlerApi {

    /**
     * Configure server.
     *
     * @param loggedInUser the current user
     * @param content the Uyuni configuration formula data
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Configure server.",
        requestClass = ConfigureRequest.class,
        isIntegerResponse = true
    )
    int configure(User loggedInUser, Map<String, Object> content);

    @Schema(name = "ConfigureServerRequest")
    interface ConfigureRequest {

        /**
         * @return the Uyuni configuration formula data
         */
        @LegacyDocParam(type = "map")
        @Schema(type = "object", description = "the Uyuni configuration formula data",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Object getContent();
    }
}
