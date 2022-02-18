/*
 * Copyright (c) 2017 SUSE LLC
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
package com.suse.manager.webui.websocket.json;

import java.util.List;

/**
 * Async job started DTO.
 */
public class AsyncJobStartEventDto extends AbstractSaltEventDto {

    private List<String> minions;
    private boolean waitForSSHMinions;

    /**
     * No arg constructor.
     */
    public AsyncJobStartEventDto() {
        super("asyncJobStart");
    }

    /**
     * @param actionTypeIn the action type
     * @param minionsIn the minions for which the job was started
     * @param waitForSSHIn true if there will be results from salt-ssh minions
     */
    public AsyncJobStartEventDto(String actionTypeIn, List<String> minionsIn,
                                 boolean waitForSSHIn) {
        super("asyncJobStart", null, actionTypeIn);
        this.minions = minionsIn;
        this.waitForSSHMinions = waitForSSHIn;
    }

    /**
     * @return the minions for which the job was started
     */
    public List<String> getMinions() {
        return minions;
    }

    /**
     * @param minionsIn the minions for which the job was started
     */
    public void setMinions(List<String> minionsIn) {
        this.minions = minionsIn;
    }

    /**
     * @return true there will be results from salt-ssh minions
     */
    public boolean isWaitForSSHMinions() {
        return waitForSSHMinions;
    }
}
