/*
 * Copyright (c) 2025 SUSE LLC
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

import com.redhat.rhn.domain.BaseDomainHelper;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * PushClient - Class representation of the table rhnpushclient.
 */
@Entity
@Table(name = "rhnPushClient")
public class PushClient extends BaseDomainHelper {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pclient_seq")
    @SequenceGenerator(name = "pclient_seq", sequenceName = "rhn_pclient_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id")
    private Server server;

    @Column(name = "jabber_id")
    private String jabberId;

    @Column(name = "shared_key", nullable = false)
    private String sharedKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    private PushClientState state;

    @Column(name = "next_action_time")
    private Date nextActionTime;

    @Column(name = "last_message_time")
    private Date lastMessageTime;

    @Column(name = "last_ping_time")
    private Date lastPingTime;

    /**
     * Getter for id
     * @return Long to get
    */
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for id
     * @param idIn to set
    */
    protected void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Getter for name
     * @return String to get
    */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for name
     * @param nameIn to set
    */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * Getter for server
     * @return Server to get
    */
    public Server getServer() {
        return this.server;
    }

    /**
     * Setter for server
     * @param serverIn to set
    */
    public void setServer(Server serverIn) {
        this.server = serverIn;
    }

    /**
     * Getter for jabberId
     * @return String to get
    */
    public String getJabberId() {
        return this.jabberId;
    }

    /**
     * Setter for jabberId
     * @param jabberIdIn to set
    */
    public void setJabberId(String jabberIdIn) {
        this.jabberId = jabberIdIn;
    }

    /**
     * Getter for sharedKey
     * @return String to get
    */
    public String getSharedKey() {
        return this.sharedKey;
    }

    /**
     * Setter for sharedKey
     * @param sharedKeyIn to set
    */
    public void setSharedKey(String sharedKeyIn) {
        this.sharedKey = sharedKeyIn;
    }

    /**
     * Getter for state
     * @return PushClientState to get
    */
    public PushClientState getState() {
        return this.state;
    }

    /**
     * Setter for state
     * @param stateIn to set
    */
    public void setState(PushClientState stateIn) {
        this.state = stateIn;
    }

    /**
     * Getter for nextActionTime
     * @return Date to get
    */
    public Date getNextActionTime() {
        return this.nextActionTime;
    }

    /**
     * Setter for nextActionTime
     * @param nextActionTimeIn to set
    */
    public void setNextActionTime(Date nextActionTimeIn) {
        this.nextActionTime = nextActionTimeIn;
    }

    /**
     * Getter for lastMessageTime
     * @return Date to get
    */
    public Date getLastMessageTime() {
        return this.lastMessageTime;
    }

    /**
     * Setter for lastMessageTime
     * @param lastMessageTimeIn to set
    */
    public void setLastMessageTime(Date lastMessageTimeIn) {
        this.lastMessageTime = lastMessageTimeIn;
    }

    /**
     * Getter for lastPingTime
     * @return Date to get
    */
    public Date getLastPingTime() {
        return this.lastPingTime;
    }

    /**
     * Setter for lastPingTime
     * @param lastPingTimeIn to set
    */
    public void setLastPingTime(Date lastPingTimeIn) {
        this.lastPingTime = lastPingTimeIn;
    }

}
