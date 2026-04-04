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

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * ServerInfo - Class representation of the table rhnServerInfo
 */
@Entity
@Table(name = "rhnServerInfo")
public class ServerInfo implements Serializable {

    @Id
    @Column(name = "server_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id")
    @MapsId
    private Server server;

    @Column
    private Date checkin;

    @Column(name = "checkin_counter")
    private Long checkinCounter;

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
    /**
     * @param serverIn The server to set.
     */
    public void setServer(Server serverIn) {
        this.server = serverIn;
    }
}
