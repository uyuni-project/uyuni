/*
 * Copyright (c) 2012--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.action.dup;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductUpgrade;
import com.redhat.rhn.domain.server.MinionServer;
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
import com.suse.manager.webui.utils.token.DownloadTokenBuilder;
import com.suse.manager.webui.utils.token.Token;
import com.suse.manager.webui.utils.token.TokenBuildingException;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.servlet.http.HttpServletRequest;

/**
 * DistUpgradeAction - Class representation of distribution upgrade action.
 */
@Entity
@DiscriminatorValue("501")
public class DistUpgradeAction extends Action {
    private static final Logger LOG = LogManager.getLogger(DistUpgradeAction.class);

    private static final long serialVersionUID = -702781375842108784L;

    private static final String SLES16_SUCCESS_STATE = "sles16_migration_success";
    private static final String SLES16_FAILED_STATE = "sles16_migration_failed";

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
            if (actionDetails == null) {
                LOG.error("No DistUpgradeActionDetails found for server: {}", minionSummary.getServerId());
                return;
            }

            Map<Boolean, List<Channel>> channelTaskMap = actionDetails
                .getChannelTasks()
                .stream()
                .collect(Collectors.partitioningBy(
                    ct -> ct.getTask() == DistUpgradeChannelTask.SUBSCRIBE,
                    Collectors.mapping(DistUpgradeChannelTask::getChannel, Collectors.toList())
                ));

            List<Channel> subscribedChannels = channelTaskMap.get(true);
            List<Channel> unsubscribedChannels = channelTaskMap.get(false);


            // Determine if this is specifically a SLES 15 SPx -> SLES 16.x migration
            boolean isSLES15to16Migration = actionDetails.isSles15To16Migration();
            List<Map<String, String>> sles16TargetChannelTokens = new ArrayList<>();
            getServerActions()
              .stream()
              .filter(sa -> Objects.equals(sa.getServerId(), minionSummary.getServerId()))
              .flatMap(sa -> sa.getServer().asMinionServer().stream())
              .findFirst()
              .ifPresent(minion -> {
                  if (isSLES15to16Migration) {
                      sles16TargetChannelTokens.addAll(generateSles16Tokens(minion, subscribedChannels));
                  }
                  else {
                      switchChannels(minion, subscribedChannels, unsubscribedChannels, false);
                  }
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
            if (isSLES15to16Migration && !sles16TargetChannelTokens.isEmpty()) {
                pillar.put("sles16_target_channels", sles16TargetChannelTokens);
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

            // Determine which Salt state to use
            String saltStateName = ApplyStatesEventMessage.DISTUPGRADE;
            if (isSLES15to16Migration) {
                pillar.put("action_id", getId().toString());
                saltStateName = ApplyStatesEventMessage.DISTUPGRADE_SLES16;
                LOG.info("Using DMS-based migration state for SLES 15 SP7 -> SLES 16.0");
            }

            var distUpgrade = State.apply(
                Collections.singletonList(saltStateName),
                Optional.of(pillar)
            );

            ret.put(distUpgrade, List.of(minionSummary));
        }));

        return ret;
    }

    /**
     * Helper to determine if this action is a SLES 15 to 16 migration.
     * @return true if it is
     */
    public boolean isSles15To16Migration() {
        return getDetailsMap().values().stream()
                .anyMatch(DistUpgradeActionDetails::isSles15To16Migration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleUpdateServerAction(ServerAction serverAction, JsonElement jsonResult, UpdateAuxArgs auxArgs) {
        if (isMajorMigrationVerificationResult(jsonResult)) {
            handleVerificationResult(serverAction, jsonResult);
            return;
        }

        Long serverId = serverAction.getServerId();
        DistUpgradeActionDetails details = getDetails(serverId);

        if (details == null) {
            return;
        }
        // For SLES 16 migrations: the initial state only creates the
        // marker file and disconnects the minion. The action must stay In Progress until
        // sles16_verify runs after the minion reconnects.
        //
        // SaltUtils.updateServerAction() calls setStatusCompleted() BEFORE calling
        // this method. We must explicitly undo that here, otherwise
        // the action would appear complete while the minion is still offline.
        if (!details.isDryRun() && details.isSles15To16Migration() &&
          serverAction.getStatus() == ActionFactory.STATUS_COMPLETED) {
            serverAction.setStatusPickedUp();
            serverAction.setCompletionTime(null);
            LOG.info("SLES 16: reset action to In Progress on server {} - waiting for post-reconnect verification.",
              serverId);
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
            unsubscribedChannels.forEach(currentChannels::remove);
            currentChannels.addAll(subscribedChannels);
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
    /**
     * Check if the result is from a sles16_verify state.
     *
     * @param jsonResult the full state.apply result
     * @return true if this is a verification state result
     */
    public static boolean isMajorMigrationVerificationResult(JsonElement jsonResult) {
        if (jsonResult == null || !jsonResult.isJsonObject()) {
            return false;
        }
        return jsonResult.getAsJsonObject().keySet().stream().anyMatch(k -> k.contains(SLES16_SUCCESS_STATE) ||
                           k.contains(SLES16_FAILED_STATE));
    }
    /**
     * Handle SLES 16 migration verification result.
     * Called when the sles16_verify state runs after the minion reconnects to the server.
     *
     * @param serverAction the server action representing the migration
     * @param jsonResult the full state.apply result map from the sles16_verify state
     */
    private void handleVerificationResult(ServerAction serverAction, JsonElement jsonResult) {
        if (ActionFactory.STATUS_COMPLETED.equals(serverAction.getStatus()) ||
                ActionFactory.STATUS_FAILED.equals(serverAction.getStatus())) {
            LOG.info("Verification result already processed for server: {}, skipping",
                    serverAction.getServerId());
            return;
        }
        boolean migrationSucceeded = parseVerificationStatus(jsonResult);
        String resultMessage = extractVerificationMessage(jsonResult);
        LocalizationService ls = LocalizationService.getInstance();

        if (migrationSucceeded) {
            serverAction.setStatusCompleted();
            String successMsg = ls.getMessage("distupgrade.sles16.migration.success", "\n\n" + resultMessage);
            String postSteps = ls.getMessage("distupgrade.sles16.migration.success.post_steps");
            serverAction.setResultMsg(successMsg + "\n\n" + postSteps);

            LOG.info("SLES 16 migration succeeded for server: {}", serverAction.getServerId());
            applyDelayedChannelSwitch(serverAction);
        }
        else {
            serverAction.setStatusFailed();
            String failureMsg = ls.getMessage("distupgrade.sles16.migration.failed", "\n\n" + resultMessage);
            serverAction.setResultMsg(failureMsg);
            LOG.error("SLES 16 migration failed for server: {}", serverAction.getServerId());
            revertToOriginalChannels(serverAction);
        }

        serverAction.setCompletionTime(new Date());
    }
    /**
     * Revert channels back to SP7 after a failed SLES 16 migration.
     * @param serverAction the server action representing the migration
     */
    private void revertToOriginalChannels(ServerAction serverAction) {
        serverAction.getServer().asMinionServer().ifPresent(minion -> {
            DistUpgradeActionDetails details = getDetails(minion.getId());
            if (details == null || details.isDryRun()) {
                return;
            }

            Map<Boolean, List<Channel>> channelTaskMap = details.getChannelTasks()
              .stream()
              .collect(Collectors.partitioningBy(
                ct -> ct.getTask() == DistUpgradeChannelTask.SUBSCRIBE,
                Collectors.mapping(DistUpgradeChannelTask::getChannel, Collectors.toList())
              ));

            List<Channel> subscribedChannels = channelTaskMap.get(true);   // SLES 16
            List<Channel> unsubscribedChannels = channelTaskMap.get(false); // SP7
            // Applying the channel state to sync repo files between Server and the minion
            switchChannels(minion, unsubscribedChannels, subscribedChannels, true);
        });
    }

    /**
     * Apply the delayed channel switch for SLES 16 migration.
     * @param serverAction the server action
     */
    private void applyDelayedChannelSwitch(ServerAction serverAction) {
        serverAction.getServer().asMinionServer().ifPresent(minion -> {
            DistUpgradeActionDetails details = getDetails(minion.getId());
            if (details == null || details.isDryRun()) {
                return;
            }
            LOG.info("Applying delayed channel switch for SLES 16 migration on server: {}", minion.getId());
                Map<Boolean, List<Channel>> channelTaskMap = details
                    .getChannelTasks()
                    .stream()
                    .collect(Collectors.partitioningBy(
                        ct -> ct.getTask() == DistUpgradeChannelTask.SUBSCRIBE,
                        Collectors.mapping(DistUpgradeChannelTask::getChannel, Collectors.toList())
                    ));

                List<Channel> subscribedChannels = channelTaskMap.get(true);
                List<Channel> unsubscribedChannels = channelTaskMap.get(false);

                switchChannels(minion, subscribedChannels, unsubscribedChannels, true);
                LOG.info("Delayed channel switch applied and channel state scheduled for server: {}", minion.getId());
        });
    }

    /**
     * Helper to swap channels, regenerate pillar, save the minion, and optionally trigger states.
     *
     * @param minion The minion server
     * @param channelsToAdd The channels to add
     * @param channelsToRemove The channels to remove
     * @param scheduleStateApply If true, publishes Events to run the "channels" salt state immediately
     */
    private void switchChannels(MinionServer minion, List<Channel> channelsToAdd, List<Channel> channelsToRemove,
                                boolean scheduleStateApply) {
        Set<Channel> currentChannels = minion.getChannels();
        channelsToRemove.forEach(currentChannels::remove);
        currentChannels.addAll(channelsToAdd);
        MinionPillarManager.INSTANCE.generatePillar(minion);
        ServerFactory.save(minion);
        if (scheduleStateApply) {
            MessageQueue.publish(new ChannelsChangedEventMessage(minion.getId()));
            MessageQueue.publish(new ApplyStatesEventMessage(minion.getId(), false, ApplyStatesEventMessage.CHANNELS));
        }
    }

    /**
     * Generate SLES 16 target channel repository tokens and return them as a list of maps for Pillar data.
     */
    private List<Map<String, String>> generateSles16Tokens(MinionServer minion, List<Channel> subscribedChannels) {
        List<Map<String, String>> tokens = new ArrayList<>();
        subscribedChannels.forEach(channel -> {
            try {
                Token token = new DownloadTokenBuilder(minion.getOrg().getId())
                        .usingServerSecret()
                        .allowingOnlyChannels(Collections.singleton(channel.getLabel()))
                        .build();
                Map<String, String> entry = new HashMap<>();
                entry.put("label", channel.getLabel());
                entry.put("name", channel.getName());
                entry.put("token", token.getSerializedForm());
                tokens.add(entry);
            }
            catch (TokenBuildingException e) {
                LOG.error("Could not generate SLES16 migration token for channel: {}", channel.getLabel(), e);
                throw new RuntimeException(
                        "Failed to generate migration token for channel: " + channel.getLabel(), e);
            }
        });
        return tokens;
    }
    /**
     * Parse migration status from the SLES 16 verification result map.
     * This method traverses the Salt result map to find either 'sles16_migration_success'
     * or 'sles16_migration_failed' and extracts the boolean "result" field
     * @param jsonResult the full state.apply result map from Salt
     * @return true if the migration success state was found and returned a true result;
     * false if failed, malformed, or no outcome state was found.
     */
    private boolean parseVerificationStatus(JsonElement jsonResult) {
        if (jsonResult == null || !jsonResult.isJsonObject()) {
            LOG.error("SLES 16: Verification result is null or not a JSON object");
            return false;
        }
        return jsonResult.getAsJsonObject().entrySet().stream()
            .filter(e -> e.getKey().contains(SLES16_SUCCESS_STATE) ||
                         e.getKey().contains(SLES16_FAILED_STATE))
            .map(Map.Entry::getValue)
            .filter(JsonElement::isJsonObject)
            .map(JsonElement::getAsJsonObject)
            .map(stateObj -> stateObj.get("result"))
            .filter(Objects::nonNull)
            .filter(JsonElement::isJsonPrimitive)
            .map(JsonElement::getAsBoolean)
            .findFirst()
            .orElseGet(() -> {
                LOG.warn("SLES 16: No primary outcome state found in verification result");
                return false;
            });
    }
    /**
     * Extract the human-readable result message from the SLES 16 verification result map.
     * This method looks for the 'comment' field within the primary migration outcome states.
     * It should handle the multi-line, YAML-encoded strings generated by the Salt SLS.
     *
     * @param result the full state.apply result map from Salt
     * @return the trimmed 'comment' string if found; a fallback error/status message otherwise.
     */
    private String extractVerificationMessage(JsonElement result) {
        if (result == null || !result.isJsonObject()) {
            return "Unable to extract result message (invalid result format)";
        }

        try {
            return result.getAsJsonObject().entrySet().stream()
            // Filter to only match primary outcome states
            .filter(entry -> entry.getKey().contains(SLES16_SUCCESS_STATE) ||
                             entry.getKey().contains(SLES16_FAILED_STATE))
            .map(Map.Entry::getValue)
            .filter(JsonElement::isJsonObject)
            .map(JsonElement::getAsJsonObject)
            // Access the nested 'comment' field within the State ID object
            .map(stateObj -> stateObj.get("comment"))
            .filter(Objects::nonNull)
            .filter(JsonElement::isJsonPrimitive)
            .map(JsonElement::getAsString)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .findFirst()
            .orElse("Migration verification finished: No migration state was executed");

        }
        catch (Exception e) {
            LOG.error("SLES 16 Migration: Failed to extract result message from nested Salt response", e);
            return "Unable to extract result message (parsing error)";
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

        DistUpgradeActionDetails details = getDetails(server.getId());
        boolean typeDistUpgradeDryRun = details != null && details.isDryRun();
        request.setAttribute("typeDistUpgradeDryRun", typeDistUpgradeDryRun);

        return typeDistUpgradeDryRun;
    }
}
