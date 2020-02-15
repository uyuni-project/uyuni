package com.suse.manager.tasks.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import com.redhat.rhn.common.util.RpmVersionComparator;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.OnboardingFailed;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.*;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.EssentialChannelDto;
import com.redhat.rhn.manager.distupgrade.DistUpgradeManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.suse.manager.reactor.messaging.RegistrationUtils;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.ActorManager;
import com.suse.manager.tasks.Command;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.impl.MinionPendingRegistrationService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.errors.SaltError;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;
import com.suse.utils.Opt;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static akka.actor.typed.javadsl.Behaviors.*;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;
import static com.suse.manager.reactor.SaltReactor.THREAD_POOL_SIZE;
import static com.suse.manager.webui.controllers.utils.ContactMethodUtil.isSSHPushContactMethod;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

public class RegisterMinionActor implements Actor {

    private final static Logger LOG = Logger.getLogger(RegisterMinionActor.class);
    // Reference to the SaltService instance
    private static final SaltService SALT_SERVICE = SaltService.INSTANCE;
    private static final String FQDN = "fqdn";
    private static final String TERMINALS_GROUP_NAME = "TERMINALS";

    @Override
    public int getMaxParallelWorkers() {
        return THREAD_POOL_SIZE;
    }

    public static class Message implements Command {
        private final String minionId;

        public Message(String minionId) {
            this.minionId = minionId;
        }
    }

    public Behavior<Command> create(ActorRef<Command> guardian) {
        return setup(context -> receive(Command.class)
                .onMessage(Message.class, message -> onMessage(message))
                .build());
    }

    private Behavior<Command> onMessage(Message message) {
        handlingTransaction(() -> execute(message), getExceptionHandler());
        return same();
    }

    public void execute(Message msg) {
        LOG.debug("processing RegisterMinionActor for minion" + msg.minionId);
        registerMinion(msg.minionId);
    }

    public static void registerMinion(String minionId){
        registerMinion(minionId, false, empty(), empty());
    }


    /**
     * Temporary HACK: Run the registration for a minion with given id.
     * Will be extracted to a separate class, this is here only because of easier rebasing.
     * @param minionId minion id
     * @param activationKeyOverride label of activation key to be applied to the system.
     *                              If left empty, activation key from grains will be used.
     * @param proxyId the proxy to which the minion connects, if any
     */
    public static void registerSSHMinion(String minionId, Optional<Long> proxyId,
                                  Optional<String> activationKeyOverride) {
        registerMinion(minionId, true, proxyId, activationKeyOverride);
    }

    /**
     * Performs minion registration.
     *
     * @param minionId minion id
     * @param isSaltSSH true if a salt-ssh system is bootstrapped
     * @param actKeyOverride label of activation key to be applied to the system.
     *                              If left empty, activation key from grains will be used.
     */
    private static void registerMinion(String minionId, boolean isSaltSSH,
                                Optional<Long> saltSSHProxyId,
                                Optional<String> actKeyOverride) {

        // Match minions via their machine id
        Opt.fold(SALT_SERVICE.getMachineId(minionId),
            () -> {
                LOG.info("Cannot find machine id for minion: " + minionId);
                return false;
            },
            machineId -> {
                boolean isReactivation = false;
                ActivationKey ak = null;
                ValueMap grains = new ValueMap(SALT_SERVICE.getGrains(minionId).orElseGet(HashMap::new));
                Optional<String> managmentKeyLabel = getManagementKeyLabelFromGrains(grains);
                if (managmentKeyLabel.isPresent()) {
                    ak = ActivationKeyFactory.lookupByKey(managmentKeyLabel.get());
                    if (ak != null && ak.getKickstartSession() != null) {
                        ak.getKickstartSession().markComplete("Installation completed.");
                    }
                    if (ak == null) {
                        LOG.info("Outdated Management Key defined for " + minionId + ": " + managmentKeyLabel.get());
                    }
                    else if (ak.getServer() == null) {
                        LOG.error("Management Key is not a reactivation key: " + managmentKeyLabel.get());
                    }
                    else {
                        isReactivation = true;
                    }
                }

                Optional<User> creator = MinionPendingRegistrationService.getCreator(minionId);
                if (!isReactivation &&
                        checkIfMinionAlreadyRegistered(minionId, machineId, creator, isSaltSSH, grains)) {
                    return true;
                }
                // Check if this minion id already exists
                if (!isReactivation && duplicateMinionNamePresent(minionId)) {
                    return false;
                }
                if (isReactivation) {
                    reactivateSystem(minionId, machineId, creator, grains, Optional.ofNullable(ak));
                }
                finalizeMinionRegistration(minionId, machineId, creator, saltSSHProxyId,
                        actKeyOverride.isPresent() ? actKeyOverride : Optional.empty(),
                        isSaltSSH, grains);
                return true;
            }
        );
    }

    private static void reactivateSystem(String minionId, String machineId, Optional<User> creator,
            ValueMap grains, Optional<ActivationKey> reactivationKey) {
        // The machine id may have changed, but we know from the reactivation key
        // which system should become this one
        reactivationKey
            .flatMap(rak -> rak.getServer().asMinionServer())
            .ifPresent(minion -> {
                minion.setMachineId(machineId);
                minion.setMinionId(minionId);
            });
    }

    /**
     * Check if a minion is already registered and update it in case so
     * @param minionId the minion id
     * @param machineId the machine id that we are trying to register
     * @param creator the optional User that created the minion
     * @param isSaltSSH true if a salt-ssh system is bootstrapped
     * @param grains grains of the minion
     * @return true if minion already registered, false otherwise
     */
    public static boolean checkIfMinionAlreadyRegistered(String minionId,
                                                  String machineId,
                                                  Optional<User> creator,
                                                  boolean isSaltSSH, ValueMap grains) {
        Optional<MinionServer> optMinion = MinionServerFactory.findByMachineId(machineId);
        if (optMinion.isPresent()) {
            MinionServer registeredMinion = optMinion.get();
            String oldMinionId = registeredMinion.getMinionId();

            if (!minionId.equals(oldMinionId)) {
                LOG.warn("Minion '" + oldMinionId + "' already registered, updating " +
                        "profile to '" + minionId + "' [" + machineId + "]");

                registeredMinion.setName(minionId);
                registeredMinion.setMinionId(minionId);
                ServerFactory.save(registeredMinion);

                SaltStateGeneratorService.INSTANCE.generatePillar(registeredMinion);
                SaltStateGeneratorService.INSTANCE.removePillar(oldMinionId);

                migrateMinionFormula(minionId, Optional.of(oldMinionId));

                SALT_SERVICE.deleteKey(oldMinionId);

                SystemManager.addHistoryEvent(registeredMinion, "Duplicate Minion ID", "Minion '" +
                        oldMinionId + "' has been updated to '" + minionId + "'");
            }

            // Saltboot treatment
            // HACK: try to guess if the minion is a retail minion based on its groups.
            // This way we don't need to call grains for each register minion event.
            if (isRetailMinion(registeredMinion)) {
                if (grains.getOptionalAsBoolean("saltboot_initrd").orElse(false)) {
                    // if we have the "saltboot_initrd" grain we want to re-deploy an image via saltboot,
                    LOG.info("Applying saltboot for minion " + minionId);
                    applySaltboot(registeredMinion);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Complete the minion registration with information from grains
     * @param minionId the minion id
     * @param machineId the machine id that we are trying to register
     * @param creator the optional User that created the minion
     * @param saltSSHProxyId optional proxy id for saltssh in case it is used
     * @param activationKeyOverride optional label of activation key to be applied to the system
     * @param isSaltSSH true if a salt-ssh system is bootstrapped
     * @param grains grains from the minion
     */
    public static void finalizeMinionRegistration(String minionId,
                                           String machineId,
                                           Optional<User> creator,
                                           Optional<Long> saltSSHProxyId,
                                           Optional<String> activationKeyOverride,
                                           boolean isSaltSSH, ValueMap grains) {

        MinionServer minion = migrateOrCreateSystem(minionId, isSaltSSH, activationKeyOverride, machineId, grains);
        Optional<String> originalMinionId = Optional.ofNullable(minion.getMinionId());

        minion.setMachineId(machineId);
        minion.setMinionId(minionId);
        minion.setName(minionId);
        minion.setDigitalServerId(machineId);

        Optional<String> activationKeyLabel = getActivationKeyLabelFromGrains(grains, activationKeyOverride);
        Optional<ActivationKey> activationKey = activationKeyLabel.map(ActivationKeyFactory::lookupByKey);


        Org org = activationKey.map(ActivationKey::getOrg)
                .orElse(creator.map(User::getOrg)
                        .orElse(OrgFactory.getSatelliteOrg()));
        if (minion.getOrg() == null) {
            minion.setOrg(org);
        }
        else if (!minion.getOrg().equals(org)) {
            // only log activation key ignore message when the activation key is not empty
            String ignoreAKMessage = activationKey.map(ak -> "Ignoring activation key " + ak + ".").orElse("");
            LOG.error("The existing server organization (" + minion.getOrg() + ") does not match the " +
                    "organization selected for registration (" + org + "). Keeping the " +
                    "existing server organization. " + ignoreAKMessage);
            activationKey = empty();
            org = minion.getOrg();
            SystemManager.addHistoryEvent(minion, "Invalid Server Organization",
                    "The existing server organization (" + minion.getOrg() + ") does not match the " +
                            "organization selected for registration (" + org + "). Keeping the " +
                            "existing server organization. " + ignoreAKMessage);
        }

        try {
            // Set creator to the user who accepted the key if available
            minion.setCreator(creator.orElse(null));

            String osfullname = grains.getValueAsString("osfullname");
            String osfamily = grains.getValueAsString("os_family");
            String osrelease = getOsRelease(minionId, grains);

            String kernelrelease = grains.getValueAsString("kernelrelease");
            String osarch = grains.getValueAsString("osarch");

            minion.setOs(osfullname);
            minion.setOsFamily(osfamily);
            minion.setRelease(osrelease);
            minion.setRunningKernel(kernelrelease);
            minion.setSecret(RandomStringUtils.randomAlphanumeric(64));
            minion.setAutoUpdate("N");
            minion.setLastBoot(System.currentTimeMillis() / 1000);
            minion.setCreated(new Date());
            minion.setModified(minion.getCreated());
            minion.setContactMethod(getContactMethod(activationKey, isSaltSSH, minionId));
            minion.setHostname(grains.getOptionalAsString(FQDN).orElse(null));

            minion.setServerArch(
                    ServerFactory.lookupServerArchByLabel(osarch + "-redhat-linux"));
            //ToDo Just a hacked version for ubuntu
            if (osfamily.equals("Debian")) {
                minion.setServerArch(
                        ServerFactory.lookupServerArchByLabel(osarch + "-debian-linux"));
            }
            else {
                minion.setServerArch(
                        ServerFactory.lookupServerArchByLabel(osarch + "-redhat-linux"));
            }

            RegistrationUtils.subscribeMinionToChannels(SALT_SERVICE, minion, grains, activationKey,
                    activationKeyLabel);

            minion.updateServerInfo();

            mapHardwareGrains(minion, grains);

            String master = SALT_SERVICE
                    .getMasterHostname(minionId)
                    .orElseThrow(() -> new SaltException(
                            "master not found in minion configuration"));

            setServerPaths(minion, master, isSaltSSH, saltSSHProxyId);

            ServerFactory.save(minion);
            giveCapabilities(minion);

            // Assign the Salt base entitlement by default
            minion.setBaseEntitlement(EntitlementManager.SALT);

            // apply activation key properties that need to be set after saving the minion
            activationKey.ifPresent(ak -> RegistrationUtils.applyActivationKeyProperties(minion, ak, grains));

            // Saltboot treatment - prepare and apply saltboot
            if (grains.getOptionalAsBoolean("saltboot_initrd").orElse(false)) {
                LOG.info("\"saltboot_initrd\" grain set to true: Preparing & applying saltboot for minion " + minionId);
                prepareRetailMinionForSaltboot(minion, org, grains);
                applySaltboot(minion);
                migrateMinionFormula(minionId, originalMinionId);
                return;
            }

            RegistrationUtils.finishRegistration(minion, activationKey, creator, !isSaltSSH);
            migrateMinionFormula(minionId, originalMinionId);
        }
        catch (Throwable t) {
            LOG.error("Error registering minion id: " + minionId, t);
            throw new RegisterMinionException(minionId, org);
        }
        finally {
            if (MinionPendingRegistrationService.containsMinion(minionId)) {
                MinionPendingRegistrationService.removeMinion(minionId);
            }
        }
    }

    private static void migrateMinionFormula(String minionId, Optional<String> originalMinionId) {
        // after everything is done, if minionId has changed, we want to put the formula data
        // to the correct location on the FS
        originalMinionId.ifPresent(oldId -> {
            if (!oldId.equals(minionId)) {
                try {
                    List<String> minionFormulas = FormulaFactory.getFormulasByMinionId(oldId);
                    FormulaFactory.saveServerFormulas(minionId, minionFormulas);
                    for (String minionFormula : minionFormulas) {
                        Optional<Map<String, Object>> formulaValues =
                                FormulaFactory.getFormulaValuesByNameAndMinionId(minionFormula, oldId);
                        if (formulaValues.isPresent()) {
                            // handle via Optional.get because of exception handling
                            FormulaFactory.saveServerFormulaData(formulaValues.get(), minionId, minionFormula);
                            FormulaFactory.deleteServerFormulaData(oldId, minionFormula);
                        }
                    }
                    FormulaFactory.saveServerFormulas(oldId, Collections.emptyList());
                }
                catch (IOException e) {
                    LOG.warn("Error when converting formulas from minionId " + oldId + "to minionId " + minionId);
                }
            }
        });
    }

    /**
     * Extract HW addresses (except the localhost one) from grains
     * @param grains the grains
     * @return HW addresses
     */
    private static Set<String> extractHwAddresses(ValueMap grains) {
        Map<String, String> hwInterfaces = (Map<String, String>) grains
                .get("hwaddr_interfaces").orElse(Collections.emptyMap());
        return hwInterfaces.values().stream()
                .filter(hwAddress -> !hwAddress.equalsIgnoreCase("00:00:00:00:00:00"))
                .collect(Collectors.toSet());
    }

    /**
     * Extract activation key label from grains
     *
     * @param grains
     * @param activationKeyOverride optional label of activation key to be applied to the system
     * @return
     */
    private static Optional<String> getActivationKeyLabelFromGrains(ValueMap grains, Optional<String> activationKeyOverride) {
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
    private static Optional<String> getManagementKeyLabelFromGrains(ValueMap grains) {
        //apply management key properties that can be set before saving the server
        return grains.getMap("susemanager")
                .flatMap(suma -> suma.getOptionalAsString("management_key"));
    }


    private static boolean isRetailMinion(MinionServer registeredMinion) {
        // for now, a retail minion is detected when it belongs to a compulsory "TERMINALS" group
        return registeredMinion.getManagedGroups().stream()
                .anyMatch(group -> group.getName().equals(TERMINALS_GROUP_NAME));
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
    private static void prepareRetailMinionForSaltboot(MinionServer minion, Org org, ValueMap grains) {
        Optional<String> manufacturer = grains.getOptionalAsString("manufacturer");
        Optional<String> productName = grains.getOptionalAsString("productname");
        Optional<String> branchId = grains.getOptionalAsString("minion_id_prefix");

        if (!manufacturer.isPresent() || !productName.isPresent() || !branchId.isPresent()) {
            throw new IllegalStateException("Missing machine manufacturer, product name or minion_id_prefix " +
                    "on retail minion registration! Aborting registration.");
        }

        String hwTypeGroupPrefix = "HWTYPE:";
        String hwType = manufacturer.get() + "-" + productName.get();
        String hwTypeGroup = hwTypeGroupPrefix + hwType.replaceAll("[^A-Za-z0-9_-]", "");

        String branchIdGroupName = branchId.get();
        ManagedServerGroup terminalsGroup = ServerGroupFactory.lookupByNameAndOrg(TERMINALS_GROUP_NAME, org);
        ManagedServerGroup branchIdGroup = ServerGroupFactory.lookupByNameAndOrg(branchIdGroupName, org);
        ManagedServerGroup hwGroup = ServerGroupFactory.lookupByNameAndOrg(hwTypeGroup, org);

        if (terminalsGroup == null || branchIdGroup == null) {
            throw new IllegalStateException("Missing required server groups (\"" + TERMINALS_GROUP_NAME + "\" or \"" +
                    branchIdGroupName + "\")! Aborting registration.");
        }

        SystemManager.addServerToServerGroup(minion, terminalsGroup);
        SystemManager.addServerToServerGroup(minion, branchIdGroup);
        if (hwGroup != null) {
            // if the system is already assigned to some HWTYPE group, skip assignment and log this only
            if (minion.getManagedGroups().stream().anyMatch(g -> g.getName().startsWith(hwTypeGroupPrefix))) {
                LOG.info("Skipping assignment of the minion " + minion + " to HW group " + hwGroup +
                        ". The minion is already in a HW group.");
            }
            else {
                SystemManager.addServerToServerGroup(minion, hwGroup);
            }
        }

        minion.asMinionServer().ifPresent(SaltStateGeneratorService.INSTANCE::generatePillar);
    }

    private static void applySaltboot(MinionServer minion) {
        List<String> states = new ArrayList<>();
        states.add(ApplyStatesActor.SYNC_STATES);
        states.add(ApplyStatesActor.SALTBOOT);

        ActorManager.defer(new ApplyStatesActor.Message(minion.getId(), null,false, states));
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
    private static MinionServer migrateOrCreateSystem(String minionId,
            boolean isSaltSSH,
            Optional<String> activationKeyOverride,
            String machineId,
            ValueMap grains) {
        Optional<String> fqdn = grains.getOptionalAsString(FQDN);
        Set<String> hwAddrs = extractHwAddresses(grains);

        return ServerFactory.findByMachineId(machineId)
            .flatMap(server -> {
                if (!server.asMinionServer().isPresent()) {
                    // change the type of the hibernate entity from Server to MinionServer
                    SystemManager.addMinionInfoToServer(server.getId(), minionId);
                    // need to clear the session to avoid NonUniqueObjectException
                    ServerFactory.getSession().clear();
                }
                return MinionServerFactory.lookupById(server.getId());
            })
            .map(minion -> {
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

                return minion;
            }).orElseGet(() -> findMatchingEmptyProfiles(fqdn, hwAddrs).orElseGet(MinionServer::new));
    }

    private static Optional<MinionServer> findMatchingEmptyProfiles(Optional<String> hostname, Set<String> hwAddrs) {
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

    /**
     * Check if a MinionServer with the given name already exists and print
     * an error message to the logs.
     *
     * @param minionId the minion id to check
     * @return true if a MinionServer with the same id already exists
     */
    private static boolean duplicateMinionNamePresent(String minionId) {
        return MinionServerFactory
                .findByMinionId(minionId)
                .map(duplicateMinion -> {
                    LOG.error("Can't register Salt minions with duplicate names." +
                            " A Salt minion named " + minionId + " is already" +
                            " registered with machine-id " + duplicateMinion.
                            getMachineId() + ". Maybe the minion has the wrong " +
                            "hostname or the /etc/machine-id of the minion has " +
                            "changed.");
                    return duplicateMinion;
                })
                .isPresent();
    }

    private static void setServerPaths(MinionServer server, String master,
                                boolean isSaltSSH, Optional<Long> saltSSHProxyId) {
        if (isSaltSSH) {
            saltSSHProxyId
                .map(proxyId -> ServerFactory.lookupById(proxyId))
                .ifPresent(proxy -> {
                    Set<ServerPath> proxyPaths = ServerFactory
                            .createServerPaths(server, proxy, proxy.getHostname());
                    server.getServerPaths().addAll(proxyPaths);
                });
        }
        else {
            ServerFactory.lookupProxyServer(master).ifPresent(proxy -> {
                Set<ServerPath> proxyPaths = ServerFactory
                        .createServerPaths(server, proxy, master);
                server.getServerPaths().addAll(proxyPaths);
            });
        }
    }

    private static void giveCapabilities(MinionServer server) {
        // Salt systems always have the script.run capability
        SystemManager.giveCapability(server.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        // Salt systems can be audited
        SystemManager.giveCapability(server.getId(), SystemManager.CAP_SCAP, 1L);

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

    private static ContactMethod getContactMethod(Optional<ActivationKey> activationKey,
            boolean isSshPush,
            String minionId) {
        return Opt.fold(
                activationKey,
                () -> isSshPush ?
                        ServerFactory.findContactMethodByLabel("ssh-push") :
                        ServerFactory.findContactMethodByLabel("default"),
                ak -> {
                    if (!isSshPush && isSSHPushContactMethod(ak.getContactMethod())) {
                        LOG.warn("Contact method changed from ssh-push to default for " +
                                "minion id " + minionId + ". Please use webui " +
                                "for salt-ssh minions.");
                        return ServerFactory.findContactMethodByLabel("default");
                    }
                    return ak.getContactMethod();
                }
        );
    }

    private static Optional<Channel> lookupBaseChannel(SUSEProduct sp, ChannelArch arch) {
        Optional<EssentialChannelDto> productBaseChannelDto =
                ofNullable(DistUpgradeManager.getProductBaseChannelDto(sp.getId(), arch));
        Optional<Channel> baseChannel = productBaseChannelDto.flatMap(base -> {
            return ofNullable(ChannelFactory.lookupById(base.getId())).map(c -> {
                LOG.info("Base channel " + c.getName() + " found for OS: " + sp.getName() +
                        ", version: " + sp.getVersion() + ", arch: " + arch.getName());
                return c;
            });
        });
        if (!baseChannel.isPresent()) {
            LOG.warn("Product Base channel not found - refresh SCC sync?");
            return empty();
        }
        return baseChannel;
    }

    private static Optional<String> rpmErrQueryRHELProvidesRelease(String minionId) {
        LOG.error("No package providing 'redhat-release' found on RHEL minion " + minionId);
        return empty();
    }

    private static Optional<String> rpmErrQueryRHELRelease(SaltError err, String minionId) {
        LOG.error("Error querying 'redhat-release' package on RHEL minion " +
                minionId + ": " + err);
        return empty();
    }

    private static String unknownRHELVersion(String minionId) {
        LOG.error("Could not determine OS release version for RHEL minion " + minionId);
        return "unknown";
    }

    private static Map<String, List<String>> parseRHELReleseQuery(String result) {
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

    private static String getOsRelease(String minionId, ValueMap grains) {
        // java port of up2dataUtils._getOSVersionAndRelease()
        String osRelease = grains.getValueAsString("osrelease");

        if ("redhat".equalsIgnoreCase(grains.getValueAsString("os")) ||
                "centos".equalsIgnoreCase(grains.getValueAsString("os"))) {
            MinionList target = new MinionList(Arrays.asList(minionId));
            Optional<Result<String>> whatprovidesRes = SALT_SERVICE.runRemoteCommand(target,
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
                            err4 -> rpmErrQueryRHELProvidesRelease(minionId)),
                    r -> of(r.split("\\r?\\n")[0]) // Take the first line if multiple results return
            ))
            .flatMap(pkgStr -> {
                String[] pkgs = StringUtils.split(pkgStr);
                if (pkgs.length > 1) {
                    LOG.warn("Multiple release packages are installed on minion: " + minionId);
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
                SALT_SERVICE.runRemoteCommand(target,
                        "rpm -q --queryformat \"" +
                            "VERSION=%{VERSION}\\n" +
                            "PROVIDENAME=[%{PROVIDENAME},]\\n" +
                            "PROVIDEVERSION=[%{PROVIDEVERSION},]\\n\" " + pkg)
                        .entrySet().stream().findFirst().map(e -> e.getValue())
                        .flatMap(res -> res.fold(
                                err -> err.fold(
                                        err1 -> rpmErrQueryRHELRelease(err1, minionId),
                                        err2 -> rpmErrQueryRHELRelease(err2, minionId),
                                        err3 -> rpmErrQueryRHELRelease(err3, minionId),
                                        err4 -> rpmErrQueryRHELRelease(err4, minionId)),
                                r -> of(r)
                        ))
                        .map(result -> {
                            if (result.split("\\r?\\n").length > 3) {
                                // Output should be 3 lines per installed package version
                                LOG.warn(String.format(
                                        "Multiple versions of release package '%s' is installed on minion: %s",
                                        pkg, minionId));
                            }
                            return parseRHELReleseQuery(result);
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

    private static void mapHardwareGrains(MinionServer server, ValueMap grains) {
        // for efficiency do this here
        server.setRam(grains.getValueAsLong("mem_total").orElse(0L));
    }

    /**
     * {@inheritDoc}
     */

    public Consumer<Exception> getExceptionHandler() {
        return e -> {
            if (e instanceof RegisterMinionException) {
                RegisterMinionException rme = (RegisterMinionException) e;
                NotificationMessage notificationMessage = UserNotificationFactory.createNotificationMessage(
                        new OnboardingFailed(rme.minionId)
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
    public static class RegisterMinionException extends RuntimeException {
        private String minionId;
        private Org org;
        RegisterMinionException(String minionIdIn, Org orgIn) {
            minionId = minionIdIn;
            org = orgIn;
        }
    }
}
