/*
 * Copyright (c) 2015--2021 SUSE LLC
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

import static com.suse.manager.webui.controllers.utils.ContactMethodUtil.isSSHPushContactMethod;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.common.util.RpmVersionComparator;
import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.OnboardingFailed;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
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
import com.redhat.rhn.frontend.dto.EssentialChannelDto;
import com.redhat.rhn.manager.distupgrade.DistUpgradeManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;

import com.suse.cloud.CloudPaygManager;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.controllers.StatesAPI;
import com.suse.manager.webui.services.SaltActionChainGeneratorService;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.impl.MinionPendingRegistrationService;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.pillar.MinionPillarManager;
import com.suse.manager.webui.utils.salt.custom.MinionStartupGrains;
import com.suse.manager.webui.utils.salt.custom.SystemInfo;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.errors.SaltError;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;
import com.suse.utils.Opt;

import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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

    private static final String FQDN = "fqdn";
    private static final String TERMINALS_GROUP_NAME = "TERMINALS";

    /**
     * Constructor taking a {@link SystemQuery} instance.
     *
     * @param systemQueryIn systemQuery instance for gathering data from a system.
     * @param saltApiIn saltApi instance for gathering data from a system.
     * @param paygMgrIn {@link CloudPaygManager} instance
     */
    public RegisterMinionEventMessageAction(SystemQuery systemQueryIn, SaltApi saltApiIn, CloudPaygManager paygMgrIn) {
        saltApi = saltApiIn;
        systemQuery = systemQueryIn;
        cloudPaygManager = paygMgrIn;
        VirtManager virtManager = new VirtManagerSalt(saltApi);
        MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);
        ServerGroupManager groupManager = new ServerGroupManager(saltApi);
        entitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(virtManager, monitoringManager, groupManager),
                new SystemEntitler(saltApi, virtManager, monitoringManager, groupManager)
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
                                    // Case 2.2a
                                    updateAlreadyRegisteredInfo(minionId, machineId, minionServer);
                                    applyMinionStartStates(minionId, minionServer, saltbootInitrd);
                                },
                                () -> {
                                    // Case 2.2b - Cleanup missing - salt DB got out of sync with Uyuni DB
                                    // Can only happen when salt key was deleted and same minion id
                                    // was accepted again
                                    String msg = String.format(
                                        "Systems with conflicting minion_id and machine-id were found (%s, %s). " +
                                        "Onboarding aborted. Please remove conflicting systems first (%s, %s)",
                                        minionId, machineId, minionServer.getId(), server.getId());
                                    LOG.error(msg);
                                    throw new RegisterMinionException(minionId, null, msg);
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

            if (cloudPaygManager.isPaygInstance() && CredentialsFactory.listSCCCredentials().size() == 0 &&
                    !RegistrationUtils.isAllowedOnPayg(systemQuery, minionId, Collections.emptySet(), grains)) {
                // BYOS or DC Instance is not allowed to register on a pure SUMA PAYG
                // exception: free products or SUSE Manager Proxy
                throw new RegisterMinionException(minionId, org, String.format(
                        "Registration of '%s' on SUSE Manager Server rejected. \n" +
                        "To manage BYOS (Bring-your-own-Subscription) or Datacenter clients you have to configure " +
                        "SCC Credentials at Admin => Setup Wizard => Organization Credentials.", minionId));
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
            String osrelease = getOsRelease(minionId, grains);

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

            // Check for Uyuni Server and create basic info
            SystemManager.updateMgrServerInfo(minion, grains);

            mapHardwareGrains(minion, grains);

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

            ServerFactory.save(minion);
            giveCapabilities(minion, isSaltSSH);

            // Assign the Salt base entitlement by default
            entitlementManager.setBaseEntitlement(minion, EntitlementManager.SALT);

            // apply activation key properties that need to be set after saving the minion
            if (activationKey.isPresent()) {
                RegistrationUtils.applyActivationKeyProperties(minion, activationKey.get(), grains);
            }

            // Saltboot treatment - prepare and apply saltboot
            if (saltbootInitrd) {
                prepareRetailMinionAndApplySaltboot(minion, org, grains);
                return;
            }

            systemInfo.getUptimeSeconds().ifPresent(us -> SaltUtils.handleUptimeUpdate(minion, us.longValue()));
            RegistrationUtils.finishRegistration(minion, activationKey, creator, !isSaltSSH, isSaltSSH);
        }
        catch (RegisterMinionException rme) {
            LOG.error("Error registering minion id: {}", minionId, rme);
            throw rme;
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
     * Extract management key label from grains
     *
     * @param grains
     * @return
     */
    private Optional<String> getManagementKeyLabelFromGrains(ValueMap grains) {
        //apply management key properties that can be set before saving the server
        return grains.getMap("susemanager")
                .flatMap(suma -> suma.getOptionalAsString("management_key"));
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
            if (!s.asMinionServer().isPresent()) {
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
                keys.stream().forEach(key -> {
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
        }

        //if not create a new server
        return new MinionServer();
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

    private static Optional<Channel> lookupBaseChannel(SUSEProduct sp, ChannelArch arch) {
        Optional<EssentialChannelDto> productBaseChannelDto =
                ofNullable(DistUpgradeManager.getProductBaseChannelDto(sp.getId(), arch));
        Optional<Channel> baseChannel = productBaseChannelDto
                .flatMap(base -> ofNullable(ChannelFactory.lookupById(base.getId())).map(c -> {
                    LOG.info("Base channel {} found for OS: {}, version: {}, arch: {}", c.getName(), sp.getName(),
                            sp.getVersion(), arch.getName());
            return c;
        }));
        if (!baseChannel.isPresent()) {
            LOG.warn("Product Base channel not found - refresh SCC sync?");
            return empty();
        }
        return baseChannel;
    }

    private Optional<String> rpmErrQueryRHELProvidesRelease(String minionId) {
        LOG.error("No package providing 'redhat-release' found on RHEL minion {}", minionId);
        return empty();
    }

    private Optional<String> rpmErrQueryRHELRelease(SaltError err, String minionId) {
        LOG.error("Error querying 'redhat-release' package on RHEL minion {}: {}", minionId, err);
        return empty();
    }

    private String unknownRHELVersion(String minionId) {
        LOG.error("Could not determine OS release version for RHEL minion {}", minionId);
        return "unknown";
    }

    private Map<String, List<String>> parseRHELReleseQuery(String result) {
        // Split the result into 3-line chunks per installed package version
        String[] resultLines = result.split("\\r?\\n");
        List<List<String>> resultItems = new ArrayList<>();
        for (int i = 0; i < resultLines.length;) {
            List<String> resultItem = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                resultItem.add(resultLines[i++]);
            }
            resultItems.add(resultItem);
        }

        // Create a map for each 3-line chunk from key-value pairs in each line
        return resultItems.stream()
                .map(list -> list.stream().map(line -> line.split("="))
                        .collect(Collectors.toMap(linetoks -> linetoks[0],
                                linetoks -> Arrays.asList(StringUtils
                                        .splitPreserveAllTokens(linetoks[1], ",")))))
                // Get the result map with the biggest rpm version
                .max(Comparator.comparing(pkgInfoMap -> pkgInfoMap.get("VERSION").get(0),
                        new RpmVersionComparator()))
                .get();
    }

    private String getOsRelease(String minionId, ValueMap grains) {
        // java port of up2dataUtils._getOSVersionAndRelease()
        String osRelease = grains.getValueAsString("osrelease");

        if ("redhat".equalsIgnoreCase(grains.getValueAsString("os")) ||
                "centos".equalsIgnoreCase(grains.getValueAsString("os")) ||
                "oel".equalsIgnoreCase(grains.getValueAsString("os")) ||
                "alibaba cloud (aliyun)".equalsIgnoreCase(grains.getValueAsString("os")) ||
                "almalinux".equalsIgnoreCase(grains.getValueAsString("os")) ||
                "amazon".equalsIgnoreCase(grains.getValueAsString("os")) ||
                "rocky".equalsIgnoreCase(grains.getValueAsString("os"))) {
            MinionList target = new MinionList(Arrays.asList(minionId));
            Optional<Result<String>> whatprovidesRes = saltApi.runRemoteCommand(target,
                    "rpm -q --whatprovides --queryformat \"%{NAME}\\n\" redhat-release")
                    .entrySet()
                    .stream()
                    .findFirst()
                    .map(e -> of(e.getValue()))
                    .orElse(empty());

            osRelease = whatprovidesRes.flatMap(res -> res.fold(
                    err -> err.fold(err1 -> rpmErrQueryRHELProvidesRelease(minionId),
                            err2 -> rpmErrQueryRHELProvidesRelease(minionId),
                            err3 -> rpmErrQueryRHELProvidesRelease(minionId),
                            err4 -> rpmErrQueryRHELProvidesRelease(minionId),
                            err5 -> rpmErrQueryRHELProvidesRelease(minionId)),
                    r -> of(r.split("\\r?\\n")[0]) // Take the first line if multiple results return
            ))
            .flatMap(pkgStr -> {
                String[] pkgs = StringUtils.split(pkgStr);
                if (pkgs.length > 1) {
                    LOG.warn("Multiple release packages are installed on minion: {}", minionId);
                    // Pick the package with the biggest precedence:
                    // sles_es > redhat > others (centos etc.)
                    return Arrays.stream(pkgs).max(Comparator.comparingInt(s -> {
                        switch (s) {
                        case "sles_es-release-server":
                            return 2;
                        case "redhat-release-server":
                            return 1;
                        default:
                            return 0;
                        }
                    }));
                }
                else {
                    return of(pkgs[0]);
                }
            })
            .flatMap(pkg ->
                saltApi.runRemoteCommand(target,
                        "rpm -q --queryformat \"" +
                            "VERSION=%{VERSION}\\n" +
                            "PROVIDENAME=[%{PROVIDENAME},]\\n" +
                            "PROVIDEVERSION=[%{PROVIDEVERSION},]\\n\" " + pkg)
                        .entrySet().stream().findFirst().map(Map.Entry::getValue)
                        .flatMap(res -> res.fold(
                                err -> err.fold(
                                        err1 -> rpmErrQueryRHELRelease(err1, minionId),
                                        err2 -> rpmErrQueryRHELRelease(err2, minionId),
                                        err3 -> rpmErrQueryRHELRelease(err3, minionId),
                                        err4 -> rpmErrQueryRHELRelease(err4, minionId),
                                        err5 -> rpmErrQueryRHELRelease(err5, minionId)),
                                Optional::of
                        ))
                        .map(result -> {
                            if (result.split("\\r?\\n").length > 3) {
                                // Output should be 3 lines per installed package version
                                LOG.warn(String.format(
                                        "Multiple versions of release package '%s' is installed on minion: %s",
                                        pkg, minionId));
                            }
                            return this.parseRHELReleseQuery(result);
                        })
                        .map(pkgtags -> {
                            Optional<String> version = Optional
                                    .ofNullable(pkgtags.get("VERSION"))
                                    .map(v -> v.stream().findFirst())
                                    .orElse(empty());
                            List<String> provideName = pkgtags.get("PROVIDENAME");
                            List<String> provideVersion = pkgtags.get("PROVIDEVERSION");
                            int idxReleasever = provideName
                                    .indexOf("system-release(releasever)");
                            if (idxReleasever > -1) {
                                version = provideVersion.size() > idxReleasever ?
                                        of(provideVersion.get(idxReleasever)) :
                                        empty();
                            }
                            return version;
                        })
                        .orElse(empty())
            )
            .orElseGet(() -> unknownRHELVersion(minionId));
        }
        return osRelease;
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
            if (e instanceof RegisterMinionException) {
                RegisterMinionException rme = (RegisterMinionException) e;
                NotificationMessage notificationMessage = UserNotificationFactory.createNotificationMessage(
                        new OnboardingFailed(rme.minionId, e.getMessage())
                );
                if (rme.org == null) {
                    UserNotificationFactory.storeNotificationMessageFor(notificationMessage,
                            Collections.singleton(RoleFactory.ORG_ADMIN), empty());
                }
                else {
                    UserNotificationFactory.storeNotificationMessageFor(notificationMessage,
                            Collections.singleton(RoleFactory.ORG_ADMIN), of(rme.org));
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
        RegisterMinionException(String minionIdIn, Org orgIn) {
            super();
            minionId = minionIdIn;
            org = orgIn;
        }
        RegisterMinionException(String minionIdIn, Org orgIn, String msgIn) {
            super(msgIn);
            minionId = minionIdIn;
            org = orgIn;
        }
    }
}
