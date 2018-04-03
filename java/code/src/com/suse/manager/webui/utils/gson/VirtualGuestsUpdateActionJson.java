/**
 * Copyright (c) 2018 SUSE LLC
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
package com.suse.manager.webui.utils.gson;

/**
 * VirtualGuestsUpdate represents the JSON data for the virtual guests
 * update action.
 */
public class VirtualGuestsUpdateActionJson extends VirtualGuestsBaseActionJson {
    private Long vcpu;
    private Long memory;

    /**
     * @return number of VCPUs to set
     */
    public Long getVcpu() {
        return vcpu;
    }

    /**
     * @param vcpuIn number of VCPUs to set
     */
    public void setVcpu(Long vcpuIn) {
        vcpu = vcpuIn;
    }

    /**
     * @return amount of memory in KiB to set
     */
    public Long getMemory() {
        return memory;
    }

    /**
     * @param memoryIn amount of memory in MB to set
     */
    public void setMemory(Long memoryIn) {
        memory = memoryIn;
    }
}
