/**
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
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.reactor.messaging.ChannelsChangedEventMessage;
import com.suse.manager.webui.utils.gson.SsmBaseChannelChangesDto;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The current plan for this class is to manage all SSM operations. However, as more is
 * ported from perl to java, there may be a need to break this class into multiple
 * managers to keep it from becoming unwieldly.
 *
 * @author Jason Dobies
 * @version $Revision$
 */
public class SsmManager {

    private static final Log LOG = LogFactory.getLog(SsmManager.class);

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
    public static void performChannelActions(User user,
            Collection<ChannelActionDAO> sysMapping) {
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

        CallableMode m = ModeFactory.getCallableMode("Channel_queries",
                "subscribe_server_to_channel");

        Map in = new HashMap();
        in.put("server_id", sid);
        in.put("user_id", uid);
        in.put("channel_id", cid);
        m.execute(in, new HashMap());
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
        List<Long> sids = new ArrayList<Long>();
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
                handleChannelChangesForSystemsWithBaseChannel(channelChanges, earliest, user);

        DataResult<EssentialServerDto> systemsWithNoBaseChannel = SystemManager.systemsWithoutBaseChannelsInSet(user);
        Stream<ChannelSelectionResult> noBaseChannelResults =
                systemsWithNoBaseChannel != null && !systemsWithNoBaseChannel.isEmpty() ?
                handleChannelChangesForSystemsWithNoBaseChannel(channelChanges,
                        earliest, user, systemsWithNoBaseChannel) :
                Stream.empty();

        List<ChannelSelectionResult> allResults = Stream.concat(withBaseChannelResults, noBaseChannelResults)
                                                        .collect(Collectors.toList());

        //success channel selections
        Stream<ChannelSelectionResult> succeededResults = allResults.stream().filter(e -> !e.isError());

        Map<ChannelSelection, List<ChannelSelectionResult>> succededMap = succeededResults
                .collect(Collectors.groupingBy(ChannelSelectionResult::getChannelSelection));

        //error channel selections
        Stream<ChannelSelectionResult> errored = allResults.stream().filter(ChannelSelectionResult::isError);

        Map<String, List<ChannelSelectionResult>> erroredMap = errored
                .collect(Collectors.groupingBy(ChannelSelectionResult::getError));

        return scheduleChannelChanges(user, earliest, actionChain, succededMap, erroredMap);
    }

    private static List<ScheduleChannelChangesResultDto> scheduleChannelChanges(User user, Date earliest,
            ActionChain actionChain, Map<ChannelSelection, List<ChannelSelectionResult>> succeded,
            Map<String, List<ChannelSelectionResult>> errored) {
        Stream<ScheduleChannelChangesResultDto> succededResults =
                succeded.entrySet().stream().map(e ->
                           scheduleSubscribeChannelsAction(succeded.get(e.getKey()),
                                                           e.getKey().getNewBaseChannel(),
                                                           e.getKey().getChildChannels(),
                                                           user, earliest, actionChain)
        ).flatMap(Set::stream);

        Stream<ScheduleChannelChangesResultDto> erroredResults =
                errored.entrySet().stream().map(e -> e.getValue().stream()
                        .map(r -> new ScheduleChannelChangesResultDto(SsmServerDto.from(r.getServer()), e.getKey())
                )
        ).flatMap(Function.identity());

        return Stream.concat(succededResults, erroredResults).collect(Collectors.toList());
    }

    private static Stream<ChannelSelectionResult> handleChannelChangesForSystemsWithNoBaseChannel(
            List<ChannelChangeDto> channelChanges, Date earliest, User user,
            DataResult<EssentialServerDto> systemsWithNoBaseChannel) {

        Set<ChannelChangeDto> srvChanges = channelChanges.stream()
                .filter(ch -> !ch.getOldBaseId().isPresent())
                .collect(Collectors.toSet());

        return systemsWithNoBaseChannel.stream()
                .map(srv -> handleSingleSystemChannelAddition(srvChanges, srv, earliest, user))
                .filter(c -> c != null);
    }

    private static ChannelSelectionResult handleSingleSystemChannelAddition(Set<ChannelChangeDto> srvChanges,
            EssentialServerDto srvDto, Date earliest, User user) {
        Server srv = ServerFactory.lookupById(srvDto.getId());

        if (defaultBaseChange(srvChanges)) {
            return handleDefaultBaseChannelChange(earliest, user, srv, srvChanges);
        }
        else if (explicitChange(srvChanges)) {
            return handleExplicitBaseChannelChange(earliest, user, Optional.empty(), srv,
                    srvChanges.stream().findFirst().get());
        }
        // no child-only changes possible in case of systems without a base channel
        return null;
    }

    private static Stream<ChannelSelectionResult> handleChannelChangesForSystemsWithBaseChannel(
            List<ChannelChangeDto> channelChanges, Date earliest, User user) {
        return ChannelManager.baseChannelsInSet(user).stream().flatMap(spc -> {
            Channel currentBase = ChannelFactory.lookupById(spc.getId().longValue());
            List<Server> oldBaseServers = SsmManager.findServersInSetByChannel(user, currentBase.getId());

            // find changes by old base
            Set<ChannelChangeDto> srvChanges = channelChanges.stream()
                    .filter(ch -> ch.getNewBaseId().isPresent()) // exclude invalid changes without a new base
                    .filter(ch -> ch.getOldBaseId().map(currentBase.getId()::equals).orElse(false))
                    .collect(Collectors.toSet());

            return oldBaseServers.stream()
                    .map(srv -> handleSingleSystemChannelChange(srvChanges, earliest, user, currentBase, srv));
        });
    }

    private static ChannelSelectionResult handleSingleSystemChannelChange(Set<ChannelChangeDto> srvChanges,
            Date earliest, User user, Channel currentBase, Server srv) {
        if (defaultBaseChange(srvChanges)) {
            return handleDefaultBaseChannelChange(earliest, user, srv, srvChanges);
        }
        else if (explicitChange(srvChanges)) {
            return handleExplicitBaseChannelChange(earliest, user,  Optional.of(currentBase), srv,
                    srvChanges.stream().findFirst().get());
        }
        else if (onlyChildChannelsChange(srvChanges)) {
            ChannelChangeDto srvChange = srvChanges.stream().findFirst().get();
            Set<Channel> childChannels = getChildChannelsForChange(user, srv, srvChange, srv.getBaseChannel());

            return new ChannelSelectionResult(srv, new ChannelSelection(Optional.empty(), childChannels));
        }
        else {
            LOG.error("Invalid channel change for serverId=" + srv.getId());
            return new ChannelSelectionResult(srv, "invalid_change");
        }
    }

    private static ChannelSelectionResult handleExplicitBaseChannelChange(Date earliest, User user,
                                                        Optional<Channel> currentBase,
                                                        Server srv,
                                                        ChannelChangeDto srvChange) {
        boolean baseChange =
                (currentBase.isPresent() && !currentBase.get().getId().equals(srvChange.getNewBaseId().orElse(null))) ||
                !(currentBase.isPresent() && srvChange.getNewBaseId().isPresent());

        boolean newBaseIsCompatible = true;
        if (baseChange) {
            if (currentBase.isPresent()) {
                List<EssentialChannelDto> compatibleBaseChannels = ChannelManager
                        .listCompatibleBaseChannelsForChannel(user, currentBase.get());
                newBaseIsCompatible =
                        // new base is in the compatible channels list
                        compatibleBaseChannels.stream()
                                .anyMatch(cbc -> cbc.getId().equals(srvChange.getNewBaseId().orElse(null)));

            }
            else {
                // system doesn't have a base
                List<EssentialChannelDto> availableBaseChannels = ChannelManager.listBaseChannelsForSystem(user, srv);
                newBaseIsCompatible = availableBaseChannels.stream()
                        .anyMatch(abc -> abc.getId().equals(srvChange.getNewBaseId().orElse(null)));
            }
        }

        if (!newBaseIsCompatible) {
            LOG.error("New base id=" + srvChange.getNewBaseId().get() +
                    " not compatible with base id=" +
                    Optional.ofNullable(srv.getBaseChannel()).map(b -> b.getId() + "").orElse("none") +
                    " for serverId=" + srv.getId());
            return new ChannelSelectionResult(srv, "incompatible_base");
        }
        else {
            Channel newBaseChannel = ChannelFactory.lookupById(srvChange.getNewBaseId().get());
            Set<Channel> childChannels = getChildChannelsForChange(user, srv, srvChange, newBaseChannel);

            return new ChannelSelectionResult(srv, new ChannelSelection(Optional.of(newBaseChannel), childChannels));
        }
    }

    private static ChannelSelectionResult handleDefaultBaseChannelChange(Date earliest, User user,
                                                       Server srv, Set<ChannelChangeDto> srvChanges) {
        Optional<Channel> guessedChannel =
                ChannelManager.guessServerBaseChannel(user, srv.getId());
        if (!guessedChannel.isPresent()) {
            LOG.error("Could not guess base channel for serverId=" +
                    srv.getId() +
                    " user=" +
                    user.getLogin());
            return new ChannelSelectionResult(srv, "no_base_channel_guess");
        }
        Optional<ChannelChangeDto> srvChange = getChangeByDefaultBase(srvChanges, guessedChannel.get());
        if (srvChange.isPresent()) {
            Channel newBaseChannel = ChannelFactory.lookupById(srvChange.get().getNewBaseId().get());
            Set<Channel> childChannels = getChildChannelsForChange(user, srv, srvChange.get(), newBaseChannel);

            return new ChannelSelectionResult(srv, new ChannelSelection(Optional.of(newBaseChannel), childChannels));
        }
        else {
            LOG.warn("No base channel change found for serverId=" + srv.getId());
            return new ChannelSelectionResult(srv, "no_base_change_found");
        }
    }

    private static Set<ScheduleChannelChangesResultDto> scheduleSubscribeChannelsAction(
                                                                                   List<ChannelSelectionResult> results,
                                                                                   Optional<Channel> baseChannel,
                                                                                   Set<Channel> childChannels,
                                                                                   User user, Date earliest,
                                                                                   ActionChain actionChain) {
        try {
            Set<Action> actions = ActionChainManager.scheduleSubscribeChannelsAction(
                    user,
                    results.stream().map(ChannelSelectionResult::getServerId).collect(Collectors.toSet()),
                    baseChannel,
                    childChannels,
                    earliest,
                    actionChain);
            long actionId = actions.stream().findFirst().map(a -> a.getId())
                    .orElseThrow(() -> new RuntimeException("No subscribe channels actions was scheduled"));

            return results.stream()
                    .map(r -> new ScheduleChannelChangesResultDto(SsmServerDto.from(r.getServer()), actionId))
                    .collect(Collectors.toSet());
        }
        catch (TaskomaticApiException e) {
            LOG.error("Taskomatic error scheduling subscribe channel action", e);
            return results.stream()
                    .map(r -> new ScheduleChannelChangesResultDto(SsmServerDto.from(r.getServer()), "taskomatic_error"))
                    .collect(Collectors.toSet());
        }
    }

    private static Set<Channel> getChildChannelsForChange(User user, Server server, ChannelChangeDto change,
                                                           Channel newBaseChannel) {
        List<Channel> accessibleChildren = ChannelFactory.getAccessibleChildChannels(newBaseChannel, user);
        // TODO ChannelManager.verifyChannelSubscribe() for base and children ?
        Set<Channel> result = new HashSet<>();
        change.getChildChannelActions().forEach((cid, action) -> {
            final long childId = cid;
            if (action == ChannelChangeDto.ChannelAction.SUBSCRIBE) {
                Optional<Channel> childToSubscribe = accessibleChildren.stream()
                        .filter(ac -> ac.getId().equals(childId))
                        .findFirst();
                if (childToSubscribe.isPresent()) {
                    result.add(childToSubscribe.get());
                }
                else {
                    LOG.warn("Child channel id=" + childId +
                            " not found in accessible children of " +
                            newBaseChannel.getName() +
                            " for user=" +
                            user.getLogin());
                }
            }
            else if (action == ChannelChangeDto.ChannelAction.NO_CHANGE) {
                server.getChildChannels().stream()
                        .filter(cc -> cc.getId() == childId)
                        .findFirst()
                        .ifPresent(result::add);
            }
        });
        return result;
    }

    private static Optional<ChannelChangeDto> getChangeByDefaultBase(Set<ChannelChangeDto> changes,
                                                                     Channel guessedChannel) {
        return changes.stream()
                .filter(ch -> ch.isNewBaseDefault() &&
                        ch.getNewBaseId().isPresent() &&
                        ch.getNewBaseId().get().longValue() == guessedChannel.getId().longValue())
                .findFirst();
    }

    private static boolean onlyChildChannelsChange(Set<ChannelChangeDto> changes) {
        return changes.size() == 1 && changes.stream()
                .anyMatch(ch -> ch.getNewBaseId().isPresent() &&
                        ch.getOldBaseId().isPresent() &&
                        ch.getOldBaseId().get().equals(ch.getNewBaseId().get())
                );
    }

    private static boolean explicitChange(Set<ChannelChangeDto> changes) {
        return changes.size() == 1 &&
                changes.stream()
                        .anyMatch(ch -> ch.getNewBaseId().isPresent() && !ch.isNewBaseDefault());
    }

    private static boolean defaultBaseChange(Set<ChannelChangeDto> changes) {
        return changes.stream()
                .allMatch(ch -> ch.getNewBaseId().isPresent() && ch.isNewBaseDefault());
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
            Channel currentBase = ChannelFactory.lookupById(spc.getId().longValue());

            Optional<SsmBaseChannelChangesDto.Change> baseChange =
                changes.getChanges().stream()
                        .filter(c -> c.getOldBaseId() == currentBase.getId())
                        .findFirst();
            baseChange.ifPresent(change -> {
                List<Server> oldBaseServers = findServersInSetByChannel(user,
                        change.getOldBaseId());

                if (change.getNewBaseId() == -1) {
                    // set base channel to default
                    List<SsmAllowedChildChannelsDto> groupByBaseChange = new ArrayList<>();
                    for (Server srv : oldBaseServers) {
                        // guess base channels for each server
                        Optional<Channel> guessedChannel =
                                ChannelManager.guessServerBaseChannel(user, srv.getId());

                        SsmAllowedChildChannelsDto allowedChildren = guessedChannel
                                .map(gc ->
                                        // we have a guess
                                        getOrCreateByOldAndNewBase(groupByBaseChange,
                                                Optional.of(currentBase),
                                                Optional.of(gc))
                                )
                                .orElseGet(() -> {
                                        // could not guess any base channel
                                        SsmAllowedChildChannelsDto noGuess =
                                                getOrCreateByOldAndNewBase(groupByBaseChange,
                                                Optional.of(currentBase),
                                                Optional.empty());
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

                    groupByBaseChange.forEach(defaultBase -> {
                        defaultBase.getNewBaseChannel().ifPresent(newBaseChannel -> {
                            fillChildChannels(user,
                                    defaultBase,
                                    newBaseChannel.getId());
                        });
                    });
                    result.addAll(groupByBaseChange);

                }
                else if (change.getNewBaseId() > 0) {
                    // explicit base channel change
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
                                    .collect(Collectors.toList()));

                    fillChildChannels(user, ac, newBase.getId());
                    result.add(ac);
                }
                else {
                    // no base change
                    SsmAllowedChildChannelsDto ac =
                            new SsmAllowedChildChannelsDto(
                                    SsmChannelDto.from(currentBase),
                                    SsmChannelDto.from(currentBase),
                                    false);
                    ac.getServers()
                            .addAll(oldBaseServers.stream()
                                    .map(SsmServerDto::from)
                                    .collect(Collectors.toList()));

                    fillChildChannels(user, ac, change.getOldBaseId());
                    result.add(ac);
                }
            });
        }

        Optional<SsmBaseChannelChangesDto.Change> withoutBaseChange =
                changes.getChanges().stream()
                    .filter(c -> c.getOldBaseId() == -1)
                    .findFirst();

        withoutBaseChange.ifPresent(change -> {
            List<SsmAllowedChildChannelsDto> allowedNoBase = new ArrayList<>();

            for (EssentialServerDto srvDto : SystemManager.systemsWithoutBaseChannelsInSet(user)) {
                Server srv = ServerFactory.lookupById(srvDto.getId());
                if (change.getNewBaseId() == -1) {
                    // set base channel to default
                    Optional<Channel> guessedChannel =
                            ChannelManager.guessServerBaseChannel(user, srv.getId());
                    SsmAllowedChildChannelsDto allowedChildren = guessedChannel
                            .map(gc ->
                                    // we have a guess
                                    getOrCreateByOldAndNewBase(allowedNoBase,
                                            Optional.empty(),
                                            Optional.of(gc))
                            )
                            .orElseGet(() ->
                                    // could not guess any base channel
                                    getOrCreateByOldAndNewBase(allowedNoBase,
                                            Optional.empty(),
                                            Optional.empty())
                            );
                    if (allowedChildren.getNewBaseChannel().isPresent()) {
                        allowedChildren.getServers().add(SsmServerDto.from(srv));
                    }
                    else {
                        // new base channel could not be guessed
                        allowedChildren.getIncompatibleServers().add(SsmServerDto.from(srv));
                    }
                }
                else if (change.getNewBaseId() > 0) {
                    // explicit base channel change
                    Channel newBase = ChannelFactory.lookupById(change.getNewBaseId());
                    // TODO check if newBase allowed
                    SsmAllowedChildChannelsDto allowed = getOrCreateByOldAndNewBase(allowedNoBase,
                            Optional.empty(),
                            Optional.of(newBase));
                    allowed.setNewBaseDefault(false);
                    if (allowed.getChildChannels().isEmpty()) {
                        fillChildChannels(user, allowed, newBase.getId());
                    }
                    allowed.getServers().add(SsmServerDto.from(srv));
                }
                // else no base change. since system has no base there are no child channels to find

            }
            allowedNoBase.forEach(allowed -> {
                allowed.getNewBaseChannel().ifPresent(newBaseChannel -> {
                    fillChildChannels(user,
                            allowed,
                            newBaseChannel.getId());
                });
            });
            result.addAll(allowedNoBase);
        });
        // else no change for systems without a base channel =>
        // there are no child channels to show

        // TEMPORARY HACK: re-iterate and set the recommended flag for the child channels
        // this is not optimal from the complexity POV (for each child channel we perform
        // a bunch of DB queries)
        result.forEach(change -> {
            Optional<SsmChannelDto> newBaseChannelDto = change.getNewBaseChannel();

            Optional<Channel> newRootChannel = newBaseChannelDto
                    .map(channelDto -> ChannelManager.lookupByIdAndUser(channelDto.getId(), user))
                    .map(channel -> ChannelManager.getOriginalChannel(channel));

            newRootChannel.ifPresent(rootChannel -> {
                Stream<Channel> childChannelStream = change.getChildChannels().stream()
                        .map(channelDto -> ChannelManager.lookupByIdAndUser(channelDto.getId(), user));
                Map<Long, Boolean> channelRecommendedFlags =
                        ChannelManager.computeChannelRecommendedFlags(rootChannel, childChannelStream);
                change.getChildChannels().forEach(childChannelDto ->
                        childChannelDto.setRecommended(channelRecommendedFlags.get(childChannelDto.getId())));
            });
        });

        return result;
    }

    private static void fillChildChannels(User user,
                                          SsmAllowedChildChannelsDto allowed,
                                          long baseChannelId) {
        List<SsmChannelDto> childChannels = ChannelManager
                .findChildChannelsByParentInSSM(user, baseChannelId);
        allowed.setChildChannels(childChannels);
    }

    private static SsmAllowedChildChannelsDto getOrCreateByOldAndNewBase(
            List<SsmAllowedChildChannelsDto> allowedChildren,
            Optional<Channel> oldBase,
            Optional<Channel> newBase) {
        return allowedChildren.stream()
                .filter(allowed ->
                        newBase.map(Channel::getId).equals(allowed.getNewBaseChannel().map(SsmChannelDto::getId)) &&
                        oldBase.map(Channel::getId).equals(allowed.getOldBaseChannel().map(SsmChannelDto::getId))
                )
                .findFirst()
                .orElseGet(() -> {
                    SsmAllowedChildChannelsDto ac =
                            new SsmAllowedChildChannelsDto(
                                    oldBase.map(SsmChannelDto::from),
                                    newBase.map(SsmChannelDto::from),
                                    true);
                    allowedChildren.add(ac);
                    return ac;
                });
    }
}
