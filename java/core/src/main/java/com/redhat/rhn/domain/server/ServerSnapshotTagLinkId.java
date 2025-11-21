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

package com.redhat.rhn.domain.server;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;

public class ServerSnapshotTagLinkId implements Serializable {

    @Serial
    private static final long serialVersionUID = -6746060398892270289L;

    private Server server;

    private ServerSnapshot snapshot;

    private SnapshotTag tag;

    /**
     * Constructor
     */
    public ServerSnapshotTagLinkId() {
    }

    /**
     * Constructor
     *
     * @param serverIn   the input server
     * @param snapshotIn the input snapshot
     * @param tagIn      the input tag
     */
    public ServerSnapshotTagLinkId(Server serverIn, ServerSnapshot snapshotIn, SnapshotTag tagIn) {
        server = serverIn;
        snapshot = snapshotIn;
        tag = tagIn;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server serverIn) {
        server = serverIn;
    }

    public ServerSnapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(ServerSnapshot snapshotIn) {
        snapshot = snapshotIn;
    }

    public SnapshotTag getTag() {
        return tag;
    }

    public void setTag(SnapshotTag tagIn) {
        tag = tagIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof ServerSnapshotTagLinkId that)) {
            return false;
        }

        return new EqualsBuilder()
                .append(server, that.server)
                .append(snapshot, that.snapshot)
                .append(tag, that.tag)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(server)
                .append(snapshot)
                .append(tag)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "ServerSnapshotTagLinkId{" +
                "server=" + server +
                ", snapshot=" + snapshot +
                ", tag=" + tag +
                '}';
    }
}
