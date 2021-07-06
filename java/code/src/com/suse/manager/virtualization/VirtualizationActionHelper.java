/**
 * Copyright (c) 2021 SUSE LLC
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
package com.suse.manager.virtualization;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.virtualization.BaseVirtualizationGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationCreateActionDiskDetails;
import com.redhat.rhn.domain.action.virtualization.VirtualizationCreateActionInterfaceDetails;
import com.redhat.rhn.domain.action.virtualization.VirtualizationCreateGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationMigrateGuestAction;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.kickstart.KickstartHelper;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.common.UninitializedCommandException;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerVirtualSystemCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.webui.controllers.virtualization.gson.VirtualGuestMigrateActionJson;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualGuestsBaseActionJson;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualGuestsUpdateActionJson;
import com.suse.manager.webui.utils.MinionActionUtils;
import com.suse.manager.webui.utils.gson.ScheduledRequestJson;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.cobbler.Profile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides utility functions to create the virtualization actions
 */
public class VirtualizationActionHelper {

    private static final Logger LOG = Logger.getLogger(VirtualizationActionHelper.class);

    private static TaskomaticApi taskomaticApi = new TaskomaticApi();

    /**
     * Schedule a virtualization action
     *
     * @param key the key identifying the object
     * @param user the user performing the request
     * @param host the host the VM is running on
     * @param actionCreator a function creation the action out of the parsed Json data
     * @param data the Json Data
     * @param <T> Type of the json data
     *
     * @return the scheduled action id
     * @throws TaskomaticApiException if the action couldn't be scheduled
     */
    public static <T extends ScheduledRequestJson> int scheduleAction(String key, User user, Server host,
                                     BiFunction<T, String, Action> actionCreator,
                                     T data) throws TaskomaticApiException {
        Action action = actionCreator.apply(data, key);
        if (action == null) {
            // Should never happen that we get no action created, but still report it
            throw new TaskomaticApiException(new NullPointerException());
        }
        action.setOrg(user.getOrg());
        action.setSchedulerUser(user);
        action.setEarliestAction(MinionActionUtils.getScheduleDate(data.getEarliest()));

        Optional<ActionChain> actionChain = data.getActionChain()
                .filter(StringUtils::isNotEmpty)
                .map(label -> ActionChainFactory.getOrCreateActionChain(label, user));

        schedule(action, host, actionChain);
        return action.getId().intValue();
    }

    /**
     * Get action creator for guest actions needing a force parameter
     *
     * @param actionType the type of the action to create
     * @param setter function setting the force value
     * @param guestNames the guests names mapped to their UUID
     * @param <T> Type of the json data
     *
     * @return the action creator
     */
    public static <T extends VirtualGuestsBaseActionJson> BiFunction<T, String, Action> getGuestForceActionCreator(
            ActionType actionType,
            BiConsumer<BaseVirtualizationGuestAction, Boolean> setter,
            Map<String, String> guestNames
    ) {
        BiFunction<T, Optional<String>,
                BaseVirtualizationGuestAction> actionCreator = (data, name) -> {
            BaseVirtualizationGuestAction action = (BaseVirtualizationGuestAction)
                    ActionFactory.createAction(actionType);
            action.setName(actionType.getName());
            setter.accept(action, data.getForce() != null ? data.getForce() : false);
            return action;
        };

        return getGuestBaseActionCreator(actionCreator, guestNames);
    }

    /**
     * Get action creator for guest simple actions
     *
     * @param actionType the type of the action to create
     * @param guestNames the guests names mapped to their UUID
     * @param <T> Type of the json data
     *
     * @return the action creator
     */
    public static <T extends VirtualGuestsBaseActionJson> BiFunction<T, String, Action> getGuestActionCreator(
            ActionType actionType,
            Map<String, String> guestNames
    ) {
        BiFunction<T, Optional<String>,
                BaseVirtualizationGuestAction> actionCreator = (data, name) -> {
            AtomicReference<BaseVirtualizationGuestAction> action =
                    new AtomicReference<>((BaseVirtualizationGuestAction) ActionFactory.createAction(actionType));
            name.ifPresent(vmName -> {
                String uuid = guestNames.entrySet().stream()
                        .filter(e -> e.getValue().equals(name.get()))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);
                List<VirtualInstance> instances = VirtualInstanceFactory.getInstance()
                        .lookupVirtualInstanceByUuid(uuid);
                if (instances.size() == 1 && actionType.equals(ActionFactory.TYPE_VIRTUALIZATION_START) &&
                        instances.get(0).getState().getLabel().equals("paused")) {
                    action.set((BaseVirtualizationGuestAction) ActionFactory
                            .createAction(ActionFactory.TYPE_VIRTUALIZATION_RESUME));
                }
            });
            action.get().setName(actionType.getName());
            return action.get();
        };

        return getGuestBaseActionCreator(actionCreator, guestNames);
    }

    /**
     * Get action creator for guest setters (vcpus and memory).
     *
     * @param actionType the type of the action to create
     * @param getter how to get the value from the data
     * @param setter how to set the value in the action
     * @param guestNames the guests names mapped to their UUID
     * @param <T> Type of the json data
     *
     * @return the action creator
     */
    public static <T extends VirtualGuestsBaseActionJson> BiFunction<T, String, Action> getGuestSetterActionCreator(
            ActionType actionType,
            Function<T, Integer> getter,
            BiConsumer<Action, Integer> setter,
            Map<String, String> guestNames
    ) {
        BiFunction<T, Optional<String>,
                BaseVirtualizationGuestAction> actionCreator = (data, name) -> {
            BaseVirtualizationGuestAction action =
                    (BaseVirtualizationGuestAction) ActionFactory.createAction(actionType);
            action.setName(actionType.getName());
            setter.accept(action, getter.apply(data));
            return action;
        };

        return getGuestBaseActionCreator(actionCreator, guestNames);
    }

    /**
     * Get action creator for guest create or update actions
     *
     * @param host the virtualization host server
     * @param user the user performing the action
     * @param request the HTTP Servlet request for the kickstart helper
     * @param guestNames the guests names mapped to their UUID
     *
     * @return the action creator
     */
    public static BiFunction<VirtualGuestsUpdateActionJson, String, Action> getGuestActionCreateCreator(
            Server host,
            User user,
            HttpServletRequest request,
            Map<String, String> guestNames
    ) {
        BiFunction<VirtualGuestsUpdateActionJson, Optional<String>,
                BaseVirtualizationGuestAction> actionCreator = (data, name) -> {
            VirtualizationCreateGuestAction action = (VirtualizationCreateGuestAction)
                    ActionFactory.createAction(ActionFactory.TYPE_VIRTUALIZATION_CREATE);

            String actionName = ActionFactory.TYPE_VIRTUALIZATION_CREATE.getName().replaceAll("\\.$", "");
            actionName += ": " + data.getName();
            if (data.getUuids() != null && !data.getUuids().isEmpty()) {
                actionName = LocalizationService.getInstance().getMessage("virt.update");
            }
            action.setName(actionName);

            action.setType(data.getType());
            // So far the salt virt.update function doesn't allow renaming a guest,
            // and that is only possible for the KVM driver.
            action.setGuestName(data.getName());
            action.setOsType(data.getOsType());
            action.setMemory(data.getMemory());
            action.setVcpus(data.getVcpu());
            action.setArch(data.getArch());
            action.setGraphicsType(data.getGraphicsType());
            action.setKernelOptions(data.getKernelOptions());
            action.setClusterDefinitions(data.getClusterDefinitions());

            if (name.isEmpty() && data.getCobblerId() != null && !data.getCobblerId().isEmpty()) {
                // Create cobbler profile
                KickstartHelper helper = new KickstartHelper(request);
                Profile cobblerProfile = Profile.lookupById(
                        CobblerXMLRPCHelper.getConnection(user), data.getCobblerId());
                KickstartData ksData = KickstartFactory.
                        lookupKickstartDataByCobblerIdAndOrg(user.getOrg(), cobblerProfile.getId());
                CobblerVirtualSystemCommand cobblerCmd = new CobblerVirtualSystemCommand(user, cobblerProfile.getName(),
                        data.getName(), ksData, host.getName(), host.getOrgId());
                String ksHost = helper.getKickstartHost();
                cobblerCmd.setKickstartHost(ksHost);
                cobblerCmd.store();

                action.setCobblerSystem(cobblerCmd.getCobblerSystemRecordName());
                action.setKickstartHost(ksHost);
            }

            if (data.getDisks() != null) {
                action.setDisks(data.getDisks().stream().map(disk -> {
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

            if (data.getInterfaces() != null) {
                action.setInterfaces(data.getInterfaces().stream().map(nic -> {
                    VirtualizationCreateActionInterfaceDetails details =
                            new VirtualizationCreateActionInterfaceDetails();
                    details.setType(nic.getType());
                    details.setSource(nic.getSource());
                    details.setMac(nic.getMac());
                    details.setAction(action);
                    return details;
                }).collect(Collectors.toList()));
            }
            action.setRemoveDisks(data.getDisks() != null && data.getDisks().isEmpty());

            action.setRemoveInterfaces(data.getInterfaces() != null && data.getInterfaces().isEmpty());
            return action;
        };

        return getGuestBaseActionCreator(actionCreator, guestNames);
    }

    /**
     * Get action creator for guest migration actions
     *
     * @param guestNames the guests names mapped to their UUID
     *
     * @return the action creator
     */
    public static BiFunction<VirtualGuestMigrateActionJson, String, Action> getGuestMigrateActionCreator(
            Map<String, String> guestNames
    ) {
        BiFunction<VirtualGuestMigrateActionJson, Optional<String>,
                BaseVirtualizationGuestAction> actionCreator = (data, name) -> {
            VirtualizationMigrateGuestAction action = (VirtualizationMigrateGuestAction)
                    ActionFactory.createAction(ActionFactory.TYPE_VIRTUALIZATION_GUEST_MIGRATE);
            action.setName(ActionFactory.TYPE_VIRTUALIZATION_GUEST_MIGRATE.getName());
            action.setUuid(data.getUuids().get(0));
            action.setPrimitive(data.getPrimitive());
            action.setTarget(data.getTarget());
            return action;
        };

        return getGuestBaseActionCreator(actionCreator, guestNames);
    }

    private static <T extends VirtualGuestsBaseActionJson> BiFunction<T, String, Action> getGuestBaseActionCreator(
            BiFunction<T, Optional<String>,
                    BaseVirtualizationGuestAction> actionCreator,
            Map<String, String> guestNames
    ) {
        return (data, key) -> {
            Optional<String> name = Optional.ofNullable(guestNames.get(key));
            BaseVirtualizationGuestAction action = actionCreator.apply(data, name);
            action.setUuid(key);
            if (name.isPresent()) {
                String actionName = action.getName().replaceAll("\\.$", "");
                action.setName(actionName + ": " + name.get());
            }
            return action;
        };
    }

    /**
     * Helper function to schedule actions.
     *
     * @param action the action to schedule
     * @param targetSystem the system to run the action on
     * @param actionChain an optional action chain to append the action to
     *
     * @throws TaskomaticApiException if an error happened while scheduling
     */
    public static void schedule(Action action, Server targetSystem, Optional<ActionChain> actionChain)
            throws TaskomaticApiException {
        if (targetSystem == null) {
            throw new UninitializedCommandException("No targetSystem for virtualization action");
        }

        LOG.debug("schedule() called.");
        ActionFactory.save(action);

        if (actionChain == null || !actionChain.isPresent()) {
            ActionManager.scheduleForExecution(action, Collections.singleton(targetSystem.getId()));
            taskomaticApi.scheduleActionExecution(action);
        }
        else {
            Integer sortOrder = ActionChainFactory.getNextSortOrderValue(actionChain.get());
            ActionChainFactory.queueActionChainEntry(action, actionChain.get(),
                    targetSystem.getId(), sortOrder);
        }
    }

    /**
     * Set the {@link TaskomaticApi} instance to use. Only needed for unit tests.
     * @param taskomaticApiIn the {@link TaskomaticApi}
     */
    public static void setTaskomaticApi(TaskomaticApi taskomaticApiIn) {
        taskomaticApi = taskomaticApiIn;
    }

    private VirtualizationActionHelper() {
    }
}
