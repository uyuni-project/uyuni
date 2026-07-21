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
package com.suse.manager.xmlrpc.admin;

import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocParam;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * API contract for {@link AdminPaygHandler}.
 */
@Tag(name = "admin.payg", description = "Provides methods to access and modify PAYG ssh connection data")
public interface AdminPaygHandlerApi {

    /**
     * Create a new ssh connection data to extract data from.
     *
     * @param loggedInUser the current user
     * @param description the description
     * @param host the hostname or IP address to the instance
     * @param port the ssh port
     * @param username the ssh username
     * @param password the ssh password
     * @param key the private key to use in authentication
     * @param keyPassword the private key password
     * @param bastionHost the bastion hostname or IP address
     * @param bastionPort the bastion ssh port
     * @param bastionUsername the bastion ssh username
     * @param bastionPassword the bastion ssh password
     * @param bastionKey the private key to use in bastion authentication
     * @param bastionKeyPassword the bastion private key password
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Create a new ssh connection data to extract data from",
        requestClass = CreateRequest.class,
        isIntegerResponse = true
    )
    int create(User loggedInUser, String description, String host, Integer port, String username,
               String password, String key, String keyPassword,
               String bastionHost, Integer bastionPort, String bastionUsername,
               String bastionPassword, String bastionKey, String bastionKeyPassword);

    /**
     * Update the details of a ssh connection data.
     *
     * @param loggedInUser the current user
     * @param host the hostname or IP address to the instance
     * @param details the connection details to update
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Updates the details of a ssh connection data",
        requestClass = SetDetailsRequest.class,
        isIntegerResponse = true
    )
    int setDetails(User loggedInUser, String host, Map<String, Object> details);

    /**
     * List the registered ssh connection data.
     *
     * @param loggedInUser the current user
     * @return the list of registered ssh connection data
     */
    @ApiEndpointDoc(
        summary = "Returns a list of ssh connection data registered.",
        responseClass = PaygSshDataListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "SSH data")
    )
    List<PaygSshData> list(User loggedInUser);

    /**
     * Get the ssh connection data registered for a given host.
     *
     * @param loggedInUser the current user
     * @param host the hostname or IP address of the instance
     * @return the ssh connection data registered for the host
     */
    @ApiEndpointDoc(
        summary = "Returns the ssh connection data registered for a given host.",
        requestClass = HostRequest.class,
        responseClass = PaygSshDataResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "SSH data")
    )
    PaygSshData getDetails(User loggedInUser, String host);

    /**
     * Delete the ssh connection data registered for a given host.
     *
     * @param loggedInUser the current user
     * @param host the hostname or IP address of the instance
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Deletes the ssh connection data registered for a given host.",
        requestClass = HostRequest.class,
        isIntegerResponse = true
    )
    int delete(User loggedInUser, String host);

    @Schema(name = "CreatePaygSshDataRequest")
    @JsonPropertyOrder({"description", "host", "port", "username", "password", "key", "keyPassword",
        "bastionHost", "bastionPort", "bastionUsername", "bastionPassword", "bastionKey", "bastionKeyPassword"})
    interface CreateRequest {

        /**
         * @return the description
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the hostname or IP address to the instance
         */
        @Schema(description = "hostname or IP address to the instance, will fail if already in use.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getHost();

        /**
         * @return the ssh port
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getPort();

        /**
         * @return the ssh username
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getUsername();

        /**
         * @return the ssh password
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getPassword();

        /**
         * @return the private key to use in authentication
         */
        @Schema(description = "private key to use in authentication", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKey();

        /**
         * @return the private key password
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getKeyPassword();

        /**
         * @return the bastion hostname or IP address
         */
        @Schema(description = "hostname or IP address to a bastion host",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getBastionHost();

        /**
         * @return the bastion ssh port
         */
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer getBastionPort();

        /**
         * @return the bastion ssh username
         */
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getBastionUsername();

        /**
         * @return the bastion ssh password
         */
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getBastionPassword();

        /**
         * @return the private key to use in bastion authentication
         */
        @Schema(description = "private key to use in bastion authentication",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getBastionKey();

        /**
         * @return the bastion private key password
         */
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getBastionKeyPassword();
    }

    @Schema(name = "HostRequest")
    interface HostRequest {

        /**
         * @return the hostname or IP address to the instance
         */
        @Schema(description = "hostname or IP address to the instance, will fail if host doesn't exist.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getHost();
    }

    @Schema(name = "SetPaygDetailsRequest")
    @JsonPropertyOrder({"host", "details"})
    interface SetDetailsRequest {

        /**
         * @return the hostname or IP address to the instance
         */
        @Schema(description = "hostname or IP address to the instance, will fail if host doesn't exist.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getHost();

        /**
         * @return the connection details to update
         */
        @LegacyDocParam(type = "struct")
        @Schema(description = "user details", requiredMode = Schema.RequiredMode.REQUIRED)
        PaygDetailsDoc getDetails();
    }

    @Schema(name = "PaygDetails", description = "user details")
    @JsonPropertyOrder({"description", "port", "username", "password", "key", "keyPassword",
        "bastionHost", "bastionPort", "bastionUsername", "bastionPassword", "bastionKey", "bastionKeyPassword"})
    interface PaygDetailsDoc {

        /**
         * @return the description
         */
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getDescription();

        /**
         * @return the ssh port
         */
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer getPort();

        /**
         * @return the ssh username
         */
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getUsername();

        /**
         * @return the ssh password
         */
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getPassword();

        /**
         * @return the private key
         */
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getKey();

        /**
         * @return the private key password
         */
        @Schema(name = "key_password", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getKeyPassword();

        /**
         * @return the bastion hostname
         */
        @Schema(name = "bastion_host", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getBastionHost();

        /**
         * @return the bastion ssh port
         */
        @Schema(name = "bastion_port", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer getBastionPort();

        /**
         * @return the bastion ssh username
         */
        @Schema(name = "bastion_username", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getBastionUsername();

        /**
         * @return the bastion ssh password
         */
        @Schema(name = "bastion_password", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getBastionPassword();

        /**
         * @return the bastion private key
         */
        @Schema(name = "bastion_key", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getBastionKey();

        /**
         * @return the bastion private key password
         */
        @Schema(name = "bastion_key_password", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getBastionKeyPassword();
    }

    @Schema(name = "PaygSshData", description = "SSH data")
    @JsonPropertyOrder({"description", "hostname", "port", "username",
        "bastionHostname", "bastionPort", "bastionUsername"})
    interface PaygSshDataDoc {

        /**
         * @return the description
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the hostname
         */
        @Schema(name = "hostname", requiredMode = Schema.RequiredMode.REQUIRED)
        String getHostname();

        /**
         * @return the ssh port
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getPort();

        /**
         * @return the ssh username
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getUsername();

        /**
         * @return the bastion hostname
         */
        @Schema(name = "bastion_hostname", requiredMode = Schema.RequiredMode.REQUIRED)
        String getBastionHostname();

        /**
         * @return the bastion ssh port
         */
        @Schema(name = "bastion_port", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getBastionPort();

        /**
         * @return the bastion ssh username
         */
        @Schema(name = "bastion_username", requiredMode = Schema.RequiredMode.REQUIRED)
        String getBastionUsername();
    }

    @Schema(name = "ApiResponsePaygSshDataList")
    interface PaygSshDataListResponse extends ApiResponseWrapper<List<PaygSshDataDoc>> { }

    @Schema(name = "ApiResponsePaygSshData")
    interface PaygSshDataResponse extends ApiResponseWrapper<PaygSshDataDoc> { }
}
