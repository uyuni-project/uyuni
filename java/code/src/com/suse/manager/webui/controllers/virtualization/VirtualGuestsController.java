/*
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
package com.suse.manager.webui.controllers.virtualization;

import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withDocsLocale;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserAndServer;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.virtualization.VirtualizationRebootGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetMemoryGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetVcpusGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationShutdownGuestAction;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceState;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.dto.VirtualSystemOverview;
import com.redhat.rhn.frontend.dto.kickstart.KickstartDto;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.kickstart.KickstartScheduleCommand;
import com.redhat.rhn.manager.kickstart.ProvisionVirtualInstanceCommand;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.VirtualInstanceManager;

import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.virtualization.DomainCapabilitiesJson;
import com.suse.manager.virtualization.GuestDefinition;
import com.suse.manager.virtualization.HostCapabilitiesJson;
import com.suse.manager.virtualization.VirtualizationActionHelper;
import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;
import com.suse.manager.webui.controllers.MinionController;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualGuestMigrateActionJson;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualGuestSetterActionJson;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualGuestsBaseActionJson;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualGuestsUpdateActionJson;
import com.suse.manager.webui.errors.NotFoundException;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.utils.TokenBuilder;
import com.suse.manager.webui.utils.WebSockifyTokenBuilder;
import com.suse.manager.webui.utils.salt.custom.VmInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jose4j.lang.JoseException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing backend for Virtual Guests list page
 */
public class VirtualGuestsController extends AbstractVirtualizationController {

    private static final Logger LOG = LogManager.getLogger(VirtualGuestsController.class);

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();

    /**
     * @param virtManagerIn instance to manage virtualization
     */
    public VirtualGuestsController(VirtManager virtManagerIn) {
        super(virtManagerIn, "templates/virtualization/guests");
    }

    /**
     * Initialize request routes for the pages served by VirtualGuestsController
     *
     * @param jade jade engine
     */
    public void initRoutes(JadeTemplateEngine jade) {
        get("/manager/systems/details/virtualization/guests/:sid",
                withUserPreferences(withCsrfToken(withDocsLocale(withUserAndServer(this::show)))), jade);
        get("/manager/systems/details/virtualization/guests/:sid/edit/:guestuuid",
                withUserPreferences(withCsrfToken(withDocsLocale(withUserAndServer(this::edit)))), jade);
        get("/manager/systems/details/virtualization/guests/:sid/new",
                withUserPreferences(withCsrfToken(withDocsLocale(withUserAndServer(this::create)))), jade);
        get("/manager/systems/details/virtualization/guests/console/:guestuuid",
                withUserPreferences(withCsrfToken(withDocsLocale(withUser(this::console)))), jade);
        get("/manager/api/systems/details/virtualization/guests/:sid/data",
                asJson(withUserAndServer(this::data)));
        post("/manager/api/systems/details/virtualization/guests/consoleToken/:guestuuid",
                withUser(this::refreshConsoleToken));
        post("/manager/api/systems/details/virtualization/guests/:sid/refresh",
                withUserAndServer(this::refresh));
        post("/manager/api/systems/details/virtualization/guests/:sid/shutdown",
                withUserAndServer(this::shutdown));
        post("/manager/api/systems/details/virtualization/guests/:sid/restart",
                withUserAndServer(this::restart));
        post("/manager/api/systems/details/virtualization/guests/:sid/setVcpu",
                withUserAndServer(this::setVcpu));
        post("/manager/api/systems/details/virtualization/guests/:sid/setMemory",
                withUserAndServer(this::setMemory));
        post("/manager/api/systems/details/virtualization/guests/:sid/new",
                withUserAndServer(this::newGuest));
        post("/manager/api/systems/details/virtualization/guests/:sid/update",
                withUserAndServer(this::update));
        post("/manager/api/systems/details/virtualization/guests/:sid/migrate",
                withUserAndServer(this::migrate));
        post("/manager/api/systems/details/virtualization/guests/:sid/:action",
                withUserAndServer(this::guestAction));
        get("/manager/api/systems/details/virtualization/guests/:sid/guest/:uuid",
                withUserAndServer(this::getGuest));
        get("/manager/api/systems/details/virtualization/guests/:sid/domains_capabilities",
                withUserAndServer(this::getDomainsCapabilities));
    }

    /**
     * Displays the virtual guests page.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param server the server
     * @return the ModelAndView object to render the page
     */
    public ModelAndView show(Request request, Response response, User user, Server server) {
        Map<String, Object> data = new HashMap<>();

        data.put("salt_entitled", server.hasEntitlement(EntitlementManager.SALT));
        data.put("foreign_entitled", server.hasEntitlement(EntitlementManager.FOREIGN));
        data.put("is_admin", user.hasRole(RoleFactory.ORG_ADMIN));
        data.put("hostInfo", server.hasVirtualizationEntitlement() && server.asMinionServer().isPresent() ?
                virtManager.getHostInfo(server.getMinionId()).map(GSON::toJson).orElse("{}") :
                "{}");

        return renderPage("show", () -> data);
    }

    /**
     * Returns JSON data describing the virtual guests
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param server the server
     * @return JSON result of the API call
     */
    public String data(Request request, Response response, User user, Server server) {
        DataResult<VirtualSystemOverview> data =
                SystemManager.virtualGuestsForHostList(user, server.getId(), null);
        data.elaborate();

        for (VirtualSystemOverview current : data) {
            current.setSystemId(current.getVirtualSystemId());

            if (current.getSystemId() != null) {
                current.updateStatusType(user);
            }
        }

        String json = GSON.toJson(data);
        List<Map<String, JsonElement>> mergeData = GSON.fromJson(json,
                new TypeToken<List<Map<String, JsonElement>>>() { }.getType());

        server.asMinionServer().ifPresent(minionServer -> {
            Optional<Map<String, Map<String, JsonElement>>> extraData =
                    virtManager.getVmInfos(minionServer.getMinionId());

            DataResult<VirtualSystemOverview> noHostData =
                    SystemManager.virtualGuestsForHostList(user, null, null);
            noHostData.elaborate();

            extraData.ifPresent(extra -> {
                // Add all the extra data to the existing ones
                extra.forEach((key, value) -> mergeData.stream()
                        .filter(element -> element.get("name").getAsString().equals(key))
                        .findFirst()
                        .ifPresent(item -> {
                            if (value.get("uuid") != null) {
                                value.remove("uuid");
                            }
                            item.putAll(value);
                        }));
                // Find the VMs that aren't listed on the host
                List<Map<String, JsonElement>> missing = extra.entrySet().stream()
                        .filter(entry -> entry.getValue().get("uuid") != null)
                        .peek(entry -> {
                            final String guid = VirtualInstanceManager.fixUuidIfSwappedUuidExists(
                                    entry.getValue().get("uuid").getAsString().replaceAll("-", ""));
                            entry.getValue().put("uuid", new JsonPrimitive(guid));
                        })
                        .filter(entry -> entry.getValue().get("uuid") != null && data.stream()
                                .noneMatch(item -> item.getUuid().equals(entry.getValue().get("uuid").getAsString())))
                        .map(entry -> {
                            VirtualSystemOverview vso = noHostData.stream()
                                    .filter(vm -> vm.getUuid().equals(entry.getValue().get("uuid").getAsString()))
                                    .findFirst()
                                    .orElseGet(() -> {
                                        VirtualSystemOverview fakeVso = new VirtualSystemOverview();
                                        fakeVso.setUuid(entry.getValue().get("uuid").getAsString());
                                        fakeVso.setMemory(entry.getValue().get("memory").getAsLong());
                                        fakeVso.setVcpus(entry.getValue().get("vcpus").getAsLong());
                                        fakeVso.setName(entry.getKey());
                                        VirtualInstanceState state = VirtualInstanceFactory.getInstance()
                                                .getStoppedState();
                                        fakeVso.setStateLabel(state.getLabel());
                                        fakeVso.setStateName(state.getName());
                                        return fakeVso;
                                    });
                            Map<String, JsonElement> vmData = GSON.fromJson(GSON.toJson(vso),
                                    new TypeToken<Map<String, JsonElement>>() { }.getType());
                            vmData.putAll(extra.get(entry.getKey()));
                            return vmData;
                        })
                        .collect(Collectors.toList());
                mergeData.addAll(missing);
            });
        });

        return GSON.toJson(mergeData);
    }

    /**
     * Return the definition of the virtual machine
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return the json response
     */
    public String getGuest(Request request, Response response, User user, Server host) {
        String uuid = request.params("uuid");
        MinionServer minion = host.asMinionServer().orElseThrow(NotFoundException::new);

        GuestDefinition definition = virtManager.getGuestDefinition(minion.getMinionId(), uuid)
                .orElseThrow(NotFoundException::new);

        return json(response, definition, new TypeToken<>() { });
    }

    /**
     * Return the list of all domain capabilities from a given salt virtual host
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return the json response
     */
    public String getDomainsCapabilities(Request request, Response response, User user, Server host) {
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
        allDomainCaps.put("osTypes", hostCaps.getGuests().stream().map(HostCapabilitiesJson.Guest::getOsType)
                .distinct()
                .collect(Collectors.toList()));
        allDomainCaps.put("domainsCaps", domainsCaps);

        return json(response, allDomainCaps, new TypeToken<>() { });
    }

    /**
     * Display the edit page of a virtual guest
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return the ModelAndView object to render the page
     */
    public ModelAndView edit(Request request, Response response, User user, Server host) {
        Map<String, Object> data = new HashMap<>();

        // Use uuids since the IDs may change
        String guestUuid = request.params("guestuuid");

        DataResult<VirtualSystemOverview> guests =
                SystemManager.virtualGuestsForHostList(user, host.getId(), null);

        Optional<VirtualSystemOverview> guest = guests.stream().
                filter(item -> item.getUuid().equals(guestUuid)).findFirst();

        if (guest.isEmpty()) {
            host.asMinionServer().ifPresentOrElse(minionServer -> {
                    Optional<Map<String, Map<String, JsonElement>>> vmInfos =
                            virtManager.getVmInfos(minionServer.getMinionId());
                    boolean inCluster = vmInfos.map(info ->  info.values().stream()
                        .anyMatch(entry -> {
                            if (entry.containsKey("uuid")) {
                                String uuid = entry.get("uuid").getAsString().replaceAll("-", "");
                                return uuid.equals(guestUuid);
                            }
                            return false;
                        })
                    ).orElse(false);
                    if (!inCluster) {
                        LOG.error("No such virtual machine");
                        throw Spark.halt(HttpStatus.SC_BAD_REQUEST, "No such virtual machine");
                    }
                },
                () -> {
                    LOG.error("No such virtual machine");
                    throw Spark.halt(HttpStatus.SC_BAD_REQUEST, "No such virtual machine");
                }
            );
        }

        /* For the rest of the template */
        MinionController.addActionChains(user, data);
        data.put("guestUuid", guestUuid);
        data.put("isSalt", host.hasEntitlement(EntitlementManager.SALT));

        return renderPage("edit", () -> data);
    }

    /**
     * Display the New Virtual Guest page
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return the ModelAndView object to render the page
     */
    public ModelAndView create(Request request, Response response, User user, Server host) {
        Map<String, Object> data = new HashMap<>();

        if (!host.hasEntitlement(EntitlementManager.SALT)) {
            LOG.error("Only for Salt-managed virtual hosts");
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Only for Salt-managed virtual hosts");
        }

        String minionId = host.asMinionServer().orElseThrow(NotFoundException::new).getMinionId();
        Map<String, Boolean> features = virtManager.getFeatures(minionId).orElse(new HashMap<>());
        List<String> templates = virtManager.getTuningTemplates(minionId).orElse(new ArrayList<>());

        /* For the rest of the template */
        MinionController.addActionChains(user, data);
        data.put("isSalt", true);
        // If the resource agent doesn't support the new start_resources we'll get into troubles with pools and networks
        data.put("inCluster", features.getOrDefault("cluster", false));
        data.put("raCanStartResources",
                features.getOrDefault("resource_agent_start_resources", false));
        data.put("templates", GSON.toJson(templates));
        data.put("uefiAutoLoader", features.getOrDefault("uefi_auto_loader", false));

        KickstartScheduleCommand cmd = new ProvisionVirtualInstanceCommand(host.getId(), user);
        DataResult<KickstartDto> profiles = cmd.getKickstartProfiles();
        data.put("cobblerProfiles",
                GSON.toJson(profiles.stream().collect(
                        Collectors.toMap(KickstartDto::getCobblerId, KickstartDto::getLabel))));

        return renderPage("create", () -> data);
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

        // Use uuids since the IDs may change
        String guestUuid = request.params("guestuuid");
        data.put("guestUuid", guestUuid);
        VirtualInstance guest = getVirtualInstanceFromUuid(guestUuid);

        if (guest == null || guest.getHostSystem() == null) {
            // Not having the VM in the database doesn't mean it doesn't exist somewhere.
            // Stopped clustered VMs are transient and thus not in the database.
            // Showing the page and then waiting for an event to come should help when the VM starts.
            data.put("serverId", "undefined");
            data.put("guestName", guest != null ? String.format("'%s'", guest.getName()) : "undefined");
            data.put("guestState", guest != null ? String.format("'%s'", guest.getState().getLabel()) : "undefined");
            data.put("graphicsType", "undefined");
            data.put("token", String.format("'%s'", getConsoleToken(null, -1)));
            return new ModelAndView(data, jadeTemplatesPath + "/console.jade");
        }
        Server host = guest.getHostSystem();

        try {
            ensureAccessToVirtualInstance(user, guest);
        }
        catch (LookupException e) {
            LOG.error("Unauthorized", e);
            Spark.halt(HttpStatus.SC_UNAUTHORIZED, e.getLocalizedMessage());
        }

        String minionId = host.asMinionServer().orElseThrow(() -> Spark.halt(HttpStatus.SC_BAD_REQUEST)).getMinionId();
        GuestDefinition def = virtManager.getGuestDefinition(minionId, guestUuid)
                .orElseThrow(() -> Spark.halt(HttpStatus.SC_BAD_REQUEST));
        String hostname = host.getName();

        data.put("serverId", String.format("'%d'", guest.getHostSystem().getId()));
        data.put("guestName", String.format("'%s'", guest.getName()));
        data.put("guestState", String.format("'%s'", guest.getState().getLabel()));
        data.put("graphicsType", String.format("'%s'", def.getGraphics().getType()));
        data.put("token", String.format("'%s'", getConsoleToken(hostname, def.getGraphics().getPort())));

        return new ModelAndView(data, jadeTemplatesPath + "/console.jade");
    }

    /**
     * Refresh the JWT token to be used for the console display.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     *
     * @return the new JWT token
     */
    public String refreshConsoleToken(Request request, Response response, User user) {
        if (!TokenBuilder.verifyToken(request.body())) {
            LOG.error("Invalid token");
            Spark.halt(HttpStatus.SC_FORBIDDEN, "Invalid token");
        }

        String guestUuid = request.params("guestuuid");
        VirtualInstance guest = getVirtualInstanceFromUuid(guestUuid);
        if (guest == null) {
            LOG.error("Virtual machine not found");
            Spark.halt(HttpStatus.SC_NOT_FOUND, "Virtual machine not found");
        }
        Server host = guest.getHostSystem();
        try {
            ensureAccessToVirtualInstance(user, guest);
        }
        catch (LookupException e) {
            LOG.error("Unauthorized", e);
            Spark.halt(HttpStatus.SC_UNAUTHORIZED, e.getLocalizedMessage());
        }

        String minionId = host.asMinionServer().orElseThrow(() -> Spark.halt(HttpStatus.SC_BAD_REQUEST)).getMinionId();
        GuestDefinition def = virtManager.getGuestDefinition(minionId, guestUuid)
                .orElseThrow(() -> Spark.halt(HttpStatus.SC_BAD_REQUEST));
        String hostname = host.getName();

        return json(response, getConsoleToken(hostname, def.getGraphics().getPort()));
    }

    private void ensureAccessToVirtualInstance(User user, VirtualInstance guest) throws LookupException {
        if (guest.getGuestSystem() != null) {
            SystemManager.ensureAvailableToUser(user, guest.getGuestSystem().getId());
        }
        else {
            SystemManager.ensureAvailableToUser(user, guest.getHostSystem().getId());
        }
    }

    private VirtualInstance getVirtualInstanceFromUuid(String uuid) {
        List<VirtualInstance> guests = VirtualInstanceFactory.getInstance().lookupVirtualInstanceByUuid(uuid);
        if (guests.isEmpty()) {
            return null;
        }
        if (guests.size() > 1) {
            if (LOG.isDebugEnabled()) {
                LOG.error("More than one virtual machine found for this UUID: {}",
                        StringUtil.sanitizeLogInput(uuid));
            }
            Spark.halt(HttpStatus.SC_NOT_FOUND, "More than one virtual machine found for this UUID");
        }
        return guests.get(0);
    }

    private String getConsoleToken(String hostname, int port) {
        String token = null;
        try {
            WebSockifyTokenBuilder tokenBuilder = new WebSockifyTokenBuilder(hostname, port);
            tokenBuilder.useServerSecret();
            token = tokenBuilder.getToken();
        }
        catch (JoseException e) {
            LOG.error("Service unavailable", e);
            Spark.halt(HttpStatus.SC_SERVICE_UNAVAILABLE);
        }
        return token;
    }

    /**
     * Refresh the database with the actual list of virtual machines from the host
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return Boolean indicating the success of the operation
     */
    public Boolean refresh(Request request, Response response, User user, Server host) {
        if (noHostSupportAction(host, true)) {
            LOG.error("Action not supported for this host");
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Action not supported for this host");
        }

        VirtualInstanceManager.updateHostVirtualInstance(host,
                VirtualInstanceFactory.getInstance().getFullyVirtType());
        Optional<List<VmInfo>> plan = virtManager.getGuestsUpdatePlan(host.getMinionId());
        plan.ifPresent(updatePlan -> VirtualInstanceManager.updateGuestsVirtualInstances(host, updatePlan));
        return plan.isPresent();
    }

    /**
     * Set the memory amount of a virtual guest
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return the action id or "Failed" in case of failure
     */
    public String setMemory(Request request, Response response, User user, Server host) {
        if (noHostSupportAction(host, false)) {
            LOG.error("Action not supported for this host");
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Action not supported for this host");
        }

        return setterAction(request, response, user, host,
                ActionFactory.TYPE_VIRTUALIZATION_SET_MEMORY,
                data -> data.getValue().intValue(),
                (action, value) -> ((VirtualizationSetMemoryGuestAction)action).setMemory(value),
                VirtualGuestSetterActionJson.class);
    }

    /**
     * Set the number of vCPUs of a virtual guest
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return the action id or "Failed" in case of failure
     */
    public String setVcpu(Request request, Response response, User user, Server host) {
        if (noHostSupportAction(host, false)) {
            LOG.error("Action not supported for this host");
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Action not supported for this host");
        }

        return setterAction(request, response, user, host,
                ActionFactory.TYPE_VIRTUALIZATION_SET_VCPUS,
                data -> data.getValue().intValue(),
                (action, value) -> ((VirtualizationSetVcpusGuestAction)action).setVcpu(value),
                VirtualGuestSetterActionJson.class);
    }

    /**
     * reboot a virtual guest
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return the action id or "Failed" in case of failure
     */
    public String restart(Request request, Response response, User user, Server host) {
        if (noHostSupportAction(host, false)) {
            LOG.error("Action not supported for this host");
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Action not supported for this host");
        }

        return doGuestAction(request, response, user, host,
                VirtualizationActionHelper.getGuestForceActionCreator(
                        ActionFactory.TYPE_VIRTUALIZATION_REBOOT,
                        (action, force) -> ((VirtualizationRebootGuestAction)action).setForce(force),
                        getGuestNames(user, host)),
                VirtualGuestsBaseActionJson.class);
    }

    /**
     * shut down a virtual guest
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return the action id or "Failed" in case of failure
     */
    public String shutdown(Request request, Response response, User user, Server host) {
        if (noHostSupportAction(host, false)) {
            LOG.error("Action not supported for this host");
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Action not supported for this host");
        }

        return doGuestAction(request, response, user, host,
                VirtualizationActionHelper.getGuestForceActionCreator(
                        ActionFactory.TYPE_VIRTUALIZATION_SHUTDOWN,
                        (action, force) -> ((VirtualizationShutdownGuestAction)action).setForce(force),
                        getGuestNames(user, host)),
                VirtualGuestsBaseActionJson.class);
    }

    /**
     * Run a guest action
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return the JSON result
     */
    public String guestAction(Request request, Response response, User user, Server host) {
        Map<String, ActionType> actionsMap = new HashMap<>();
        actionsMap.put("start", ActionFactory.TYPE_VIRTUALIZATION_START);
        actionsMap.put("suspend", ActionFactory.TYPE_VIRTUALIZATION_SUSPEND);
        actionsMap.put("resume", ActionFactory.TYPE_VIRTUALIZATION_RESUME);
        actionsMap.put("restart", ActionFactory.TYPE_VIRTUALIZATION_REBOOT);
        actionsMap.put("delete", ActionFactory.TYPE_VIRTUALIZATION_DELETE);

        if (noHostSupportAction(host, false)) {
            LOG.error("Action not supported for this host");
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Action not supported for this host");
        }

        String actionParam = request.params("action");
        ActionType actionType = actionsMap.get(actionParam);

        return doGuestAction(request, response, user, host,
                VirtualizationActionHelper.getGuestActionCreator(actionType, getGuestNames(user, host)),
                VirtualGuestsBaseActionJson.class);
    }

    /**
     * Migrate a virtual guest to an other virtual host
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return the action id or "Failed" in case of failure
     */
    public String migrate(Request request, Response response, User user, Server host) {
        if (noHostSupportAction(host, true)) {
            LOG.error("Action not supported for this host");
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Action not supported for this host");
        }

        return doGuestAction(request, response, user, host,
                VirtualizationActionHelper.getGuestMigrateActionCreator(getGuestNames(user, host)),
                VirtualGuestMigrateActionJson.class);
    }

    /**
     * Update the definition of a virtual guest.
     *
     * For traditional guests only the vCPU count and memory amount will be changed.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return the action id or "Failed" in case of failure
     */
    public String update(Request request, Response response, User user, Server host) {
        if (noHostSupportAction(host, false)) {
            LOG.error("Action not supported for this host");
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Action not supported for this host");
        }

        if (host.asMinionServer().isPresent()) {
            return newGuest(request, response, user, host);
        }

        String memResult = setterAction(request, response, user, host,
                ActionFactory.TYPE_VIRTUALIZATION_SET_MEMORY,
                data -> data.getMemory().intValue(),
                (action, value) -> ((VirtualizationSetMemoryGuestAction)action).setMemory(value),
                VirtualGuestsUpdateActionJson.class);
        Map<String, String> results = new HashMap<>(
                GSON.fromJson(memResult, new TypeToken<Map<String, String>>() { }.getType())
        );

        String vcpuResult = setterAction(request, response, user, host,
                ActionFactory.TYPE_VIRTUALIZATION_SET_VCPUS,
                data -> data.getVcpu().intValue(),
                (action, value) -> ((VirtualizationSetVcpusGuestAction)action).setVcpu(value),
                VirtualGuestsUpdateActionJson.class);
        results.putAll(GSON.fromJson(vcpuResult, new TypeToken<Map<String, String>>() { }.getType()));
        return json(response, results, new TypeToken<>() { });
    }

    /**
     * Create or update a virtual guest
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return the action id or "Failed" in case of failure
     */
    public String newGuest(Request request, Response response, User user, Server host) {
        if (noHostSupportAction(host, true)) {
            LOG.error("Action not supported for this host");
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Action not supported for this host");
        }

        return doGuestAction(request, response, user, host,
                VirtualizationActionHelper.getGuestActionCreateCreator(host, user, request.raw(),
                        getGuestNames(user, host)), VirtualGuestsUpdateActionJson.class);
    }

    private <T extends VirtualGuestsBaseActionJson> String setterAction(Request request, Response response,
                                User user, Server host, ActionType actionType,
                                Function<T, Integer> getter,
                                BiConsumer<Action, Integer> setter,
                                Class<T> dataClass) {
        if (noHostSupportAction(host, false)) {
            LOG.error("Action not supported for this host");
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Action not supported for this host");
        }

        return doGuestAction(request, response, user, host,
                VirtualizationActionHelper.getGuestSetterActionCreator(
                        actionType, getter, setter, getGuestNames(user, host)),
                dataClass);
    }

    private Map<String, String> getGuestNames(User user, Server host) {
        DataResult<VirtualSystemOverview> guests =
                SystemManager.virtualGuestsForHostList(user, host.getId(), null);
        Map<String, String> names = guests.stream().collect(Collectors.toMap(
                VirtualSystemOverview::getUuid,
                SystemOverview::getName
        ));

        // Get the names of the VMs defined on the cluster if any
        host.asMinionServer()
                .flatMap(minionServer -> virtManager.getVmInfos(minionServer.getMinionId()))
                .ifPresent(data -> names.putAll(data.entrySet().stream()
                        .filter(entry -> entry.getValue().containsKey("uuid"))
                        .collect(Collectors.toMap(
                                entry -> entry.getValue().get("uuid").getAsString().replaceAll("-", ""),
                                Map.Entry::getKey))));

        return names;
    }

    private boolean noHostSupportAction(Server host, boolean saltOnly) {
        if (host.hasEntitlement(EntitlementManager.FOREIGN)) {
            LOG.warn("Foreign systems don't support virtual guest actions");
            return true;
        }
        // Traditionally registered systems aren't able to really delete the VM: fail
        // the delete action for them
        if (host.hasEntitlement(EntitlementManager.MANAGEMENT) && saltOnly) {
            LOG.warn("Traditional systems don't support virtual guests deletion");
            return true;
        }

        return false;
    }

    private <T extends VirtualGuestsBaseActionJson> String doGuestAction(Request request, Response response, User user,
                                 Server server,
                                 BiFunction<T, String, Action> actionCreator,
                                 Class<T> jsonClass) {
        return action(request, response, user, server, actionCreator,
                VirtualGuestsBaseActionJson::getUuids,
                jsonClass
        );
    }
}
