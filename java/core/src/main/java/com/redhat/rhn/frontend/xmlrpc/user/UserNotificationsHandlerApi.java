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
package com.redhat.rhn.frontend.xmlrpc.user;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.redhat.rhn.domain.notification.UserNotification;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link UserNotificationsHandler}.
 */
@Tag(name = "user.notifications", description = "Provides methods to manage user notifications.")
public interface UserNotificationsHandlerApi {

    /**
     * Lists the notifications of the current user.
     *
     * @param loggedInUser the current user
     * @param unread whether only unread notifications are returned
     * @return the notifications
     */
    @ApiEndpointDoc(
        summary = "Get all notifications from a user.",
        method = HttpMethod.get,
        responseClass = UserNotificationListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "notification")
    )
    List<UserNotification> getNotifications(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "unread", description = "If true, return only unread notifications.",
            in = ParameterIn.QUERY, required = true) boolean unread
    );

    /**
     * Marks the given notifications as read.
     *
     * @param loggedInUser the current user
     * @param notifications the notification identifiers
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set notifications of the given user as read",
        requestClass = SetNotificationsReadRequest.class,
        isIntegerResponse = true
    )
    int setNotificationsRead(User loggedInUser, List<Integer> notifications);

    /**
     * Marks every notification of the current user as read.
     *
     * @param loggedInUser the current user
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set all notifications from a user as read",
        isIntegerResponse = true
    )
    int setAllNotificationsRead(User loggedInUser);

    /**
     * Deletes the given notifications.
     *
     * @param loggedInUser the current user
     * @param notifications the notification identifiers
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Deletes multiple notifications",
        requestClass = DeleteNotificationsRequest.class,
        isIntegerResponse = true
    )
    int deleteNotifications(User loggedInUser, List<Integer> notifications);

    @Schema(name = "SetNotificationsReadRequest")
    interface SetNotificationsReadRequest {

        /**
         * @return the notification identifiers
         */
        @Schema(description = "The list of notification IDs to set as 'read'.", requiredMode = REQUIRED)
        List<Integer> getNotifications();
    }

    @Schema(name = "DeleteNotificationsRequest")
    interface DeleteNotificationsRequest {

        /**
         * @return the notification identifiers
         */
        @Schema(description = "The list of notification IDs to delete.", requiredMode = REQUIRED)
        List<Integer> getNotifications();
    }

    @Schema(name = "UserNotificationDoc", description = "notification")
    @JsonPropertyOrder({"id", "read", "message", "summary", "details", "type", "created"})
    interface UserNotificationDoc {

        /**
         * @return the notification identifier
         */
        @Schema(requiredMode = REQUIRED)
        @LegacyDocResponse(type = "long")
        Long getId();

        /**
         * @return whether the notification has been read
         */
        @Schema(requiredMode = REQUIRED)
        Boolean getRead();

        /**
         * @return the notification message
         */
        @Schema(requiredMode = REQUIRED)
        String getMessage();

        /**
         * @return the notification summary
         */
        @Schema(requiredMode = REQUIRED)
        String getSummary();

        /**
         * @return the notification details
         */
        @Schema(requiredMode = REQUIRED)
        String getDetails();

        /**
         * @return the notification type
         */
        @Schema(requiredMode = REQUIRED)
        @LegacyDocResponse(type = "notificationType")
        String getType();

        /**
         * @return the date the notification was created
         */
        @Schema(requiredMode = REQUIRED)
        @LegacyDocResponse(type = "date")
        Date getCreated();
    }

    @Schema(name = "ApiResponseUserNotificationList")
    interface UserNotificationListResponse extends ApiResponseWrapper<List<UserNotificationDoc>> { }
}
