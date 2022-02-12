/*
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.webui.controllers.virtualization.gson;

import com.redhat.rhn.domain.action.virtualization.VirtualizationPoolCreateActionSource;

import com.suse.manager.virtualization.PoolTarget;

/**
*
* represents the pool create action request body structure.
*/
public class VirtualPoolCreateActionJson extends VirtualPoolBaseActionJson {

    private String uuid;
    private String type;
    private boolean autostart;
    private VirtualizationPoolCreateActionSource source;
    private PoolTarget target;


    /**
     * @return Returns the uuid.
     */
    public String getUuid() {
        return uuid;
    }


    /**
     * @param uuidIn The uuid to set.
     */
    public void setUuid(String uuidIn) {
        uuid = uuidIn;
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }

    /**
     * @param typeIn The type to set.
     */
    public void setType(String typeIn) {
        type = typeIn;
    }

    /**
     * @return Returns whether the pool is to be autostarted.
     */
    public boolean isAutostart() {
        return autostart;
    }

    /**
     * @param autostartIn whether the pool is to be autostarted.
     */
    public void setAutostart(boolean autostartIn) {
        autostart = autostartIn;
    }

    /**
     * @return Returns the source.
     */
    public VirtualizationPoolCreateActionSource getSource() {
        return source;
    }

    /**
     * @param sourceIn The source to set.
     */
    public void setSource(VirtualizationPoolCreateActionSource sourceIn) {
        source = sourceIn;
    }

    /**
     * @return Returns the target.
     */
    public PoolTarget getTarget() {
        return target;
    }

    /**
     * @param targetIn The target to set.
     */
    public void setTarget(PoolTarget targetIn) {
        target = targetIn;
    }
}
