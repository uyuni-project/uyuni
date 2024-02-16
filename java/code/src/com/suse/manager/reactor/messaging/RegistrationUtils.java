/*
 * Copyright (c) 2018--2021 SUSE LLC
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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toSet;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductChannel;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerArch;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.state.PackageStates;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.state.VersionConstraints;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.reactor.utils.RhelUtils;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.controllers.StatesAPI;
import com.suse.manager.webui.controllers.channels.ChannelsUtils;
import com.suse.manager.webui.services.iface.RedhatProductInfo;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.pillar.MinionPillarManager;
import com.suse.salt.netapi.calls.modules.Zypper;
import com.suse.utils.Opt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Common registration logic that can be used from multiple places
 */
public class RegistrationUtils {

    private static final List<String> BLACKLIST = List.of("rhncfg", "rhncfg-actions", "rhncfg-client",
            "rhn-virtualization-host", "osad", "mgr-cfg", "mgr-cfg-actions", "mgr-cfg-client",
            "mgr-virtualization-host", "mgr-osad");

    private static final String OS = "os";
    private static final String OS_ARCH = "osarch";

    private static final Logger LOG = LogManager.getLogger(RegistrationUtils.class);

    private static SystemEntitlementManager systemEntitlementManager = GlobalInstanceHolder.SYSTEM_ENTITLEMENT_MANAGER;

    private RegistrationUtils() {
    }

    /**
     * Perform the final registration steps for the minion.
     *
     * @param minion the minion
     * @param activationKey the activation key
     * @param creator user performing the registration
     * @param enableMinionService true if salt-minion service should be enabled and running
     * @param isSaltSSH true if the minion is ssh push minion
     */
    public static void finishRegistration(MinionServer minion, Optional<ActivationKey> activationKey,
            Optional<User> creator, boolean enableMinionService, boolean isSaltSSH) {
        String minionId = minion.getMinionId();

        // Generate pillar data
        try {
            MinionPillarManager.INSTANCE.generatePillar(minion);

            // Subscribe to config channels assigned to the activation key or initialize empty channel profile
            minion.subscribeConfigChannels(
                    activationKey.map(ActivationKey::getAllConfigChannels).orElse(emptyList()),
                    creator.orElse(null));
        }
        catch (RuntimeException e) {
            LOG.error("Error generating Salt files for minion '{}':{}", minionId, e.getMessage());
        }

        LOG.info("Finished minion registration: {}", minionId);

        StatesAPI.generateServerPackageState(minion);

        // Should we apply the highstate?
        boolean applyHighstate = activationKey.isPresent() && (activationKey.get().getDeployConfigs() ||
                activationKey.get().getEntitlements().stream().anyMatch(e -> !e.isBase()));

        // Apply initial states asynchronously
        List<String> statesToApply = new ArrayList<>();
        statesToApply.add(ApplyStatesEventMessage.CERTIFICATE);
        statesToApply.add(ApplyStatesEventMessage.CHANNELS);
        statesToApply.add(ApplyStatesEventMessage.PACKAGES);
        if (enableMinionService) {
            statesToApply.add(ApplyStatesEventMessage.SALT_MINION_SERVICE);
        }
        if (isSaltSSH) {
            // SSH Minions need this to set last booted value.
            statesToApply.add(ApplyStatesEventMessage.SYSTEM_INFO);
        }
        MessageQueue.publish(new ApplyStatesEventMessage(
                minion.getId(),
                minion.getCreator() != null ? minion.getCreator().getId() : null,
                !applyHighstate, // Refresh package list if we're not going to apply the highstate afterwards
                statesToApply
        ));

        // Call final highstate to deploy config channels if required
        if (applyHighstate) {
            MessageQueue.publish(new ApplyStatesEventMessage(
                    minion.getId(),
                    minion.getCreator() != null ? minion.getCreator().getId() : null,
                    true,
                    emptyList()));
        }
        SystemManager.setReportDbUser(minion, false);

        // get hardware and network async
        // Hardware refresh depends on channels being assigned so as a temporary
        // solution we schedule it 1 minute in the future. This should be fixed when
        // refactoring this method to provide clear dependencies for example via action chains.
        triggerHardwareRefresh(minion);
    }

    private static void triggerHardwareRefresh(MinionServer server) {
        try {
            ActionManager.scheduleHardwareRefreshAction(server.getOrg(), server,
                    Date.from(Instant.now().plus(1, ChronoUnit.MINUTES)));
        }
        catch (TaskomaticApiException e) {
            LOG.error("Could not schedule hardware refresh for system: {}", server.getId());
            throw new RhnRuntimeException(e);
        }
    }

    /**
     * Apply activation key properties to server state
     *
     * @param server object representing the table rhnServer
     * @param ak activation key
     * @param grains map of minion grains
     */
    public static void applyActivationKeyProperties(Server server, ActivationKey ak, ValueMap grains) {
        ak.getToken().getActivatedServers().add(server);
        ActivationKeyFactory.save(ak);

        ak.getServerGroups().forEach(group -> ServerFactory.addServerToGroup(server, group));

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
                        }).collect(toSet())
        );
        StateFactory.save(serverStateRevision);

        // Set additional entitlements, if any
        Set<Entitlement> validEntits = server.getOrg().getValidAddOnEntitlementsForOrg();
        ak.getToken().getEntitlements().forEach(sg -> {
            Entitlement e = sg.getAssociatedEntitlement();
            if (validEntits.contains(e) &&
                    e.isAllowedOnServer(server, grains) &&
                    systemEntitlementManager.canEntitleServer(server, e)) {
                ValidatorResult vr = systemEntitlementManager.addEntitlementToServer(server, e);
                if (vr.getWarnings().size() > 0) {
                    LOG.warn(vr.getWarnings().toString());
                }
                if (vr.getErrors().size() > 0) {
                    LOG.error(vr.getErrors().toString());
                }
            }
        });
    }

    /**
     * Subscribes minion to channels
     *
     * @param systemQuery the systemQuery instance
     * @param server the minion
     * @param grains the grains
     * @param activationKey the activation key
     * @param activationKeyLabel the activation key label
     */
    public static void subscribeMinionToChannels(SystemQuery systemQuery, MinionServer server,
            ValueMap grains, Optional<ActivationKey> activationKey, Optional<String> activationKeyLabel) {
        String minionId = server.getMinionId();

        if (!activationKey.isPresent() && activationKeyLabel.isPresent()) {
            LOG.warn("Default channel(s) will NOT be subscribed to: specified Activation Key {} is not valid " +
                    "for minionId {}", activationKeyLabel.get(), minionId);
            SystemManager.addHistoryEvent(server, "Invalid Activation Key",
                    "Specified Activation Key " + activationKeyLabel.get() +
                            " is not valid. Default channel(s) NOT subscribed to.");
            return;
        }

        Set<Channel> unfilteredChannels = Opt.fold(
                activationKey,
                // No ActivationKey
                () -> {
                    // if server has a base channel assigned and there is no AK -> don't assign new channels
                    if (server.getBaseChannel() != null) {
                        return server.getChannels();
                    }
                    // otherwise identify the product installed on the server
                    // and assign to it basic/common channels for its products installed
                    Set<SUSEProduct> suseProducts = identifyProduct(systemQuery, server, grains);
                    return findChannelsForProducts(suseProducts, minionId);
                },
                ak -> Opt.<Channel, Set<Channel>>fold(
                        ofNullable(ak.getBaseChannel()),
                        // ActivationKey without base channel (SUSE Manager Default)
                        () -> {
                            Set<SUSEProduct> suseProducts = identifyProduct(systemQuery, server, grains);
                            Set<Channel> channelsForProducts = findChannelsForProducts(suseProducts, minionId);
                            Set<Channel> baseChannels = channelsForProducts.stream()
                                    .filter(Channel::isBaseChannel)
                                    .collect(toSet());
                            if (baseChannels.isEmpty()) {
                                return emptySet();
                            }
                            else {
                                Channel baseChannel = baseChannels.stream().findFirst().get();
                                // assign the identified base channel, all mandatory channels
                                // and all and only other channels selected within the activation key
                                return Stream.concat(
                                        Stream.concat(
                                                Stream.of(baseChannel),
                                                ChannelsUtils.mandatoryChannelsByBaseChannel(baseChannel)),
                                        ak.getChannels().stream()
                                                .filter(c -> c.getParentChannel() != null &&
                                                        c.getParentChannel().getId().equals(baseChannel.getId())))
                                        .collect(toSet());
                            }
                        },
                        // assign base channel, all mandatory channels and all and only activation key channels selected
                        baseChannel ->
                                Stream.concat(
                                    Stream.of(baseChannel),
                                    Stream.concat(
                                            ChannelsUtils.mandatoryChannelsByBaseChannel(baseChannel),
                                            ak.getChannels().stream())
                                ).collect(toSet())
                )
        );

        server.setChannels(
                filterCompatibleChannelsForServerArch(server.getServerArch(), unfilteredChannels, activationKey));
    }

    /**
     * Given a set of channels, filter those channels that are compatible with the passed serverArch.
     * If an activationKey is passed, the base channel of the activationKey is used if it's compatible with the passed
     * serverArch.
     *
     * NOTE: this is needed for "special" cases like RES6 that has multiple architectures in one product, or in case an
     * activation key specifies a base channel for an incorrect arch.
     *
     * @param serverArch the serverArch
     * @param channels the channels to be filtered
     * @param activationKey the activationKey
     * @return a set of compatible channels
     */
    private static Set<Channel> filterCompatibleChannelsForServerArch(ServerArch serverArch, Set<Channel> channels,
            Optional<ActivationKey> activationKey) {
        Map<Boolean, List<Channel>> compatibleChannels =
                channels.stream().filter(c -> c.getChannelArch().getCompatibleServerArches().contains(serverArch))
                        .collect(partitioningBy(Channel::isBaseChannel));

        Optional<Channel> activationKeyBaseChannel = activationKey.flatMap(ak -> ofNullable(ak.getBaseChannel()));

        Stream<Channel> compatibleBaseChannels =
                Opt.fold(activationKeyBaseChannel, () -> compatibleChannels.get(true).stream(),
                        bc -> compatibleChannels.get(true).stream().filter(c -> bc.getId().equals(c.getId())));

        return Stream.concat(compatibleBaseChannels, compatibleChannels.get(false).stream()).collect(toSet());
    }

    private static Set<Channel> findChannelsForProducts(Set<SUSEProduct> suseProducts, String minionId) {
        Map<Boolean, List<SUSEProduct>> baseAndExtProd = suseProducts.stream()
                .collect(partitioningBy(SUSEProduct::isBase));
        Optional<SUSEProduct> baseProductOpt = ofNullable(baseAndExtProd.get(true))
                .flatMap(s -> s.stream().findFirst());
        List<SUSEProduct> extProducts = baseAndExtProd.get(false);

        return Opt.fold(
                baseProductOpt,
                () -> {
                    LOG.warn("Server {} has no identifiable base product and will register without base channel " +
                            "assignment", minionId);
                    return emptySet();
                },
                baseProduct -> Stream.concat(
                        lookupRequiredChannelsForProduct(baseProduct),
                        extProducts.stream()
                                .flatMap(ext -> recommendedChannelsByBaseProduct(baseProduct, ext))
                ).collect(toSet())
        );
    }

    /**
     * Check if the products installed on the minion are allowed for free
     * use on a SUSE Manager PAYG Server
     * @param systemQuery query api
     * @param minionId the minionId
     * @param channels assigned channels
     * @param grains the system grains
     * @param instanceFlavor public cloud instance flavor
     * @return true if the use is allowed
     */
    public static boolean isAllowedOnPayg(SystemQuery systemQuery, String minionId, Set<Channel> channels,
                                          ValueMap grains, String instanceFlavor) {
        if ("PAYG".equals(instanceFlavor)) {
            return true;
        }
        return identifyProduct(systemQuery, minionId, grains.getValueAsString("osarch"), channels, grains)
                .stream()
                .allMatch(p -> {
                    if (p.getFree()) {
                        return true;
                    }
                    ChannelFamily cf = p.getChannelFamily();
                    if (cf != null) {
                        return cf.getLabel().equals("SMP") || cf.getLabel().equals("SLE-M-T");
                    }
                    return false;
                });
    }

    private static Set<SUSEProduct> identifyProduct(SystemQuery systemQuery, MinionServer server, ValueMap grains) {
        return identifyProduct(systemQuery, server.getMinionId(), server.getServerArch().getLabel(),
                server.getChannels(), grains);
    }
    private static Set<SUSEProduct> identifyProduct(SystemQuery systemQuery, String minionId, String arch,
                                                    Set<Channel> channels, ValueMap grains) {
        if ("suse".equalsIgnoreCase(grains.getValueAsString(OS))) {
            Optional<List<Zypper.ProductInfo>> productList =
                    systemQuery.getProducts(minionId);
            return Opt.stream(productList).flatMap(pl -> pl.stream()
                    .flatMap(pi -> {
                        String osName = pi.getName().toLowerCase();
                        String osVersion = pi.getVersion();
                        String osArch = pi.getArch();
                        String osRelease = pi.getRelease();
                        Optional<SUSEProduct> suseProduct =
                                ofNullable(SUSEProductFactory.findSUSEProduct(osName,
                                        osVersion, osRelease, osArch, true));
                        if (!suseProduct.isPresent()) {
                            LOG.warn("No product match found for: {} {} {} {}", osName, osVersion, osRelease, osArch);
                        }
                        return Opt.stream(suseProduct);
                    })).collect(toSet());
        }
        else if (Set.of("redhat", "centos", "oel", "alibaba cloud (aliyun)", "almalinux", "amazon", "rocky")
                .contains(grains.getValueAsString(OS).toLowerCase())) {

            Optional<RedhatProductInfo> redhatProductInfo = systemQuery.redhatProductInfo(minionId);

            String productArch = arch.replace("-redhat-linux", "");
            Optional<RhelUtils.RhelProduct> rhelProduct =
                redhatProductInfo.flatMap(x -> RhelUtils.detectRhelProduct(
                    channels, productArch, x.getWhatProvidesRes(), x.getWhatProvidesSLL(), x.getRhelReleaseContent(),
                    x.getCentosReleaseContent(), x.getOracleReleaseContent(), x.getAlibabaReleaseContent(),
                    x.getAlmaReleaseContent(), x.getAmazonReleaseContent(), x.getRockyReleaseContent()));
            return Opt.stream(rhelProduct).flatMap(rhel -> {

                if (rhel.getSuseBaseProduct().isEmpty()) {
                    LOG.warn("No product match found for: {} {} {} {}", rhel.getName(), rhel.getVersion(),
                            rhel.getRelease(), productArch);
                    return Stream.empty();
                }
                return rhel.getAllSuseProducts().stream();
            }).collect(toSet());
        }
        else if ("ubuntu".equalsIgnoreCase(grains.getValueAsString(OS))) {
            SUSEProduct product = SUSEProductFactory.findSUSEProduct("ubuntu-client",
                    grains.getValueAsString("osrelease"), null, grains.getValueAsString(OS_ARCH) + "-deb", false);
            if (product != null) {
                return Collections.singleton(product);
            }
        }
        else if ("debian".equalsIgnoreCase(grains.getValueAsString(OS))) {
           SUSEProduct product = SUSEProductFactory.findSUSEProduct("debian-client",
                   grains.getValueAsString("osmajorrelease"), null, grains.getValueAsString(OS_ARCH) + "-deb", false);
           if (product != null) {
               return Collections.singleton(product);
           }
        }
        LOG.warn("No product match found. OS grain is {}, arch is {}", grains.getValueAsString(OS),
                grains.getValueAsString(OS_ARCH));
        return emptySet();
    }

    private static Stream<Channel> lookupRequiredChannelsForProduct(SUSEProduct sp) {
        return RegistrationUtils.recommendedChannelsByBaseProduct(sp);
    }

    private static Stream<Channel> recommendedChannelsByBaseProduct(SUSEProduct root) {
        return recommendedChannelsByBaseProduct(root, root);
    }

    private static Stream<Channel> recommendedChannelsByBaseProduct(SUSEProduct root, SUSEProduct product) {
        return root.parentChannel()
                .map(Channel::getLabel)
                .map(rootChannelLabel -> {

                    Stream<Channel> channelStream = product.getSuseProductChannels().stream()
                            .filter(SUSEProductChannel::isMandatory)
                            .map(SUSEProductChannel::getChannel)
                            // we want the parent channel (== null) and its childs
                            .filter(c -> c.getParentChannel() == null ||
                                c.getParentChannel().getLabel().equals(rootChannelLabel));

                    Stream<Channel> stream = SUSEProductFactory.findAllProductExtensionsOf(product, root)
                            .stream()
                            .flatMap(pe -> {
                                //NOTE: this assumes that all extensions of a product are not recommended
                                // if the product itself is not recommended
                                if (pe.isRecommended()) {
                                    return recommendedChannelsByBaseProduct(root, pe.getExtensionProduct());
                                }
                                else {
                                    return Stream.empty();
                                }
                            });
                    return Stream.concat(
                            channelStream,
                            stream
                    );
                }).orElseGet(Stream::empty);
    }
}
