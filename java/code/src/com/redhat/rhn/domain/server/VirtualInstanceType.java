/*
 * Copyright (c) 2021 SUSE LLC
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_ONLY;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.Immutable;

import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * VirtualInstanceType
 */
@Entity
@Table(name = "RhnVirtualInstanceType")
@Immutable
@Cache(usage = READ_ONLY)
public class VirtualInstanceType extends BaseDomainHelper {

    @Id
    private Long id;

    @Column
    private String name;

    @Column
    private String label;

    /**
     * Constructor
     */
    VirtualInstanceType() {
    }

    public Long getId() {
        return id;
    }

    protected void setId(Long idIn) {
        id = idIn;
    }

    public String getName() {
        return name;
    }

    public void setName(String nameIn) {
        name = nameIn;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String labelIn) {
        label = labelIn;
    }

    /**
     * @return the cloud provider as string when available
     */
    public Optional<String> getCloudProvider() {
        switch(getLabel()) {
            case "azure": return Optional.of("Microsoft");
            case "aws": return Optional.of("Amazon");
            case "aws_nitro": return Optional.of("Amazon");
            case "aws_xen": return Optional.of("Amazon");
            case "gce": return Optional.of("Google");
            default: return Optional.empty();
        }
    }

    /**
     * @return the hypervisor as string when available
     */
    public Optional<String> getHypervisor() {
        switch(getLabel()) {
            case "fully_virtualized": return Optional.of("Xen");
            case "para_virtualized": return Optional.of("Xen");
            case "aws": return Optional.of("Xen");
            case "aws_xen": return Optional.of("Xen");
            case "aws_nitro": return Optional.of("Nitro");
            case "qemu": return Optional.of("KVM");
            case "vmware": return Optional.of("VMware");
            case "hyperv": return Optional.of("Hyper-V");
            case "nutanix": return Optional.of("Nutanix");
            case "virtualbox": return Optional.of("VirtualBox");
            case "virtualpc": return Optional.of("VirtualPC");
            case "virtage": return Optional.of("Virtage");
            default: return Optional.empty();
        }
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof VirtualInstanceType that)) {
            return false;
        }
        return new EqualsBuilder().append(this.getName(), that.getName())
                .append(this.getLabel(), that.getLabel()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getName()).append(getLabel()).toHashCode();
    }
}
