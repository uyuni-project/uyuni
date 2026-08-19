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
package com.redhat.rhn.frontend.xmlrpc.packages.provider;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.redhat.rhn.domain.rhnpackage.PackageKey;
import com.redhat.rhn.domain.rhnpackage.PackageProvider;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Set;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link PackagesProviderHandler}.
 */
@Tag(name = "packages.provider",
     description = "Methods to retrieve information about Package Providers associated with packages.")
public interface PackagesProviderHandlerApi {

    /**
     * Lists the package providers.
     *
     * @param loggedInUser the current user
     * @return the package providers
     */
    @ApiEndpointDoc(
        summary = "List all Package Providers.",
        description = "User executing the request must be a Uyuni administrator.",
        responseClass = PackageProviderListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package provider")
    )
    List<PackageProvider> list(User loggedInUser);

    /**
     * Lists the security keys associated with a package provider.
     *
     * @param loggedInUser the current user
     * @param providerName the provider name
     * @return the security keys
     */
    @ApiEndpointDoc(
        summary = "List all security keys associated with a package provider.",
        description = "User executing the request must be a Uyuni administrator.",
        method = HttpMethod.get,
        responseClass = PackageKeyListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "package security key")
    )
    Set<PackageKey> listKeys(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "providerName", description = "The provider name",
            in = ParameterIn.QUERY, required = true) String providerName
    );

    /**
     * Associates a package security key with a package provider.
     *
     * @param loggedInUser the current user
     * @param providerName the provider name
     * @param key the actual key
     * @param type the type of the key
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Associate a package security key and with the package provider.",
        description = "If the provider or key doesn't exist, it is created. User executing the " +
                "request must be a Uyuni administrator.",
        requestClass = AssociateKeyRequest.class,
        isIntegerResponse = true
    )
    int associateKey(User loggedInUser, String providerName, String key, String type);

    @Schema(name = "AssociateKeyRequest")
    @JsonPropertyOrder({"providerName", "key", "type"})
    interface AssociateKeyRequest {

        /**
         * @return the provider name
         */
        @Schema(description = "The provider name", requiredMode = REQUIRED)
        String getProviderName();

        /**
         * @return the actual key
         */
        @Schema(description = "The actual key", requiredMode = REQUIRED)
        String getKey();

        /**
         * @return the type of the key
         */
        @Schema(description = "The type of the key. Currently, only 'gpg' is supported",
                requiredMode = REQUIRED)
        String getType();
    }

    @Schema(name = "PackageSecurityKey", description = "package security key")
    @JsonPropertyOrder({"key", "type"})
    interface PackageKeyDoc {

        /**
         * @return the actual key
         */
        @Schema(requiredMode = REQUIRED)
        String getKey();

        /**
         * @return the type of the key
         */
        @Schema(requiredMode = REQUIRED)
        String getType();
    }

    @Schema(name = "PackageProvider", description = "package provider")
    @JsonPropertyOrder({"name", "keys"})
    interface PackageProviderDoc {

        /**
         * @return the provider name
         */
        @Schema(requiredMode = REQUIRED)
        String getName();

        /**
         * @return the security keys associated with the provider
         */
        @Schema(requiredMode = REQUIRED)
        List<PackageKeyDoc> getKeys();
    }

    @Schema(name = "ApiResponsePackageProviderList")
    interface PackageProviderListResponse extends ApiResponseWrapper<List<PackageProviderDoc>> { }

    @Schema(name = "ApiResponsePackageSecurityKeyList")
    interface PackageKeyListResponse extends ApiResponseWrapper<List<PackageKeyDoc>> { }
}
