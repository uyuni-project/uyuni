/*
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.domain.action.salt;

import com.redhat.rhn.domain.action.ActionFormatter;

import java.util.stream.Collectors;

/**
 * ApplyStatesActionFormatter - Class that extends from ActionFormatter to display
 * information specific to ApplyStatesAction objects.
 */
public class ApplyStatesActionFormatter extends ActionFormatter {

    private final ApplyStatesActionDetails actionDetails;

    /**
     * Create a new ApplyStatesActionFormatter.
     * @param actionIn the ApplyStatesAction to be formatted
     */
    public ApplyStatesActionFormatter(ApplyStatesAction actionIn) {
        super(actionIn);
        actionDetails = actionIn.getDetails();
    }

    @Override
    public String getActionType() {
        String states = actionDetails.getMods().isEmpty() ? "highstate" :
                actionDetails.getMods().stream().collect(Collectors.joining(", "));
        String ret = "Apply states (" + states + ")";
        if (actionDetails.isTest()) {
            ret += " in test-mode";
        }
        return ret;
    }
}
