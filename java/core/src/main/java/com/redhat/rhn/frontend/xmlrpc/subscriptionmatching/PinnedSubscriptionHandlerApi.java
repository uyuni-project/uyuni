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
package com.redhat.rhn.frontend.xmlrpc.subscriptionmatching;

import com.redhat.rhn.domain.server.PinnedSubscription;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * API contract for {@link PinnedSubscriptionHandler}.
 */
@Tag(name = "subscriptionmatching.pinnedsubscription",
     description = "Provides the namespace for operations on Pinned Subscriptions")
public interface PinnedSubscriptionHandlerApi {

    /**
     * Lists all pinned subscriptions.
     *
     * @param loggedInUser the current user
     * @return the list of pinned subscriptions
     */
    @ApiEndpointDoc(
        summary = "Lists all PinnedSubscriptions",
        responseClass = PinnedSubscriptionListResponse.class
    )
    List<PinnedSubscription> list(User loggedInUser);

    /**
     * Creates a pinned subscription based on the given subscription and system.
     *
     * @param loggedInUser the current user
     * @param subscriptionId the id of the subscription
     * @param sid the id of the system
     * @return the created pinned subscription
     */
    @ApiEndpointDoc(
        summary = "Creates a Pinned Subscription based on given subscription and system",
        requestClass = CreatePinnedSubscriptionRequest.class,
        responseClass = PinnedSubscriptionResponse.class
    )
    PinnedSubscription create(User loggedInUser, Integer subscriptionId, Integer sid);

    /**
     * Deletes the pinned subscription with the given id.
     *
     * @param loggedInUser the current user
     * @param subscriptionId the id of the pinned subscription to delete
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Deletes Pinned Subscription with given id",
        requestClass = DeletePinnedSubscriptionRequest.class,
        isIntegerResponse = true
    )
    int delete(User loggedInUser, Integer subscriptionId);

    @Schema(name = "CreatePinnedSubscriptionRequest")
    @JsonPropertyOrder({"subscriptionId", "sid"})
    interface CreatePinnedSubscriptionRequest {

        /**
         * @return the id of the subscription
         */
        @Schema(description = "Subscription ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSubscriptionId();

        /**
         * @return the id of the system
         */
        @Schema(description = "System ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();
    }

    @Schema(name = "DeletePinnedSubscriptionRequest")
    interface DeletePinnedSubscriptionRequest {

        /**
         * @return the id of the pinned subscription to delete
         */
        @Schema(description = "Pinned Subscription ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSubscriptionId();
    }

    @Schema(name = "PinnedSubscription", description = "pinned subscription")
    @JsonPropertyOrder({"id", "subscriptionId", "systemId"})
    interface PinnedSubscriptionDoc {

        /**
         * @return the pinned subscription id
         */
        @Schema(description = "pinned subscription id", requiredMode = Schema.RequiredMode.REQUIRED)
        Long getId();

        /**
         * @return the subscription id
         */
        @Schema(name = "subscription_id", description = "subscription id",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long getSubscriptionId();

        /**
         * @return the system id
         */
        @Schema(name = "system_id", description = "system id",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long getSystemId();
    }

    @Schema(name = "ApiResponsePinnedSubscription")
    interface PinnedSubscriptionResponse extends ApiResponseWrapper<PinnedSubscriptionDoc> { }

    @Schema(name = "ApiResponsePinnedSubscriptionList")
    interface PinnedSubscriptionListResponse extends ApiResponseWrapper<List<PinnedSubscriptionDoc>> { }
}
