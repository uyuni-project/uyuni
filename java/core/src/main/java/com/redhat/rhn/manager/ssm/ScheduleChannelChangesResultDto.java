/*
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

package com.redhat.rhn.manager.ssm;

import java.util.Optional;

/**
 * Object holding the results of scheduling SSM channel changes.
 */
public class ScheduleChannelChangesResultDto {

    private SsmServerDto server;
    private final Optional<Long> actionId;
    private final Optional<String> errorMessage;

    /**
     * Constructor.
     * @param serverIn server
     * @param actionIdIn action id
     */
    public ScheduleChannelChangesResultDto(SsmServerDto serverIn, long actionIdIn) {
        this.server = serverIn;
        this.actionId = Optional.of(actionIdIn);
        this.errorMessage = Optional.empty();
    }

    /**
     * Constructor.
     * @param serverIn server
     * @param errMessage error message
     */
    public ScheduleChannelChangesResultDto(SsmServerDto serverIn, String errMessage) {
        this.server = serverIn;
        this.errorMessage = Optional.of(errMessage);
        this.actionId = Optional.empty();
    }

    /**
     * @return serverId to get
     */
    public SsmServerDto getServer() {
        return server;
    }

    /**
     * @return actionId to get
     */
    public Optional<Long> getActionId() {
        return actionId;
    }

    /**
     * @return errorMessage to get
     */
    public Optional<String> getErrorMessage() {
        return errorMessage;
    }
}
