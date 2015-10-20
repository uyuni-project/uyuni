/**
 * Copyright (c) 2015 SUSE LLC
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

package com.suse.manager.gatherer;

import java.util.Map;

/**
 * Json representation of a Virtual Host system.
 */
public class JSONHost {

    /** The name. */
    public String name;

    /** The total CPU socket count. */
    public Integer totalCpuSockets;

    /** The total CPU core count. */
    public Integer totalCpuCores;

    /** The total CPU thread count. */
    public Integer totalCpuThreads;

    /** The CPU architecture. */
    public String cpuArch;

    /** The CPU description. */
    public String cpuDescription;

    /** The CPU vendor. */
    public String cpuVendor;

    /** The CPU frequency. */
    public Double cpuMhz;

    /** The OS. */
    public String os;

    /** The OS version. */
    public String osVersion;

    /** The type. */
    public String type;

    /** The amount of RAM. */
    public Integer ramMb;

    /** The map of virtual guests running on this host. */
    public Map<String, String> vms;

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the total number of CPU sockets of the system.
     *
     * @return the number of sockets
     */
    public Integer getTotalCpuSockets() {
        return totalCpuSockets;
    }

    /**
     * Gets the total number of CPU cores of the system.
     *
     * @return the cpu cores
     */
    public Integer getTotalCpuCores() {
        return totalCpuCores;
    }

    /**
     * Gets the total number of CPU threads of the system.
     *
     * @return the number of threads
     */
    public Integer getTotalCpuThreads() {
        return totalCpuThreads;
    }

    /**
     * Gets the CPU architecture.
     *
     * @return the architecture
     */
    public String getCpuArch() {
        return cpuArch;
    }

    /**
     * Gets the CPU descriptions.
     *
     * @return the description
     */
    public String getCpuDescription() {
        return cpuDescription;
    }

    /**
     * Gets the CPU vendor.
     *
     * @return the vendor
     */
    public String getCpuVendor() {
        return cpuVendor;
    }

    /**
     * Gets the CPU speed in MHZ.
     *
     * @return cpu speed in MHZ
     */
    public Double getCpuMhz() {
        return cpuMhz;
    }

    /**
     * Gets the OS.
     *
     * @return the OS
     */
    public String getOs() {
        return os;
    }

    /**
     * Gets the OS version.
     *
     * @return the OS version
     */
    public String getOsVersion() {
        return osVersion;
    }

    /**
     * Gets the hypervisor type.
     *
     * @return hypervisor type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the memory in MB.
     *
     * @return the ram in MB
     */
    public Integer getRamMb() {
        return ramMb;
    }

    /**
     * Gets a map of virtual guests running on this host.
     * The key is the name of the guest while the value is the UUID.
     * @return map of guests
     */
    public Map<String, String> getVms() {
        return vms;
    }
}
