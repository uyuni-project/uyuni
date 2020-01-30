/**
 * Copyright (c) 2019 SUSE LLC
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
package com.suse.manager.webui.utils.salt;

import com.google.gson.annotations.SerializedName;

import java.util.Optional;

/**
 * Class to represent grains defined against 'start_event_grains'
 */
public class MinionStartupGrains {
    @SerializedName("machine_id")
    private Optional<String> machineId = Optional.empty();
    @SerializedName("saltboot_initrd")
    private boolean saltbootInitrd = false;

    /**
     * no-arg constructor
     */
    public MinionStartupGrains() {

    }

    /**
     * Constructor which accepts machine_id andd salboot_intrd grain
     * @param machineIdIn machineIdIn
     * @param saltbootInitrdIn saltbootInitrdIn
     */
    public MinionStartupGrains(Optional<String> machineIdIn, boolean saltbootInitrdIn) {
        this.machineId = machineIdIn;
        this.saltbootInitrd = saltbootInitrdIn;
    }

    public Optional<String> getMachineId() {
        return machineId;
    }

    public boolean getSaltbootInitrd() {
        return saltbootInitrd;
    }
}
