/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
 * Copyright (c) 2024 SUSE LLC
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

package com.redhat.rhn.domain.server;

import com.redhat.rhn.domain.rhnpackage.PackageEvr;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;


/**
 * @author paji
 */
@Entity
@Table(name = "rhnProxyInfo")
public class ProxyInfo implements Serializable {
    @Id
    @Column(name = "server_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private Server server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proxy_evr_id")
    private PackageEvr version;

    @Column(name = "ssh_port")
    private Integer sshPort;

    @Column(name = "ssh_public_key")
    private byte[] sshPublicKey;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param sid the server_id to set
     */
    protected void setId(Long sid) {
        this.id = sid;
    }

    /**
     * @return Returns the version.
     */
    public PackageEvr getVersion() {
        return version;
    }


    /**
     * @param aVersion The version to set.
     */
    public void setVersion(PackageEvr aVersion) {
        version = aVersion;
    }

    /**
     * @return the server
     */
    public Server getServer() {
        return server;
    }

    /**
     * @param s the server to set
     */
    public void setServer(Server s) {
        this.server = s;
    }

    /**
     * @return value of sshPort
     */
    public Integer getSshPort() {
        return sshPort;
    }

    /**
     * @param sshPortIn value of sshPort
     */
    public void setSshPort(Integer sshPortIn) {
        sshPort = sshPortIn;
    }

    /**
     * @return value of sshPublicKey
     */
    public byte[] getSshPublicKey() {
        return sshPublicKey;
    }

    /**
     * @param sshPublicKeyIn value of sshPublicKey
     */
    public void setSshPublicKey(byte[] sshPublicKeyIn) {
        sshPublicKey = sshPublicKeyIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProxyInfo proxyInfo = (ProxyInfo) o;

        return new EqualsBuilder()
                .append(server, proxyInfo.server)
                .append(version, proxyInfo.version)
                .append(sshPort, proxyInfo.sshPort)
                .append(sshPublicKey, proxyInfo.sshPublicKey)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(server)
                .append(version)
                .append(sshPort)
                .append(sshPublicKey)
                .toHashCode();
    }
}
