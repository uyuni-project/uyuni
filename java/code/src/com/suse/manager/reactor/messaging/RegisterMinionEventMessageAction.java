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
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.state.PackageStates;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.state.VersionConstraints;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import com.redhat.rhn.manager.system.SystemManager;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.controllers.StatesAPI;
import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.manager.webui.utils.RepoFileUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.jose4j.lang.JoseException;

import java.io.IOException;
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
       Arrays.asList("rhncfg", "rhncfg-actions", "rhncfg-client", "rhn-virtualization-host")
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
        String machineId = SALT_SERVICE.getMachineId(minionId);
        if (machineId == null) {
            LOG.info("Cannot find machine id for minion: " + minionId);
            return;
        }
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
        try {
            // Create the server
            MinionServer server = new MinionServer();
            server.setMachineId(machineId);
            server.setMinionId(minionId);
            server.setName(minionId);
            server.setDigitalServerId(machineId);


            SALT_SERVICE.syncGrains(minionId);
            SALT_SERVICE.syncModules(minionId);

            ValueMap grains = new ValueMap(SALT_SERVICE.getGrains(minionId));

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

            server.updateServerInfo();

            mapHardwareGrains(server, grains);

            ServerFactory.save(server);

            SaltStateGeneratorService.INSTANCE.registerServer(server);

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

            triggerGetHardwareInfo(server, grains);
            triggerGetNetworkInfo(server, grains);

            // Assign the Salt base entitlement by default
            server.setBaseEntitlement(EntitlementManager.SALT);

            Map<String, String> data = new HashMap<>();
            data.put("minionId", minionId);
            data.put("machineId", machineId);
            SALT_SERVICE.sendEvent("susemanager/minion/registered", data);
            LOG.info("Finished minion registration: " + minionId);

            StatesAPI.generateServerPackageState(server);

            // Asynchronously get the uptime of this minion
            MessageQueue.publish(new MinionStartEventDatabaseMessage(minionId));

            // Generate the .repo file for this server
            try {
                RepoFileUtils.generateRepositoryFile(server);
            }
            catch (IOException | JoseException e) {
                LOG.error("Error generating repo file: " + e.getMessage());
            }

            // Refresh pillars before applying any states
            SALT_SERVICE.refreshPillar(minionId);

            // Apply initial states asynchronously
            MessageQueue.publish(new ApplyStatesEventMessage(
                    server.getId(),
                    ApplyStatesEventMessage.CERTIFICATE,
                    ApplyStatesEventMessage.CHANNELS,
                    ApplyStatesEventMessage.PACKAGES
            ));
        }
        catch (Throwable t) {
            LOG.error("Error registering minion for event: " + event, t);
        }
    }

    private void mapHardwareGrains(MinionServer server, ValueMap grains) {
        // for efficiency do this here
        server.setRam(grains.getValueAsLong("mem_total").orElse(0L));
    }

    private void triggerGetNetworkInfo(MinionServer server, ValueMap grains) {
        MessageQueue.publish(
               new GetNetworkInfoEventMessage(server.getId(), grains));
    }

    private void triggerGetHardwareInfo(MinionServer server, ValueMap grains) {
        MessageQueue.publish(new GetHardwareInfoEventMessage(server.getId()));
    }

}
