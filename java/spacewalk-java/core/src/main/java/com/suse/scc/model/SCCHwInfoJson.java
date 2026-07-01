/*
 * Copyright (c) 2023 SUSE LLC
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
package com.suse.scc.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

/**
 * This is a System Item send to SCC for registration.
 */
public class SCCHwInfoJson {

    private int cpus;
    private int sockets;

    @SerializedName("mem_total")
    private int memTotal;

    private String arch;
    private String uuid;
    private String hypervisor;

    private String uname;

    @SerializedName("container_runtime")
    private String containerRuntime;

    @SerializedName("cloud_provider")
    private String cloudProvider;

    private Set<SAPJson> sap;
    @SerializedName("arch_specs")
    private Map<String, Object> archSpecs;

    public int getCpus() {
        return cpus;
    }

    public void setCpus(int cpusIn) {
        cpus = cpusIn;
    }

    public int getSockets() {
        return sockets;
    }

    public void setSockets(int socketsIn) {
        sockets = socketsIn;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String archIn) {
        arch = archIn;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuidIn) {
        uuid = uuidIn;
    }

    public String getHypervisor() {
        return hypervisor;
    }

    public void setHypervisor(String hypervisorIn) {
        hypervisor = hypervisorIn;
    }

    public String getCloudProvider() {
        return cloudProvider;
    }

    public void setCloudProvider(String cloudProviderIn) {
        cloudProvider = cloudProviderIn;
    }

    public int getMemTotal() {
        return memTotal;
    }

    public void setMemTotal(int memTotalIn) {
        memTotal = memTotalIn;
    }

    public Set<SAPJson> getSap() {
        return sap;
    }

    public void setSap(Set<SAPJson> sapIn) {
        sap = sapIn;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String unameIn) {
        uname = unameIn;
    }

    public String getContainerRuntime() {
        return containerRuntime;
    }

    public void setContainerRuntime(String containerRuntimeIn) {
        containerRuntime = containerRuntimeIn;
    }
    public Map<String, Object> getArchSpecs() {
        return archSpecs;
    }

    public void setArchSpecs(Map<String, Object> archSpecsIn) {
        archSpecs = archSpecsIn;
    }
}
