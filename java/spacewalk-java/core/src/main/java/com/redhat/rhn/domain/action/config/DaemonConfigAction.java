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
package com.redhat.rhn.domain.action.config;

import com.redhat.rhn.domain.action.Action;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;

/**
 * DaemonConfigAction - Class representing ActionType.TYPE_DAEMON_CONFIG: 32
 */
@Entity
@DiscriminatorValue("32")
public class DaemonConfigAction extends Action {

    @OneToOne(mappedBy = "parentAction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private DaemonConfigDetails daemonConfigDetails;

    /**
     * @return Returns the daemonConfigDetails.
     */
    public DaemonConfigDetails getDaemonConfigDetails() {
        return daemonConfigDetails;
    }
    /**
     * @param daemonConfigDetailsIn The configRevisionActions to set.
     */
    public void setDaemonConfigDetails(DaemonConfigDetails daemonConfigDetailsIn) {
        this.daemonConfigDetails = daemonConfigDetailsIn;
    }

}
