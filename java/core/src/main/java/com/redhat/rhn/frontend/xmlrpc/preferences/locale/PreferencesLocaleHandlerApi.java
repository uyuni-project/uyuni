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
package com.redhat.rhn.frontend.xmlrpc.preferences.locale;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.PublicApiEndpoint;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link PreferencesLocaleHandler}.
 */
@Tag(name = "preferences.locale", description = "Provides methods to access and modify user locale information")
public interface PreferencesLocaleHandlerApi {

    /**
     * Set a user's timezone.
     *
     * @param loggedInUser current user
     * @param login the login of the user whose timezone will be changed
     * @param tzid the timezone ID to set
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set a user's timezone.",
        requestClass = SetTimeZoneRequest.class,
        isIntegerResponse = true
    )
    int setTimeZone(User loggedInUser, String login, Integer tzid);

    /**
     * Set a user's locale.
     *
     * @param loggedInUser current user
     * @param login the login of the user whose locale will be changed
     * @param locale the locale code to set
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set a user's locale.",
        requestClass = SetLocaleRequest.class,
        isIntegerResponse = true
    )
    int setLocale(User loggedInUser, String login, String locale);

    /**
     * Returns a list of all understood timezones. Results can be used as input to setTimeZone.
     *
     * @return list of all understood timezones
     */
    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Returns a list of all understood timezones. Results can be used as input to setTimeZone.",
        method = HttpMethod.get,
        responseClass = TimeZoneListResponse.class
    )
    Object[] listTimeZones();

    /**
     * Returns a list of all understood locales. Can be used as input to setLocale.
     *
     * @return list of all understood locales
     */
    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Returns a list of all understood locales. Can be used as input to setLocale.",
        method = HttpMethod.get,
        responseClass = LocaleListResponse.class,
        responseDescription = "Locale code."
    )
    Object[] listLocales();

    @Schema(name = "SetTimeZoneRequest")
    @JsonPropertyOrder({"login", "tzid"})
    interface SetTimeZoneRequest {

        /**
         * @return the login of the user whose timezone will be changed
         */
        @Schema(description = "User's login name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();

        /**
         * @return the timezone ID to set
         */
        @Schema(description = "Timezone ID. (from listTimeZones)", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getTzid();
    }

    @Schema(name = "SetLocaleRequest")
    @JsonPropertyOrder({"login", "locale"})
    interface SetLocaleRequest {

        /**
         * @return the login of the user whose locale will be changed
         */
        @Schema(description = "User's login name.", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLogin();

        /**
         * @return the locale code to set
         */
        @Schema(description = "Locale to set. (from listLocales)", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLocale();
    }

    @Schema(name = "Timezone", description = "timezone")
    @JsonPropertyOrder({"timeZoneId", "olsonName"})
    interface RhnTimeZoneDoc {

        /**
         * @return the unique identifier for the timezone
         */
        @Schema(name = "time_zone_id", description = "unique identifier for timezone",
                requiredMode = Schema.RequiredMode.REQUIRED)
        int getTimeZoneId();

        /**
         * @return the name as identified by the Olson database
         */
        @Schema(name = "olson_name", description = "name as identified by the Olson database",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getOlsonName();
    }

    @Schema(name = "ApiResponseTimeZoneList")
    interface TimeZoneListResponse extends ApiResponseWrapper<List<RhnTimeZoneDoc>> { }

    @Schema(name = "ApiResponseLocaleList")
    interface LocaleListResponse extends ApiResponseWrapper<List<String>> { }
}
