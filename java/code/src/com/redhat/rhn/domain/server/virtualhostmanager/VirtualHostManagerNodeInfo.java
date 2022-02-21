/*
 * Copyright (c) 2017 SUSE LLC
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

package com.redhat.rhn.domain.server.virtualhostmanager;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.server.ServerArch;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * VirtualHostManagerNodeInfo
 */
@Entity
@Table(name = "suseVirtualHostManagerNodeInfo")
public class VirtualHostManagerNodeInfo extends BaseDomainHelper {

    private Long id;
    private String identifier;
    private String name;
    private Integer cpuSockets;
    private Integer cpuCores;
    private Integer ram;
    private ServerArch nodeArch;
    private String os;
    private String osVersion;

    /**
     * @return the id
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vhmnodeinfo_seq")
    @SequenceGenerator(name = "vhmnodeinfo_seq", sequenceName = "suse_vhm_nodeinfo_id_seq")
    public Long getId() {
        return id;
    }

    /**
     * @param idIn the id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return the cpuSockets
     */
    @Column(name = "cpu_sockets")
    public Integer getCpuSockets() {
        return cpuSockets;
    }

    /**
     * @param cpusIn the cpuSockets
     */
    public void setCpuSockets(Integer cpusIn) {
        this.cpuSockets = cpusIn;
    }

    /**
     * @return the memory
     */
    @Column(name = "ram")
    public Integer getRam() {
        return ram;
    }

    /**
     * @param memoryIn the memory
     */
    public void setRam(Integer memoryIn) {
        this.ram = memoryIn;
    }

    /**
     * @return the node arch
     */
    @ManyToOne
    @JoinColumn(name = "node_arch_id")
    public ServerArch getNodeArch() {
        return nodeArch;
    }

    /**
     * @param nodeArchIn the node arch
     */
    public void setNodeArch(ServerArch nodeArchIn) {
        this.nodeArch = nodeArchIn;
    }

    /**
     * @return the identifier
     */
    @Column(name = "identifier")
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifierIn to set
     */
    public void setIdentifier(String identifierIn) {
        this.identifier = identifierIn;
    }

    /**
     * @return the name
     */
    @Column(name = "name")
    public String getName() {
        return name;
    }

    /**
     * @param nameIn to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return the cpuCores
     */
    @Column(name = "cpu_cores")
    public Integer getCpuCores() {
        return cpuCores;
    }

    /**
     * @param cpuCoresIn to set
     */
    public void setCpuCores(Integer cpuCoresIn) {
        this.cpuCores = cpuCoresIn;
    }

    /**
     * @return the os
     */
    @Column(name = "os")
    public String getOs() {
        return os;
    }

    /**
     * @param osIn to set
     */
    public void setOs(String osIn) {
        this.os = osIn;
    }

    /**
     * @return the osVersion
     */
    @Column(name = "os_version")
    public String getOsVersion() {
        return osVersion;
    }

    /**
     * @param osVersionIn to set
     */
    public void setOsVersion(String osVersionIn) {
        this.osVersion = osVersionIn;
    }
}
