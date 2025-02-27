/*
 * Copyright (c) 2025 Red Hat, Inc.
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
package com.redhat.rhn.domain.server;

import com.redhat.rhn.domain.BaseDomainHelper;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

/**
 * Server - Class representation of the table rhnServer.
 */
@Entity
@Table(name = "rhnServerGroupMembers")
public class ServerGroupMember extends BaseDomainHelper {

    @Embeddable
    public static class ServerGroupMemberId implements Serializable {

        private Long serverId;
        private Long serverGroupId;

        public Long getServerId() {
            return serverId;
        }

        public void setServerId(Long serverIdIn) {
            serverId = serverIdIn;
        }

        public Long getServerGroupId() {
            return serverGroupId;
        }

        public void setServerGroupId(Long serverGroupIdIn) {
            serverGroupId = serverGroupIdIn;
        }


        @Override
        public boolean equals(Object oIn) {
            if (!(oIn instanceof ServerGroupMemberId that)) {
                return false;
            }
            return Objects.equals(serverId, that.serverId) && Objects.equals(serverGroupId, that.serverGroupId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(serverId, serverGroupId);
        }
    }

    @EmbeddedId
    private ServerGroupMemberId id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @MapsId("serverId")
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @MapsId("serverGroupId")
    @JoinColumn(name = "server_group_id", nullable = false)
    private ServerGroup serverGroup;


    public ServerGroup getServerGroup() {
        return serverGroup;
    }

    public void setServerGroup(ServerGroup serverGroupIn) {
        serverGroup = serverGroupIn;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server serverIn) {
        server = serverIn;
    }

    public ServerGroupMemberId getId() {
        return id;
    }

    public void setId(ServerGroupMemberId idIn) {
        id = idIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof ServerGroupMember that)) {
            return false;
        }
        return Objects.equals(id, that.id) && Objects.equals(server, that.server) &&
                Objects.equals(serverGroup, that.serverGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, server, serverGroup);
    }
}
