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
package com.suse.manager.xmlrpc.maintenance;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;
import com.suse.manager.maintenance.rescheduling.RescheduleResult;
import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * API contract for {@link MaintenanceHandler}.
 */
@Tag(name = "maintenance", description = "Provides methods to access and modify Maintenance Schedules related " +
        "entities")
public interface MaintenanceHandlerApi {

    /**
     * Lists the schedule names visible to the user.
     *
     * @param loggedInUser the current user
     * @return the maintenance schedule names
     */
    @ApiEndpointDoc(
        summary = "List Schedule Names visible to user",
        responseClass = StringListResponse.class,
        responseDescription = "maintenance schedule names"
    )
    List<String> listScheduleNames(User loggedInUser);

    /**
     * Looks up a maintenance schedule.
     *
     * @param loggedInUser the current user
     * @param name the maintenance schedule name
     * @return the maintenance schedule
     */
    @ApiEndpointDoc(
        summary = "Lookup a specific maintenance schedule",
        requestClass = ScheduleNameRequest.class,
        responseClass = MaintenanceScheduleResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "maintenance schedule information")
    )
    MaintenanceSchedule getScheduleDetails(User loggedInUser, String name);

    /**
     * Creates a maintenance schedule.
     *
     * @param loggedInUser the current user
     * @param name the maintenance schedule name
     * @param type the schedule type
     * @param calendar the maintenance calendar label
     * @return the created maintenance schedule
     */
    @ApiEndpointDoc(
        summary = "Create a new maintenance Schedule",
        requestClass = CreateScheduleRequest.class,
        responseClass = MaintenanceScheduleResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "maintenance schedule information")
    )
    MaintenanceSchedule createSchedule(User loggedInUser, String name, String type, String calendar);

    /**
     * Updates a maintenance schedule.
     *
     * @param loggedInUser the current user
     * @param name the maintenance schedule name
     * @param details the values to update
     * @param rescheduleStrategy the strategy module names
     * @return the reschedule result
     */
    @ApiEndpointDoc(
        summary = "Update a maintenance schedule",
        requestClass = UpdateScheduleRequest.class,
        responseClass = RescheduleResultResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "reschedule information")
    )
    RescheduleResult updateSchedule(User loggedInUser, String name, Map<String, String> details,
        List<String> rescheduleStrategy);

    /**
     * Removes a maintenance schedule.
     *
     * @param loggedInUser the current user
     * @param name the maintenance schedule name
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Remove a maintenance schedule",
        requestClass = ScheduleNameRequest.class,
        isIntegerResponse = true
    )
    int deleteSchedule(User loggedInUser, String name);

    /**
     * Lists the calendar labels visible to the user.
     *
     * @param loggedInUser the current user
     * @return the maintenance calendar labels
     */
    @ApiEndpointDoc(
        summary = "List schedule names visible to user",
        responseClass = StringListResponse.class,
        responseDescription = "maintenance calendar labels"
    )
    List<String> listCalendarLabels(User loggedInUser);

    /**
     * Looks up a maintenance calendar.
     *
     * @param loggedInUser the current user
     * @param label the maintenance calendar label
     * @return the maintenance calendar
     */
    @ApiEndpointDoc(
        summary = "Lookup a specific maintenance schedule",
        requestClass = CalendarLabelRequest.class,
        responseClass = MaintenanceCalendarResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "maintenance calendar information")
    )
    MaintenanceCalendar getCalendarDetails(User loggedInUser, String label);

    /**
     * Creates a maintenance calendar.
     *
     * @param loggedInUser the current user
     * @param label the maintenance calendar label
     * @param ical the ICal calendar data
     * @return the created maintenance calendar
     */
    @ApiEndpointDoc(
        summary = "Create a new maintenance calendar",
        requestClass = CreateCalendarRequest.class,
        responseClass = MaintenanceCalendarResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "maintenance calendar information")
    )
    MaintenanceCalendar createCalendar(User loggedInUser, String label, String ical);

    /**
     * Creates a maintenance calendar from a URL.
     *
     * @param loggedInUser the current user
     * @param label the maintenance calendar label
     * @param url the download URL of the ICal calendar data
     * @return the created maintenance calendar
     */
    @ApiEndpointDoc(
        summary = "Create a new maintenance calendar",
        requestClass = CreateCalendarWithUrlRequest.class,
        responseClass = MaintenanceCalendarResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "maintenance calendar information")
    )
    MaintenanceCalendar createCalendarWithUrl(User loggedInUser, String label, String url);

    /**
     * Updates a maintenance calendar.
     *
     * @param loggedInUser the current user
     * @param label the maintenance calendar label
     * @param details the values to update
     * @param rescheduleStrategy the strategy module names
     * @return the reschedule results
     */
    @ApiEndpointDoc(
        summary = "Update a maintenance calendar",
        requestClass = UpdateCalendarRequest.class,
        responseClass = RescheduleResultListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "reschedule information")
    )
    List<RescheduleResult> updateCalendar(User loggedInUser, String label, Map<String, String> details,
        List<String> rescheduleStrategy);

    /**
     * Refreshes a maintenance calendar from its configured URL.
     *
     * @param loggedInUser the current user
     * @param label the maintenance calendar label
     * @param rescheduleStrategy the strategy module names
     * @return the reschedule results
     */
    @ApiEndpointDoc(
        summary = "Refresh maintenance calendar data using the configured URL",
        requestClass = RefreshCalendarRequest.class,
        responseClass = RescheduleResultListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "reschedule information")
    )
    List<RescheduleResult> refreshCalendar(User loggedInUser, String label, List<String> rescheduleStrategy);

    /**
     * Removes a maintenance calendar.
     *
     * @param loggedInUser the current user
     * @param label the maintenance calendar label
     * @param cancelScheduledActions whether the actions of affected schedules are cancelled
     * @return the reschedule results
     */
    @ApiEndpointDoc(
        summary = "Remove a maintenance calendar",
        requestClass = DeleteCalendarRequest.class,
        responseClass = RescheduleResultListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "reschedule information")
    )
    List<RescheduleResult> deleteCalendar(User loggedInUser, String label, boolean cancelScheduledActions);

    /**
     * Assigns a schedule to the given systems.
     *
     * @param loggedInUser the current user
     * @param scheduleName the schedule name
     * @param sids the system IDs
     * @param rescheduleStrategy the strategy module names
     * @return the number of involved systems
     */
    @ApiEndpointDoc(
        summary = "Assign schedule with given name to systems with given IDs.",
        requestClass = AssignScheduleRequest.class,
        responseClass = IntegerListResponse.class,
        responseDescription = "number of involved systems"
    )
    Integer assignScheduleToSystems(User loggedInUser, String scheduleName, List<Integer> sids,
        List<String> rescheduleStrategy);

    /**
     * Retracts a schedule from the given systems.
     *
     * @param loggedInUser the current user
     * @param sids the system IDs
     * @return the number of involved systems
     */
    @ApiEndpointDoc(
        summary = "Retract schedule with given name from systems with given IDs",
        requestClass = SystemIdsRequest.class,
        responseClass = IntegerListResponse.class,
        responseDescription = "number of involved systems"
    )
    Integer retractScheduleFromSystems(User loggedInUser, List<Integer> sids);

    /**
     * Lists the systems that have the given schedule assigned.
     *
     * @param loggedInUser the current user
     * @param scheduleName the schedule name
     * @return the system IDs
     */
    @ApiEndpointDoc(
        summary = "List IDs of systems that have given schedule assigned",
        requestClass = ScheduleNameOnlyRequest.class,
        responseClass = IntegerListResponse.class,
        responseDescription = "system IDs"
    )
    List<Long> listSystemsWithSchedule(User loggedInUser, String scheduleName);

    @Schema(name = "ApiResponseStringList")
    interface StringListResponse extends ApiResponseWrapper<List<String>> { }

    @Schema(name = "ApiResponseIntegerList")
    interface IntegerListResponse extends ApiResponseWrapper<List<Integer>> { }

    @Schema(name = "ApiResponseMaintenanceSchedule")
    interface MaintenanceScheduleResponse extends ApiResponseWrapper<MaintenanceScheduleDoc> { }

    @Schema(name = "ApiResponseMaintenanceCalendar")
    interface MaintenanceCalendarResponse extends ApiResponseWrapper<MaintenanceCalendarDoc> { }

    @Schema(name = "ApiResponseRescheduleResult")
    interface RescheduleResultResponse extends ApiResponseWrapper<RescheduleResultDoc> { }

    @Schema(name = "ApiResponseRescheduleResultList")
    interface RescheduleResultListResponse extends ApiResponseWrapper<List<RescheduleResultDoc>> { }

    @Schema(name = "MaintenanceScheduleNameRequest")
    interface ScheduleNameRequest {

        /**
         * @return the maintenance schedule name
         */
        @Schema(description = "maintenance Schedule Name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();
    }

    @Schema(name = "MaintenanceScheduleNameOnlyRequest")
    interface ScheduleNameOnlyRequest {

        /**
         * @return the schedule name
         */
        @Schema(description = "the schedule name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getScheduleName();
    }

    @Schema(name = "MaintenanceCalendarLabelRequest")
    interface CalendarLabelRequest {

        /**
         * @return the maintenance calendar label
         */
        @Schema(description = "maintenance calendar label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();
    }

    @Schema(name = "MaintenanceCreateScheduleRequest")
    @JsonPropertyOrder({"name", "type", "calendar"})
    interface CreateScheduleRequest {

        /**
         * @return the maintenance schedule name
         */
        @Schema(description = "maintenance schedule name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the schedule type
         */
        @Schema(description = "schedule type: single, multi", requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * The longer handler overload adds the calendar label, so it is documented as optional.
         *
         * @return the maintenance calendar label
         */
        @Schema(description = "maintenance calendar label", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getCalendar();
    }

    @Schema(name = "MaintenanceUpdateScheduleRequest")
    @JsonPropertyOrder({"name", "details", "rescheduleStrategy"})
    interface UpdateScheduleRequest {

        /**
         * @return the maintenance schedule name
         */
        @Schema(description = "maintenance schedule name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the values to update
         */
        @Schema(description = "maintenance schedule details", requiredMode = Schema.RequiredMode.REQUIRED)
        ScheduleDetailsDoc getDetails();

        /**
         * @return the strategy module names
         */
        @ArraySchema(schema = @Schema(allowableValues = {"Cancel", "Fail"},
            extensions = @Extension(name = "x-uyuni-doc-option-descriptions", properties = {
                @ExtensionProperty(name = "Cancel",
                    value = "cancel actions which are outside the maintenance windows"),
                @ExtensionProperty(name = "Fail", value = "let update fail. The calendar stays untouched")
            })),
            arraySchema = @Schema(description = "available:", requiredMode = Schema.RequiredMode.REQUIRED))
        List<String> getRescheduleStrategy();
    }

    @Schema(name = "MaintenanceCreateCalendarRequest")
    @JsonPropertyOrder({"label", "ical"})
    interface CreateCalendarRequest {

        /**
         * @return the maintenance calendar label
         */
        @Schema(description = "maintenance calendar label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the ICal calendar data
         */
        @Schema(description = "ICal calendar data", requiredMode = Schema.RequiredMode.REQUIRED)
        String getIcal();
    }

    @Schema(name = "MaintenanceCreateCalendarWithUrlRequest")
    @JsonPropertyOrder({"label", "url"})
    interface CreateCalendarWithUrlRequest {

        /**
         * @return the maintenance calendar label
         */
        @Schema(description = "maintenance calendar label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the download URL of the ICal calendar data
         */
        @Schema(description = "download URL for ICal calendar data", requiredMode = Schema.RequiredMode.REQUIRED)
        String getUrl();
    }

    @Schema(name = "MaintenanceUpdateCalendarRequest")
    @JsonPropertyOrder({"label", "details", "rescheduleStrategy"})
    interface UpdateCalendarRequest {

        /**
         * @return the maintenance calendar label
         */
        @Schema(description = "maintenance calendar label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the values to update
         */
        @Schema(description = "maintenance calendar details", requiredMode = Schema.RequiredMode.REQUIRED)
        CalendarDetailsDoc getDetails();

        /**
         * @return the strategy module names
         */
        @ArraySchema(schema = @Schema(allowableValues = {"Cancel", "Fail"},
            extensions = @Extension(name = "x-uyuni-doc-option-descriptions", properties = {
                @ExtensionProperty(name = "Cancel",
                    value = "cancel actions which are outside the maintenance windows"),
                @ExtensionProperty(name = "Fail", value = "let update fail. The calendar stay untouched")
            })),
            arraySchema = @Schema(description = "available:", requiredMode = Schema.RequiredMode.REQUIRED))
        List<String> getRescheduleStrategy();
    }

    @Schema(name = "MaintenanceRefreshCalendarRequest")
    @JsonPropertyOrder({"label", "rescheduleStrategy"})
    interface RefreshCalendarRequest {

        /**
         * @return the maintenance calendar label
         */
        @Schema(description = "maintenance calendar label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the strategy module names
         */
        @ArraySchema(schema = @Schema(allowableValues = {"Cancel", "Fail"},
            extensions = @Extension(name = "x-uyuni-doc-option-descriptions", properties = {
                @ExtensionProperty(name = "Cancel",
                    value = "cancel actions which are outside the maintenance windows"),
                @ExtensionProperty(name = "Fail", value = "let update fail. The calendar stay untouched")
            })),
            arraySchema = @Schema(description = "available:", requiredMode = Schema.RequiredMode.REQUIRED))
        List<String> getRescheduleStrategy();
    }

    @Schema(name = "MaintenanceDeleteCalendarRequest")
    @JsonPropertyOrder({"label", "cancelScheduledActions"})
    interface DeleteCalendarRequest {

        /**
         * @return the maintenance calendar label
         */
        @Schema(description = "maintenance calendar label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return whether the actions of affected schedules are cancelled
         */
        @Schema(description = "cancel actions of affected schedules", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getCancelScheduledActions();
    }

    @Schema(name = "MaintenanceAssignScheduleRequest")
    @JsonPropertyOrder({"scheduleName", "sids", "rescheduleStrategy"})
    interface AssignScheduleRequest {

        /**
         * @return the schedule name
         */
        @Schema(description = "The schedule name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getScheduleName();

        /**
         * @return the system IDs
         */
        @Schema(description = "system IDs", requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSids();

        /**
         * @return the strategy module names
         */
        @ArraySchema(schema = @Schema(allowableValues = {"Cancel", "Fail"},
            extensions = @Extension(name = "x-uyuni-doc-option-descriptions", properties = {
                @ExtensionProperty(name = "Cancel",
                    value = "cancel actions which are outside the maintenance windows"),
                @ExtensionProperty(name = "Fail", value = "let assignment fail. No operation will be performed")
            })),
            arraySchema = @Schema(description = "available:", requiredMode = Schema.RequiredMode.REQUIRED))
        List<String> getRescheduleStrategy();
    }

    @Schema(name = "MaintenanceSystemIdsRequest")
    interface SystemIdsRequest {

        /**
         * @return the system IDs
         */
        @Schema(description = "system IDs", requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSids();
    }

    @Schema(name = "MaintenanceScheduleDetails")
    @JsonPropertyOrder({"type", "calendar"})
    interface ScheduleDetailsDoc {

        /**
         * @return the new schedule type
         */
        @Schema(description = "new schedule type", allowableValues = {"single", "multi"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return the new calendar label
         */
        @Schema(description = "new calendar label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getCalendar();
    }

    @Schema(name = "MaintenanceCalendarDetails")
    @JsonPropertyOrder({"ical", "url"})
    interface CalendarDetailsDoc {

        /**
         * @return the new ical calendar data
         */
        @Schema(description = "new ical calendar data", requiredMode = Schema.RequiredMode.REQUIRED)
        String getIcal();

        /**
         * @return the new calendar URL
         */
        @Schema(description = "new calendar URL", requiredMode = Schema.RequiredMode.REQUIRED)
        String getUrl();
    }

    @Schema(name = "MaintenanceScheduleInformation")
    @JsonPropertyOrder({"id", "orgId", "name", "type", "calendar"})
    interface MaintenanceScheduleDoc {

        /**
         * @return the ID of the schedule
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the ID of the organization
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getOrgId();

        /**
         * @return the name of the schedule
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the type of the schedule
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * The doclet embeds the calendar serializer directly, so the property carries the
         * calendar struct label rather than a property name of its own.
         *
         * @return the maintenance calendar of the schedule
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "maintenance calendar information")
        MaintenanceCalendarDoc getCalendar();
    }

    @Schema(name = "MaintenanceCalendarInformation")
    @JsonPropertyOrder({"id", "orgId", "label", "url", "ical"})
    interface MaintenanceCalendarDoc {

        /**
         * @return the ID of the calendar
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the ID of the organization
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getOrgId();

        /**
         * @return the label of the calendar
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the URL of the calendar
         */
        @Schema(description = "calendar url if present", requiredMode = Schema.RequiredMode.REQUIRED)
        String getUrl();

        /**
         * @return the ICal data of the calendar
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getIcal();
    }

    @Schema(name = "RescheduleInformation")
    @JsonPropertyOrder({"strategy", "forScheduleName", "status", "message", "actions"})
    interface RescheduleResultDoc {

        /**
         * @return the selected strategy
         */
        @Schema(description = "selected strategy", requiredMode = Schema.RequiredMode.REQUIRED)
        String getStrategy();

        /**
         * @return the schedule the actions belong to
         */
        @Schema(name = "for_schedule_name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getForScheduleName();

        /**
         * @return the status of the reschedule
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getStatus();

        /**
         * @return the message of the reschedule
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getMessage();

        /**
         * @return the affected actions
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "action information")
        List<RescheduleActionDoc> getActions();
    }

    @Schema(name = "RescheduleActionInformation")
    @JsonPropertyOrder({"id", "name", "type", "scheduler", "earliest", "prerequisite", "affectedSystemIds",
        "details"})
    interface RescheduleActionDoc {

        /**
         * @return the ID of the action
         */
        @Schema(description = "action ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the name of the action
         */
        @Schema(description = "action name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the type of the action
         */
        @Schema(description = "action type", requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return the user that scheduled the action
         */
        @Schema(description = "the user that scheduled the action (optional)",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getScheduler();

        /**
         * @return the earliest date and time the action will be performed
         */
        @Schema(description = "the earliest date and time the action will be performed",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliest();

        /**
         * @return the ID of the prerequisite action
         */
        @Schema(description = "ID of the prerequisite action (optional)",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer getPrerequisite();

        /**
         * @return the affected system IDs
         */
        @Schema(name = "affected_system_ids", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "affected system IDs")
        List<Integer> getAffectedSystemIds();

        /**
         * @return the details of the action
         */
        @Schema(description = "action details string", requiredMode = Schema.RequiredMode.REQUIRED)
        String getDetails();
    }
}
