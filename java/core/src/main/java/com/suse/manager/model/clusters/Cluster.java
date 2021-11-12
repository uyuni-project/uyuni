/**
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

package com.suse.manager.model.clusters;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Cluster entity bean.
 */
@Entity
@Table(name = "suseClusters")
@NamedQueries
        ({
                @NamedQuery(name = "Clusters.findByOrg",
                        query = "from com.suse.manager.model.clusters.Cluster c where c.org.id = :orgId"),
                @NamedQuery(name = "Clusters.findByGroup",
                        query = "from com.suse.manager.model.clusters.Cluster c where c.group.id = :groupId")
        })
public class Cluster {

    private long id;
    private Org org;
    private String name;
    private String label;
    private String description;
    private String provider;
    private MinionServer managementNode;
    private ManagedServerGroup group;

    /**
     * @return id to get
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clust_seq")
    @SequenceGenerator(name = "clust_seq", sequenceName = "suse_cluster_id_seq",
            allocationSize = 1)
    public long getId() {
        return id;
    }

    /**
     * @param idIn to set
     */
    public void setId(long idIn) {
        this.id = idIn;
    }

    /**
     * @return org to get
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    public Org getOrg() {
        return org;
    }

    /**
     * @param orgIn to set
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }

    /**
     * @return name to get
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
     * @return provider to get
     */
    @Column(name = "provider")
    public String getProvider() {
        return provider;
    }

    /**
     * @param providerIn to set
     */
    public void setProvider(String providerIn) {
        this.provider = providerIn;
    }

    /**
     * @return label to get
     */
    @Column(name = "label")
    public String getLabel() {
        return label;
    }

    /**
     * @param labelIn to set
     */
    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    /**
     * @return description to get
     */
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    /**
     * @param descriptionIn to set
     */
    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }

    /**
     * @return managementNode to get
     */
    @ManyToOne
    @JoinColumn(name = "management_node_id")
    public MinionServer getManagementNode() {
        return managementNode;
    }

    /**
     * @param managementNodeIn to set
     */
    public void setManagementNode(MinionServer managementNodeIn) {
        this.managementNode = managementNodeIn;
    }

    /**
     * @return group to get
     */
    @ManyToOne
    @JoinColumn(name = "group_id")
    public ManagedServerGroup getGroup() {
        return group;
    }

    /**
     * @param groupIn to set
     */
    public void setGroup(ManagedServerGroup groupIn) {
        this.group = groupIn;
    }
}
