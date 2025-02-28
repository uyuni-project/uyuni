/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * ServerInfo - Class representation of the table rhnServerInfo
 */
@Entity
@Table(name = "rhnServerInfo")
public class ServerInfo implements Serializable {

    @Id
    @Column(name = "server_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    @MapsId
    @JoinColumn(name = "server_id", referencedColumnName = "id", insertable = true, updatable = true)
    private Server server;

    @Column(name = "checkin")
    @CreationTimestamp
    private Date checkin  = new Date();

    @Column(name = "checkin_counter")
    private Long checkinCounter = 0L;

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }
    /**
     * @param idIn The id to set.
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }
    /**
     * @return Returns the checkin.
     */
    public Date getCheckin() {
        return checkin;
    }
    /**
     * @param checkinIn The checkin to set.
     */
    public void setCheckin(Date checkinIn) {
        this.checkin = checkinIn;
    }
    /**
     * @return Returns the checkinCounter.
     */
    public Long getCheckinCounter() {
        return checkinCounter;
    }
    /**
     * @param checkinCounterIn The checkinCounter to set.
     */
    public void setCheckinCounter(Long checkinCounterIn) {
        this.checkinCounter = checkinCounterIn;
    }
    /**
     * @return Returns the server.
     */
    public Server getServer() {
        return server;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof ServerInfo that)) {
            return false;
        }
        return Objects.equals(id, that.id) && Objects.equals(server, that.server) &&
                Objects.equals(checkin, that.checkin) && Objects.equals(checkinCounter, that.checkinCounter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, server, checkin, checkinCounter);
    }

    /**
     * @param serverIn The server to set.
     */
    public void setServer(Server serverIn) {
        this.server = serverIn;
    }
}
