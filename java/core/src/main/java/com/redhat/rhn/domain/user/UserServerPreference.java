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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.user;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.legacy.UserImpl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * UserServerPreference - Class representation of the table rhnUserServerprefs.
 */
@Entity
@Table(name = "rhnUserServerprefs")
@IdClass(UserServerPreferenceId.class)
public class UserServerPreference extends BaseDomainHelper {
    @Id
    @ManyToOne(targetEntity = UserImpl.class)
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "server_id")
    private Server server;

    @Id
    @Column
    private String name;

    @Column
    private String value;

    /**
     * Constructor
     */
    public UserServerPreference() { }

    /**
     * Create a new UserServerPreference
     * @param userIn user corresponding to the preference
     * @param serverIn server corresponding to the preference
     * @param nameIn property name corresponding to the preference
     */
    public UserServerPreference(User userIn, Server serverIn, String nameIn) {
        this.user = userIn;
        this.server = serverIn;
        this.name = nameIn;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @param nameIn The name to set.
     */
    public void setName(String nameIn) {
        this.name = nameIn;
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
    /**
     * @return Returns the user.
     */
    public User getUser() {
        return user;
    }
    /**
     * @param userIn The user to set.
     */
    public void setUser(User userIn) {
        this.user = userIn;
    }

    /**
     * Getter for value
     * @return String to get
    */
    public String getValue() {
        return this.value;
    }

    /**
     * Setter for value
     * @param valueIn to set
    */
    public void setValue(String valueIn) {
        this.value = valueIn;
    }
}
