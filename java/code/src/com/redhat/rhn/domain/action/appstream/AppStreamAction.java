/*
 * Copyright (c) 2024 SUSE LLC
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
package com.redhat.rhn.domain.action.appstream;

import static java.util.Collections.singletonList;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.server.MinionSummary;

import com.suse.manager.webui.services.SaltParameters;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AppStreamAction extends Action {
    private static final long serialVersionUID = 1L;
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
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @param action action which has all the revisions
     * @return minion summaries grouped by local call
     */
    public static Map<LocalCall<?>, List<MinionSummary>> appStreamAction(
            List<MinionSummary> minionSummaries, AppStreamAction action) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();

        Map<Boolean, Set<AppStreamActionDetails>> details = action.getDetails().stream()
                .collect(Collectors.partitioningBy(AppStreamActionDetails::isEnable, Collectors.toSet()));

        var enableParams = details.get(true).stream()
                .map(d -> d.getStream() == null ?
                        singletonList(d.getModuleName()) :
                        Arrays.asList(d.getModuleName(), d.getStream()))
                .toList();
        var disableParams = details.get(false).stream().map(AppStreamActionDetails::getModuleName).toList();

        Optional<Map<String, Object>> params = Optional.of(Map.of(
                SaltParameters.PARAM_APPSTREAMS_ENABLE, enableParams,
                SaltParameters.PARAM_APPSTREAMS_DISABLE, disableParams
        ));
        ret.put(State.apply(List.of(SaltParameters.APPSTREAMS_CONFIGURE), params), minionSummaries);
        return ret;
    }

}
