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

package com.suse.manager.webui.utils.gson;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * DTO for identifying ansible playbook execution
 */
public class AnsiblePlaybookExecutionJson {

    private String playbookPath;
    private Optional<String> inventoryPath = Optional.empty();
    private long controlNodeId;
    private boolean testMode;
    private boolean flushCache;
    private Optional<LocalDateTime> earliest = Optional.empty();
    private Optional<String> actionChainLabel = Optional.empty();

    /**
     * Gets the playbookPath.
     *
     * @return playbookPath
     */
    public String getPlaybookPath() {
        return playbookPath;
    }

    /**
     * Gets the inventoryPath.
     *
     * @return inventoryPath
     */
    public Optional<String> getInventoryPath() {
        return inventoryPath;
    }

    /**
     * Gets the controlNodeId.
     *
     * @return controlNodeId
     */
    public long getControlNodeId() {
        return controlNodeId;
    }

    /**
     * True if the execution should be in test mode.
     *
     * @return testMode
     */
    public boolean isTestMode() {
        return testMode;
    }

    /**
     * True if Ansible's --flush-cache flag is to be set
     *
     * @return the --flush-cache flag
     */
    public boolean isFlushCache() {
        return flushCache;
    }

    /**
     * Gets the earliest.
     *
     * @return earliest
     */
    public Optional<LocalDateTime> getEarliest() {
        return earliest;
    }

    /**
     * Gets the actionChainLabel.
     *
     * @return actionChainLabel
     */
    public Optional<String> getActionChainLabel() {
        return actionChainLabel;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("playbookPath", playbookPath)
                .append("inventoryPath", inventoryPath)
                .append("controlNodeId", controlNodeId)
                .append("earliest", earliest)
                .append("actionChainLabel", actionChainLabel)
                .toString();
    }
}
