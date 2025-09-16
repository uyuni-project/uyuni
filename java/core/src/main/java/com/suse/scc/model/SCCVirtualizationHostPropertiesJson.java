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

import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.Server;

import com.google.gson.annotations.SerializedName;

import java.util.Optional;

/**
 * SCCVirtualizationHostPropertiesJson
 */
public class SCCVirtualizationHostPropertiesJson {

    private String name;

    private String arch;

    private long cores;

    private long sockets;

    private long threads;

    @SerializedName("ram_mb")
    private long ramMb;

    private String type;

    /**
     * Constructor
     * @param nameIn the name
     * @param archIn the architecture
     * @param coresIn the total number of CPU cores
     * @param socketsIn the total number of CPU sockets
     * @param threadsIn the total number of CPU threads
     * @param ramMbIn the amount of memory in MB
     * @param typeIn the hypervisor type
     */
    public SCCVirtualizationHostPropertiesJson(String nameIn, String archIn, long coresIn, long socketsIn,
                                               long threadsIn, long ramMbIn, String typeIn) {
        name = nameIn;
        if (archIn.endsWith("-linux")) {
            arch = archIn.split("-")[0];
        }
        else {
            arch = archIn;
        }
        cores = coresIn;
        sockets = socketsIn;
        threads = threadsIn;
        ramMb = ramMbIn;
        type = typeIn;
    }

    /**
     * Constructor
     * @param s the server
     */
    public SCCVirtualizationHostPropertiesJson(Server s) {
        name = s.getHostname();
        arch = s.getServerArch().getLabel().split("-")[0];
        sockets = Optional.ofNullable(s.getCpu()).map(CPU::getNrsocket).orElse(1L);
        cores = Optional.ofNullable(s.getCpu()).map(CPU::getNrCore).orElse(1L) * sockets;
        threads = Optional.ofNullable(s.getCpu()).map(CPU::getNrThread).orElse(1L) * cores;
        ramMb = s.getRam();
        type = s.getGuests().stream()
                .sorted((v1, v2) -> {
                    if (v1.isRegisteredGuest() == v2.isRegisteredGuest()) {
                        return 0;
                    }
                    else if (v1.isRegisteredGuest()) {
                        return -1;
                    }
                    return 1;
                })
                .flatMap(vi -> vi.getType().getHypervisor().stream())
                .findFirst()
                .orElse("");
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the architecture
     */
    public String getArch() {
        return arch;
    }

    /**
     * @return the total number of CPU cores
     */
    public long getCores() {
        return cores;
    }

    /**
     * @return the total number of CPU sockets
     */
    public long getSockets() {
        return sockets;
    }

    /**
     * @return the total number of CPU threads
     */
    public long getThreads() {
        return threads;
    }

    /**
     * @return the total number of memory in MB
     */
    public long getRamMb() {
        return ramMb;
    }

    /**
     * @return the hypervisor type
     */
    public String getType() {
        return type;
    }
}
