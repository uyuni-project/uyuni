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

import static com.suse.manager.webui.controllers.utils.ContactMethodUtil.isSSHPushContactMethod;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.common.messaging.MessageQueue;
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
import com.redhat.rhn.domain.product.SUSEProductChannel;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.ContactMethod;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.NetworkInterfaceFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.ServerHistoryEvent;
import com.redhat.rhn.domain.server.ServerPath;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.EssentialChannelDto;
import com.redhat.rhn.manager.distupgrade.DistUpgradeManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.reactor.utils.RhelUtils;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.impl.MinionPendingRegistrationService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.salt.custom.PkgProfileUpdateSlsResult;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.modules.Zypper;
import com.suse.salt.netapi.calls.modules.Zypper.ProductInfo;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.errors.SaltError;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.CmdExecCodeAll;
import com.suse.salt.netapi.results.Result;
import com.suse.utils.Opt;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Event handler to create system records for salt minions.
 */
public class RegisterMinionEventMessageAction implements MessageAction {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(
            RegisterMinionEventMessageAction.class);

    // Reference to the SaltService instance
    private final SaltService SALT_SERVICE;

    private static final String TERMINALS_GROUP_NAME = "TERMINALS";

    /**
     * Default constructor.
     */
    public RegisterMinionEventMessageAction() {
        this(SaltService.INSTANCE);
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
    @Override
    public void execute(EventMessage msg) {
        registerMinion(((RegisterMinionEventMessage) msg).getMinionId(), false,
                empty(), empty());
    }

    /**
     * Temporary HACK: Run the registration for a minion with given id.
     * Will be extracted to a separate class, this is here only because of easier rebasing.
     * @param minionId minion id
     * @param activationKeyOverride label of activation key to be applied to the system.
     *                              If left empty, activation key from grains will be used.
     * @param proxyId the proxy to which the minion connects, if any
     */
    public void registerSSHMinion(String minionId, Optional<Long> proxyId,
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
    private void registerMinion(String minionId, boolean isSaltSSH,
                                Optional<Long> saltSSHProxyId,
                                Optional<String> actKeyOverride) {

        // Match minions via their machine id
        Opt.fold(SALT_SERVICE.getMachineId(minionId),
            () -> {
                LOG.info("Cannot find machine id for minion: " + minionId);
                return false;
            },
            machineId -> {
                Optional<User> creator = MinionPendingRegistrationService.getCreator(minionId);
                if (checkIfMinionAlreadyRegistered(minionId, machineId, creator, isSaltSSH)) {
                    return true;
                }
                // Check if this minion id already exists
                if (duplicateMinionNamePresent(minionId)) {
                    return false;
                }
                finalizeMinionRegistration(minionId, machineId, creator, saltSSHProxyId, actKeyOverride, isSaltSSH);
                return true;
            }
        );
    }

    /**
     * Check if a minion is already registered and update it in case so
     * @param minionId the minion id
     * @param machineId the machine id that we are trying to register
     * @param creator the optional User that created the minion
     * @param isSaltSSH true if a salt-ssh system is bootstrapped
     * @return true if minion already registered, false otherwise
     */
    public boolean checkIfMinionAlreadyRegistered(String minionId,
                                                  String machineId,
                                                  Optional<User> creator,
                                                  boolean isSaltSSH) {
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
                addHistoryEvent(registeredMinion, "Duplicate Machine ID", "Minion '" +
                        oldMinionId + "' has been updated to '" + minionId + "'");

                if (!minionId.equals(oldMinionId)) {
                    SALT_SERVICE.deleteKey(oldMinionId);
                }
            }

            // Saltboot treatment
            // HACK: try to guess if the minion is a retail minion based on its groups.
            // This way we don't need to call grains for each register minion event.
            if (isRetailMinion(registeredMinion)) {
                ValueMap grains = new ValueMap(SALT_SERVICE.getGrains(minionId).orElseGet(HashMap::new));
                grains.getOptionalAsBoolean("saltboot_initrd").ifPresent(initrd -> {
                    // if we have the "saltboot_initrd" grain we want to re-deploy an image via saltboot,
                    // otherwise the image has been already fully deployed and we want to finalize the registration
                    LOG.info("\"saltboot_initrd\" present for minion " + minionId);

                    if (initrd) {
                        LOG.info("Applying saltboot for minion " + minionId);
                        applySaltboot(registeredMinion);
                    }
                    else {
                        Optional<String> activationKeyLabel = grains
                                .getMap("susemanager")
                                .flatMap(suma -> suma.getOptionalAsString("activation_key"));
                        Optional<ActivationKey> activationKey = activationKeyLabel
                                .map(ActivationKeyFactory::lookupByKey);

                        // TODO this is to be implemented in the future
                        // for now we don't have any means to detect if the image has been redeployed and we simply
                        // call the 'finishRegistration' on every minion start
                        // BEWARE: this also means the activation key is applied on each retail minion start for now
                        LOG.info("Finishing registration for minion " + minionId);
                        subscribeMinionToChannels(SALT_SERVICE, minionId, registeredMinion, grains, activationKey,
                                activationKeyLabel);
                        activationKey.ifPresent(ak ->
                                RegistrationUtils.applyActivationKeyProperties(registeredMinion, ak, grains));
                        RegistrationUtils.finishRegistration(registeredMinion, activationKey, creator, !isSaltSSH);
                    }
                });
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
     */
    public void finalizeMinionRegistration(String minionId,
                                           String machineId,
                                           Optional<User> creator,
                                           Optional<Long> saltSSHProxyId,
                                           Optional<String> activationKeyOverride,
                                           boolean isSaltSSH) {
        Map<String, Object> grainsMap = SALT_SERVICE.getGrains(minionId).orElseGet(HashMap::new);
        ValueMap grains = new ValueMap(grainsMap);

        Collection<String> hwAddrs = extractHwAddresses(grainsMap);
        MinionServer minion = migrateOrCreateSystem(minionId, isSaltSSH, activationKeyOverride, machineId, hwAddrs);
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
            addHistoryEvent(minion, "Invalid Server Organization",
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
            minion.setHostname(grains.getOptionalAsString("fqdn").orElse(null));

            minion.setServerArch(
                    ServerFactory.lookupServerArchByLabel(osarch + "-redhat-linux"));

            subscribeMinionToChannels(SALT_SERVICE, minionId, minion, grains, activationKey, activationKeyLabel);

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
    /**
     * Extract HW addresses (except the localhost one) from grains
     * @param grains the grains
     * @return HW addresses
     */
    private Set<String> extractHwAddresses(Map<String, Object> grains) {
        Map<String, String> hwInterfaces = (Map<String, String>) grains
                .getOrDefault("hwaddr_interfaces", Collections.emptyMap());

        return hwInterfaces.values().stream()
                .filter(hwAddress -> !hwAddress.equalsIgnoreCase("00:00:00:00:00:00"))
                .collect(Collectors.toSet());
    }

    private void migrateMinionFormula(String minionId, Optional<String> originalMinionId) {
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
                        FormulaFactory.saveServerFormulas(oldId, Collections.emptyList());
                    }
                }
                catch (IOException e) {
                    LOG.warn("Error when converting formulas from minionId " + oldId + "to minionId " + minionId);
                }
            }
        });
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


    private boolean isRetailMinion(MinionServer registeredMinion) {
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
    private void prepareRetailMinionForSaltboot(MinionServer minion, Org org, ValueMap grains) {
        Optional<String> manufacturer = grains.getOptionalAsString("manufacturer");
        Optional<String> productName = grains.getOptionalAsString("productname");
        Optional<String> branchId = grains.getOptionalAsString("minion_id_prefix");

        if (!manufacturer.isPresent() || !productName.isPresent() || !branchId.isPresent()) {
            throw new IllegalStateException("Missing machine manufacturer, product name or minion_id_prefix " +
                    "on retail minion registration! Aborting registration.");
        }

        String hwType = manufacturer.get() + "-" + productName.get();
        String hwTypeGroup = "HWTYPE:" + hwType.replaceAll("[^A-Za-z0-9_-]", "");

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
            SystemManager.addServerToServerGroup(minion, hwGroup);
        }

        minion.asMinionServer().ifPresent(SaltStateGeneratorService.INSTANCE::generatePillar);
    }

    private void applySaltboot(MinionServer minion) {
        List<String> states = new ArrayList<>();
        states.add(ApplyStatesEventMessage.SYNC_STATES);
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
     * @param hwAddrs collection of hardware addresses
     * @return a migrated or new MinionServer instance
     */
    private MinionServer migrateOrCreateSystem(String minionId,
            boolean isSaltSSH,
            Optional<String> activationKeyOverride,
            String machineId,
            Collection<String> hwAddrs) {
        return ServerFactory.findByMachineId(machineId)
            .flatMap(server -> {
                // change the type of the hibernate entity from Server to MinionServer
                SystemManager.addMinionInfoToServer(server.getId(), minionId);
                // need to clear the session to avoid NonUniqueObjectException
                ServerFactory.getSession().clear();
                return MinionServerFactory
                        .lookupById(server.getId());
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

                // change base channel
                minion.getChannels().clear();

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
            }).orElseGet(() -> hwAddrs.stream()
                    .flatMap(hwAddr -> NetworkInterfaceFactory.lookupNetworkInterfacesByHwAddress(hwAddr))
                    .map(nic -> nic.getServer())
                    .filter(server -> server.hasEntitlement(EntitlementManager.BOOTSTRAP))
                    .findFirst()
                    .flatMap(server -> server.asMinionServer())
                    .orElseGet(MinionServer::new));
    }

    /**
     * Check if a MinionServer with the given name already exists and print
     * an error message to the logs.
     *
     * @param minionId the minion id to check
     * @return true if a MinionServer with the same id already exists
     */
    private boolean duplicateMinionNamePresent(String minionId) {
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

    public static Set<SUSEProduct> identifyProduct(SaltService saltService, MinionServer server, ValueMap grains) {
        if ("suse".equalsIgnoreCase(grains.getValueAsString("os"))) {
            Optional<List<ProductInfo>> productList =
                    saltService.callSync(Zypper.listProducts(false), server.getMinionId());
            return Opt.stream(productList).flatMap(pl -> {
                return pl.stream()
                        .flatMap(pi -> {
                            String osName = pi.getName().toLowerCase();
                            String osVersion = pi.getVersion();
                            String osArch = pi.getArch();
                            String osRelease = pi.getRelease();
                            Optional<SUSEProduct> suseProduct =
                                    ofNullable(SUSEProductFactory.findSUSEProduct(osName,
                                            osVersion, osRelease, osArch, true));
                            if (!suseProduct.isPresent()) {
                                LOG.warn("No product match found for: " + osName + " " +
                                        osVersion + " " + osRelease + " " + osArch);
                            }
                            return Opt.stream(suseProduct);
                        });
            }).collect(Collectors.toSet());
        }
        else if ("redhat".equalsIgnoreCase(grains.getValueAsString("os")) ||
                "centos".equalsIgnoreCase(grains.getValueAsString("os"))) {
            Optional<Map<String, State.ApplyResult>> applyResultMap = saltService
                    .applyState(server.getMinionId(), "packages.redhatproductinfo");
            Optional<String> centosReleaseContent = applyResultMap.map(
                    map -> map.get(PkgProfileUpdateSlsResult.PKG_PROFILE_CENTOS_RELEASE))
                    .map(r -> r.getChanges(CmdExecCodeAll.class)).map(c -> c.getStdout());
            Optional<String> rhelReleaseContent = applyResultMap.map(
                    map -> map.get(PkgProfileUpdateSlsResult.PKG_PROFILE_REDHAT_RELEASE))
                    .map(r -> r.getChanges(CmdExecCodeAll.class)).map(c -> c.getStdout());
            Optional<String> whatProvidesRes = applyResultMap.map(map -> map
                    .get(PkgProfileUpdateSlsResult.PKG_PROFILE_WHATPROVIDES_SLES_RELEASE))
                    .map(r -> r.getChanges(CmdExecCodeAll.class)).map(c -> c.getStdout());

            Optional<RhelUtils.RhelProduct> rhelProduct = RhelUtils.detectRhelProduct(
                    server, whatProvidesRes, rhelReleaseContent, centosReleaseContent);
            return Opt.stream(rhelProduct).flatMap(rhel -> {
                if (!rhel.getSuseProduct().isPresent()) {
                    LOG.warn("No product match found for: " + rhel.getName() + " " +
                            rhel.getVersion() + " " + rhel.getRelease() + " " +
                            server.getServerArch().getCompatibleChannelArch());
                    return Stream.empty();
                }
                else {
                    return Opt.stream(rhel.getSuseProduct());
                }
            }).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    public static void subscribeMinionToChannels(SaltService saltService, String minionId, MinionServer server,
            ValueMap grains, Optional<ActivationKey> activationKey,
            Optional<String> activationKeyLabel) {

        if (!activationKey.isPresent() && activationKeyLabel.isPresent()) {
            LOG.warn(
                    "Default channel(s) will NOT be subscribed to: specified Activation Key " +
                            activationKeyLabel.get() + " is not valid for minionId " +
                            minionId);
            addHistoryEvent(server, "Invalid Activation Key",
                    "Specified Activation Key " + activationKeyLabel.get() +
                            " is not valid. Default channel(s) NOT subscribed to.");
            return;
        }

        Set<Channel> channelsToAssign = Opt.fold(
                activationKey,
                // No ActivationKey
                () -> {
                    Set<SUSEProduct> suseProducts = identifyProduct(saltService, server, grains);
                    Map<Boolean, List<SUSEProduct>> baseAndExtProd = suseProducts.stream()
                            .collect(Collectors.partitioningBy(SUSEProduct::isBase));

                    Optional<SUSEProduct> baseProductOpt = Optional.ofNullable(baseAndExtProd.get(true))
                            .flatMap(s -> s.stream().findFirst());
                    List<SUSEProduct> extProducts = baseAndExtProd.get(false);

                    return Opt.fold(
                            baseProductOpt,
                            // No ActivationKey and no base product identified
                            () -> {
                                LOG.warn("Server " + minionId + " has no identifiable base product" +
                                        " and will register without base channel assignment");
                                return Collections.emptySet();
                            },
                            baseProduct -> {
                                return Stream.concat(
                                        lookupRequiredChannelsForProduct(baseProduct),
                                        extProducts.stream()
                                            .flatMap(ext -> recommendedChannelsByBaseProduct(baseProduct, ext))
                                ).collect(Collectors.toSet());
                            }
                    );
                },
                ak -> {
                    return Opt.<Channel, Set<Channel>>fold(
                            Optional.ofNullable(ak.getBaseChannel()),
                            // AktivationKey without base channel (SUSE Manager Default)
                            () -> {
                                Set<SUSEProduct> suseProducts = identifyProduct(saltService, server, grains);
                                Map<Boolean, List<SUSEProduct>> baseAndExtProd = suseProducts.stream()
                                        .collect(Collectors.partitioningBy(SUSEProduct::isBase));

                                Optional<SUSEProduct> baseProductOpt = Optional.ofNullable(baseAndExtProd.get(true))
                                        .flatMap(s -> s.stream().findFirst());
                                List<SUSEProduct> extProducts = baseAndExtProd.get(false);

                                return Opt.fold(
                                        baseProductOpt,
                                        // ActivationKey and no base product identified
                                        () -> {
                                            LOG.warn("Server " + minionId + "has no identifyable base product" +
                                                    " and will register without base channel assignment");
                                            return Collections.emptySet();
                                        },
                                        baseProduct -> {
                                            return Stream.concat(
                                                    lookupRequiredChannelsForProduct(baseProduct),
                                                    extProducts.stream().flatMap(
                                                            ext -> recommendedChannelsByBaseProduct(baseProduct, ext))
                                            ).collect(Collectors.toSet());
                                        }
                                );
                            },
                            baseChannel -> {
                                return Opt.fold(
                                        SUSEProductFactory.findProductByChannelLabel(baseChannel.getLabel()),
                                        () -> {
                                            // ActivationKey with custom channel
                                            return Stream.concat(
                                                    Stream.of(baseChannel),
                                                    ak.getChannels().stream()
                                            ).collect(Collectors.toSet());
                                        },
                                        baseProduct -> {
                                            // ActivationKey with vendor or cloned vendor channel
                                            return Stream.concat(
                                                    lookupRequiredChannelsForProduct(baseProduct.getProduct()),
                                                    ak.getChannels().stream()
                                                            .filter(c ->
                                                                    c.getParentChannel() != null &&
                                                                    c.getParentChannel().getId()
                                                                        .equals(baseChannel.getId())
                                                            )
                                            ).collect(Collectors.toSet());

                                        }
                                );
                            }
                    );
                }
        );

        channelsToAssign.forEach(server::addChannel);
    }

    private static ServerHistoryEvent addHistoryEvent(MinionServer server, String summary,
            String details) {
        ServerHistoryEvent historyEvent = new ServerHistoryEvent();
        historyEvent.setCreated(new Date());
        historyEvent.setServer(server);
        historyEvent.setSummary(summary);
        historyEvent.setDetails(details);
        server.getHistory().add(historyEvent);
        return historyEvent;
    }

    private void setServerPaths(MinionServer server, String master,
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

    private void giveCapabilities(MinionServer server) {
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

    private static Stream<Channel> lookupRequiredChannelsForProduct(SUSEProduct sp) {
        return recommendedChannelsByBaseProduct(sp);
    }

    private Optional<String> rpmErrQueryRHELProvidesRelease(String minionId) {
        LOG.error("No package providing 'redhat-release' found on RHEL minion " + minionId);
        return empty();
    }

    private Optional<String> rpmErrQueryRHELRelease(SaltError err, String minionId) {
        LOG.error("Error querying 'redhat-release' package on RHEL minion " +
                minionId + ": " + err);
        return empty();
    }

    private String unknownRHELVersion(String minionId) {
        LOG.error("Could not determine OS release version for RHEL minion " + minionId);
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

    private static Stream<Channel> recommendedChannelsByBaseProduct(SUSEProduct base) {
            return recommendedChannelsByBaseProduct(base, base);
    }

    private static Stream<Channel> recommendedChannelsByBaseProduct(SUSEProduct root, SUSEProduct base) {
        return root.getSuseProductChannels().stream()
                .filter(c -> c.getParentChannelLabel() == null)
                .map(SUSEProductChannel::getChannelLabel)
                .findFirst().map(rootChannelLabel -> {
                    List<SUSEProduct> allExtensionProductsOf =
                            SUSEProductFactory.findAllExtensionProductsOf(base);



                    Stream<Channel> channelStream = SUSEProductFactory.findAllSUSEProductChannels().stream()
                            .filter(pc -> pc.getProduct().equals(base))
                            .map(SUSEProductChannel::getChannel)
                            .filter(Objects::nonNull)
                            .filter(c -> c.getParentChannel() == null ||
                                    c.getParentChannel().getLabel().equals(rootChannelLabel));

                    Stream<Channel> stream = allExtensionProductsOf.stream().flatMap(ext -> {
                        return SUSEProductFactory.findSUSEProductExtension(root, base, ext).map(pe -> {
                            if (pe.isRecommended()) {
                                return recommendedChannelsByBaseProduct(root, ext);
                            }
                            else {
                                return Stream.<Channel>empty();
                            }
                        }).orElseGet(Stream::empty);
                    });

                    return Stream.concat(
                            channelStream,
                            stream
                    );
                }).orElseGet(Stream::empty);
    }


    private String getOsRelease(String minionId, ValueMap grains) {
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
    public class RegisterMinionException extends RuntimeException {
        private String minionId;
        private Org org;
        RegisterMinionException(String minionIdIn, Org orgIn) {
            minionId = minionIdIn;
            org = orgIn;
        }
    }
}
