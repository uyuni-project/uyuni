/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerHistoryEvent;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.state.PackageStates;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.state.VersionConstraints;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import com.redhat.rhn.manager.system.SystemManager;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.controllers.StatesAPI;
import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.manager.webui.utils.salt.Zypper.ProductInfo;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Event handler to create system records for salt minions.
 */
public class RegisterMinionEventMessageAction extends AbstractDatabaseAction {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(
            RegisterMinionEventMessageAction.class);

    // Reference to the SaltService instance
    private final SaltService SALT_SERVICE;

    private static final List<String> BLACKLIST = Collections.unmodifiableList(
       Arrays.asList("rhncfg", "rhncfg-actions", "rhncfg-client", "rhn-virtualization-host",
               "osad")
    );

    /**
     * Default constructor.
     */
    public RegisterMinionEventMessageAction() {
        this(SaltAPIService.INSTANCE);
    }

    /**
     * Constructor taking a {@link SaltService} instance.
     *
     * @param saltService the salt service to use
     */
    public RegisterMinionEventMessageAction(SaltService saltService) {
        SALT_SERVICE = saltService;
    }

    /**
     * {@inheritDoc}
     */
    public void doExecute(EventMessage msg) {
        RegisterMinionEventMessage event = (RegisterMinionEventMessage) msg;
        String minionId = event.getMinionId();

        // Match minions via their machine id
        Optional<String> optMachineId = SALT_SERVICE.getMachineId(minionId);
        if (!optMachineId.isPresent()) {
            LOG.info("Cannot find machine id for minion: " + minionId);
            return;
        }
        //FIXME: refactor this whole function so we don't have to call get
        //in so many places.
        String machineId = optMachineId.get();
        MinionServer minionServer;

        Optional<MinionServer> optMinion = MinionServerFactory.findByMachineId(machineId);
        if (optMinion.isPresent()) {
            MinionServer registeredMinion = optMinion.get();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Minion already registered, updating profile: " +
                        minionId + " [" + machineId + "]");
            }
            if (!minionId.equals(registeredMinion.getName())) {
                registeredMinion.setName(minionId);
                registeredMinion.setMinionId(minionId);
                ServerFactory.save(registeredMinion);
            }
            return;
        }
        else {
            minionServer = ServerFactory.findByMachineId(machineId)
                .flatMap(server -> {
                    // migrate it to a minion server
                    ServerFactory.changeServerToMinionServer(
                            server.getId(), machineId);
                    return MinionServerFactory
                            .lookupById(server.getId());
                })
                .map(minion -> {
                    // hardware will be refreshed anyway
                    // new secret will be generated later

                    // remove package profile
                    minion.getPackages().clear();

                    // change base channel
                    minion.getChannels().clear();

                    // add reactivation event to server history
                    ServerHistoryEvent historyEvent = new ServerHistoryEvent();
                    historyEvent.setCreated(new Date());
                    historyEvent.setServer(minion);
                    historyEvent.setSummary("Server reactivated as Salt minion");
                    historyEvent.setDetails(
                            "System type was changed from Management to Salt");
                    minion.getHistory().add(historyEvent);

                    return minion;
            }).orElseGet(MinionServer::new);
        }

        try {
            MinionServer server = minionServer;

            server.setMachineId(machineId);
            server.setMinionId(minionId);
            server.setName(minionId);
            server.setDigitalServerId(machineId);

            ValueMap grains = new ValueMap(
                    SALT_SERVICE.getGrains(minionId).orElseGet(HashMap::new));

            //apply activation key properties that can be set before saving the server
            Optional<ActivationKey> activationKey = grains
                    .getMap("susemanager")
                    .flatMap(suma -> suma.getOptionalAsString("activation_key"))
                    .map(ActivationKeyFactory::lookupByKey);
            server.setOrg(activationKey
                    .map(ActivationKey::getOrg)
                    .orElse(OrgFactory.getSatelliteOrg()));
            activationKey.map(ActivationKey::getChannels)
                    .ifPresent(channels -> channels.forEach(server::addChannel));

            String osfullname = grains.getValueAsString("osfullname");
            String osrelease = grains.getValueAsString("osrelease");
            String kernelrelease = grains.getValueAsString("kernelrelease");
            String osarch = grains.getValueAsString("osarch");

            server.setOs(osfullname);
            server.setRelease(osrelease);
            server.setRunningKernel(kernelrelease);
            server.setSecret(RandomStringUtils.randomAlphanumeric(64));
            server.setAutoUpdate("N");
            server.setLastBoot(System.currentTimeMillis() / 1000);
            server.setCreated(new Date());
            server.setModified(server.getCreated());
            server.setContactMethod(ServerFactory.findContactMethodByLabel("default"));
            server.setServerArch(
                    ServerFactory.lookupServerArchByLabel(osarch + "-redhat-linux"));

            if (!activationKey.isPresent()) {
                LOG.info("No base channel added, adding default channel (if applicable)");
                lookupAndAddDefaultChannel(minionId, server);
            }

            server.updateServerInfo();

            mapHardwareGrains(server, grains);

            ServerFactory.save(server);

            // apply activation key properties that need to be set after saving the server
            activationKey.ifPresent(ak -> {
                ak.getToken().getActivatedServers().add(server);
                ActivationKeyFactory.save(ak);

                ak.getServerGroups().forEach(group -> {
                    ServerFactory.addServerToGroup(server, group);
                });

                ServerStateRevision serverStateRevision = new ServerStateRevision();
                serverStateRevision.setServer(server);
                serverStateRevision.setCreator(ak.getCreator());
                serverStateRevision.setPackageStates(
                    ak.getPackages().stream()
                            .filter(p -> !BLACKLIST.contains(p.getPackageName().getName()))
                            .map(tp -> {
                        PackageState state = new PackageState();
                        state.setArch(tp.getPackageArch());
                        state.setName(tp.getPackageName());
                        state.setPackageState(PackageStates.INSTALLED);
                        state.setVersionConstraint(VersionConstraints.ANY);
                        state.setStateRevision(serverStateRevision);
                        return state;
                    }).collect(Collectors.toSet())
                );
                StateFactory.save(serverStateRevision);
            });

            // salt systems always have script.run capability
            SystemManager.giveCapability(server.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

            // Assign the Salt base entitlement by default
            server.setBaseEntitlement(EntitlementManager.SALT);

            // get hardware and network async
            triggerHardwareRefresh(server);

            Map<String, String> data = new HashMap<>();
            data.put("minionId", minionId);
            data.put("machineId", machineId);
            LOG.info("Finished minion registration: " + minionId);

            StatesAPI.generateServerPackageState(server);

            // Asynchronously get the uptime of this minion
            MessageQueue.publish(new MinionStartEventDatabaseMessage(minionId));

            // Generate pillar data
            try {
                SaltStateGeneratorService.INSTANCE.registerServer(server);
            }
            catch (RuntimeException e) {
                LOG.error("Error generating Salt files for server '" + minionId +
                        "':" + e.getMessage());
            }

            // Apply initial states asynchronously
            MessageQueue.publish(new ApplyStatesEventMessage(
                    server.getId(),
                    true,
                    ApplyStatesEventMessage.CERTIFICATE,
                    ApplyStatesEventMessage.CHANNELS,
                    ApplyStatesEventMessage.CHANNELS_DISABLE_LOCAL_REPOS,
                    ApplyStatesEventMessage.PACKAGES
            ));
        }
        catch (Throwable t) {
            LOG.error("Error registering minion for event: " + event, t);
        }
    }

    private void lookupAndAddDefaultChannel(String minionId, MinionServer server) {
        ProductInfo pi = SALT_SERVICE.getListProducts(minionId).get().get(0);
        String osName = pi.getName().toLowerCase();
        String osVersion = pi.getVersion();
        String osArch = pi.getArch();
        Channel c = SUSEProductFactory
                .lookupSUSEProductBaseChannel(osName, osVersion, osArch)
                .getChannel();
        LOG.info("Channel " + c.getName() + " found for OS: " + osName
                + ", version: " + osVersion + ", arch: " + osArch
                + " - adding channel");
        server.addChannel(c);
    }

    private void mapHardwareGrains(MinionServer server, ValueMap grains) {
        // for efficiency do this here
        server.setRam(grains.getValueAsLong("mem_total").orElse(0L));
    }

    private void triggerHardwareRefresh(MinionServer server) {
        Action action = ActionManager
                .scheduleHardwareRefreshAction(server.getOrg(), server, new Date());
        MessageQueue.publish(new RefreshHardwareEventMessage(server.getMinionId(), action));
    }

}
