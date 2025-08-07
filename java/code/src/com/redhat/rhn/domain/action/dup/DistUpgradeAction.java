/*
 * Copyright (c) 2012 SUSE LLC
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

package com.redhat.rhn.domain.action.dup;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductUpgrade;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.ServerFactory;

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
import com.suse.utils.Opt;

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

import javax.servlet.http.HttpServletRequest;

/**
 * DistUpgradeAction - Class representation of distribution upgrade action.
 */
public class DistUpgradeAction extends Action {
    private static final Logger LOG = LogManager.getLogger(DistUpgradeAction.class);

    private static final long serialVersionUID = 1585401756449185047L;
    private DistUpgradeActionDetails details;

    /**
     * Return the details.
     * @return details
     */
    public DistUpgradeActionDetails getDetails() {
        return details;
    }

    /**
     * Set the details.
     * @param detailsIn details
     */
    public void setDetails(DistUpgradeActionDetails detailsIn) {
        detailsIn.setParentAction(this);
        this.details = detailsIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {
        Map<Boolean, List<Channel>> collect = getDetails().getChannelTasks()
                .stream().collect(Collectors.partitioningBy(
                        ct -> ct.getTask() == DistUpgradeChannelTask.SUBSCRIBE,
                        Collectors.mapping(DistUpgradeChannelTask::getChannel,
                                Collectors.toList())
                ));

        List<Channel> subbed = collect.get(true);
        List<Channel> unsubbed = collect.get(false);

        getServerActions()
                .stream()
                .flatMap(s -> Opt.stream(s.getServer().asMinionServer()))
                .forEach(minion -> {
                    Set<Channel> currentChannels = minion.getChannels();
                    unsubbed.forEach(currentChannels::remove);
                    currentChannels.addAll(subbed);
                    MinionPillarManager.INSTANCE.generatePillar(minion);
                    ServerFactory.save(minion);
                });

        Map<String, Object> pillar = new HashMap<>();
        Map<String, Object> susemanager = new HashMap<>();
        pillar.put("susemanager", susemanager);
        Map<String, Object> distupgrade = new HashMap<>();
        susemanager.put("distupgrade", distupgrade);
        distupgrade.put("dryrun", getDetails().isDryRun());
        distupgrade.put(SaltParameters.ALLOW_VENDOR_CHANGE, getDetails().isAllowVendorChange());
        distupgrade.put("channels", subbed.stream()
                .sorted()
                .map(c -> "susemanager:" + c.getLabel())
                .collect(Collectors.toList()));
        if (Objects.nonNull(getDetails().getMissingSuccessors())) {
            pillar.put("missing_successors", Arrays.asList(getDetails().getMissingSuccessors().split(",")));
        }
        getDetails().getProductUpgrades().stream()
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

        LocalCall<Map<String, State.ApplyResult>> distUpgrade = State.apply(
                Collections.singletonList(ApplyStatesEventMessage.DISTUPGRADE),
                Optional.of(pillar)
        );
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        ret.put(distUpgrade, minionSummaries);

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleUpdateServerAction(ServerAction serverAction, JsonElement jsonResult, UpdateAuxArgs auxArgs) {
        if (details.isDryRun()) {
            Map<Boolean, List<Channel>> collect = details.getChannelTasks()
                    .stream().collect(Collectors.partitioningBy(
                            ct -> ct.getTask() == DistUpgradeChannelTask.SUBSCRIBE,
                            Collectors.mapping(DistUpgradeChannelTask::getChannel,
                                    Collectors.toList())
                    ));
            List<Channel> subbed = collect.get(true);
            List<Channel> unsubbed = collect.get(false);
            Set<Channel> currentChannels = serverAction.getServer().getChannels();
            currentChannels.removeAll(subbed);
            currentChannels.addAll(unsubbed);
            ServerFactory.save(serverAction.getServer());
            MessageQueue.publish(
                    new ChannelsChangedEventMessage(serverAction.getServerId()));
            MessageQueue.publish(
                    new ApplyStatesEventMessage(serverAction.getServerId(), false,
                            ApplyStatesEventMessage.CHANNELS));
            String message = parseDryRunMessage(jsonResult);
            serverAction.setResultMsg(message);
        }
        else {
            String message = parseMigrationMessage(jsonResult);
            serverAction.setResultMsg(message);

            // Make sure grains are updated after dist upgrade
            serverAction.getServer().asMinionServer().ifPresent(minionServer -> {
                MinionList minionTarget = new MinionList(minionServer.getMinionId());
                auxArgs.getSaltApi().syncGrains(minionTarget);
            });
        }
    }

    private String parseDryRunMessage(JsonElement jsonResult) {
        try {
            DistUpgradeDryRunSlsResult distUpgradeSlsResult = Json.GSON.fromJson(
                    jsonResult, DistUpgradeDryRunSlsResult.class);
            if (distUpgradeSlsResult.getSpmigration() != null) {
                return String.join(" ",
                        distUpgradeSlsResult.getSpmigration().getChanges().getRetOpt().orElse(""),
                        distUpgradeSlsResult.getSpmigration().getComment());
            }
        }
        catch (JsonSyntaxException e) {
            try {
                DistUpgradeOldSlsResult distUpgradeSlsResult = Json.GSON.fromJson(
                        jsonResult, DistUpgradeOldSlsResult.class);
                return String.join(" ",
                        distUpgradeSlsResult.getSpmigration().getChanges().getRetOpt()
                                .map(ModuleRun::getComment).orElse(""),
                        distUpgradeSlsResult.getSpmigration().getComment());
            }
            catch (JsonSyntaxException ex) {
                LOG.error("Unable to parse dry run result", ex);
            }
        }
        return "Unable to parse dry run result";
    }

    private String parseMigrationMessage(JsonElement jsonResult) {
        try {
            DistUpgradeSlsResult distUpgradeSlsResult = Json.GSON.fromJson(jsonResult, DistUpgradeSlsResult.class);
            if (distUpgradeSlsResult.getSpmigration() != null) {
                StateApplyResult<RetOpt<Map<String, Change<String>>>> spmig =
                        distUpgradeSlsResult.getSpmigration();
                String message = spmig.getComment();
                if (spmig.isResult()) {
                    message = spmig.getChanges().getRetOpt().map(ret -> ret.entrySet().stream().map(entry ->
                            entry.getKey() + ":" + entry.getValue().getOldValue() +
                                    "->" + entry.getValue().getNewValue()
                    ).collect(Collectors.joining(","))).orElse(spmig.getComment());
                }
                return message;
            }
            else if (distUpgradeSlsResult.getLiberate() != null) {
                StateApplyResult<CmdResult> liberate = distUpgradeSlsResult.getLiberate();
                String message = SaltUtils.getJsonResultWithPrettyPrint(jsonResult);
                if (liberate.isResult()) {
                    message = liberate.getChanges().getStdout();
                }
                return message;
            }
            return SaltUtils.getJsonResultWithPrettyPrint(jsonResult);
        }
        catch (JsonSyntaxException e) {
            try {
                DistUpgradeOldSlsResult distUpgradeSlsResult = Json.GSON.fromJson(
                        jsonResult, DistUpgradeOldSlsResult.class);
                return distUpgradeSlsResult.getSpmigration().getChanges()
                        .getRetOpt().map(ret -> {
                            if (ret.isResult()) {
                                return ret.getChanges().entrySet().stream()
                                        .map(entry -> entry.getKey() + ":" + entry.getValue().getOldValue() + "->" +
                                                entry.getValue().getNewValue()).collect(Collectors.joining(","));
                            }
                            else {
                                return ret.getComment();
                            }
                        }).orElse("");
            }
            catch (JsonSyntaxException ex) {
                try {
                    TypeToken<List<String>> typeToken = new TypeToken<>() {
                    };
                    List<String> saltError = Json.GSON.fromJson(jsonResult, typeToken.getType());
                    return String.join("\n", saltError);
                }
                catch (JsonSyntaxException exc) {
                    LOG.error("Unable to parse migration result", exc);
                }
            }
        }
        return "Unable to parse migration result";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setRequestAttributeDryRun(HttpServletRequest request) {
        boolean typeDistUpgradeDryRun = getDetails().isDryRun();
        request.setAttribute("typeDistUpgradeDryRun", typeDistUpgradeDryRun);
        return typeDistUpgradeDryRun;
    }
}
