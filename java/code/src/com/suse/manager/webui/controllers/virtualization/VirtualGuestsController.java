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
package com.suse.manager.webui.controllers.virtualization;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withDocsLocale;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.virtualization.BaseVirtualizationGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationCreateActionDiskDetails;
import com.redhat.rhn.domain.action.virtualization.VirtualizationCreateActionInterfaceDetails;
import com.redhat.rhn.domain.action.virtualization.VirtualizationCreateGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationRebootGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetMemoryGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetVcpusGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationShutdownGuestAction;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.kickstart.KickstartHelper;
import com.redhat.rhn.frontend.dto.VirtualSystemOverview;
import com.redhat.rhn.frontend.dto.kickstart.KickstartDto;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.kickstart.KickstartScheduleCommand;
import com.redhat.rhn.manager.kickstart.ProvisionVirtualInstanceCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerVirtualSystemCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.VirtualInstanceManager;

import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.virtualization.DomainCapabilitiesJson;
import com.suse.manager.virtualization.GuestDefinition;
import com.suse.manager.virtualization.HostCapabilitiesJson;
import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;
import com.suse.manager.webui.controllers.MinionController;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualGuestSetterActionJson;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualGuestsBaseActionJson;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualGuestsUpdateActionJson;
import com.suse.manager.webui.errors.NotFoundException;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.utils.WebSockifyTokenBuilder;
import com.suse.manager.webui.utils.salt.custom.VmInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.cobbler.Profile;
import org.jose4j.lang.JoseException;

import java.time.LocalDateTime;
import java.util.Arrays;
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

    private static final Logger LOG = Logger.getLogger(VirtualGuestsController.class);

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
                withUserPreferences(withCsrfToken(withDocsLocale(withUser(this::show)))), jade);
        get("/manager/systems/details/virtualization/guests/:sid/edit/:guestuuid",
                withUserPreferences(withCsrfToken(withDocsLocale(withUser(this::edit)))), jade);
        get("/manager/systems/details/virtualization/guests/:sid/new",
                withUserPreferences(withCsrfToken(withDocsLocale(withUser(this::create)))), jade);
        get("/manager/systems/details/virtualization/guests/:sid/console/:guestuuid",
                withUserPreferences(withCsrfToken(withDocsLocale(withUser(this::console)))), jade);
        get("/manager/api/systems/details/virtualization/guests/:sid/data",
                withUser(this::data));
        post("/manager/api/systems/details/virtualization/guests/:sid/refresh", withUser(this::refresh));
        post("/manager/api/systems/details/virtualization/guests/:sid/shutdown",
                withUser(this::shutdown));
        post("/manager/api/systems/details/virtualization/guests/:sid/restart",
                withUser(this::restart));
        post("/manager/api/systems/details/virtualization/guests/:sid/setVcpu",
                withUser(this::setVcpu));
        post("/manager/api/systems/details/virtualization/guests/:sid/setMemory",
                withUser(this::setMemory));
        post("/manager/api/systems/details/virtualization/guests/:sid/new",
                withUser(this::newGuest));
        post("/manager/api/systems/details/virtualization/guests/:sid/update",
                withUser(this::update));
        post("/manager/api/systems/details/virtualization/guests/:sid/:action",
                withUser(this::guestAction));
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
        Server server = getServer(request, user);

        /* For the rest of the template */
        data.put("salt_entitled", server.hasEntitlement(EntitlementManager.SALT));
        data.put("foreign_entitled", server.hasEntitlement(EntitlementManager.FOREIGN));
        data.put("is_admin", user.hasRole(RoleFactory.ORG_ADMIN));
        data.put("hypervisor", server.hasVirtualizationEntitlement() && server.asMinionServer().isPresent() ?
                virtManager.getHypervisor(server.getMinionId()).orElse("") :
                "");

        return renderPage(request, response, user, "show", () -> data);
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
        Long serverId = getServerId(request);

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
        String uuid = request.params("uuid");
        Server host = getServer(request, user);
        MinionServer minion = host.asMinionServer().orElseThrow(NotFoundException::new);
        DataResult<VirtualSystemOverview> guests = SystemManager.virtualGuestsForHostList(user, host.getId(), null);
        VirtualSystemOverview guest = guests.stream().filter(vso -> vso.getUuid().equals(uuid))
                .findFirst().orElseThrow(NotFoundException::new);

        GuestDefinition definition = virtManager.getGuestDefinition(minion.getMinionId(),
                guest.getName()).orElseThrow(NotFoundException::new);

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
        Server host = getServer(request, user);
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

        return json(response, allDomainCaps);
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

        // Use uuids since the IDs may change
        String guestUuid = request.params("guestuuid");

        Server host = getServer(request, user);
        DataResult<VirtualSystemOverview> guests =
                SystemManager.virtualGuestsForHostList(user, host.getId(), null);

        Optional<VirtualSystemOverview> guest = guests.stream().
                filter(item -> item.getUuid().equals(guestUuid)).findFirst();

        if (guest.isEmpty()) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }

        /* For the rest of the template */
        MinionController.addActionChains(user, data);
        data.put("guestUuid", guestUuid);
        data.put("isSalt", host.hasEntitlement(EntitlementManager.SALT));

        return renderPage(request, response, user, "edit", () -> data);
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
        Server host = getServer(request, user);

        if (!host.hasEntitlement(EntitlementManager.SALT)) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Only for Salt-managed virtual hosts");
        }

        /* For the rest of the template */
        MinionController.addActionChains(user, data);
        data.put("isSalt", true);

        KickstartScheduleCommand cmd = new ProvisionVirtualInstanceCommand(host.getId(), user);
        DataResult<KickstartDto> profiles = cmd.getKickstartProfiles();
        data.put("cobblerProfiles",
                GSON.toJson(profiles.stream().collect(
                        Collectors.toMap(KickstartDto::getCobblerId, KickstartDto::getLabel))));

        return renderPage(request, response, user, "create", () -> data);
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
        long hostId = getServerId(request);

        // Use uuids since the IDs may change
        String guestUuid = request.params("guestuuid");

        VirtualInstance guest = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByHostIdAndUuid(hostId, guestUuid);
        Server host = guest.getHostSystem();

        String minionId = host.asMinionServer().orElseThrow(() -> Spark.halt(HttpStatus.SC_BAD_REQUEST)).getMinionId();
        GuestDefinition def = virtManager.getGuestDefinition(minionId, guest.getName()).
                orElseThrow(() -> Spark.halt(HttpStatus.SC_BAD_REQUEST));
        String hostname = host.getName();
        int port = def.getGraphics().getPort();

        /* For the rest of the template */
        data.put("guestUuid", guestUuid);
        data.put("guestName", guest.getName());
        data.put("graphicsType", def.getGraphics().getType());

        String token = null;
        if (Arrays.asList("spice", "vnc").contains(def.getGraphics().getType())) {
            try {
                WebSockifyTokenBuilder tokenBuilder = new WebSockifyTokenBuilder(hostname, port);
                tokenBuilder.useServerSecret();
                token = tokenBuilder.getToken();
            }
            catch (JoseException e) {
                LOG.error(e);
                Spark.halt(HttpStatus.SC_SERVICE_UNAVAILABLE);
            }
        }
        data.put("token", token);

        return renderPage(request, response, user, "console", () -> data);
    }

    /**
     * Refresh the database with the actual list of virtual machines from the host
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return Boolean indicating the success of the operation
     */
    public Boolean refresh(Request request, Response response, User user) {
        Server host = getServer(request, user);
        if (noHostSupportAction(host, true)) {
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
     * @return the action id or "Failed" in case of failure
     */
    public String setMemory(Request request, Response response, User user) {
        Server host = getServer(request, user);
        if (noHostSupportAction(host, false)) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Action not supported for this host");
        }

        return setterAction(request, response, user,
                ActionFactory.TYPE_VIRTUALIZATION_SET_MEMORY,
                (data) -> ((VirtualGuestSetterActionJson)data).getValue().intValue(),
                (action, value) -> ((VirtualizationSetMemoryGuestAction)action).setMemory(value),
                VirtualGuestSetterActionJson.class);
    }

    /**
     * Set the number of vCPUs of a virtual guest
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the action id or "Failed" in case of failure
     */
    public String setVcpu(Request request, Response response, User user) {
        Server host = getServer(request, user);
        if (noHostSupportAction(host, false)) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Action not supported for this host");
        }

        return setterAction(request, response, user,
                ActionFactory.TYPE_VIRTUALIZATION_SET_VCPUS,
                (data) -> ((VirtualGuestSetterActionJson)data).getValue().intValue(),
                (action, value) -> ((VirtualizationSetVcpusGuestAction)action).setVcpu(value),
                VirtualGuestSetterActionJson.class);
    }

    /**
     * reboot a virtual guest
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the action id or "Failed" in case of failure
     */
    public String restart(Request request, Response response, User user) {
        Server host = getServer(request, user);
        if (noHostSupportAction(host, false)) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Action not supported for this host");
        }

        return doGuestAction(request, response, user, (data) -> {
            VirtualizationRebootGuestAction action = (VirtualizationRebootGuestAction)
                    ActionFactory.createAction(ActionFactory.TYPE_VIRTUALIZATION_REBOOT);
            action.setName(ActionFactory.TYPE_VIRTUALIZATION_REBOOT.getName());
            action.setForce(data.getForce());
            return action;
        });
    }

    /**
     * shut down a virtual guest
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the action id or "Failed" in case of failure
     */
    public String shutdown(Request request, Response response, User user) {
        Server host = getServer(request, user);
        if (noHostSupportAction(host, false)) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Action not supported for this host");
        }

        return doGuestAction(request, response, user, (data) -> {
            VirtualizationShutdownGuestAction action = (VirtualizationShutdownGuestAction)
                    ActionFactory.createAction(ActionFactory.TYPE_VIRTUALIZATION_SHUTDOWN);
            action.setName(ActionFactory.TYPE_VIRTUALIZATION_SHUTDOWN.getName());
            action.setForce(data.getForce());
            return action;
        });
    }

    /**
     * Run a guest action
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the JSON result
     */
    public String guestAction(Request request, Response response, User user) {
        Map<String, ActionType> actionsMap = new HashMap<>();
        actionsMap.put("start", ActionFactory.TYPE_VIRTUALIZATION_START);
        actionsMap.put("suspend", ActionFactory.TYPE_VIRTUALIZATION_SUSPEND);
        actionsMap.put("resume", ActionFactory.TYPE_VIRTUALIZATION_RESUME);
        actionsMap.put("restart", ActionFactory.TYPE_VIRTUALIZATION_REBOOT);
        actionsMap.put("delete", ActionFactory.TYPE_VIRTUALIZATION_DELETE);

        Server host = getServer(request, user);
        if (noHostSupportAction(host, false)) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Action not supported for this host");
        }

        String actionParam = request.params("action");
        ActionType actionType = actionsMap.get(actionParam);

        return doGuestAction(request, response, user, (data) -> {
            BaseVirtualizationGuestAction action = (BaseVirtualizationGuestAction)
                    ActionFactory.createAction(actionType);
            action.setName(actionType.getName());
            return action;
        });
    }

    /**
     * Update the definition of a virtual guest.
     *
     * For traditional guests only the vCPU count and memory amount will be changed.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the action id or "Failed" in case of failure
     */
    public String update(Request request, Response response, User user) {
        Server host = getServer(request, user);
        if (noHostSupportAction(host, false)) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Action not supported for this host");
        }

        if (host.asMinionServer().isPresent()) {
            return newGuest(request, response, user);
        }

        String memResult = setterAction(request, response, user,
                ActionFactory.TYPE_VIRTUALIZATION_SET_MEMORY,
                (data) -> ((VirtualGuestsUpdateActionJson)data).getMemory().intValue(),
                (action, value) -> ((VirtualizationSetMemoryGuestAction)action).setMemory(value),
                VirtualGuestsUpdateActionJson.class);
        Map<String, String> results = new HashMap<>(
                GSON.fromJson(memResult, new TypeToken<Map<String, String>>() { }.getType())
        );

        String vcpuResult = setterAction(request, response, user,
                ActionFactory.TYPE_VIRTUALIZATION_SET_VCPUS,
                (data) -> ((VirtualGuestsUpdateActionJson)data).getVcpu().intValue(),
                (action, value) -> ((VirtualizationSetVcpusGuestAction)action).setVcpu(value),
                VirtualGuestsUpdateActionJson.class);
        results.putAll(GSON.fromJson(vcpuResult, new TypeToken<Map<String, String>>() { }.getType()));
        return json(response, results);
    }

    /**
     * Create or update a virtual guest
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the action id or "Failed" in case of failure
     */
    public String newGuest(Request request, Response response, User user) {
        Server host = getServer(request, user);
        if (noHostSupportAction(host, true)) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Action not supported for this host");
        }

        return doGuestAction(request, response, user, (data, guest) -> {
            VirtualizationCreateGuestAction action = (VirtualizationCreateGuestAction)
                    ActionFactory.createAction(ActionFactory.TYPE_VIRTUALIZATION_CREATE);

            VirtualGuestsUpdateActionJson createData = (VirtualGuestsUpdateActionJson) data;

            String actionName = ActionFactory.TYPE_VIRTUALIZATION_CREATE.getName().replaceAll("\\.$", "");
            actionName += ": " + createData.getName();
            if (data.getUuids() != null && !data.getUuids().isEmpty()) {
                actionName = LocalizationService.getInstance().getMessage("virt.update");
            }
            action.setName(actionName);

            action.setType(createData.getType());
            // So far the salt virt.update function doesn't allow renaming a guest,
            // and that is only possible for the KVM driver.
            action.setGuestName(createData.getName());
            action.setOsType(createData.getOsType());
            action.setMemory(createData.getMemory());
            action.setVcpus(createData.getVcpu());
            action.setArch(createData.getArch());
            action.setGraphicsType(createData.getGraphicsType());
            action.setKernelOptions(createData.getKernelOptions());

            if (guest.isEmpty() && createData.getCobblerId() != null && !createData.getCobblerId().isEmpty()) {
                // Create cobbler profile
                KickstartHelper helper = new KickstartHelper(request.raw());
                Profile cobblerProfile = Profile.lookupById(
                        CobblerXMLRPCHelper.getConnection(user), createData.getCobblerId());
                KickstartData ksData = KickstartFactory.
                        lookupKickstartDataByCobblerIdAndOrg(user.getOrg(), cobblerProfile.getId());
                CobblerVirtualSystemCommand cobblerCmd = new CobblerVirtualSystemCommand(user, cobblerProfile.getName(),
                        createData.getName(), ksData, host.getName(), host.getOrgId());
                String ksHost = helper.getKickstartHost();
                cobblerCmd.setKickstartHost(ksHost);
                cobblerCmd.store();

                action.setCobblerSystem(cobblerCmd.getCobblerSystemRecordName());
                action.setKickstartHost(ksHost);
            }

            if (createData.getDisks() != null) {
                 action.setDisks(createData.getDisks().stream().map(disk -> {
                    VirtualizationCreateActionDiskDetails details = new VirtualizationCreateActionDiskDetails();
                    details.setDevice(disk.getDevice());
                    details.setTemplate(disk.getTemplate());
                    details.setSize(disk.getSize());
                    details.setBus(disk.getBus());
                    details.setPool(disk.getPool());
                    details.setSourceFile(disk.getSourceFile());
                    details.setFormat(disk.getFormat());
                    details.setAction(action);
                    return details;
                }).collect(Collectors.toList()));
            }

            if (createData.getInterfaces() != null) {
                action.setInterfaces(createData.getInterfaces().stream().map(nic -> {
                    VirtualizationCreateActionInterfaceDetails details =
                            new VirtualizationCreateActionInterfaceDetails();
                    details.setType(nic.getType());
                    details.setSource(nic.getSource());
                    details.setMac(nic.getMac());
                    details.setAction(action);
                    return details;
                }).collect(Collectors.toList()));
            }
            action.setRemoveDisks(createData.getDisks() != null && createData.getDisks().isEmpty());

            action.setRemoveInterfaces(createData.getInterfaces() != null && createData.getInterfaces().isEmpty());

            return action;
        }, VirtualGuestsUpdateActionJson.class);
    }

    private String setterAction(Request request, Response response, User user, ActionType actionType,
                                Function<VirtualGuestsBaseActionJson, Integer> getter,
                                BiConsumer<Action, Integer> setter,
                                Class<? extends VirtualGuestsBaseActionJson> dataClass) {
        Server host = getServer(request, user);
        if (noHostSupportAction(host, false)) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Action not supported for this host");
        }

        return doGuestAction(request, response, user, (data, guest) -> {
            BaseVirtualizationGuestAction action =
                    (BaseVirtualizationGuestAction)ActionFactory.createAction(actionType);
            action.setName(actionType.getName());
            setter.accept(action, getter.apply(data));
            return action;
        }, dataClass);
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

    private String doGuestAction(Request request, Response response, User user,
                                 Function<VirtualGuestsBaseActionJson, BaseVirtualizationGuestAction> actionCreator) {
        BiFunction<VirtualGuestsBaseActionJson, Optional<VirtualSystemOverview>,
                   BaseVirtualizationGuestAction> creator = (data, vm) -> actionCreator.apply(data);
        return doGuestAction(request, response, user, creator, VirtualGuestsBaseActionJson.class);
    }

    private String doGuestAction(Request request, Response response, User user,
                                 BiFunction<VirtualGuestsBaseActionJson, Optional<VirtualSystemOverview>,
                                        BaseVirtualizationGuestAction> actionCreator,
                                 Class<? extends VirtualGuestsBaseActionJson> jsonClass) {
        Long serverId = getServerId(request);
        DataResult<VirtualSystemOverview> guests =
                SystemManager.virtualGuestsForHostList(user, serverId, null);
        return action(request, response, user,
                (data, key) -> {
                    VirtualGuestsBaseActionJson guestData = (VirtualGuestsBaseActionJson)data;
                    Optional<VirtualSystemOverview> guest = guests.stream()
                            .filter(item -> item.getUuid().equals(key))
                            .findFirst();
                    BaseVirtualizationGuestAction action = actionCreator.apply(guestData, guest);
                    action.setUuid(key);
                    if (guest.isPresent()) {
                        String actionName = action.getName().replaceAll("\\.$", "");
                        action.setName(actionName + ": " + guest.get().getName());
                    }
                    return action;
                },
                (data) -> ((VirtualGuestsBaseActionJson)data).getUuids(),
                jsonClass
        );
    }
}
