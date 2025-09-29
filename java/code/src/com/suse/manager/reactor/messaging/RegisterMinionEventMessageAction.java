/*
 * Copyright (c) 2015--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.reactor.messaging;

import static com.suse.manager.webui.controllers.utils.ContactMethodUtil.isSSHPushContactMethod;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.OnboardingFailed;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.ContactMethod;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerArch;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.ServerHistoryEvent;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.cloud.CloudPaygManager;
import com.suse.manager.attestation.AttestationManager;
import com.suse.manager.model.attestation.ServerCoCoAttestationConfig;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.controllers.StatesAPI;
import com.suse.manager.webui.services.SaltActionChainGeneratorService;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.impl.MinionPendingRegistrationService;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.pillar.MinionPillarManager;
import com.suse.manager.webui.utils.salt.custom.MinionStartupGrains;
import com.suse.manager.webui.utils.salt.custom.SumaUtil.PublicCloudInstanceFlavor;
import com.suse.manager.webui.utils.salt.custom.SystemInfo;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.utils.Opt;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Event handler to create system records for salt minions.
 */
public class RegisterMinionEventMessageAction implements MessageAction {

    // Logger for this class
    private static final Logger LOG = LogManager.getLogger(
            RegisterMinionEventMessageAction.class);

    // Reference to the SaltService instance
    private final SaltApi saltApi;
    private final SystemQuery systemQuery;
    private final SystemEntitlementManager entitlementManager;

    private final CloudPaygManager cloudPaygManager;
    private final AttestationManager attestationManager;

    private static final String FQDN = "fqdn";
    private static final String TERMINALS_GROUP_NAME = "TERMINALS";

    /**
     * Constructor taking a {@link SystemQuery} instance.
     *
     * @param systemQueryIn systemQuery instance for gathering data from a system.
     * @param saltApiIn saltApi instance for gathering data from a system.
     * @param paygMgrIn {@link CloudPaygManager} instance
     * @param attMgrIn {@link AttestationManager} instance
     */
    public RegisterMinionEventMessageAction(SystemQuery systemQueryIn, SaltApi saltApiIn, CloudPaygManager paygMgrIn,
                                            AttestationManager attMgrIn) {
        saltApi = saltApiIn;
        systemQuery = systemQueryIn;
        cloudPaygManager = paygMgrIn;
        attestationManager = attMgrIn;
        entitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(saltApi), new SystemEntitler(saltApi)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(EventMessage msg) {
        RegisterMinionEventMessage registerMinionEventMessage = ((RegisterMinionEventMessage) msg);
        Optional<MinionStartupGrains> startupGrainsOpt = Opt.or(registerMinionEventMessage.getMinionStartupGrains(),
                () -> saltApi.getGrains(registerMinionEventMessage.getMinionId(),
                        new TypeToken<MinionStartupGrains>() { }, "machine_id", "saltboot_initrd", "susemanager"));
        registerMinion(registerMinionEventMessage.getMinionId(), false, empty(), empty(), empty(),  startupGrainsOpt);
    }

    /**
     * Temporary HACK: Run the registration for a minion with given id.
     * Will be extracted to a separate class, this is here only because of easier rebasing.
     * @param minionId minion id
     * @param sshPushPort the port to use for bootstrapping
     * @param proxyId the proxy to which the minion connects, if any
     * @param activationKeyOverride label of activation key to be applied to the system.
 *                              If left empty, activation key from grains will be used.
     */
    public void registerSSHMinion(String minionId, Integer sshPushPort, Optional<Long> proxyId,
                                  Optional<String> activationKeyOverride) {
        Optional<MinionStartupGrains> startupGrainsOpt = saltApi.getGrains(minionId,
                new TypeToken<>() {
                }, "machine_id", "saltboot_initrd", "susemanager");
        registerMinion(minionId, true, of(sshPushPort), proxyId, activationKeyOverride, startupGrainsOpt);
    }

    /**
     * Performs minion registration or reactivation.
     * @param minionId minion id
     * @param isSaltSSH true if a salt-ssh system is bootstrapped
     * @param sshPort the port to use for ssh only bootstrapping
     * @param activationKeyOverride label of activation key to be applied to the system.
     *                       If left empty, activation key from grains will be used.
     * @param startupGrains Grains needed for initial phase of registration
     */
    private void registerMinion(String minionId, boolean isSaltSSH, Optional<Integer> sshPort, Optional<Long> proxyId,
                                Optional<String> activationKeyOverride, Optional<MinionStartupGrains> startupGrains) {
        Opt.consume(startupGrains,
            ()-> LOG.error("Aborting: needed grains are not found for minion: {}",
                    StringUtil.sanitizeLogInput(minionId)),
            grains-> {
                boolean saltbootInitrd = grains.getSaltbootInitrd();
                Optional<String> mkey = grains.getSuseManagerGrain()
                        .flatMap(MinionStartupGrains.SuseManagerGrain::getManagementKey);
                Optional<ActivationKey> managementKey =
                        mkey.flatMap(mk -> ofNullable(ActivationKeyFactory.lookupByKey(mk)));
                Optional<String> validReactivationKey =
                        mkey.filter(mk -> isValidReactivationKey(managementKey, minionId));
                updateKickStartSession(managementKey);
                Optional<String> machineIdOpt = grains.getMachineId();
                Opt.consume(machineIdOpt,
                    ()-> LOG.error("Aborting: cannot find machine id for minion: {}", minionId),
                    machineId -> registerMinion(minionId, isSaltSSH, sshPort, proxyId, activationKeyOverride,
                            validReactivationKey, machineId, saltbootInitrd));
            });
    }


    /**
     * Performs minion registration or reactivation.
     *
     * When no re-activation key is provided, but existing machines were
     * found it will be handled this way:
     *
     * No system in the Uyuni DB found with the requested minion_id:
     *
     * Case 1.1: new minion_id and new machine-id => new registration
     * Case 1.2: new minion_id and existing machine-id => update the existing system with the new minion_id
     *
     * The requested minion_id already exists in the Uyuni DB:
     * This can only happen when somebody removed the salt-key manually, but did not
     * remove the system in the Uyuni Database. The new system is "wanted" so we try to cleanup the DB.
     *
     * Case 2.1: existing minion_id and new machine-id => migrate the existing minion
     * Case 2.2: existing minion_id and existing machine-id
     * Case 2.2a: minion_id and machine-id are the same as the new requested once => update the existing system
     * Case 2.2b: minion_id and/or machine-id are different => throw exception
     *
     * @param minionId minion id
     * @param isSaltSSH true if a salt-ssh system is bootstrapped
     * @param sshPort the port to use for ssh only bootstrapping
     * @param actKeyOverride label of activation key to be applied to the system.
     *                       If left empty, activation key from grains will be used.
     * @param reActivationKey valid reactivation key
     * @param machineId Machine Id of the minion
     * @param saltbootInitrd saltboot_initrd, to be used for retail minions
     */
    private void registerMinion(String minionId, boolean isSaltSSH, Optional<Integer> sshPort,
                                Optional<Long> saltSSHProxyId, Optional<String> actKeyOverride,
                                Optional<String> reActivationKey, String machineId, boolean saltbootInitrd) {
        Opt.consume(reActivationKey,
            //Case A: Registration
            () -> Opt.consume(ServerFactory.findByMachineId(machineId),
                () -> Opt.consume(MinionServerFactory.findByMinionId(minionId),
                        () -> {
                            // Case 1.1 - new registration
                            finalizeMinionRegistration(minionId, machineId, sshPort, saltSSHProxyId, actKeyOverride,
                                    isSaltSSH, saltbootInitrd);
                        },
                        minionServer -> {
                            // Case 2.1 - update found system with new values
                            LOG.warn(String.format(
                                    "A system with minion_id '%s' already exists, but with different " +
                                    "machine-id ( %s vs. %s). Updating existing system with System ID: %s",
                                    minionId, machineId, minionServer.getMachineId(), minionServer.getId()));
                            updateAlreadyRegisteredInfo(minionId, machineId, minionServer);
                            applyMinionStartStates(minionId, minionServer, saltbootInitrd);
                        }),
                server -> Opt.consume(MinionServerFactory.findByMinionId(minionId),
                        () -> {
                            // Case 1.2 - System got a new minion id
                            Opt.consume(server.asMinionServer(),
                                () -> {
                                    // traditional client wants migration to salt
                                    finalizeMinionRegistration(minionId, machineId, sshPort, saltSSHProxyId,
                                            actKeyOverride, isSaltSSH, saltbootInitrd);
                                },
                                registeredMinion -> {
                                    updateAlreadyRegisteredInfo(minionId, machineId, registeredMinion);
                                    applyMinionStartStates(minionId, registeredMinion, saltbootInitrd);
                                });
                        },
                        minionServer -> server.asMinionServer().filter(ms -> ms.equals(minionServer)).ifPresentOrElse(
                                serverAsMinion -> {
                                    // Case 2.2a - minion_id and machine-id are the same
                                    updateAlreadyRegisteredInfo(minionId, machineId, minionServer);
                                    applyMinionStartStates(minionId, minionServer, saltbootInitrd);
                                },
                                () -> {
                                    // Case 2.2b - Cleanup missing - salt DB got out of sync with Uyuni DB
                                    // Can only happen when salt key was deleted and same minion id
                                    // was accepted again
                                    Object[] args = {minionId, machineId, minionServer.getId().toString(),
                                            server.getId().toString()};
                                    LOG.error(LocalizationService.getInstance().getMessage(
                                            "bootstrap.minion.error.conflicting.minionid", Locale.US, args));
                                    throw new RegisterMinionException(minionId, null,
                                            "bootstrap.minion.error.conflicting.minionid", args);
                                }))),
            //Case B : Reactivation
            rk -> {
                reactivateSystem(minionId, machineId, rk);
                finalizeMinionRegistration(minionId, machineId, sshPort,
                    saltSSHProxyId, actKeyOverride, isSaltSSH, saltbootInitrd);
            }
        );
    }

    /**
     * Check if the specified management_key is valid reactivation key or not
     * @param activationKey activationKey
     * @param minionId minion Id
     * @return true/false based on if activation is a valid reaction key or not
     */
    private boolean isValidReactivationKey(Optional<ActivationKey> activationKey, String minionId) {
        return Opt.fold(activationKey,
            () -> {
                LOG.info("Outdated Management Key defined for {}: {}", minionId, activationKey);
                return false;
            },
            ak -> {
                if (Objects.isNull(ak.getServer())) {
                    if (Objects.isNull(ak.getKickstartSession())) {
                        LOG.error("Management Key is not a reactivation key: {}", ak.getKey());
                    }
                    return false;
                }
                //considered valid reactivation key only in this case
                return true;
            }
        );
    }

    /**
     * Apply the states needed for regular start of an already registered minion
     * @param minionId
     * @param registeredMinion
     * @param saltbootInitrd
     */
    private void applyMinionStartStates(String minionId, MinionServer registeredMinion, boolean saltbootInitrd) {
        if (saltbootInitrd) {
            // if we have the "saltboot_initrd" grain we want to re-deploy an image via saltboot,
            LOG.info("Applying saltboot for minion {}", minionId);
            applySaltboot(registeredMinion, true);
        }
        else {
            // this is either regular minion
            // or saltboot minion which already finished initrd phase
            // this is the time to update SystemInfo
            MinionList minionTarget = new MinionList(minionId);
            saltApi.updateSystemInfo(minionTarget);
            scheduleCoCoAttestation(registeredMinion);
            schedulePackageListRefresh(registeredMinion);
        }
    }

    /**
     * Schedule a Confidential Compute Attestation when the minion has CoCoAttestation
     * enabled and attestOnBoot is enabled
     *
     * @param minion the minion
     */
    private void scheduleCoCoAttestation(MinionServer minion) {
        if (minion.getOptCocoAttestationConfig()
                .filter(ServerCoCoAttestationConfig::isEnabled)
                .filter(ServerCoCoAttestationConfig::isAttestOnBoot)
                .isEmpty()) {
            // no attestation configured or wanted on startup
            return;
        }

        try {
            // eariest 1 minute later to finish the boot process
            // randomize a bit to prevent an attestation storm on a mass reboot action
            int rand = ThreadLocalRandom.current().nextInt(60, 90);
            Date scheduleAt = Date.from(Instant.now().plus(rand, ChronoUnit.SECONDS));
            attestationManager.scheduleAttestationActionFromSystem(minion.getOrg(), minion, scheduleAt);
        }
        catch (TaskomaticApiException e) {
            LOG.error("Unable to schedule attestation action. ", e);
        }
    }

    /**
     * Schedule a package list refresh when the minion supports transactional update
     *
     * @param minion the minion
     */
    private void schedulePackageListRefresh(MinionServer minion) {
        if (!minion.doesOsSupportsTransactionalUpdate()) {
            // no package list refresh wanted on startup
            return;
        }

        try {
            // eariest 1 minute later to finish the boot process
            // randomize a bit to prevent a package list refresh storm on a mass reboot action
            int rand = ThreadLocalRandom.current().nextInt(60, 90);
            Date scheduleAt = Date.from(Instant.now().plus(rand, ChronoUnit.SECONDS));
            ActionManager.schedulePackageRefresh(Optional.empty(), minion, scheduleAt);
        }
        catch (TaskomaticApiException e) {
            LOG.error("Unable to schedule package list refresh action. ", e);
        }
    }

    /**
     * Reactivate the system
     * @param minionId minion id of the minion
     * @param machineId machine_id of the minion
     * @param reActivationKey valid Reaction key
     */
    private void reactivateSystem(String minionId, String machineId, String reActivationKey) {
        // The machine id may have changed, but we know from the reactivation key
        // which system should become this one
        of(ActivationKeyFactory.lookupByKey(reActivationKey))
                .flatMap(ak -> ak.getServer().asMinionServer())
                .ifPresent(minion -> {
                    minion.setMachineId(machineId);
                    minion.setMinionId(minionId);
        });
    }

    /**
     * Update information of already registered minion, in case minion_id is different
     * or machine-id is different.
     * @param minionId the new minion id
     * @param machineId the new machine id
     * @param registeredMinion existing registered minion
     */
    public void updateAlreadyRegisteredInfo(String minionId, String machineId, MinionServer registeredMinion) {
        String oldMinionId = registeredMinion.getMinionId();
        String oldMachineId = registeredMinion.getMachineId();
        if (!minionId.equals(oldMinionId)) {
            LOG.warn("Minion '{}' already registered, updating profile to '{}' [{}]", oldMinionId, minionId,
                    registeredMinion.getMachineId());
            registeredMinion.setName(minionId);
            registeredMinion.setMinionId(minionId);
            ServerFactory.save(registeredMinion);

            MinionPillarManager.INSTANCE.generatePillar(registeredMinion);

            saltApi.deleteKey(oldMinionId);
            SystemManager.addHistoryEvent(registeredMinion, "Duplicate Minion ID", "Minion '" +
                    oldMinionId + "' has been updated to '" + minionId + "'");
        }
        else if (!machineId.equals(oldMachineId)) {
            SaltStateGeneratorService.INSTANCE.removeConfigChannelAssignments(registeredMinion);
            SaltActionChainGeneratorService.INSTANCE.removeActionChainSLSFilesForMinion(
                    registeredMinion.getMachineId(), Optional.empty());
            StatesAPI.removePackageState(registeredMinion.getMachineId());

            registeredMinion.setMachineId(machineId);
            registeredMinion.setDigitalServerId(machineId);
            ServerFactory.save(registeredMinion);

            ServerStateRevision serverRev = StateFactory
                    .latestStateRevision(registeredMinion)
                    .orElseGet(() -> {
                        ServerStateRevision rev = new ServerStateRevision();
                        rev.setServer(registeredMinion);
                        return rev;
                    });
            SaltStateGeneratorService.INSTANCE.generateConfigState(serverRev);
            StatesAPI.generateServerPackageState(registeredMinion);

            SystemManager.addHistoryEvent(registeredMinion, "Minion migrated", "The machine-id of Minion '" +
                    minionId + "' has been updated from '" + oldMachineId + "' to '" + machineId + "'");
        }
    }

    /**
     * Mark the kickstart session of activation key as complete if activation key has kickstart session
     * @param activationKey activationKey key
     */
    private void updateKickStartSession(Optional<ActivationKey> activationKey) {
        activationKey.ifPresent(ak -> {
            if (Objects.nonNull(ak.getKickstartSession())) {
                ak.getKickstartSession().markComplete("Installation completed.");
            }
        });
    }

    /**
     * Complete the minion registration with information from grains
     * @param minionId the minion id
     * @param machineId the machine id that we are trying to register
     * @param sshPort the port to use for ssh only bootstrapping
     * @param saltSSHProxyId optional proxy id for saltssh in case it is used
     * @param activationKeyOverride optional label of activation key to be applied to the system
     * @param isSaltSSH true if a salt-ssh system is bootstrapped
     * @param saltbootInitrd true if saltboot initrd grain is set
     */
    public void finalizeMinionRegistration(String minionId,
                                           String machineId,
                                           Optional<Integer> sshPort,
                                           Optional<Long> saltSSHProxyId,
                                           Optional<String> activationKeyOverride,
                                           boolean isSaltSSH,
                                           boolean saltbootInitrd) {
        Optional<User> creator = MinionPendingRegistrationService.getCreator(minionId);
        Org org = null;

        try {
            SystemInfo systemInfo = saltApi.getSystemInfoFull(minionId)
                .orElseThrow(() -> new SaltException("Missing systeminfo result. Aborting registration."));

            ValueMap grains = systemInfo.getGrains();
            PublicCloudInstanceFlavor instanceFlavor = PublicCloudInstanceFlavor.UNKNOWN;
            if (cloudPaygManager.isPaygInstance() && !cloudPaygManager.hasSCCCredentials()) {
                instanceFlavor = saltApi.getInstanceFlavor(minionId);
                if (!RegistrationUtils.isAllowedOnPayg(systemQuery, minionId, Collections.emptySet(), grains,
                                                       instanceFlavor)) {
                    Object[] args = {minionId};
                    // If the minion is not in the cloud
                    if (grains.getValueAsString("instance_id").isEmpty()) {
                        // DC instances are not allowed to be onboarded without SCC credentials
                        throw new RegisterMinionException(minionId, org, "bootstrap.minion.error.payg.dcregistered",
                                                          args);
                    }
                    else {
                        // BYOS in cloud instances is not allowed to register on a pure SUMA PAYG
                        // exception: free products or SUSE Manager Proxy
                        // Attention: minion could be PAYG, so it might lack of package `instance-flavor-check`
                        throw new RegisterMinionException(minionId, org,
                                                          "bootstrap.minion.error.payg.byosregistered", args);
                    }
                }
            }

            MinionServer minion = migrateOrCreateSystem(minionId, isSaltSSH, activationKeyOverride, machineId, grains);

            minion.setMachineId(machineId);
            minion.setMinionId(minionId);
            minion.setName(minionId);
            minion.setDigitalServerId(machineId);

            Optional<String> activationKeyLabel = getActivationKeyLabelFromGrains(grains, activationKeyOverride);
            Optional<ActivationKey> activationKey = activationKeyLabel.map(ActivationKeyFactory::lookupByKey);

            String master = systemInfo.getMaster()
                    .orElseThrow(() -> new SaltException(
                            "Master not found in minion configuration"));

            org = activationKey.map(ActivationKey::getOrg)
                            .orElseGet(() -> creator.map(User::getOrg)
                            .orElseGet(() -> getProxyOrg(master, isSaltSSH, saltSSHProxyId)
                            .orElseGet(OrgFactory::getSatelliteOrg)));
            if (minion.getOrg() == null) {
                minion.setOrg(org);
            }
            else if (!minion.getOrg().equals(org)) {
                // only log activation key ignore message when the activation key is not empty
                String ignoreAKMessage = activationKey.map(ak -> "Ignoring activation key " + ak + ".").orElse("");
                LOG.error("The existing server organization ({}) does not match the organization selected for " +
                        "registration ({}). Keeping the existing server organization. {}", minion.getOrg(), org,
                        ignoreAKMessage);
                activationKey = empty();
                org = minion.getOrg();
                SystemManager.addHistoryEvent(minion, "Invalid Server Organization",
                        "The existing server organization (" + minion.getOrg() + ") does not match the " +
                                "organization selected for registration (" + org + "). Keeping the " +
                                "existing server organization. " + ignoreAKMessage);
            }

            // Set creator to the user who accepted the key if available
            minion.setCreator(creator.orElse(null));

            String osfullname = grains.getValueAsString("osfullname");
            String osfamily = grains.getValueAsString("os_family");
            String osrelease = grains.getOptionalAsString("osrelease").orElse("unknown");

            String kernelrelease = grains.getValueAsString("kernelrelease");
            String osarch = grains.getValueAsString("osarch");
            String cpe = grains.getValueAsString("cpe");

            minion.setOs(osfullname);
            minion.setOsFamily(osfamily);
            minion.setRelease(osrelease);
            minion.setRunningKernel(kernelrelease);
            minion.setSecret(RandomStringUtils.random(64, 0, 0, true, true, null, new SecureRandom()));
            minion.setAutoUpdate("N");
            minion.setLastBoot(System.currentTimeMillis() / 1000);
            minion.setCreated(new Date());
            minion.setModified(minion.getCreated());
            minion.setContactMethod(getContactMethod(activationKey, isSaltSSH, minionId));
            minion.setHostname(grains.getOptionalAsString(FQDN).orElse(null));
            minion.setCpe(cpe);
            systemInfo.getKernelLiveVersion().ifPresent(minion::setKernelLiveVersion);
            minion.setPayg(instanceFlavor.equals(PublicCloudInstanceFlavor.PAYG));

            String serverArch = String.format("%s-%s", osarch,
                    osfamily.equals("Debian") ? "debian-linux" : "redhat-linux");
            ServerArch arch = ServerFactory.lookupServerArchByLabel(serverArch);
            if (arch == null) {
                LOG.error("Unable to find the server architecture for " +
                        "osfamily: '{}' and osarch: '{}'", osfamily, osarch);
                throw new IllegalArgumentException("Unable to get the server architecture");
            }
            minion.setServerArch(arch);

            if (!saltbootInitrd) {
                RegistrationUtils.subscribeMinionToChannels(systemQuery, minion, grains, activationKey,
                    activationKeyLabel);
            }
            minion.updateServerInfo();
            mapHardwareGrains(minion, grains);
            ServerFactory.save(minion);

            if (isSaltSSH) {
                minion.updateServerPaths(saltSSHProxyId);
                minion.setSSHPushPort(sshPort.orElse(SaltSSHService.SSH_PUSH_PORT));
            }
            else {
                minion.updateServerPaths(master);
                //it might be useful during re-activation from pushSSH to salt
                if (minion.getSSHPushPort() != null && minion.getSSHPushPort() != 0) {
                    minion.setSSHPushPort(null);
                }
            }

            giveCapabilities(minion, isSaltSSH);

            // Assign the Salt base entitlement by default
            entitlementManager.setBaseEntitlement(minion, EntitlementManager.SALT);

            // apply activation key properties that need to be set after saving the minion
            activationKey.ifPresent(activationKeyIn ->
                    RegistrationUtils.applyActivationKeyProperties(minion, activationKeyIn, grains));

            // Saltboot treatment - prepare and apply saltboot
            if (saltbootInitrd) {
                prepareRetailMinionAndApplySaltboot(minion, org, grains);
                return;
            }

            systemInfo.getUptimeSeconds().ifPresent(us -> SaltUtils.handleUptimeUpdate(minion, us.longValue()));
            RegistrationUtils.finishRegistration(minion, activationKey, creator, !isSaltSSH, isSaltSSH);
            ServerFactory.save(minion);
        }
        catch (RegisterMinionException rme) {
            LOG.error("Error registering minion id: {}", minionId, rme);
            throw rme;
        }
        catch (JsonSyntaxException t) {
            //log error without stack trace
            LOG.error("Error registering minion id [{}]: {}", minionId, t.getMessage());
            //convert into RegisterMinionException without stack trace
            RegisterMinionException exception = new RegisterMinionException(minionId, org, t.getMessage());
            exception.setStackTrace(new StackTraceElement[0]);
            throw exception;
        }
        catch (Exception t) {
            LOG.error("Error registering minion id: {}", minionId, t);
            throw new RegisterMinionException(minionId, org);
        }
        finally {
            if (MinionPendingRegistrationService.containsMinion(minionId)) {
                MinionPendingRegistrationService.removeMinion(minionId);
            }
        }
    }

    private Optional<Org> getProxyOrg(String master, boolean isSaltSSH, Optional<Long> saltSSHProxyId) {
        if (isSaltSSH) {
            return saltSSHProxyId
                    .map(ServerFactory::lookupById)
                    .map(Server::getOrg);
        }
        else {
            return ServerFactory.lookupProxyServer(master).map(Server::getOrg);
        }
    }

    /**
     * Extract HW addresses (except the localhost one) from grains
     * @param grains the grains
     * @return HW addresses
     */
    private Set<String> extractHwAddresses(ValueMap grains) {
        Map<String, String> hwInterfaces = (Map<String, String>) grains
                .get("hwaddr_interfaces").orElse(Collections.emptyMap());
        return hwInterfaces.values().stream()
                .filter(hwAddress -> !hwAddress.equalsIgnoreCase("00:00:00:00:00:00"))
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    /**
     * Extract activation key label from grains
     *
     * @param grains
     * @param activationKeyOverride optional label of activation key to be applied to the system
     * @return
     */
    private Optional<String> getActivationKeyLabelFromGrains(ValueMap grains, Optional<String> activationKeyOverride) {
        //apply activation key properties that can be set before saving the server
        Optional<String> activationKeyFromGrains = grains
                .getMap("susemanager")
                .flatMap(suma -> suma.getOptionalAsString("activation_key"));
        return activationKeyOverride.isPresent() ? activationKeyOverride : activationKeyFromGrains;
    }

    /**
     * Prepare saltboot configuration for retail minion and apply saltboot state
     *
     * @param minion - the minion
     * @param org - the organization in which minion is to be registered
     * @param grains - grains
     */
    private void prepareRetailMinionAndApplySaltboot(MinionServer minion, Org org, ValueMap grains) {
        String minionId = minion.getMinionId();
        LOG.info("\"saltboot_initrd\" grain set to true: Preparing & applying saltboot for minion {}", minionId);
        try {
            prepareRetailMinionForSaltboot(minion, org, grains);
            applySaltboot(minion, false);
        }
        catch (Exception t) {
            LOG.error("Error preparing saltboot configuration for minion id: {}", minionId, t);
            // do not throw, leave the minion as registered in initrd
        }
    }

    /**
     * Prepare a retail minion to be registered in SUMA:
     *  - assign needed groups
     *  - generate pillar
     *
     * @param minion - the minion
     * @param org - the organization in which minion is to be registered
     * @param grains - grains
     */
    private void prepareRetailMinionForSaltboot(MinionServer minion, Org org, ValueMap grains) {
        Optional<String> manufacturer = grains.getOptionalAsString("manufacturer");
        Optional<String> productName = grains.getOptionalAsString("productname");
        Optional<String> branchId = grains.getOptionalAsString("minion_id_prefix");

        if (!manufacturer.isPresent() || !productName.isPresent() || !branchId.isPresent()) {
            SystemManager.addHistoryEvent(minion, "Saltboot configuration failed",
                    "Missing machine manufacturer, product name or minion_id_prefix " +
                    "on retail minion registration! Aborting saltboot configuration.");

            throw new IllegalStateException("Missing machine manufacturer, product name or minion_id_prefix " +
                    "on retail minion registration! Aborting saltboot configuration.");
        }

        String hwTypeGroupPrefix = "HWTYPE:";
        String hwType = manufacturer.get() + "-" + productName.get();
        String hwTypeGroup = hwTypeGroupPrefix + hwType.replaceAll("[^A-Za-z0-9_-]", "");

        String branchIdGroupName = branchId.get();
        ManagedServerGroup terminalsGroup = ServerGroupFactory.lookupByNameAndOrg(TERMINALS_GROUP_NAME, org);
        ManagedServerGroup branchIdGroup = ServerGroupFactory.lookupByNameAndOrg(branchIdGroupName, org);
        ManagedServerGroup hwGroup = ServerGroupFactory.lookupByNameAndOrg(hwTypeGroup, org);

        if (branchIdGroup == null) {
            SystemManager.addHistoryEvent(minion, "Saltboot configuration failed", "Missing required server group (\"" +
                    branchIdGroupName + "\")!" + " Aborting saltboot configuration.");

            throw new IllegalStateException("Missing required server group (\"" + branchIdGroupName + "\")!" +
                    " Aborting saltboot configuration.");
        }

        SystemManager systemManager = new SystemManager(ServerFactory.SINGLETON, ServerGroupFactory.SINGLETON, saltApi);
        systemManager.addServerToServerGroup(minion, branchIdGroup);
        if (terminalsGroup != null) {
            systemManager.addServerToServerGroup(minion, terminalsGroup);
        }
        if (hwGroup != null) {
            // if the system is already assigned to some HWTYPE group, skip assignment and log this only
            if (minion.getManagedGroups().stream().anyMatch(g -> g.getName().startsWith(hwTypeGroupPrefix))) {
                LOG.info("Skipping assignment of the minion {} to HW group {}. The minion is already in a HW group.",
                        minion, hwGroup);
            }
            else {
                systemManager.addServerToServerGroup(minion, hwGroup);
                SystemManager.addHistoryEvent(minion, "Saltboot HWTYPE group assigned",
                        "Minion assigned to HW group \"" + hwTypeGroup + "\"");
            }
        }
        else {
            SystemManager.addHistoryEvent(minion, "Saltboot HWTYPE group not found",
                    "HWTYPE group does not exist: \"" + hwTypeGroup + "\"");
        }

        minion.asMinionServer().ifPresent(MinionPillarManager.INSTANCE::generatePillar);
    }

    private void applySaltboot(MinionServer minion, boolean sync) {
        List<String> states = new ArrayList<>();
        if (sync) {
            states.add(ApplyStatesEventMessage.SYNC_STATES);
        }
        states.add(ApplyStatesEventMessage.SALTBOOT);

        MessageQueue.publish(new ApplyStatesEventMessage(minion.getId(), false, states));
    }

    /**
     * If exists, migrate a server (either traditional or bootstrap one, in this order) to a minion.
     * Otherwise return a new MinionServer instance.
     *
     * @param minionId the minion id
     * @param isSaltSSH true if salt-ssh minion
     * @param activationKeyOverride the activation key
     * @param machineId the machine-id
     * @param grains the grains
     * @return a migrated or new MinionServer instance
     */
    private MinionServer migrateOrCreateSystem(String minionId,
            boolean isSaltSSH,
            Optional<String> activationKeyOverride,
            String machineId,
            ValueMap grains) {

        Optional<Server> server = ServerFactory.findByMachineId(machineId);

        if (server.isPresent()) {
            Server s = server.get();
            if (s.asMinionServer().isEmpty()) {
                // change the type of the hibernate entity from Server to MinionServer
                SystemManager.addMinionInfoToServer(s.getId(), minionId);
                // need to clear the session to avoid NonUniqueObjectException
                HibernateFactory.getSession().clear();
            }
            Optional<MinionServer> optMinionServer = MinionServerFactory.lookupById(s.getId());
            if (optMinionServer.isPresent()) {
                MinionServer minion = optMinionServer.get();
                // hardware will be refreshed anyway
                // new secret will be generated later

                // set new contact method in case it has changed
                // to prevent using the old one to communicate
                // with the minion. (bsc#1040394)
                Optional<ActivationKey> activationKey = activationKeyOverride
                        .map(ActivationKeyFactory::lookupByKey);
                minion.setContactMethod(
                        getContactMethod(activationKey, isSaltSSH, minionId));

                // remove package profile
                minion.getPackages().clear();

                // clear config channels
                minion.setConfigChannels(Collections.emptyList(), minion.getCreator());

                // clear previous capabilities
                minion.getCapabilities().clear();

                // Remove lock if exist
                if (minion.getLock() != null) {
                    SystemManager.unlockServer(minion);
                }

                // Remove relations to previously used activation keys
                List<ActivationKey> keys = ActivationKeyFactory.lookupByActivatedServer(minion);
                keys.forEach(key -> {
                    Set<Server> activatedServers = key.getToken().getActivatedServers();
                    activatedServers.remove(minion);
                });

                // add reactivation event to server history
                ServerHistoryEvent historyEvent = new ServerHistoryEvent();
                historyEvent.setCreated(new Date());
                historyEvent.setServer(minion);
                historyEvent.setSummary("Server reactivated as Salt minion");
                historyEvent.setDetails(
                        "System type was changed from Management to Salt");
                minion.getHistory().add(historyEvent);

                SystemManager.updateSystemOverview(minion.getId());
                return minion;
            }
        }

        //if minion was not found, fallback to FQDN and hwAddrs found using previous grains
        if (grains.isPresent()) {
            Optional<String> fqdn = grains.getOptionalAsString(FQDN);
            Set<String> hwAddrs = extractHwAddresses(grains);
            Optional<MinionServer> optMinionServer =  findMatchingEmptyProfiles(fqdn, hwAddrs);
            if (optMinionServer.isPresent()) {
                return optMinionServer.get();
            }
            optMinionServer = migrateFromContainerProxy(minionId, fqdn);
            if (optMinionServer.isPresent()) {
                return optMinionServer.get();
            }
        }

        //if not create a new server
        return new MinionServer();
    }

    private Optional<MinionServer> migrateFromContainerProxy(String minionId, Optional<String> fqdn) {
        if (fqdn.isPresent()) {
            Optional<Server> proxyServer = ServerFactory.listByFqdn(fqdn.get())
                    .stream()
                    .filter(Server::isProxy)
                    .filter(Server::isForeign)
                    .findFirst();

            if (proxyServer.isPresent()) {
                Server s = proxyServer.get();
                if (s.asMinionServer().isEmpty()) {
                    // change the type of the hibernate entity from Server to MinionServer
                    SystemManager.addMinionInfoToServer(s.getId(), minionId);
                    // need to clear the session to avoid NonUniqueObjectException
                    HibernateFactory.getSession().clear();
                }
                return MinionServerFactory.lookupById(s.getId());
            }
        }
        return Optional.empty();
    }

    private Optional<MinionServer> findMatchingEmptyProfiles(Optional<String> hostname, Set<String> hwAddrs) {
        List<MinionServer> matchingEmptyProfiles = SystemManager.findMatchingEmptyProfiles(hostname, hwAddrs);
        if (matchingEmptyProfiles.isEmpty()) {
            return empty();
        }
        if (matchingEmptyProfiles.size() == 1) {
            return of(matchingEmptyProfiles.get(0));
        }
        throw new IllegalStateException(matchingEmptyProfiles.size() + " matching empty profiles found when matching" +
                " with " + hostname.map(n -> "hostname: " + n + " and ").orElse("") + " HW addresseses: " + hwAddrs);
    }

    private void giveCapabilities(MinionServer server, boolean isSaltSSH) {
        // Salt systems always have the script.run capability
        SystemManager.giveCapability(server.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        if (!isSaltSSH) {
            // Not Salt ssh systems can be audited
            SystemManager.giveCapability(server.getId(), SystemManager.CAP_SCAP, 1L);
        }

        //Capabilities to enable configuration management for minions
        SystemManager.giveCapability(server.getId(),
                SystemManager.CAP_CONFIGFILES_BASE64_ENC, 1L);
        SystemManager.giveCapability(server.getId(),
                SystemManager.CAP_CONFIGFILES_DEPLOY, 1L);
        SystemManager.giveCapability(server.getId(),
                SystemManager.CAP_CONFIGFILES_MTIME_UPLOAD, 1L);
        SystemManager.giveCapability(server.getId(),
                SystemManager.CAP_CONFIGFILES_DIFF, 1L);
        SystemManager.giveCapability(server.getId(),
                SystemManager.CAP_CONFIGFILES_UPLOAD, 1L);
    }

    private ContactMethod getContactMethod(Optional<ActivationKey> activationKey,
            boolean isSshPush,
            String minionId) {
        return Opt.fold(
                activationKey,
                () -> isSshPush ?
                        ServerFactory.findContactMethodByLabel("ssh-push") :
                        ServerFactory.findContactMethodByLabel("default"),
                ak -> {
                    if (!isSshPush && isSSHPushContactMethod(ak.getContactMethod())) {
                        LOG.warn("Contact method changed from ssh-push to default for minion id {}. Please use webui " +
                                "for salt-ssh minions.", minionId);
                        return ServerFactory.findContactMethodByLabel("default");
                    }
                    return ak.getContactMethod();
                }
        );
    }

    private void mapHardwareGrains(MinionServer server, ValueMap grains) {
        // for efficiency do this here
        server.setRam(grains.getValueAsLong("mem_total").orElse(0L));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRunConcurrently() {
        return true;
    }

    @Override
    public Consumer<Exception> getExceptionHandler() {
        return e -> {
            if (e instanceof RegisterMinionException rme) {
                NotificationMessage notificationMessage = UserNotificationFactory.createNotificationMessage(
                        new OnboardingFailed(rme.minionId, e.getLocalizedMessage())
                );
                if (rme.org == null) {
                    UserNotificationFactory.storeNotificationMessageFor(notificationMessage, RoleFactory.ORG_ADMIN);
                }
                else {
                    UserNotificationFactory.storeNotificationMessageFor(notificationMessage,
                            RoleFactory.ORG_ADMIN, of(rme.org));
                }
            }
        };
    }

    /**
     * Represents an Exception during the registration process.
     */
    public class RegisterMinionException extends RuntimeException {
        private final String minionId;
        private final Org org;
        private final String messageId;
        private final transient Object [] arguments;

        RegisterMinionException(String minionIdIn, Org orgIn) {
            super();
            messageId = "";
            minionId = minionIdIn;
            org = orgIn;
            arguments = null;
        }
        RegisterMinionException(String minionIdIn, Org orgIn, String msgIn) {
            super(msgIn);
            messageId = "";
            minionId = minionIdIn;
            org = orgIn;
            arguments = null;
        }

        RegisterMinionException(String minionIdIn, Org orgIn, String msgId, Object [] args) {
            super(LocalizationService.getInstance().getMessage(msgId, Locale.US, args));
            messageId = msgId;
            minionId = minionIdIn;
            org = orgIn;
            arguments = args;
        }

        /**
         * @return return the message localized - if it was translated
         */
        @Override
        public String getLocalizedMessage() {
            if (messageId.isEmpty()) {
                return getMessage();
            }
            return LocalizationService.getInstance().getMessage(messageId, arguments);
        }
    }
}
