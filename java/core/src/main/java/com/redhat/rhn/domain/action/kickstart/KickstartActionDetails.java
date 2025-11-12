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
package com.redhat.rhn.domain.action.kickstart;


import org.hibernate.annotations.Type;
import org.hibernate.type.YesNoConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * KickstartActionDetails - java representation of the rhnActionKickstart table
 */
@Entity
@Table(name = "rhnActionKickstart")
public class KickstartActionDetails extends BaseKickstartActionDetails {

    @Column(name = "static_device")
    private String staticDevice;

    @Column(nullable = false)
    @Convert(converter = YesNoConverter.class)
    private boolean upgrade = false;


    /**
     * @return Returns the staticDevice.
     */
    public String getStaticDevice() {
        return staticDevice;
    }

    /**
     * @param s The staticDevice to set.
     */
    public void setStaticDevice(String s) {
        this.staticDevice = s;
    }

    /**
     * @return Returns the upgrade.
     */
    public boolean getUpgrade() {
        return upgrade;
    }

    /**
     * @param upgradeIn The upgrade to set.
     */
    public void setUpgrade(boolean upgradeIn) {
        this.upgrade = upgradeIn;
    }
}
