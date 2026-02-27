/*
 * Copyright (c) 2026 SUSE LLC
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
package com.suse.manager.api;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Defines a standard API response wrapper for API endpoints.
 *
 * @param <T> payload type
 */
@Schema(name = "ApiResponse", description = "Standard API Response wrapper")
public interface ApiResponseWrapper<T> {

    /**
     * @return operation success status
     */
    @Schema(description = "Operation success status", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    boolean isSuccess();

    /**
     * @return error message when the operation fails
     */
    @Schema(description = "Error message if success is false", nullable = true)
    String getMessage();

    /**
     * @return payload result
     */
    @Schema(description = "The payload result", requiredMode = Schema.RequiredMode.REQUIRED)
    T getResult();
}
