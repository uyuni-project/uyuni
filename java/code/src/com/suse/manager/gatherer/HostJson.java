/*
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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;


/**
 * Json representation of a Virtual Host system.
 */
public class HostJson {

    /** Host name. */
    private String name;

    /** Host Identifier. */
    private String hostIdentifier;

    /**
     * For VMWare we had to change the value for the host identifier. This fields contains the value previously used,
     * to allow the code to match and update the value.
     */
    private String fallbackHostIdentifier;

    /** Total CPU socket count. */
    private Integer totalCpuSockets;

    /** Total CPU core count. */
    private Integer totalCpuCores;

    /** Total CPU thread count. */
    private Integer totalCpuThreads;

    /** CPU architecture. */
    private String cpuArch;

    /** CPU description. */
    private String cpuDescription;

    /** CPU vendor. */
    private String cpuVendor;

    /** CPU frequency in MHz. */
    private Double cpuMhz;

    /** OS. */
    private String os;

    /** OS version. */
    private String osVersion;

    /** Hypervisor type. */
    private String type;

    /** Amount of memory in MB. */
    private Integer ramMb;

    /** Maps virtual guests names to UUIDs. */
    private Map<String, String> vms;

    /** Maps virtual guest names to optional VM data. */
    private Map<String, Map<String, String>> optionalVmData;

    /**
     * Gets the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the host identifier
     * @return the host identifier
     */
    public String getHostIdentifier() {
        return hostIdentifier;
    }

    /**
     * Gets the fallback host identifier
     * @return the fallback host identifier
     */
    public String getFallbackHostIdentifier() {
        return fallbackHostIdentifier;
    }

    /**
     * Gets the total CPU socket count.
     * @return the number of sockets - can be 0 if insufficient data were send
     */
    public Integer getTotalCpuSockets() {
        return totalCpuSockets == null ? 0 : totalCpuSockets;
    }

    /**
     * Gets the total CPU core count.
     * @return the cpu cores
     */
    public Integer getTotalCpuCores() {
        return totalCpuCores == null ? 1 : totalCpuCores;
    }

    /**
     * Gets the total CPU thread count.
     * @return the CPU thread count
     */
    public Integer getTotalCpuThreads() {
        return totalCpuThreads == null ? 1 : totalCpuThreads;
    }

    /**
     * Gets the CPU architecture.
     * @return the architecture
     */
    public String getCpuArch() {
        return cpuArch;
    }

    /**
     * Gets the CPU description.
     * @return the description
     */
    public String getCpuDescription() {
        return cpuDescription;
    }

    /**
     * Gets the CPU vendor.
     * @return the vendor
     */
    public String getCpuVendor() {
        return cpuVendor;
    }

    /**
     * Gets the CPU frequency in MHz.
     * @return the CPU frequency in MHz
     */
    public Double getCpuMhz() {
        return cpuMhz;
    }

    /**
     * Gets the OS.
     * @return the OS
     */
    public String getOs() {
        return os;
    }

    /**
     * Gets the OS version.
     * @return the OS version
     */
    public String getOsVersion() {
        return osVersion;
    }

    /**
     * Gets the hypervisor type.
     * @return hypervisor type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the amount of memory in MB.
     * @return the ram in MB
     */
    public Integer getRamMb() {
        return ramMb;
    }

    /**
     * Gets a map of virtual guests running on this host.
     * Keys are guest names and values are corresponding UUIDs.
     * @return map of guests
     */
    public Map<String, String> getVms() {
        return vms;
    }

    /**
     * Gets a map of optional VM data for virtual guests running on this host.
     * Keys are guest names and values are corresponding VM optional data attributes.
     * @return map of guests optional data
     */
    public Map<String, Map<String, String>> getOptionalVmData() {
        return Objects.requireNonNullElse(this.optionalVmData, Collections.emptyMap());
    }

    /**
     * Sets the name.
     * @param nameIn the new name
     */
    public void setName(String nameIn) {
        name = nameIn;
    }

    /**
     * Sets the host identifier.
     * @param hostIdentifierIn the host identifier
     */
    public void setHostIdentifier(String hostIdentifierIn) {
        this.hostIdentifier = hostIdentifierIn;
    }

    /**
     * Sets the fallback host identifier.
     * @param hostUuidIn the fallback host identifier
     */
    public void setFallbackHostIdentifier(String hostUuidIn) {
        this.fallbackHostIdentifier = hostUuidIn;
    }

    /**
     * Sets the total CPU socket count.
     * @param totalCpuSocketsIn the number of sockets
     */
    public void setTotalCpuSockets(Integer totalCpuSocketsIn) {
        totalCpuSockets = totalCpuSocketsIn;
    }

    /**
     * Sets the the total CPU core count.
     *
     * @param totalCpuCoresIn the cpu cores
     */
    public void setTotalCpuCores(Integer totalCpuCoresIn) {
        totalCpuCores = totalCpuCoresIn;
    }

    /**
     * Sets the total CPU thread count.
     * @param totalCpuThreadsIn the thread count
     */
    public void setTotalCpuThreads(Integer totalCpuThreadsIn) {
        totalCpuThreads = totalCpuThreadsIn;
    }

    /**
     * Sets the CPU architecture
     * @param cpuArchIn the architecture
     */
    public void setCpuArch(String cpuArchIn) {
        cpuArch = cpuArchIn;
    }

    /**
     * Sets the CPU description.
     * @param cpuDescriptionIn the description
     */
    public void setCpuDescription(String cpuDescriptionIn) {
        cpuDescription = cpuDescriptionIn;
    }

    /**
     * Sets the CPU vendor.
     * @param cpuVendorIn the vendor
     */
    public void setCpuVendor(String cpuVendorIn) {
        cpuVendor = cpuVendorIn;
    }

    /**
     * Sets the the CPU frequency in MHz.
     *
     * @param cpuMhzIn the CPU frequency in MHz.
     */
    public void setCpuMhz(Double cpuMhzIn) {
        cpuMhz = cpuMhzIn;
    }

    /**
     * Sets the OS.
     * @param osIn the new OS
     */
    public void setOs(String osIn) {
        os = osIn;
    }

    /**
     * Sets the OS version.
     * @param osVersionIn the new OS version
     */
    public void setOsVersion(String osVersionIn) {
        osVersion = osVersionIn;
    }

    /**
     * Sets the hypervisor type.
     * @param typeIn the new type
     */
    public void setType(String typeIn) {
        type = typeIn;
    }

    /**
     * Sets the the amount of memory in MB.
     * @param ramMbIn the amount of memory in MB
     */
    public void setRamMb(Integer ramMbIn) {
        ramMb = ramMbIn;
    }

    /**
     * Sets a map of virtual guests running on this host.
     * Keys are guest names and values are corresponding UUIDs.
     * @param vmsIn the map of guests
     */
    public void setVms(Map<String, String> vmsIn) {
        vms = vmsIn;
    }

    /**
     * Sets a map of optional VM data for virtual guests running on this host.
     * Keys are guest names and values are corresponding VM info attributes.
     * @param optionalVmDataIn the map of guests optional data
     */
    public void setOptionalVmData(Map<String, Map<String, String>> optionalVmDataIn) {
        optionalVmData = optionalVmDataIn;
    }
}
