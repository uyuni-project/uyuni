/*
 * Copyright (c) 2024--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.action.appstream;

import static java.util.Collections.singletonList;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.ServerAppStream;

import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.SaltParameters;
import com.suse.manager.webui.utils.salt.custom.AppStreamsChangeSlsResult;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.utils.Json;

import com.google.gson.JsonElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

@Entity
@DiscriminatorValue("524")
public class AppStreamAction extends Action {
    @Serial
    private static final long serialVersionUID = 1L;

    @OneToMany(mappedBy = "parentAction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<AppStreamActionDetails> details = new HashSet<>();

    public Set<AppStreamActionDetails> getDetails() {
        return details;
    }

    /**
     * Sets the details for this AppStreamAction.
     *
     * @param detailsIn the Set of AppStreamActionDetails to be set
     */
    public void setDetails(Set<AppStreamActionDetails> detailsIn) {
        if (detailsIn != null) {
            details = new HashSet<>(detailsIn);
            details.forEach(d -> d.setParentAction(this));
        }
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }
        if (!(oIn instanceof AppStreamAction that)) {
            return false;
        }
        return new EqualsBuilder().appendSuper(super.equals(oIn)).append(details, that.details).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(details)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();

        Map<Boolean, Set<AppStreamActionDetails>> det = getDetails().stream()
                .collect(Collectors.partitioningBy(AppStreamActionDetails::isEnable, Collectors.toSet()));

        var enableParams = det.get(true).stream()
                .map(d -> d.getStream() == null ?
                        singletonList(d.getModuleName()) :
                        Arrays.asList(d.getModuleName(), d.getStream()))
                .toList();
        var disableParams = det.get(false).stream().map(AppStreamActionDetails::getModuleName).toList();

        Optional<Map<String, Object>> params = Optional.of(Map.of(
                SaltParameters.PARAM_APPSTREAMS_ENABLE, enableParams,
                SaltParameters.PARAM_APPSTREAMS_DISABLE, disableParams
        ));
        ret.put(State.apply(List.of(SaltParameters.APPSTREAMS_CONFIGURE), params), minionSummaries);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleUpdateServerAction(ServerAction serverAction, JsonElement jsonResult, UpdateAuxArgs auxArgs) {
        Optional<MinionServer> server = serverAction.getServer().asMinionServer();
        if (server.isEmpty()) {
            return;
        }

        if (serverAction.isStatusFailed()) {
            // Filter out the subsequent errors to find the root cause
            var originalErrorMsg = SaltUtils.jsonEventToStateApplyResults(jsonResult)
                    .map(AppStreamAction::getOriginalStateApplyError)
                    .orElseThrow(() -> new RuntimeException("Failed to parse the state.apply error result"))
                    .map(StateApplyResult::getComment)
                    .map(msg -> msg.isEmpty() ? null : msg)
                    .orElse("Error while configuring AppStreams on the system.\nGot no result from the system.");

            serverAction.setResultMsg(originalErrorMsg);
            return;
        }

        var currentlyEnabled = Json.GSON.fromJson(jsonResult, AppStreamsChangeSlsResult.class).getCurrentlyEnabled();
        Set<ServerAppStream> enabledModules = currentlyEnabled.stream()
                .map(nsvca -> new ServerAppStream(server.get(), nsvca))
                .collect(Collectors.toSet());
        server.get().getAppStreams().clear();
        server.get().getAppStreams().addAll(enabledModules);
        serverAction.setResultMsg("Successfully changed system AppStreams.");
    }

    /**
     * Returns the root cause of a failed state.apply result by filtering out the subsequent failures.
     * @param stateApplyResultMap the map of the state apply results
     * @return the first failed state.apply result
     * @param <R> the type of the state.apply result
     */
    private static <R> Optional<StateApplyResult<R>> getOriginalStateApplyError(
            Map<String, StateApplyResult<R>> stateApplyResultMap) {
        return stateApplyResultMap.values().stream()
                .filter(r -> !r.getComment().startsWith("One or more requisite failed"))
                .findFirst();
    }

}
