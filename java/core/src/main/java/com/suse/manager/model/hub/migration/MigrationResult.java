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

package com.suse.manager.model.hub.migration;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MigrationResult {

    private MigrationResultCode resultCode;

    private final Set<MigrationMessage> messageSet;

    /**
     * Default constructor
     */
    public MigrationResult() {
        this.resultCode = MigrationResultCode.SUCCESS;
        this.messageSet = new HashSet<>();
    }

    public synchronized MigrationResultCode getResultCode() {
        return resultCode;
    }

    public synchronized void setResultCode(MigrationResultCode resultCodeIn) {
        this.resultCode = resultCodeIn;
    }

    public Set<MigrationMessage> getMessages() {
        return Collections.unmodifiableSet(messageSet);
    }

    /**
     * Add a message to the result messages
     * @param severity the severity level
     * @param message the message
     */
    public synchronized void addMessage(MigrationMessageLevel severity, String message) {
        messageSet.add(new MigrationMessage(severity, message));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof MigrationResult that)) {
            return false;
        }

        return new EqualsBuilder()
            .append(resultCode, that.resultCode)
            .append(messageSet, that.messageSet)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(resultCode)
            .append(messageSet)
            .toHashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MigrationResult{");
        sb.append("success=").append(resultCode);
        sb.append(", messageList=").append(messageSet);
        sb.append('}');
        return sb.toString();
    }
}
