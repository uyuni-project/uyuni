/*
 * Copyright (c) 2021 SUSE LLC
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
package com.redhat.rhn.domain.action.ansible;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.services.SaltParameters;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

/**
 * PlaybookAction - Action class representing the execution of an Ansible playbook
 */
public class PlaybookAction extends Action {
    private static final String INVENTORY_PATH = "/etc/ansible/hosts";

    private PlaybookActionDetails details;

    /**
     * Return the details.
     * @return details
     */
    public PlaybookActionDetails getDetails() {
        return details;
    }

    /**
     * Set the details.
     * @param detailsIn details
     */
    public void setDetails(PlaybookActionDetails detailsIn) {
        if (detailsIn != null) {
            detailsIn.setParentAction(this);
        }
        this.details = detailsIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }
        if (!(oIn instanceof PlaybookAction that)) {
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
        return singletonMap(executePlaybookActionCall(), minionSummaries);
    }

    private LocalCall<?> executePlaybookActionCall() {
        String playbookPath = details.getPlaybookPath();
        String rundir = new File(playbookPath).getAbsoluteFile().getParent();
        String inventoryPath = details.getInventoryPath();

        if (StringUtils.isEmpty(inventoryPath)) {
            inventoryPath = INVENTORY_PATH;
        }

        Map<String, Object> pillarData = new HashMap<>();
        pillarData.put("playbook_path", playbookPath);
        pillarData.put("inventory_path", inventoryPath);
        pillarData.put("rundir", rundir);
        pillarData.put("flush_cache", details.isFlushCache());
        pillarData.put("extra_vars", details.getExtraVarsContents());
        return State.apply(singletonList(SaltParameters.ANSIBLE_RUNPLAYBOOK),
                Optional.of(pillarData), Optional.of(true),
                Optional.of(details.isTestMode()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRequestAttributePlaybook(HttpServletRequest request, ServerAction serverAction, User user) {
        request.setAttribute("typePlaybook", true);
        String inventory = new PlaybookActionFormatter(this).getTargetedSystems(serverAction, user);
        request.setAttribute("inventory", inventory);
    }
}
