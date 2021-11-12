/**
 * Copyright (c) 2018 SUSE LLC
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

package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.manager.ssm.ScheduleChannelChangesResultDto;

import java.util.List;
import java.util.Optional;

/**
 * Object holding the result of scheduling SSM channel changes.
 */
public class SsmScheduleChannelChangesResultJson {

    private Optional<Long> actionChainId;
    private List<ScheduleChannelChangesResultDto> result;

    /**
     * @param actionChainIdIn the optional action channel id
     * @param resultIn results for scheduling each action
     */
    public SsmScheduleChannelChangesResultJson(Optional<Long> actionChainIdIn,
                                               List<ScheduleChannelChangesResultDto> resultIn) {
        this.actionChainId = actionChainIdIn;
        this.result = resultIn;
    }

    /**
     * @return actionChainId to get
     */
    public Optional<Long> getActionChainId() {
        return actionChainId;
    }

    /**
     * @return result to get
     */
    public List<ScheduleChannelChangesResultDto> getResult() {
        return result;
    }
}
