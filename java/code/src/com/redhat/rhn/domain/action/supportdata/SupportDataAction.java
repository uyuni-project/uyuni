/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.action.supportdata;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.server.MinionSummary;

import com.suse.manager.webui.utils.MinionActionUtils;
import com.suse.manager.webui.utils.salt.LocalCallWithExecutors;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.modules.Test;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SupportDataAction - Class representing TYPE_SUPPORTDATA_GET
 */
public class SupportDataAction extends Action {

    private SupportDataActionDetails details;

    public SupportDataActionDetails getDetails() {
        return details;
    }

    /**
     * Sets the details for this SupportDataAction.
     *
     * @param detailsIn the Set of SupportDataActionDetails to be set
     */
    public void setDetails(SupportDataActionDetails detailsIn) {
        details = detailsIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }
        if (!(oIn instanceof SupportDataAction that)) {
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

    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();

        var partitioned = minionSummaries.stream().collect(Collectors.partitioningBy(minionSummary -> {
            var actionPath = MinionActionUtils.getFullActionPath(getOrg().getId(), minionSummary.getServerId(),
                    getId());
            var bundle = actionPath.resolve("bundle.tar");
            return Files.exists(bundle);
        }));

        var pillar = Optional.ofNullable(getDetails().getParameter())
                .map(p -> Map.of("arguments", (Object)p));
        var full = partitioned.get(false);
        var onlyUpload = partitioned.get(true);
        if (!full.isEmpty()) {
            // supportdata should be taken always in direct mode - also on transactional systems
            var apply = State.apply(List.of("supportdata"), pillar);
            ret.put(new LocalCallWithExecutors<>(apply, List.of("direct_call"), Collections.emptyMap()), full);
        }
        if (!onlyUpload.isEmpty()) {
            ret.put(Test.echo("supportdata"), onlyUpload);
        }
        return ret;
    }
}
