/*
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.webui.utils.salt.custom;

import com.google.gson.annotations.SerializedName;

/**
 *
 */
public class GuestProperties {

    @SerializedName("memory_size")
    private final long memorySize;

    private final String name;

    private final String state;

    private final String uuid;

    private final int vcpus;

    @SerializedName("virt_type")
    private final String virtType;

    /**
     * Constructor
     *
     * @param memorySizeIn memory size of the guest
     * @param nameIn name of the guest
     * @param stateIn state of the guest
     * @param uuidIn uuid of the guest
     * @param vcpusIn cpus of the guest
     * @param virtTypeIn virt type of the guest
     */
    public GuestProperties(long memorySizeIn, String nameIn, String stateIn, String uuidIn,
            int vcpusIn, String virtTypeIn) {
        this.memorySize = memorySizeIn;
        this.name = nameIn;
        this.state = stateIn;
        this.uuid = uuidIn;
        this.vcpus = vcpusIn;
        this.virtType = virtTypeIn;
    }

    /**
     * @return the memorySize
     */
    public long getMemorySize() {
        return memorySize;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @return the vcpus
     */
    public int getVcpus() {
        return vcpus;
    }

    /**
     * @return the virtType
     */
    public String getVirtType() {
        return virtType;
    }
}
