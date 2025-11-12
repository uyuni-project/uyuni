/*
 * Copyright (c) 2012--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.redhat.rhn.domain.action.dup;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductUpgrade;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.frontend.struts.RequestContext;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.messaging.ChannelsChangedEventMessage;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.SaltParameters;
import com.suse.manager.webui.services.pillar.MinionPillarManager;
import com.suse.manager.webui.utils.salt.custom.DistUpgradeDryRunSlsResult;
import com.suse.manager.webui.utils.salt.custom.DistUpgradeOldSlsResult;
import com.suse.manager.webui.utils.salt.custom.DistUpgradeSlsResult;
import com.suse.manager.webui.utils.salt.custom.RetOpt;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.results.Change;
import com.suse.salt.netapi.results.CmdResult;
import com.suse.salt.netapi.results.ModuleRun;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.utils.Json;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.servlet.http.HttpServletRequest;

/**
 * DistUpgradeAction - Class representation of distribution upgrade action.
 */
@Entity
@DiscriminatorValue("501")
public class DistUpgradeAction extends Action {
    private static final Logger LOG = LogManager.getLogger(DistUpgradeAction.class);

    private static final long serialVersionUID = -702781375842108784L;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyColumn(name = "server_id")
    @JoinColumn(name = "action_id", nullable = false)
    private Map<Long, DistUpgradeActionDetails> detailsMap;

    /**
     * Return the details for the specified server.
     * @param serverId the server id
     * @return details
     */
    public DistUpgradeActionDetails getDetails(Long serverId) {
        return detailsMap.get(serverId);
    }

    /**
     * Set the details of a specific server.
     * @param detailsIn details
     */
    public void setDetails(DistUpgradeActionDetails detailsIn) {
        detailsIn.setParentAction(this);
        detailsMap.put(detailsIn.getServer().getId(), detailsIn);
    }

    /**
     * Gets the migration details associated to this action, mapped for each server
     * @return a map where the key is the server id and the value is a {@link  DistUpgradeActionDetails}
     */
    public Map<Long, DistUpgradeActionDetails> getDetailsMap() {
        return detailsMap;
    }

    /**
     * Sets the migration details for each server and associates them with this action
     * @param detailsMapIn a map where the key is the server id and the value is a {@link  DistUpgradeActionDetails}
     */
    public void setDetailsMap(Map<Long, DistUpgradeActionDetails> detailsMapIn) {
        detailsMapIn.values().forEach(details -> details.setParentAction(this));
        this.detailsMap = detailsMapIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();

        minionSummaries.forEach((minionSummary -> {
            DistUpgradeActionDetails actionDetails = getDetails(minionSummary.getServerId());

            Map<Boolean, List<Channel>> channelTaskMap = actionDetails
                .getChannelTasks()
                .stream()
                .collect(Collectors.partitioningBy(
                    ct -> ct.getTask() == DistUpgradeChannelTask.SUBSCRIBE,
                    Collectors.mapping(DistUpgradeChannelTask::getChannel, Collectors.toList())
                ));

            List<Channel> subscribedChannels = channelTaskMap.get(true);
            List<Channel> unsubscribedChannels = channelTaskMap.get(false);

            getServerActions()
                .stream()
                .filter(s -> Objects.equals(s.getServerId(), minionSummary.getServerId()))
                .flatMap(s -> s.getServer().asMinionServer().stream())
                .forEach(minion -> {
                    Set<Channel> currentChannels = minion.getChannels();
                    unsubscribedChannels.forEach(currentChannels::remove);
                    currentChannels.addAll(subscribedChannels);
                    MinionPillarManager.INSTANCE.generatePillar(minion);
                    ServerFactory.save(minion);
                });

            Map<String, Object> pillar = new HashMap<>();
            Map<String, Object> susemanager = new HashMap<>();
            pillar.put("susemanager", susemanager);
            Map<String, Object> distupgrade = new HashMap<>();
            susemanager.put("distupgrade", distupgrade);
            distupgrade.put("dryrun", actionDetails.isDryRun());
            distupgrade.put(SaltParameters.ALLOW_VENDOR_CHANGE, actionDetails.isAllowVendorChange());
            distupgrade.put("channels", subscribedChannels.stream()
                .sorted()
                .map(c -> "susemanager:" + c.getLabel())
                .collect(Collectors.toList()));

            if (Objects.nonNull(actionDetails.getMissingSuccessors())) {
                pillar.put("missing_successors", Arrays.asList(actionDetails.getMissingSuccessors().split(",")));
            }

            actionDetails.getProductUpgrades().stream()
                .map(SUSEProductUpgrade::getToProduct)
                .filter(SUSEProduct::isBase)
                .forEach(tgt -> {
                    Map<String, String> baseproduct = new HashMap<>();
                    baseproduct.put("name", tgt.getName());
                    baseproduct.put("version", tgt.getVersion());
                    baseproduct.put("arch", tgt.getArch().getLabel());
                    distupgrade.put("targetbaseproduct", baseproduct);
                });

            HibernateFactory.commitTransaction();

            var distUpgrade = State.apply(
                Collections.singletonList(ApplyStatesEventMessage.DISTUPGRADE),
                Optional.of(pillar)
            );

            ret.put(distUpgrade, List.of(minionSummary));
        }));

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleUpdateServerAction(ServerAction serverAction, JsonElement jsonResult, UpdateAuxArgs auxArgs) {
        Long serverId = serverAction.getServerId();
        DistUpgradeActionDetails details = getDetails(serverId);

        if (details == null) {
            return;
        }

        if (details.isDryRun()) {
            Map<Boolean, List<Channel>> channelTaskMap = details.getChannelTasks()
                .stream()
                .collect(Collectors.partitioningBy(
                    ct -> ct.getTask() == DistUpgradeChannelTask.SUBSCRIBE,
                    Collectors.mapping(DistUpgradeChannelTask::getChannel, Collectors.toList())
                ));

            List<Channel> subscribedChannels = channelTaskMap.get(true);
            List<Channel> unsubscribedChannels = channelTaskMap.get(false);

            Set<Channel> currentChannels = serverAction.getServer().getChannels();
            subscribedChannels.forEach(currentChannels::remove);
            currentChannels.addAll(unsubscribedChannels);
            ServerFactory.save(serverAction.getServer());

            var channelsChangedEvent = new ChannelsChangedEventMessage(serverId);
            var applyStatesEvent = new ApplyStatesEventMessage(serverId, false, ApplyStatesEventMessage.CHANNELS);

            MessageQueue.publish(channelsChangedEvent);
            MessageQueue.publish(applyStatesEvent);

            String result = parseDryRunMessage(jsonResult);
            serverAction.setResultMsg(result);
        }
        else {
            String result = parseMigrationMessage(jsonResult);
            serverAction.setResultMsg(result);

            // Make sure grains are updated after dist upgrade
            serverAction.getServer().asMinionServer().ifPresent(minionServer -> {
                MinionList minionTarget = new MinionList(minionServer.getMinionId());
                auxArgs.getSaltApi().syncGrains(minionTarget);
            });
        }
    }

    private String parseDryRunMessage(JsonElement jsonResult) {
        try {
            var distUpgradeResult = Json.GSON.fromJson(jsonResult, DistUpgradeDryRunSlsResult.class);
            if (distUpgradeResult.getSpmigration() != null) {
                return String.join(" ",
                    distUpgradeResult.getSpmigration().getChanges().getRetOpt().orElse(""),
                    distUpgradeResult.getSpmigration().getComment()
                );
            }
        }
        catch (JsonSyntaxException e) {
            try {
                var oldFormatResult = Json.GSON.fromJson(jsonResult, DistUpgradeOldSlsResult.class);
                return String.join(" ",
                    oldFormatResult.getSpmigration().getChanges().getRetOpt().map(ModuleRun::getComment).orElse(""),
                    oldFormatResult.getSpmigration().getComment()
                );
            }
            catch (JsonSyntaxException ex) {
                LOG.error("Unable to parse dry run result", ex);
            }
        }
        return "Unable to parse dry run result";
    }

    private String parseMigrationMessage(JsonElement jsonResult) {
        try {
            var distUpgradeResult = Json.GSON.fromJson(jsonResult, DistUpgradeSlsResult.class);
            if (distUpgradeResult.getSpmigration() != null) {
                StateApplyResult<RetOpt<Map<String, Change<String>>>> spmig = distUpgradeResult.getSpmigration();
                String message = spmig.getComment();

                if (spmig.isResult()) {
                    message = spmig.getChanges().getRetOpt()
                        .map(ret -> getChangesString(ret))
                        .orElse(spmig.getComment());
                }

                return message;
            }
            else if (distUpgradeResult.getLiberate() != null) {
                StateApplyResult<CmdResult> liberate = distUpgradeResult.getLiberate();
                String message = SaltUtils.getJsonResultWithPrettyPrint(jsonResult);
                if (liberate.isResult()) {
                    message = liberate.getChanges().getStdout();

                    StateApplyResult<CmdResult> liberatedResult = distUpgradeResult.getLiberatedResult();
                    if (liberatedResult.isResult()) {
                        message = Optional.ofNullable(message).orElse("") + "\n" +
                                liberatedResult.getChanges().getStdout();
                    }
                }

                return message;
            }

            return SaltUtils.getJsonResultWithPrettyPrint(jsonResult);
        }
        catch (JsonSyntaxException e) {
            try {
                var oldFormatResult = Json.GSON.fromJson(jsonResult, DistUpgradeOldSlsResult.class);

                return oldFormatResult.getSpmigration().getChanges().getRetOpt()
                    .map(ret -> {
                        if (ret.isResult()) {
                            return getChangesString(ret.getChanges());
                        }

                        return ret.getComment();
                    })
                    .orElse("");
            }
            catch (JsonSyntaxException ex) {
                try {
                    List<String> saltError = Json.GSON.fromJson(jsonResult, new TypeToken<>() { }.getType());
                    return String.join("\n", saltError);
                }
                catch (JsonSyntaxException exc) {
                    LOG.error("Unable to parse migration result", exc);
                }
            }
        }
        return "Unable to parse migration result";
    }

    private static String getChangesString(Map<String, Change<String>> ret) {
        return ret.entrySet().stream()
            .map(entry -> {
                String key = entry.getKey();

                String oldValue = entry.getValue().getOldValue();
                String newValue = entry.getValue().getNewValue();

                return "%s: %s->%s".formatted(key, oldValue, newValue);
            })
            .collect(Collectors.joining(","));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setRequestAttributeDryRun(HttpServletRequest request) {
        RequestContext requestContext = new RequestContext(request);
        Server server = requestContext.lookupAndBindServer();

        boolean typeDistUpgradeDryRun = getDetails(server.getId()).isDryRun();
        request.setAttribute("typeDistUpgradeDryRun", typeDistUpgradeDryRun);

        return typeDistUpgradeDryRun;
    }
}
