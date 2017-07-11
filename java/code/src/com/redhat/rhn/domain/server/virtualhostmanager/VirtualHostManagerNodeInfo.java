/**
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
    private Integer cpus;
    private Integer memory;
    private ServerArch nodeArch;

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
     * @return the cpus
     */
    @Column(name = "cpus")
    public Integer getCpus() {
        return cpus;
    }

    /**
     * @param cpusIn the cpus
     */
    public void setCpus(Integer cpusIn) {
        this.cpus = cpusIn;
    }

    /**
     * @return the memory
     */
    @Column(name = "memory")
    public Integer getMemory() {
        return memory;
    }

    /**
     * @param memoryIn the memory
     */
    public void setMemory(Integer memoryIn) {
        this.memory = memoryIn;
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

}
