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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.kickstart;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.Action;


import java.util.Date;

import javax.persistence.CascadeType;
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
 * KickstartSessionHistory - Class representation of the table rhnkickstartsessionhistory.
 */
@Entity
@Table(name = "rhnKickstartSessionHistory")
public class KickstartSessionHistory extends BaseDomainHelper {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RHN_KS_SESSIONHIST_ID_SEQ")
    @SequenceGenerator(name = "RHN_KS_SESSIONHIST_ID_SEQ", sequenceName = "RHN_KS_SESSIONHIST_ID_SEQ",
            allocationSize = 1)
    private Long id;

    @Column
    private String message;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinColumn(name = "kickstart_session_id")
    private KickstartSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    private KickstartSessionState state;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id")
    private Action action;

    @Column(nullable = false, updatable = false, insertable = false)
    private Date time;

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
     * Getter for session
     * @return KickstartSession to get
    */
    public KickstartSession getSession() {
        return this.session;
    }

    /**
     * Setter for session
     * @param sessionIn to set
    */
    public void setSession(KickstartSession sessionIn) {
        this.session = sessionIn;
    }

    /**
     * Getter for action
     * @return Action to get
    */
    public Action getAction() {
        return this.action;
    }

    /**
     * Setter for action
     * @param actionIn to set
    */
    public void setAction(Action actionIn) {
        this.action = actionIn;
    }

    /**
     * Getter for state
     * @return KickstartSessionState to get
    */
    public KickstartSessionState getState() {
        return this.state;
    }

    /**
     * Setter for state
     * @param stateIn to set
    */
    public void setState(KickstartSessionState stateIn) {
        this.state = stateIn;
    }

    /**
     * Getter for time
     * @return Date to get
    */
    public Date getTime() {
        return this.time;
    }

    /**
     * Setter for time
     * @param timeIn to set
    */
    public void setTime(Date timeIn) {
        this.time = timeIn;
    }


    /**
     * Getter for message
     * @return String to get
    */
    public String getMessage() {
        return this.message;
    }

    /**
     * Setter for message
     * @param messageIn to set
    */
    public void setMessage(String messageIn) {
        this.message = messageIn;
    }

}
