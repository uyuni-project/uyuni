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
package com.suse.manager.virtualization;

import java.util.Map;

/**
 * Represents the result from a Salt virt.vm_info call.
 */
public class VmInfoJson {
    /** Maps the disk target type to its informations */
    private Map<String, VmInfoDiskJson> disks;

    /**
     * @return the disks informations mapped to their target name (hda, vdb...)
     */
    public Map<String, VmInfoDiskJson> getDisks() {
        return disks;
    }

    /**
     * @param disksIn the disks informations mapped to their target name (hda, vdb...)
     */
    public void setDisks(Map<String, VmInfoDiskJson> disksIn) {
        disks = disksIn;
    }
}

