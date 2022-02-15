/*
 * Copyright (c) 2014 SUSE LLC
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
package com.redhat.rhn.frontend.dto;

import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.ServerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * DTO for bare-metal systems.
 */
public class BootstrapSystemOverview extends SystemOverview {

    /** The CPU count. */
    private Long cpuCount;

    /** The CPU clock frequency in MHz. */
    private String cpuMhz;

    /** The disk count. */
    private Long diskCount;

    /** The RAM size in MB. */
    private Long ram;

    /**
     * Gets the CPU count.
     * @return the CPU count
     */
    public Long getCpuCount() {
        return cpuCount;
    }

    /**
     * Sets the CPU count.
     * @param cpuCountIn the new CPU count
     */
    public void setCpuCount(Long cpuCountIn) {
        cpuCount = cpuCountIn;
    }

    /**
     * Gets the CPU clock frequency in GHz.
     * @return the CPU clock frequency
     */
    public Integer getCpuClockFrequency() {
        try {
            return Integer.parseInt(cpuMhz) / 1000;
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Sets the CPU frequency in MHz.
     * @param cpuMhzIn the new CPU frequency
     */
    public void setCpuMhz(String cpuMhzIn) {
        cpuMhz = cpuMhzIn;
    }

    /**
     * Gets the disk count.
     * @return the disk count
     */
    public Long getDiskCount() {
        return diskCount;
    }

    /**
     * Sets the disk count.
     * @param diskCountIn the new disk count
     */
    public void setDiskCount(Long diskCountIn) {
        diskCount = diskCountIn;
    }

    /**
     * Gets the RAM size.
     * @return the RAM size
     */
    public Long getRam() {
        return ram;
    }

    /**
     * Sets the RAM size.
     *
     * @param ramIn the new RAM size
     */
    public void setRam(Long ramIn) {
        ram = ramIn;
    }

    /**
     * Gets the network interfaces' hardware addresses.
     * @return the MAC addresses
     */
    public List<String> getMacs() {
        List<String> result = new LinkedList<>();
        for (NetworkInterface networkInterface : ServerFactory.lookupById(getId())
                .getNetworkInterfaces()) {
            if (networkInterface.isMacValid()) {
                result.add(networkInterface.getHwaddr());
            }
        }
        return result;
    }
}
