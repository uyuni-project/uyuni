package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;

import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceState;
import com.redhat.rhn.manager.system.VirtualInstanceManager;

import com.google.gson.JsonElement;
import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import com.suse.manager.virtualization.GuestDefinition;
import com.suse.manager.virtualization.VirtManager;
import com.suse.salt.netapi.event.EngineEvent;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import akka.actor.typed.Behavior;

public class LibvirtEngineActor implements Actor {

    private final static Logger LOG = Logger.getLogger(LibvirtEngineActor.class);

    private static final int LIBVIRT_EVENTS_ADDITIONAL_PARTS_COUNT = 3;

    public static class Message implements Command {
        private final String connection;
        private final Optional<String> minionId;
        private final String timestamp;
        private final String domainName;
        private final String domainId;
        private final String domainUUID;
        private final String event;
        private final String detail;


        public Message(String connection, Optional<String> minionId, String timestamp, String domainName, String domainId, String domainUUID, String event, String detail) {
            this.connection = connection;
            this.minionId = minionId;
            this.timestamp = timestamp;
            this.domainName = domainName;
            this.domainId = domainId;
            this.domainUUID = domainUUID;
            this.event = event;
            this.detail = detail;
        }
    }

    /**
     * Parse the engine event if it is a libvirt_events one.
     *
     * @param engineEvent the event to parse
     * @return the corresponding event message or <code>null</code> if not handled
     *
     * @throws IllegalArgumentException if the event is invalid
     */
    public static Message create(EngineEvent engineEvent) throws IllegalArgumentException {
        List<String> additionalParts = Arrays.asList(engineEvent.getAdditional().split("/"));
        if (additionalParts.size() < LIBVIRT_EVENTS_ADDITIONAL_PARTS_COUNT) {
            throw new IllegalArgumentException("Invalid libvirt engine event: " + engineEvent.getAdditional());
        }
        String eventType = additionalParts.get(additionalParts.size() - 1);
        String objectType = additionalParts.get(additionalParts.size() - 2);
        String connection = String.join("/", additionalParts.subList(0, additionalParts.size() - 2));

        if ("domain".equals(objectType)) {
            return createDomainMessage(connection,
                    eventType, engineEvent.getMinionId(), engineEvent.getTimestamp(),
                    engineEvent.getData(JsonElement.class));
        }
        return null;
    }

    /**
     * Create a domain message corresponding to the event type
     *
     * @param connection libvirt connection
     * @param eventType libvirt event type (lifecycle, refresh, etc)
     * @param minionId the minion ID or empty if the message comes from the master
     * @param timestamp the event time stamp
     * @param data the JSon data of the event
     *
     * @return a specialized object matching the event
     */
    public static Message createDomainMessage(String connection,
                                              String eventType, Optional<String> minionId, String timestamp,
                                              JsonElement data) {
        if (eventType.equals("lifecycle")) {
            var domain = data.getAsJsonObject().get("domain").getAsJsonObject();
            var domainName = domain.get("name").getAsString();
            var domainId = domain.get("id").getAsString();
            var domainUUID = domain.get("uuid").getAsString();
            var event = data.getAsJsonObject().get("event").getAsString();
            var detail = data.getAsJsonObject().get("detail").getAsString();

            return new Message(connection, minionId, timestamp, domainName, domainId, domainUUID, event, detail);
        }
        return null;
    }


    public Behavior<Command> create() {
        return setup(context -> receive(Command.class)
                .onMessage(Message.class, message -> onMessage(message))
                .build());
    }

    private Behavior<Command> onMessage(Message message) {
        handlingTransaction(() -> execute(message));
        return same();
    }

    public void execute(Message message) {
        if (message.minionId.isPresent()) {
            String minionId = message.minionId.get();
            String event = message.event;

            MinionServerFactory.findByMinionId(minionId).ifPresent(minion -> {
                LOG.debug("Processing " + message.event + "/" + message.detail + " on minion " + minionId);
                VirtualInstanceManager.updateHostVirtualInstance(minion,
                        VirtualInstanceFactory.getInstance().getFullyVirtType());

                final String guid = VirtualInstanceManager.fixUuidIfSwappedUuidExists(
                        message.domainUUID.replaceAll("-", ""));

                List<VirtualInstance> vms = VirtualInstanceFactory.getInstance().lookupVirtualInstanceByUuid(guid);
                if (vms.isEmpty()) {
                    // We got a machine created from outside SUMA,
                    // ask Salt for details on it to create it
                    Optional<GuestDefinition> result = VirtManager.getGuestDefinition(
                            minionId, message.domainName);

                    result.ifPresent(def -> {
                        VirtualInstanceState state = VirtualInstanceFactory.getInstance().getStoppedState();
                        if (Arrays.asList("started", "resumed").contains(event)) {
                            state = VirtualInstanceFactory.getInstance().getRunningState();
                        }

                        LOG.debug("Adding VM " + def.getName() + " with state to " + state.getLabel());
                        VirtualInstanceManager.addGuestVirtualInstance(def.getUuid().replaceAll("-", ""),
                                def.getName(), def.getVirtualInstanceType(), state, minion, null,
                                def.getVcpu().getMax(), def.getMaxMemory());
                    });
                }
                else {
                    List<String> stoppedEvents = Arrays.asList("defined", "stopped", "shutdown");
                    List<String> runningEvents = Arrays.asList("started", "resumed");
                    List<String> pausedEvents = Arrays.asList("suspended", "pmsuspended");
                    List<String> crashedEvents = Arrays.asList("crashed");

                    Map<List<String>, VirtualInstanceState> statesMap =
                            new HashMap<List<String>, VirtualInstanceState>();
                    statesMap.put(stoppedEvents, VirtualInstanceFactory.getInstance().getStoppedState());
                    statesMap.put(runningEvents, VirtualInstanceFactory.getInstance().getRunningState());
                    statesMap.put(pausedEvents, VirtualInstanceFactory.getInstance().getPausedState());
                    statesMap.put(crashedEvents, VirtualInstanceFactory.getInstance().getCrashedState());

                    VirtualInstanceState state = statesMap.entrySet().stream()
                            .filter(entry -> entry.getKey().contains(event))
                            .map(entry -> entry.getValue())
                            .findFirst().orElse(VirtualInstanceFactory.getInstance().getUnknownState());

                    LOG.debug("Changing VM " + vms.get(0) + " state to " + state.getLabel());

                    // We need to check if the VM is still defined and delete it if needed
                    if (Arrays.asList("undefined", "stopped", "shutdown", "crashed").contains(event) &&
                            !VirtManager.getGuestDefinition(minionId, message.domainName).isPresent()) {
                        vms.forEach(vm -> VirtualInstanceManager.deleteGuestVirtualInstance(vm));
                    }
                    else {
                        final Optional<GuestDefinition> updatedDef = message.detail.equals("updated") ?
                                VirtManager.getGuestDefinition(minionId, message.domainName) :
                                Optional.empty();

                        vms.forEach(vm -> {
                            String name = updatedDef.isPresent() ? updatedDef.get().getName() : vm.getName();
                            Integer cpuCount = updatedDef.isPresent() ?
                                    updatedDef.get().getVcpu().getMax() :
                                    vm.getNumberOfCPUs();
                            Long memory = updatedDef.isPresent() ?
                                    updatedDef.get().getMaxMemory() :
                                    vm.getTotalMemory();
                            VirtualInstanceManager.updateGuestVirtualInstanceProperties(
                                    vm, name, state, cpuCount, memory);
                        });
                    }
                }
            });
        }
    }
}
