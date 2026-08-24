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
package com.redhat.rhn.frontend.xmlrpc.kickstart.profile.keys;

import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link KeysHandler}.
 */
@Tag(name = "kickstart.profile.keys", description = "Provides methods to access and modify the list of activation " +
        "keys associated with a kickstart profile.")
public interface KeysHandlerApi {

    /**
     * Lookup the activation keys associated with the kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @return the activation keys associated with the profile
     */
    @ApiEndpointDoc(
        summary = "Lookup the activation keys associated with the kickstart profile.",
        method = HttpMethod.get,
        responseClass = ActivationKeyListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "activation key")
    )
    List<ActivationKey> getActivationKeys(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "ksLabel", description = "the kickstart profile label",
            in = ParameterIn.QUERY, required = true) String ksLabel);

    /**
     * Add an activation key association to the kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @param key the activation key
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Add an activation key association to the kickstart profile",
        requestClass = ActivationKeyRequest.class,
        isIntegerResponse = true
    )
    int addActivationKey(User loggedInUser, String ksLabel, String key);

    /**
     * Remove an activation key association from the kickstart profile.
     *
     * @param loggedInUser the current user
     * @param ksLabel the kickstart profile label
     * @param key the activation key
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Remove an activation key association from the kickstart profile",
        requestClass = ActivationKeyRequest.class,
        isIntegerResponse = true
    )
    int removeActivationKey(User loggedInUser, String ksLabel, String key);

    @Schema(name = "ApiResponseActivationKeyList")
    interface ActivationKeyListResponse extends ApiResponseWrapper<List<ActivationKeyDoc>> { }

    @Schema(name = "KickstartActivationKeyRequest")
    @JsonPropertyOrder({"ksLabel", "key"})
    interface ActivationKeyRequest {

        /**
         * @return the kickstart profile label
         */
        @Schema(description = "the kickstart profile label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKsLabel();

        /**
         * @return the activation key
         */
        @Schema(description = "the activation key", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKey();
    }

    @Schema(name = "ActivationKeyInfo")
    @JsonPropertyOrder({"key", "description", "usageLimit", "baseChannelLabel", "childChannelLabels", "entitlements",
        "serverGroupIds", "packageNames", "packages", "universalDefault", "disabled", "contactMethod"})
    interface ActivationKeyDoc {

        /**
         * @return the activation key
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getKey();

        /**
         * @return the description of the key
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the usage limit of the key
         */
        @Schema(name = "usage_limit", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getUsageLimit();

        /**
         * @return the label of the base channel
         */
        @Schema(name = "base_channel_label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getBaseChannelLabel();

        /**
         * @return the labels of the child channels
         */
        @Schema(name = "child_channel_labels", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "childChannelLabel")
        List<String> getChildChannelLabels();

        /**
         * @return the entitlement labels
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "entitlementLabel")
        List<String> getEntitlements();

        /**
         * @return the server group IDs
         */
        @Schema(name = "server_group_ids", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "serverGroupId")
        List<String> getServerGroupIds();

        /**
         * @return the package names
         */
        @Schema(name = "package_names", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "packageName - (deprecated by packages)")
        List<String> getPackageNames();

        /**
         * @return the packages
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "package")
        List<ActivationKeyPackageDoc> getPackages();

        /**
         * @return whether the key is the universal default
         */
        @Schema(name = "universal_default", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getUniversalDefault();

        /**
         * @return whether the key is disabled
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getDisabled();

        /**
         * @return the contact method of the key
         */
        @Schema(name = "contact_method", description = "One of the following:",
                allowableValues = {"default", "ssh-push", "ssh-push-tunnel"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getContactMethod();
    }

    @Schema(name = "ActivationKeyPackageInfo")
    @JsonPropertyOrder({"name", "arch"})
    interface ActivationKeyPackageDoc {

        /**
         * @return the name of the package
         */
        @Schema(description = "packageName", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the architecture label of the package
         */
        @Schema(description = "archLabel - optional", requiredMode = Schema.RequiredMode.REQUIRED)
        String getArch();
    }
}
