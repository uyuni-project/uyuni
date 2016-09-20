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

import static java.util.Optional.ofNullable;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerHistoryEvent;
import com.redhat.rhn.domain.server.ServerPath;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.state.PackageStates;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.state.VersionConstraints;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.frontend.dto.EssentialChannelDto;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.distupgrade.DistUpgradeManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.reactor.utils.RhelUtils;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.controllers.StatesAPI;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.salt.Zypper;
import com.suse.manager.webui.utils.salt.Zypper.ProductInfo;

import com.suse.manager.webui.utils.salt.custom.PkgProfileUpdateSlsResult;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.errors.SaltError;
import com.suse.salt.netapi.results.CmdExecCodeAllResult;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.utils.Opt;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            String osfamily = grains.getValueAsString("os_family");
            String osrelease = getOsRelease(minionId, grains);

            String kernelrelease = grains.getValueAsString("kernelrelease");
            String osarch = grains.getValueAsString("osarch");

            server.setOs(osfullname);
            server.setOsFamily(osfamily);
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
                lookupAndAddDefaultChannels(server, grains);
            }

            server.updateServerInfo();

            mapHardwareGrains(server, grains);

            String master = SALT_SERVICE
                    .getMasterHostname(minionId)
                    .orElseThrow(() -> new SaltException(
                            "master not found in minion configuration"));

            ServerFactory.lookupProxyServer(master).ifPresent(proxy -> {
                ServerPath path = ServerFactory.createServerPath(server, proxy, master);
                server.setServerPath(path);
            });

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
                    ApplyStatesEventMessage.PACKAGES,
                    ApplyStatesEventMessage.SALT_MINION_SERVICE
            ));
        }
        catch (Throwable t) {
            LOG.error("Error registering minion for event: " + event, t);
        }
    }

    private void lookupAndAddDefaultChannels(MinionServer server, ValueMap grains) {
        if ("suse".equalsIgnoreCase(grains.getValueAsString("os"))) {
            Optional<List<ProductInfo>> productList =
                    SALT_SERVICE.callSync(Zypper.listProducts(false), server.getMinionId());
            productList.ifPresent(pl -> {
                pl.stream().filter(pif -> pif.getIsbase()).findFirst().ifPresent(pi -> {
                    String osName = pi.getName().toLowerCase();
                    String osVersion = pi.getVersion();
                    String osArch = pi.getArch();
                    Optional<SUSEProduct> suseProduct = ofNullable(SUSEProductFactory
                            .findSUSEProduct(osName, osVersion, null, osArch, false));
                    if (!suseProduct.isPresent()) {
                        LOG.warn("No product match found for: " + osName + " " + osVersion +
                                " " + osArch);
                    }

                    Opt.stream(suseProduct).flatMap(sp ->
                            lookupBaseAndRequiredChannels(osName, osVersion, osArch, sp)
                    ).forEach(reqChan -> {
                        LOG.info("Adding required channel: " + reqChan.getName());
                        server.addChannel(reqChan);
                    });
                });
            });
        }
        else if ("redhat".equalsIgnoreCase(grains.getValueAsString("os")) ||
                "centos".equalsIgnoreCase(grains.getValueAsString("os"))) {
            String minionId = server.getMinionId();

            Optional<Map<String, State.ApplyResult>> applyResultMap = SALT_SERVICE
                    .applyState(server.getMinionId(), "packages.redhatproductinfo");
            Optional<String> centosReleaseContent =
                    applyResultMap.map(map ->
                        map.get(PkgProfileUpdateSlsResult.PKG_PROFILE_CENTOS_RELEASE))
                    .map(r -> r.getChanges(CmdExecCodeAllResult.class))
                    .map(c -> c.getStdout());
            Optional<String> rhelReleaseContent =
                    applyResultMap.map(map ->
                        map.get(PkgProfileUpdateSlsResult.PKG_PROFILE_REDHAT_RELEASE))
                    .map(r -> r.getChanges(CmdExecCodeAllResult.class))
                    .map(c -> c.getStdout());
            Optional<String> whatProvidesRes =
                    applyResultMap.map(map ->
                        map.get(
                            PkgProfileUpdateSlsResult
                                .PKG_PROFILE_WHATPROVIDES_SLES_RELEASE))
                    .map(r -> r.getChanges(CmdExecCodeAllResult.class))
                    .map(c -> c.getStdout());

            Optional<RhelUtils.RhelProduct> rhelProduct = RhelUtils
                    .detectRhelProduct(
                            server, whatProvidesRes,
                            rhelReleaseContent, centosReleaseContent);
            rhelProduct
                .ifPresent(rhel -> {
                        String arch = server.getServerArch()
                                .getLabel().replace("-redhat-linux", "");
                        Opt.stream(rhel.getSuseProduct()).flatMap(sp ->
                                lookupBaseAndRequiredChannels(
                                        rhel.getName(), rhel.getVersion(),
                                        arch, sp)
                        ).forEach(reqChan -> {
                            LOG.info("Adding required channel: " + reqChan.getName());
                            server.addChannel(reqChan);
                        });
                        if (!rhel.getSuseProduct().isPresent()) {
                            LOG.info("Not setting default channels for minion: " +
                                    minionId + " os: " + rhel.getName() +
                                    " " + rhel.getVersion() +
                                    " " + arch);
                        }
                    }
                );
        }
    }

    private Stream<Channel> lookupBaseAndRequiredChannels(String osName,
            String osVersion, String osArch, SUSEProduct sp) {
        Optional<EssentialChannelDto> productBaseChannelDto =
                ofNullable(DistUpgradeManager.getProductBaseChannelDto(sp.getId(),
                        ChannelFactory.lookupArchByName(osArch)));

        return productBaseChannelDto.map(base ->
            ofNullable(ChannelFactory.lookupById(base.getId())).map(c -> {
                LOG.info("Base channel " + c.getName() + " found for OS: " + osName +
                        ", version: " + osVersion + ", arch: " + osArch);
                SUSEProductSet installedProducts = new SUSEProductSet();
                installedProducts.setBaseProduct(sp);
                Stream<Channel> requiredChannels = DistUpgradeManager
                    .getRequiredChannels(installedProducts, c.getId())
                    .stream()
                    .flatMap(reqChan ->
                        ofNullable(ChannelFactory.lookupById(reqChan.getId()))
                            .map(Stream::of)
                            .orElseGet(() -> {
                                LOG.error("Can't retrieve required channel id " +
                                        "from database");
                                return Stream.empty();
                            })
                    );
                return Stream.concat(
                    Stream.of(c),
                    requiredChannels
                );
            }).orElseGet(() -> {
                LOG.error("Can't retrieve channel id from database");
                return Stream.empty();
            })
        ).orElseGet(() -> {
            LOG.info("Product Base channel not found - refresh SCC sync?");
            return Stream.empty();
        });
    }

    private Optional<String> rpmErrQueryRHELProvidesRelease(String minionId) {
        LOG.error("No package providing 'redhat-release' found on RHEL minion " + minionId);
        return Optional.empty();
    }

    private Optional<String> rpmErrQueryRHELRelease(SaltError err, String minionId) {
        LOG.error("Error querying 'redhat-release' package on RHEL minion " +
                minionId + ": " + err);
        return Optional.empty();
    }

    private String unknownRHELVersion(String minionId) {
        LOG.error("Could not determine OS release version for RHEL minion " + minionId);
        return "unknown";
    }

    private Map<String, List<String>> parseRHELReleseQuery(String result) {
        return Arrays.stream(result.split("\\r?\\n")).map(line -> line.split("="))
                .collect(
                        Collectors.<String[], String, List<String>>toMap(
                                linetoks -> linetoks[0],
                                linetoks -> Arrays.asList(
                                        StringUtils.splitPreserveAllTokens(linetoks[1], ",")
                                        )));
    }

    private String getOsRelease(String minionId, ValueMap grains) {
        // java port of up2dataUtils._getOSVersionAndRelease()
        String osRelease = grains.getValueAsString("osrelease");

        if ("redhat".equalsIgnoreCase(grains.getValueAsString("os")) ||
                "centos".equalsIgnoreCase(grains.getValueAsString("os"))) {
            MinionList target = new MinionList(Arrays.asList(minionId));
            Optional<Result<String>> whatprovidesRes = SALT_SERVICE.runRemoteCommand(target,
                    "rpm -q --whatprovides --queryformat \"%{NAME}\" redhat-release")
                    .entrySet()
                    .stream()
                    .findFirst()
                    .map(e -> Optional.of(e.getValue()))
                    .orElse(Optional.empty());

            osRelease = whatprovidesRes.flatMap(res -> res.fold(
                    err -> err.fold(err1 -> rpmErrQueryRHELProvidesRelease(minionId),
                            err2 -> rpmErrQueryRHELProvidesRelease(minionId),
                            err3 -> rpmErrQueryRHELProvidesRelease(minionId)),
                    r -> Optional.of(r)
            ))
            .flatMap(pkg ->
                SALT_SERVICE.runRemoteCommand(target,
                        "rpm -q --queryformat \"" +
                            "VERSION=%{VERSION}\\n" +
                            "PROVIDENAME=[%{PROVIDENAME},]\\n" +
                            "PROVIDEVERSION=[%{PROVIDEVERSION},]\" " + pkg)
                        .entrySet().stream().findFirst().map(e -> e.getValue())
                        .flatMap(res -> res.fold(
                                err -> err.fold(
                                        err1 -> rpmErrQueryRHELRelease(err1, minionId),
                                        err2 -> rpmErrQueryRHELRelease(err2, minionId),
                                        err3 -> rpmErrQueryRHELRelease(err3, minionId)),
                                r -> Optional.of(r)
                        ))
                        .map(this::parseRHELReleseQuery)
                        .map(pkgtags -> {
                            Optional<String> version = Optional
                                    .ofNullable(pkgtags.get("VERSION"))
                                    .map(v -> v.stream().findFirst())
                                    .orElse(Optional.empty());
                            List<String> provideName = pkgtags.get("PROVIDENAME");
                            List<String> provideVersion = pkgtags.get("PROVIDEVERSION");
                            int idxReleasever = provideName
                                    .indexOf("system-release(releasever)");
                            if (idxReleasever > -1) {
                                version = provideVersion.size() > idxReleasever ?
                                        Optional.of(provideVersion.get(idxReleasever)) :
                                        Optional.empty();
                            }
                            return version;
                        })
                        .orElse(Optional.empty())
            )
            .orElseGet(() -> unknownRHELVersion(minionId));
        }
        return osRelease;
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
