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
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.virtualization.BaseVirtualizationAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationCreateAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationCreateActionDiskDetails;
import com.redhat.rhn.domain.action.virtualization.VirtualizationCreateActionInterfaceDetails;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
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
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.virtualization.DomainCapabilitiesJson;
import com.suse.manager.virtualization.GuestDefinition;
import com.suse.manager.virtualization.HostCapabilitiesJson;
import com.suse.manager.webui.errors.NotFoundException;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.utils.MinionActionUtils;
import com.suse.manager.webui.utils.WebSockifyTokenBuilder;
import com.suse.manager.webui.utils.gson.VirtualGuestSetterActionJson;
import com.suse.manager.webui.utils.gson.VirtualGuestsBaseActionJson;
import com.suse.manager.webui.utils.gson.VirtualGuestsUpdateActionJson;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.jose4j.lang.JoseException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing backend for Virtual Guests list page
 */
public class VirtualGuestsController {

    private static final Logger LOG = Logger.getLogger(VirtualGuestsController.class);

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();

    private final VirtManager virtManager;

    /**
     * @param virtManagerIn instance to manage virtualization
     */
    public VirtualGuestsController(VirtManager virtManagerIn) {
        this.virtManager = virtManagerIn;
    }

    /**
     * Initialize request routes for the pages served by VirtualGuestsController
     *
     * @param jade jade engine
     */
    public void initRoutes(JadeTemplateEngine jade) {
        get("/manager/systems/details/virtualization/guests/:sid",
                withUserPreferences(withCsrfToken(withUser(this::show))), jade);
        get("/manager/systems/details/virtualization/guests/:sid/edit/:guestuuid",
                withUserPreferences(withCsrfToken(withUser(this::edit))), jade);
        get("/manager/systems/details/virtualization/guests/:sid/new",
                withUserPreferences(withCsrfToken(withUser(this::create))), jade);
        get("/manager/systems/details/virtualization/guests/:sid/console/:guestuuid",
                withUserPreferences(withCsrfToken(withUser(this::console))), jade);
        get("/manager/api/systems/details/virtualization/guests/:sid/data",
                withUser(this::data));
        post("/manager/api/systems/details/virtualization/guests/:sid/:action",
                withUser(this::action));
        get("/manager/api/systems/details/virtualization/guests/:sid/guest/:uuid",
                withUser(this::getGuest));
        get("/manager/api/systems/details/virtualization/guests/:sid/domains_capabilities",
                withUser(this::getDomainsCapabilities));
    }

    /**
     * Displays the virtual guests page.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public ModelAndView show(Request request, Response response, User user) {
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
        data.put("foreign_entitled", server.hasEntitlement(EntitlementManager.FOREIGN));
        data.put("is_admin", user.hasRole(RoleFactory.ORG_ADMIN));

        return new ModelAndView(data, "templates/virtualization/guests/show.jade");
    }

    /**
     * Returns JSON data describing the virtual guests
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public String data(Request request, Response response, User user) {
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
    public String getGuest(Request request, Response response, User user) {
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

        GuestDefinition definition = virtManager.getGuestDefinition(host.asMinionServer().get().getMinionId(),
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
    public String getDomainsCapabilities(Request request, Response response, User user) {
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

        Map<String, JsonElement> capabilities = virtManager.getCapabilities(minionId)
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
    public String action(Request request, Response response, User user) {
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
        actionsMap.put("new", VirtualGuestsUpdateActionJson.class);

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
        if (data.getUuids() == null || data.getUuids().isEmpty()) {
            String result = triggerGuestUpdateSaltAction(host, null, user, (VirtualGuestsUpdateActionJson)data);
            actionResults.put("create-guest", result != null ? result : "Failed");
        }
        else {
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
                            Map<String, String> context = new HashMap<>();
                            if (data.getForce() != null) {
                                context.put(BaseVirtualizationAction.FORCE_STRING, Boolean.toString(data.getForce()));
                            }
                            result = triggerGuestAction(host, guest, type, user, context);
                        }
                    }
                    String status = result != null ? result : "Failed";
                    actionResults.put(guest.getUuid(), status);
                }
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
    public ModelAndView edit(Request request, Response response, User user) {
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
        MinionController.addActionChains(user, data);
        data.put("guestUuid", guestUuid);
        data.put("isSalt", host.hasEntitlement(EntitlementManager.SALT));
        return new ModelAndView(data, "templates/virtualization/guests/edit.jade");
    }

    /**
     * Display the New Virtual Guest page
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public ModelAndView create(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();

        Long hostId = Long.parseLong(request.params("sid"));
        Server host = SystemManager.lookupByIdAndUser(hostId, user);

        if (!host.hasEntitlement(EntitlementManager.SALT)) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Only for Salt-managed virtual hosts");
        }

        /* For system-common.jade */
        data.put("server", host);
        data.put("inSSM", RhnSetDecl.SYSTEMS.get(user).contains(hostId));

        /* For the rest of the template */
        MinionController.addActionChains(user, data);
        data.put("isSalt", host.hasEntitlement(EntitlementManager.SALT));

        return new ModelAndView(data, "templates/virtualization/guests/create.jade");
    }

    /**
     * Display the console page of a virtual guest
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public ModelAndView console(Request request, Response response, User user) {
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

        VirtualInstance guest = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByHostIdAndUuid(hostId, guestUuid);
        Server host = guest.getHostSystem();

        String minionId = host.asMinionServer().orElseThrow(() -> Spark.halt(HttpStatus.SC_BAD_REQUEST)).getMinionId();
        GuestDefinition def = virtManager.getGuestDefinition(minionId, guest.getName()).
                orElseThrow(() -> Spark.halt(HttpStatus.SC_BAD_REQUEST));
        String hostname = host.getName();
        Integer port = def.getGraphics().getPort();

        /* For system-common.jade */
        data.put("server", host);
        data.put("inSSM", RhnSetDecl.SYSTEMS.get(user).contains(hostId));

        /* For the rest of the template */
        data.put("guestUuid", guestUuid);
        data.put("guestName", guest.getName());
        data.put("graphicsType", def.getGraphics().getType());

        String url = null;
        if (Arrays.asList("spice", "vnc").contains(def.getGraphics().getType())) {
            try {
                WebSockifyTokenBuilder tokenBuilder = new WebSockifyTokenBuilder(hostname, port);
                tokenBuilder.useServerSecret();
                url = "wss://" + ConfigDefaults.get().getHostname() +
                        "/rhn/websockify/?token=" + tokenBuilder.getToken();
            }
            catch (JoseException e) {
                LOG.error(e);
                Spark.halt(HttpStatus.SC_SERVICE_UNAVAILABLE);
            }
        }
        data.put("socketUrl", url);

        return new ModelAndView(data, "templates/virtualization/guests/console.jade");
    }

    private String triggerGuestUpdateAction(Server host,
                                                   VirtualSystemOverview guest,
                                                   User user,
                                                   VirtualGuestsUpdateActionJson data) {
        if (host.asMinionServer().isPresent()) {
            return triggerGuestUpdateSaltAction(host, guest, user, data);
        }

        if (data.getUuids().isEmpty()) {
            LOG.error("Creating a virtual machine is only possible for salt minions not for " + host.getHostname());
            return null;
        }

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

    private String triggerGuestUpdateSaltAction(Server host,
                                                       VirtualSystemOverview guest,
                                                       User user,
                                                       VirtualGuestsUpdateActionJson data) {
        String status = null;
        Map<String, Object> context = new HashMap<String, Object>();

        // So far the salt virt.update function doesn't allow renaming a guest,
        // and that is only possible for the KVM driver.
        data.setName(guest != null ? guest.getName() : data.getName());

        context.put(VirtualizationCreateAction.TYPE, data.getType());
        context.put(VirtualizationCreateAction.NAME, data.getName());
        context.put(VirtualizationCreateAction.OS_TYPE, data.getOsType());
        context.put(VirtualizationCreateAction.MEMORY, data.getMemory());
        context.put(VirtualizationCreateAction.VCPUS, data.getVcpu());
        context.put(VirtualizationCreateAction.ARCH, data.getArch());
        context.put(VirtualizationCreateAction.GRAPHICS, data.getGraphicsType());

        if (data.getDisks() != null) {
            context.put(VirtualizationCreateAction.DISKS, data.getDisks().stream().map(disk -> {
                VirtualizationCreateActionDiskDetails details = new VirtualizationCreateActionDiskDetails();
                details.setType(disk.getType());
                details.setDevice(disk.getDevice());
                details.setTemplate(disk.getTemplate());
                details.setSize(disk.getSize());
                details.setBus(disk.getBus());
                details.setPool(disk.getPool());
                details.setSourceFile(disk.getSourceFile());
                return details;
            }).collect(Collectors.toList()));
        }

        if (data.getInterfaces() != null) {
            context.put(VirtualizationCreateAction.INTERFACES, data.getInterfaces().stream().map(nic -> {
                VirtualizationCreateActionInterfaceDetails details = new VirtualizationCreateActionInterfaceDetails();
                details.setType(nic.getType());
                details.setSource(nic.getSource());
                details.setMac(nic.getMac());
                return details;
            }).collect(Collectors.toList()));
        }

        Optional<ActionChain> actionChain = data.getActionChain()
                .filter(StringUtils::isNotEmpty)
                .map(label -> ActionChainFactory.getOrCreateActionChain(label, user));

        VirtualizationActionCommand cmd
            = new VirtualizationActionCommand(user,
                                              MinionActionUtils.getScheduleDate(data.getEarliest()),
                                              actionChain,
                                              ActionFactory.TYPE_VIRTUALIZATION_CREATE,
                                              host,
                                              guest != null ? guest.getUuid() : null,
                                              data.getName(),
                                              context);
        try {
            cmd.store();
            status = String.valueOf(cmd.getAction().getId());
        }
        catch (TaskomaticApiException e) {
            LOG.error("Could not schedule virtualization action:");
            LOG.error(e);
            return null;
        }
        return status;
    }

    private String triggerGuestSetterAction(Server host,
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

    private String triggerGuestAction(Server host,
                                             VirtualSystemOverview guest,
                                             ActionType actionType,
                                             User user,
                                             Map<String, String> context) {
        if (host.hasEntitlement(EntitlementManager.FOREIGN)) {
            LOG.warn("Foreign systems don't support virtual guest actions");
            return null;
        }
        // Traditionally registered systems aren't able to really delete the VM: fail
        // the delete action for them
        if (host.hasEntitlement(EntitlementManager.MANAGEMENT) &&
                actionType.equals(ActionFactory.TYPE_VIRTUALIZATION_DELETE)) {
            LOG.warn("Traditional systems don't support virtual guests deletion");
            return null;
        }

        VirtualizationActionCommand cmd
            = new VirtualizationActionCommand(user, new Date(), null, actionType, host, guest.getUuid(),
                                              guest.getName(), context);
        Long actionId;
        try {
            cmd.store();
            actionId = cmd.getAction().getId();
        }
        catch (TaskomaticApiException e) {
            LOG.error("Could not schedule virtualization action:");
            LOG.error(e);
            return null;
        }

        return String.valueOf(actionId);
    }
}
