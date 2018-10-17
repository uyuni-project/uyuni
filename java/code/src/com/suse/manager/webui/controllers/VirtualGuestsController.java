/**
 * Copyright (c) 2018 SUSE LLC
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
package com.suse.manager.webui.controllers;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.VirtualSystemOverview;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.VirtualizationActionCommand;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.virtualization.DomainCapabilitiesJson;
import com.suse.manager.virtualization.GuestDefinition;
import com.suse.manager.virtualization.HostCapabilitiesJson;
import com.suse.manager.virtualization.VirtManager;
import com.suse.manager.webui.errors.NotFoundException;
import com.suse.manager.webui.utils.gson.VirtualGuestSetterActionJson;
import com.suse.manager.webui.utils.gson.VirtualGuestsBaseActionJson;
import com.suse.manager.webui.utils.gson.VirtualGuestsUpdateActionJson;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * Controller class providing backend for Virtual Guests list page
 */
public class VirtualGuestsController {

    private static Logger log = Logger.getLogger(VirtualGuestsController.class);

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();

    private VirtualGuestsController() { }

    /**
     * Displays the virtual guests page.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView show(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        Long serverId;
        Server server;

        try {
            serverId = Long.parseLong(request.params("sid"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }

        try {
            server = SystemManager.lookupByIdAndUser(serverId, user);
        }
        catch (LookupException e) {
            throw new NotFoundException();
        }

        /* For system-common.jade */
        data.put("server", server);
        data.put("inSSM", RhnSetDecl.SYSTEMS.get(user).contains(serverId));

        /* For the rest of the template */
        data.put("salt_entitled", server.hasEntitlement(EntitlementManager.SALT));
        data.put("is_admin", user.hasRole(RoleFactory.ORG_ADMIN));

        return new ModelAndView(data, "virtualization/guests/show.jade");
    }

    /**
     * Returns JSON data describing the virtual guests
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public static String data(Request request, Response response, User user) {
        Long serverId;

        try {
            serverId = Long.parseLong(request.params("sid"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }

        DataResult<VirtualSystemOverview> data =
                SystemManager.virtualGuestsForHostList(user, serverId, null);
        data.elaborate();

        for (VirtualSystemOverview current : data) {
            current.setSystemId(current.getVirtualSystemId());

            if (current.getSystemId() != null) {
                current.updateStatusType(user);
            }
        }

        response.type("application/json");
        return GSON.toJson(data);
    }

    /**
     * Return the definition of the virtual machine
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the json response
     */
    public static String getGuest(Request request, Response response, User user) {
        Long serverId;
        try {
            serverId = Long.parseLong(request.params("sid"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }

        String uuid = request.params("uuid");
        Server host = SystemManager.lookupByIdAndUser(serverId, user);
        DataResult<VirtualSystemOverview> guests = SystemManager.virtualGuestsForHostList(user, serverId, null);
        VirtualSystemOverview guest = guests.stream().filter(vso -> vso.getUuid().equals(uuid))
                .findFirst().orElseThrow(() -> new NotFoundException());

        GuestDefinition definition = VirtManager.getGuestDefinition(host.asMinionServer().get().getMinionId(),
                guest.getName()).orElseThrow(() -> new NotFoundException());

        return json(response, definition);
    }

    /**
     * Return the list of all domain capabilities from a given salt virtual host
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the json response
     */
    public static String getDomainsCapabilities(Request request, Response response, User user) {
        Long serverId;
        try {
            serverId = Long.parseLong(request.params("sid"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }

        Server host = SystemManager.lookupByIdAndUser(serverId, user);
        String minionId = host.asMinionServer().orElseThrow(() ->
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Can only get capabilities of Salt system")).getMinionId();

        Map<String, JsonElement> capabilities = VirtManager.getCapabilities(minionId)
                .orElseThrow(() -> Spark.halt(HttpStatus.SC_BAD_REQUEST,
                        "Failed to get virtual host capabilities"));

        HostCapabilitiesJson hostCaps = GSON.fromJson(capabilities.get("host"),
                new TypeToken<HostCapabilitiesJson>() { }.getType());
        List<DomainCapabilitiesJson> domainsCaps = GSON.fromJson(capabilities.get("domains"),
                new TypeToken<List<DomainCapabilitiesJson>>() { }.getType());

        Map<String, Object> allDomainCaps = new HashMap<>();
        allDomainCaps.put("osTypes", hostCaps.getGuests().stream().map(guest -> guest.getOsType())
                .distinct()
                .collect(Collectors.toList()));
        allDomainCaps.put("domainsCaps", domainsCaps);

        return json(response, allDomainCaps);
    }

    /**
     * Run an action on one or more virtual machine
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the json response
     */
    public static String action(Request request, Response response, User user) {
        HashMap<String, Class<? extends VirtualGuestsBaseActionJson>> actionsMap = new HashMap<>();
        actionsMap.put("start", VirtualGuestsBaseActionJson.class);
        actionsMap.put("suspend", VirtualGuestsBaseActionJson.class);
        actionsMap.put("resume", VirtualGuestsBaseActionJson.class);
        actionsMap.put("restart", VirtualGuestsBaseActionJson.class);
        actionsMap.put("shutdown", VirtualGuestsBaseActionJson.class);
        actionsMap.put("setVcpu", VirtualGuestSetterActionJson.class);
        actionsMap.put("setMemory", VirtualGuestSetterActionJson.class);
        actionsMap.put("delete", VirtualGuestsBaseActionJson.class);
        actionsMap.put("update", VirtualGuestsUpdateActionJson.class);

        Long serverId;
        try {
            serverId = Long.parseLong(request.params("sid"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }

        String action = request.params("action");
        VirtualGuestsBaseActionJson data;
        try {
            data = GSON.fromJson(request.body(), actionsMap.get(action));
        }
        catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }

        Server host;
        try {
            host = SystemManager.lookupByIdAndUser(serverId, user);
        }
        catch (LookupException e) {
            throw new NotFoundException();
        }
        DataResult<VirtualSystemOverview> guests =
                SystemManager.virtualGuestsForHostList(user, serverId, null);

        HashMap<String, String> actionResults = new HashMap<>();
        for (VirtualSystemOverview guest : guests) {
            ActionType type = VirtualizationActionCommand.lookupActionType(guest.getStateLabel(), action);
            if (data.getUuids().contains(guest.getUuid())) {
                String result = null;
                if (data instanceof VirtualGuestsUpdateActionJson) {
                    result = triggerGuestUpdateAction(host, guest, user, (VirtualGuestsUpdateActionJson)data);
                }
                else if (type != null) {
                    if (data instanceof VirtualGuestSetterActionJson) {
                        result = triggerGuestSetterAction(host, guest, action, type, user,
                                                          ((VirtualGuestSetterActionJson)data).getValue());
                    }
                    else {
                        result = triggerGuestAction(host, guest, type, user, new HashMap<>());
                    }
                }
                String status = result != null ? result : "Failed";
                actionResults.put(guest.getUuid(), status);
            }
        }

        return json(response, actionResults);
    }

    /**
     * Display the edit page of a virtual guest
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView edit(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();

        Long hostId;
        try {
            hostId = Long.parseLong(request.params("sid"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }

        // Use uuids since the IDs may change
        String guestUuid = request.params("guestuuid");

        Server host;
        try {
            host = SystemManager.lookupByIdAndUser(hostId, user);
        }
        catch (LookupException e) {
            throw new NotFoundException();
        }
        DataResult<VirtualSystemOverview> guests =
                SystemManager.virtualGuestsForHostList(user, hostId, null);

        Optional<VirtualSystemOverview> guest = guests.stream().
                filter(item -> item.getUuid().equals(guestUuid)).findFirst();

        if (!guest.isPresent()) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }

        /* For system-common.jade */
        data.put("server", host);
        data.put("inSSM", RhnSetDecl.SYSTEMS.get(user).contains(hostId));

        /* For the rest of the template */
        data.put("guestUuid", guestUuid);
        data.put("isSalt", host.hasEntitlement(EntitlementManager.SALT));
        return new ModelAndView(data, "virtualization/guests/edit.jade");
    }

    private static String triggerGuestUpdateAction(Server host,
                                                   VirtualSystemOverview guest,
                                                   User user,
                                                   VirtualGuestsUpdateActionJson data) {
        List<String> results = new ArrayList<>();
        // Comparing against the DB data may not be accurate, but that will change with virt.running state soon
        if (data.getVcpu().longValue() != guest.getVcpus().longValue()) {
            results.add(triggerGuestSetterAction(host, guest, "setVcpu", ActionFactory.TYPE_VIRTUALIZATION_SET_VCPUS,
                                                 user, data.getVcpu()));
        }
        if (data.getMemory().longValue() != guest.getMemory().longValue()) {
            results.add(triggerGuestSetterAction(host, guest, "setMemory",
                                                 ActionFactory.TYPE_VIRTUALIZATION_SET_MEMORY,
                                                 user, data.getMemory()));
        }
        return GSON.toJson(results);
    }

    private static String triggerGuestSetterAction(Server host,
                                                   VirtualSystemOverview guest,
                                                   String actionName,
                                                   ActionType actionType,
                                                   User user,
                                                   Long value) {
        if (value == null) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
        HashMap<String, String> context = new HashMap<>();
        context.put(actionName, String.valueOf(value));

        return triggerGuestAction(host, guest, actionType, user, context);
    }

    private static String triggerGuestAction(Server host,
                                             VirtualSystemOverview guest,
                                             ActionType actionType,
                                             User user,
                                             Map<String, String> context) {
        // Traditionally registered systems aren't able to really delete the VM: fail
        // the delete action for them
        if (host.hasEntitlement(EntitlementManager.MANAGEMENT) &&
                actionType.equals(ActionFactory.TYPE_VIRTUALIZATION_DELETE)) {
            log.warn("Traditional systems don't support virtual guests deletion");
            return null;
        }

        VirtualizationActionCommand cmd
            = new VirtualizationActionCommand(user, new Date(), actionType, host, guest.getUuid(), context);
        Long actionId;
        try {
            cmd.store();
            actionId = cmd.getAction().getId();
        }
        catch (TaskomaticApiException e) {
            log.error("Could not schedule virtualization action:");
            log.error(e);
            return null;
        }

        return String.valueOf(actionId);
    }
}
