/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.manager.ssm;

import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.rhnset.RhnSetElement;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.channel.ssm.ChannelActionDAO;
import com.redhat.rhn.frontend.dto.EssentialChannelDto;
import com.redhat.rhn.frontend.dto.EssentialServerDto;
import com.redhat.rhn.frontend.dto.SystemsPerChannelDto;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.manager.ssm.channelchange.ChannelChangeFactory;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.reactor.messaging.ChannelsChangedEventMessage;
import com.suse.manager.webui.utils.gson.SsmBaseChannelChangesDto;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The current plan for this class is to manage all SSM operations. However, as more is
 * ported from perl to java, there may be a need to break this class into multiple
 * managers to keep it from becoming unwieldly.
 *
 * @author Jason Dobies
 */
public class SsmManager {

    private static final Logger LOG = LogManager.getLogger(SsmManager.class);

    public static final String SSM_SYSTEM_FEATURE = "ftr_system_grouping";

    /** Private constructor to enforce the stateless nature of this class. */
    private SsmManager() {
    }

    /**
     * Performs channel actions
     *
     * @param user user performing the action creations
     * @param sysMapping a collection of ChannelActionDAOs
     */
    public static void performChannelActions(User user, Collection<ChannelActionDAO> sysMapping) {
        Set<Long> serverChannelsChanged = new HashSet<>();

        for (ChannelActionDAO system : sysMapping) {
            for (Long cid : system.getSubscribeChannelIds()) {
                subscribeChannel(system.getId(), cid, user.getId());
                serverChannelsChanged.add(system.getId());
            }
            for (Long cid : system.getUnsubscribeChannelIds()) {
                SystemManager.unsubscribeServerFromChannel(system.getId(), cid);
                serverChannelsChanged.add(system.getId());
            }
        }
        for (Long sid : serverChannelsChanged) {
            MessageQueue.publish(new ChannelsChangedEventMessage(sid, user.getId()));
        }
    }


    private static void subscribeChannel(Long sid, Long cid, Long uid) {

        CallableMode m = ModeFactory.getCallableMode("Channel_queries", "subscribe_server_to_channel");

        Map<String, Object> in = new HashMap<>();
        in.put("server_id", sid);
        in.put("user_id", uid);
        in.put("channel_id", cid);

        m.execute(in, new HashMap<>());
    }

    /**
     * Adds the selected server IDs to the SSM RhnSet.
     *
     * @param user      cannot be <code>null</code>
     * @param serverIds cannot be <code>null</code>
     */
    public static void addServersToSsm(User user, String[] serverIds) {
        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addAll(Arrays.asList(serverIds));
        RhnSetManager.store(set);
    }

    /**
     * Returns a list of server-ids of the servers in the SSM selection, for the specified
     * user
     *
     * @param user user whose system-set we care about
     * @return list of server-ids
     */
    public static List<Long> listServerIds(User user) {
        RhnSet ssm = RhnSetDecl.SYSTEMS.lookup(user);
        List<Long> sids = new ArrayList<>();
        if (ssm != null) {
            for (RhnSetElement rse : ssm.getElements()) {
                sids.add(rse.getElement());
            }
        }
        return sids;
    }

    /**
     * Find servers in the user's system set that are subscribed
     * to the given channel.
     * @param user the user
     * @param baseChannelId the channel id
     * @return servers in the user's system set that are subscribed
     * to the given channel.
     */
    public static List<Server> findServersInSetByChannel(User user, Long baseChannelId) {
        List<Long> serverIds = ServerFactory.findServersInSetByChannel(user, baseChannelId);
        return serverIds.stream()
                .map(ServerFactory::lookupById)
                .collect(Collectors.toList());
    }

    /**
     * Schedule changing channels of systems in SSM.
     *
     * @param channelChanges changes to be scheduled
     * @param earliest earliest date/time to execute the actions
     * @param actionChain the action chain to which to add the action. Can be null.
     * @param user the user that schedules the change
     * @return list of results
     */
    public static List<ScheduleChannelChangesResultDto> scheduleChannelChanges(
            List<ChannelChangeDto> channelChanges, Date earliest, ActionChain actionChain, User user) {
        Stream<ChannelSelectionResult> withBaseChannelResults =
                handleChannelChangesForSystemsWithBaseChannel(channelChanges, user);

        DataResult<EssentialServerDto> systemsWithNoBaseChannel = SystemManager.systemsWithoutBaseChannelsInSet(user);
        Stream<ChannelSelectionResult> noBaseChannelResults = CollectionUtils.isNotEmpty(systemsWithNoBaseChannel) ?
            handleChannelChangesForSystemsWithNoBaseChannel(channelChanges, user, systemsWithNoBaseChannel) :
            Stream.empty();

        List<ChannelSelectionResult> allResults = Stream.concat(withBaseChannelResults, noBaseChannelResults).toList();

        // success channel selections
        Stream<ChannelSelectionResult> succeededResults = allResults.stream().filter(e -> !e.isError());
        var succededMap = succeededResults.collect(Collectors.groupingBy(ChannelSelectionResult::getChannelSelection));

        // error channel selections
        Stream<ChannelSelectionResult> errored = allResults.stream().filter(ChannelSelectionResult::isError);
        var erroredMap = errored.collect(Collectors.groupingBy(ChannelSelectionResult::getError));

        return scheduleChannelChanges(user, earliest, actionChain, succededMap, erroredMap);
    }

    private static List<ScheduleChannelChangesResultDto> scheduleChannelChanges(User user, Date earliest,
            ActionChain actionChain, Map<ChannelSelection, List<ChannelSelectionResult>> succeeded,
            Map<String, List<ChannelSelectionResult>> errored) {
        Stream<ScheduleChannelChangesResultDto> succeededResults = succeeded.keySet().stream()
            .flatMap(channelSelectionResults -> {
                List<ChannelSelectionResult> results = succeeded.get(channelSelectionResults);
                Optional<Channel> base = channelSelectionResults.getNewBaseChannel();
                Set<Channel> children = channelSelectionResults.getChildChannels();

                return scheduleSubscribeChannelsAction(results, base, children, user, earliest, actionChain).stream();
            });

        Stream<ScheduleChannelChangesResultDto> erroredResults = errored.entrySet().stream()
            .flatMap(entry -> {
                List<ChannelSelectionResult> results = entry.getValue();
                String errorMessage = entry.getKey();

                return results.stream()
                    .map(result -> SsmServerDto.from(result.getServer()))
                    .map(server -> new ScheduleChannelChangesResultDto(server, errorMessage));
            });

        return Stream.concat(succeededResults, erroredResults).collect(Collectors.toList());
    }

    private static Stream<ChannelSelectionResult> handleChannelChangesForSystemsWithNoBaseChannel(
            List<ChannelChangeDto> channelChanges, User user, DataResult<EssentialServerDto> systemsWithNoBaseChannel) {

        Set<ChannelChangeDto> srvChanges = channelChanges.stream()
                .filter(ch -> ch.getOldBaseId().isEmpty())
                .collect(Collectors.toSet());

        return systemsWithNoBaseChannel.stream()
                .map(srv -> handleSingleSystemChannelAddition(srvChanges, srv, user))
                .filter(Objects::nonNull);
    }

    private static ChannelSelectionResult handleSingleSystemChannelAddition(Set<ChannelChangeDto> srvChanges,
            EssentialServerDto srvDto, User user) {

        return ChannelChangeFactory.parseChanges(srvChanges)
            .map(change-> change.handleChange(user, ServerFactory.lookupById(srvDto.getId()), new HashMap<>()))
            .orElse(null);
    }

    private static Stream<ChannelSelectionResult> handleChannelChangesForSystemsWithBaseChannel(
            List<ChannelChangeDto> channelChanges, User user) {
        return ChannelManager.baseChannelsInSet(user).stream().flatMap(spc -> {
            Channel currentBase = ChannelFactory.lookupById(spc.getId());
            List<Server> oldBaseServers = SsmManager.findServersInSetByChannel(user, currentBase.getId());

            // find changes by old base
            Set<ChannelChangeDto> srvChanges = channelChanges.stream()
                    .filter(ch -> ch.getNewBaseId().isPresent()) // exclude invalid changes without a new base
                    .filter(ch -> ch.getOldBaseId().map(currentBase.getId()::equals).orElse(false))
                    .collect(Collectors.toSet());

            Map<String, List<Channel>> accessibleChildrenByBase =
                    oldBaseServers.stream()
                            .map(Server::getBaseChannel)
                            .distinct()
                            .collect(Collectors.toMap(Channel::getLabel,
                                    bc -> ChannelFactory.getAccessibleChildChannels(bc, user)));

            return oldBaseServers.stream()
                    .map(srv -> handleSingleSystemChannelChange(srvChanges, user, currentBase, srv,
                            accessibleChildrenByBase));
        });
    }

    private static ChannelSelectionResult handleSingleSystemChannelChange(
            Set<ChannelChangeDto> srvChanges, User user, Channel currentBase,
            Server srv, Map<String, List<Channel>> accessibleChildrenByBase) {

        return ChannelChangeFactory.parseChanges(srvChanges, currentBase)
            .map(channelChange -> channelChange.handleChange(user, srv, accessibleChildrenByBase))
            .orElseGet(() -> {
                LOG.error("Invalid channel change for serverId={}", srv.getId());
                return new ChannelSelectionResult(srv, "invalid_change");
            });
    }

    private static Set<ScheduleChannelChangesResultDto> scheduleSubscribeChannelsAction(
            List<ChannelSelectionResult> results, Optional<Channel> baseChannel, Set<Channel> childChannels,
            User user, Date earliest, ActionChain actionChain) {
        try {
            Set<Action> actions = ActionChainManager.scheduleSubscribeChannelsAction(
                    user,
                    results.stream().map(ChannelSelectionResult::getServerId).collect(Collectors.toSet()),
                    baseChannel,
                    childChannels,
                    earliest,
                    actionChain
            );

            long actionId = actions.stream().findFirst()
                .map(Action::getId)
                .orElseThrow(() -> new RuntimeException("No subscribe channels actions was scheduled"));

            return results.stream().map(result -> SsmServerDto.from(result.getServer()))
                .map(server -> new ScheduleChannelChangesResultDto(server, actionId))
                .collect(Collectors.toSet());
        }
        catch (TaskomaticApiException e) {
            LOG.error("Taskomatic error scheduling subscribe channel action", e);

            return results.stream().map(r -> SsmServerDto.from(r.getServer()))
                .map(server -> new ScheduleChannelChangesResultDto(server, "taskomatic_error"))
                .collect(Collectors.toSet());
        }
    }

    /**
     * Compute which child changes are allowed for the given base channel changes.
     *
     * @param changes base channel changes
     * @param user the user
     * @return allowed child channels for the given base channel changes
     */
    public static List<SsmAllowedChildChannelsDto> computeAllowedChannelChanges(SsmBaseChannelChangesDto changes,
                                                                                User user) {
        List<SsmAllowedChildChannelsDto> result = new ArrayList<>();

        for (SystemsPerChannelDto spc : ChannelManager.baseChannelsInSet(user)) {
            Channel currentBase = ChannelFactory.lookupById(spc.getId());

            // Handle changes with the same base
            changes.getChanges().stream()
                .filter(c -> c.getOldBaseId() == currentBase.getId())
                .findFirst()
                .ifPresent(change -> result.addAll(handleChangeWithSameBase(user, change, currentBase)));
        }

        // Now handle changes without a base
        changes.getChanges().stream()
            .filter(c -> c.getOldBaseId() == -1)
            .findFirst()
            .ifPresent(change -> result.addAll(handleChangeWithoutBase(user, change)));

        // else no change for systems without a base channel =>
        // there are no child channels to show

        // TEMPORARY HACK: re-iterate and set the recommended flag for the child channels
        // this is not optimal from the complexity POV (for each child channel we perform
        // a bunch of DB queries)
        result.forEach(change -> setChildChannelsRecommendedFlag(user, change));

        return result;
    }

    /**
     * Retrieves the IDs of all SSM systems that are subscribed to the specified channel.
     *
     * @param user      the user
     * @param channelId the ID of the channel to check subscriptions against
     * @return a {@code Set} of server IDs.
     */
    @SuppressWarnings("unchecked")
    public static Set<Long> listSsmServerIdsInChannel(User user, Long channelId) {
        String sql = """
                SELECT ST.element as server_id
                FROM rhnSet ST
                JOIN rhnServerChannel rsc ON ST.element = rsc.server_id
                WHERE ST.user_id = :user_id
                AND ST.label = :set_label
                AND rsc.channel_id = :channel_id
                """;

        List<Long> results = HibernateFactory.getSession().createNativeQuery(sql, Long.class)
                .addSynchronizedEntityClass(Server.class)
                .addScalar("server_id", Long.class)
                .setParameter("user_id", user.getId())
                .setParameter("set_label", RhnSetDecl.SYSTEMS.getLabel())
                .setParameter("channel_id", channelId)
                .getResultList();
        return new HashSet<>(results);
    }

    private static List<SsmAllowedChildChannelsDto> handleChangeWithSameBase(User user,
                                                                             SsmBaseChannelChangesDto.Change change,
                                                                             Channel currentBase) {
        List<Server> oldBaseServers = findServersInSetByChannel(user, change.getOldBaseId());

        // set base channel to default
        if (change.getNewBaseId() == -1) {
            List<SsmAllowedChildChannelsDto> groupByBaseChange = new ArrayList<>();
            for (Server srv : oldBaseServers) {
                // guess base channels for each server
                Optional<Channel> guessedChannel = ChannelManager.guessServerBaseChannel(user, srv.getId());

                SsmAllowedChildChannelsDto allowedChildren = guessedChannel
                    // we have a guess
                    .map(gc -> getOrCreateByOldAndNewBase(groupByBaseChange, currentBase, gc))
                    // could not guess any base channel
                    .orElseGet(() -> {
                        var noGuess = getOrCreateByOldAndNewBase(groupByBaseChange, currentBase, null);
                        noGuess.setNewBaseDefault(false);
                        return noGuess;
                    });

                if (allowedChildren.getNewBaseChannel().isPresent()) {
                    allowedChildren.getServers().add(SsmServerDto.from(srv));
                }
                else {
                    // new base channel could not be guessed
                    allowedChildren.getIncompatibleServers().add(SsmServerDto.from(srv));
                }
            }

            groupByBaseChange.forEach(defaultBase -> defaultBase.getNewBaseChannel()
                    .ifPresent(newBaseChannel -> fillChildChannels(user,
                        defaultBase,
                        newBaseChannel.getId())));

            return groupByBaseChange;
        }

        // explicit base channel change
        if (change.getNewBaseId() > 0) {
            List<EssentialChannelDto> compatibleBases = ChannelManager
                    .listCompatibleBaseChannelsForChannel(user, currentBase);
            Channel newBase = compatibleBases.stream()
                    .filter(cb -> cb.getId() == change.getNewBaseId())
                    .findFirst()
                    .map(ec -> ChannelFactory.lookupById(ec.getId()))
                    .orElseThrow(() ->
                            new IllegalArgumentException("New base id not compatible with old base"));
            SsmAllowedChildChannelsDto ac =
                    new SsmAllowedChildChannelsDto(
                            SsmChannelDto.from(currentBase),
                            SsmChannelDto.from(newBase),
                            false);
            ac.getServers()
                    .addAll(oldBaseServers.stream()
                            .map(SsmServerDto::from)
                            .toList());

            fillChildChannels(user, ac, newBase.getId());
            return List.of(ac);
        }

        // no base change
        SsmAllowedChildChannelsDto ac = new SsmAllowedChildChannelsDto(
                        SsmChannelDto.from(currentBase),
                        SsmChannelDto.from(currentBase),
                        false);

        ac.getServers().addAll(oldBaseServers.stream().map(SsmServerDto::from).toList());

        fillChildChannels(user, ac, change.getOldBaseId());
        return List.of(ac);
    }

    private static List<SsmAllowedChildChannelsDto> handleChangeWithoutBase(User user,
                                                                            SsmBaseChannelChangesDto.Change change) {
        List<SsmAllowedChildChannelsDto> allowedNoBase = new ArrayList<>();

        for (EssentialServerDto essentialServerDto : SystemManager.systemsWithoutBaseChannelsInSet(user)) {
            Server srv = ServerFactory.lookupById(essentialServerDto.getId());
            SsmServerDto ssmServerDto = SsmServerDto.from(srv);

            // set base channel to default
            if (change.getNewBaseId() == -1) {
                Optional<Channel> guessedChannel = ChannelManager.guessServerBaseChannel(user, srv.getId());

                SsmAllowedChildChannelsDto allowedChildren = guessedChannel
                    // we have a guess
                    .map(gc -> getOrCreateByOldAndNewBase(allowedNoBase, null, gc))
                    // could not guess any base channel
                    .orElseGet(() -> getOrCreateByOldAndNewBase(allowedNoBase, null, null));

                if (allowedChildren.getNewBaseChannel().isPresent()) {
                    allowedChildren.getServers().add(ssmServerDto);
                }
                else {
                    // new base channel could not be guessed
                    allowedChildren.getIncompatibleServers().add(ssmServerDto);
                }
            }
            // explicit base channel change
            else if (change.getNewBaseId() > 0) {
                Channel newBase = ChannelFactory.lookupById(change.getNewBaseId());

                // TODO check if newBase allowed
                SsmAllowedChildChannelsDto allowed = getOrCreateByOldAndNewBase(allowedNoBase, null, newBase);
                allowed.setNewBaseDefault(false);
                if (allowed.getChildChannels().isEmpty()) {
                    fillChildChannels(user, allowed, newBase.getId());
                }

                allowed.getServers().add(ssmServerDto);
            }

            // else no base change. since system has no base there are no child channels to find
        }

        allowedNoBase.forEach(allowed -> allowed.getNewBaseChannel()
            .ifPresent(newBaseChannel -> fillChildChannels(user, allowed, newBaseChannel.getId())));

        return allowedNoBase;
    }

    private static void setChildChannelsRecommendedFlag(User user, SsmAllowedChildChannelsDto change) {
        Optional<SsmChannelDto> newBaseChannelDto = change.getNewBaseChannel();

        newBaseChannelDto
            .map(channelDto -> ChannelManager.lookupByIdAndUser(channelDto.getId(), user))
            .map(ChannelManager::getOriginalChannel)
            .ifPresent(rootChannel -> {
                Stream<Channel> childChannelStream = change.getChildChannels().stream()
                    .map(channelDto -> ChannelManager.lookupByIdAndUser(channelDto.getId(), user));

                Map<Long, Boolean> channelRecommendedFlags =
                    ChannelManager.computeChannelRecommendedFlags(rootChannel, childChannelStream);

                change.getChildChannels().forEach(childChannelDto ->
                    childChannelDto.setRecommended(channelRecommendedFlags.get(childChannelDto.getId())));
            });
    }

    private static void fillChildChannels(User user, SsmAllowedChildChannelsDto allowed, long baseChannelId) {
        List<SsmChannelDto> childChannels = ChannelManager.findChildChannelsByParentInSSM(user, baseChannelId);
        allowed.setChildChannels(childChannels);
    }

    private static SsmAllowedChildChannelsDto getOrCreateByOldAndNewBase(
        List<SsmAllowedChildChannelsDto> allowedChildren, Channel oldBaseChannel, Channel newBaseChannel) {
        Optional<Channel> oldBase = Optional.ofNullable(oldBaseChannel);
        Optional<Channel> newBase = Optional.ofNullable(newBaseChannel);

        return allowedChildren.stream()
            .filter(allowed ->
                newBase.map(Channel::getId).equals(allowed.getNewBaseChannel().map(SsmChannelDto::getId)) &&
                oldBase.map(Channel::getId).equals(allowed.getOldBaseChannel().map(SsmChannelDto::getId))
            )
            .findFirst()
            .orElseGet(() -> {
                var ac = new SsmAllowedChildChannelsDto(
                    oldBase.map(SsmChannelDto::from), newBase.map(SsmChannelDto::from), true
                );

                allowedChildren.add(ac);
                return ac;
            });
    }
}
