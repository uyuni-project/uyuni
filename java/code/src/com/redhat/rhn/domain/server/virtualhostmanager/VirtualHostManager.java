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

package com.redhat.rhn.domain.server.virtualhostmanager;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.credentials.VHMCredentials;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.Server;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Virtual Host Manager entity
 */
@Entity
@Table(name = "suseVirtualHostManager")
public class VirtualHostManager extends BaseDomainHelper {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suse_vhms_seq")
    @SequenceGenerator(name = "suse_vhms_seq", sequenceName = "suse_vhms_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Org org;

    @Column(nullable = false)
    private String label;

    @Column(name = "gatherer_module", nullable = false)
    private String gathererModule;

    @ManyToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinColumn(name = "cred_id")
    private VHMCredentials credentials;

    @OneToMany(mappedBy = "virtualHostManager",
            cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn(name = "virtual_host_manager_id")
    private Set<VirtualHostManagerConfig> configs;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "suseServerVirtualHostManager",
            joinColumns = @JoinColumn(name = "vhmserver_id"),
            inverseJoinColumns = @JoinColumn(name = "server_id"))
    private Set<Server> servers;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "suseServerVirtualHostManager",
            joinColumns = @JoinColumn(name = "vhmserver_id"),
            inverseJoinColumns = @JoinColumn(name = "node_id"))
    private Set<VirtualHostManagerNodeInfo> nodes;

    /**
     * Constructor
     */
    public VirtualHostManager() {
        configs = new HashSet<>();
        servers = new HashSet<>();
        nodes = new HashSet<>();
    }

    /**
     * Gets the id
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id
     * @param idIn - the new id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Gets the organization
     * @return organization
     */
    public Org getOrg() {
        return org;
    }

    /**
     * Sets the organization
     * @param orgIn the new organization
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }

    /**
     * Gets the label
     * @return label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label
     * @param labelIn - the new label
     */
    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    /**
     * Gets the gatherer module
     * @return gathererModule
     */
    public String getGathererModule() {
        return gathererModule;
    }

    /**
     * Sets the gatherer module
     * @param gathererModuleIn - the new gatherer module
     */
    public void setGathererModule(String gathererModuleIn) {
        this.gathererModule = gathererModuleIn;
    }

    /**
     * Gets the credentials
     * @return credentials
     */
    public VHMCredentials getCredentials() {
        return credentials;
    }

    /**
     * Sets the credentials
     * @param credentialsIn - the new credentials
     */
    public void setCredentials(VHMCredentials credentialsIn) {
        this.credentials = credentialsIn;
    }

    /**
     * Gets the set of Virtual Host Manager configurations
     * @return the set of Virtual Host Manager configurations
     */
    public Set<VirtualHostManagerConfig> getConfigs() {
        return configs;
    }

    /**
     * Sets the set of Virtual Host Manager configuration
     * @param configsIn - set of Virtual Host Manager configuration
     */
    public void setConfigs(Set<VirtualHostManagerConfig> configsIn) {
        this.configs = configsIn;
    }

    /**
     * Get the set of servers managed by this virtual host manager
     * @return set of servers
     */
    public Set<Server> getServers() {
        return this.servers;
    }

    /**
     * Sets the set of servers managed by this virtual host manager
     * @param serversIn set of servers
     */
    public void setServers(Set<Server> serversIn) {
        this.servers = serversIn;
    }

    /**
     * Add a server to the set of managed server by this virtual host manager
     * @param serverIn a server
     */
    public void addServer(Server serverIn) {
        this.servers.add(serverIn);
    }

    /**
     * Remove a server from the set of managed server by this virtual host manager
     * @param serverIn a server
     */
    public void removeServer(Server serverIn) {
        this.servers.remove(serverIn);
    }

    /**
     * @return the nodes
     */
    public Set<VirtualHostManagerNodeInfo> getNodes() {
        return nodes;
    }

    /**
     * @param nodesIn the nodes
     */
    public void setNodes(Set<VirtualHostManagerNodeInfo> nodesIn) {
        this.nodes = nodesIn;
    }

    /**
     * Remove a node from the set of managed nodes by this virtual host manager
     * @param nodeIn a server
     */
    public void removeNode(VirtualHostManagerNodeInfo nodeIn) {
        this.nodes.remove(nodeIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VirtualHostManager that = (VirtualHostManager) o;

        return new EqualsBuilder()
                .append(label, that.label)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(label)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("label", label)
                .append("gathererModule", gathererModule)
                .toString();
    }
}
