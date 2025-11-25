/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2010 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.kickstart;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * KickstartVirtualizationType -
 * Class representation of the table rhnKickstartVirtualizationType.
 */
@Entity
@Table(name = "rhnKickstartVirtualizationType")
public class KickstartVirtualizationType extends BaseDomainHelper
                        implements Comparable<KickstartVirtualizationType> {

    /**
     * These are the constants for the labels that are stored in this database
     * table.
     */
    public static final String XEN_PARAVIRT = "xenpv";
    public static final String XEN_FULLYVIRT = "xenfv";
    public static final String KVM_FULLYVIRT = "qemu";
    public static final String PARA_HOST  = "para_host";
    public static final String NONE  = "none";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "kvt_seq")
    @SequenceGenerator(name = "kvt_seq", sequenceName = "rhn_kvt_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String name;

    /**
     * @return the Kickstart Virtualization type associated to xen para virt
     */
    public static KickstartVirtualizationType xenPV() {
        return KickstartFactory.lookupKickstartVirtualizationTypeByLabel(XEN_PARAVIRT);
    }

    /**
     * @return the Kickstart Virtualization type associated to xen full virt
     */
    public static KickstartVirtualizationType xenFV() {
        return KickstartFactory.lookupKickstartVirtualizationTypeByLabel(XEN_FULLYVIRT);
    }

    /**
     * Note this is presently used for default virt type also..
     * @return the Kickstart Virtualization type associated to kvm guest
     */
    public static KickstartVirtualizationType kvmGuest() {
        return KickstartFactory.lookupKickstartVirtualizationTypeByLabel(KVM_FULLYVIRT);
    }

    /**
     * @return the Kickstart Virtualization type associated to para host
     */
    public static KickstartVirtualizationType paraHost() {
        return KickstartFactory.lookupKickstartVirtualizationTypeByLabel(PARA_HOST);
    }

    /**
     * @return the Kickstart Virtualization type associated to para host
     */
    public static KickstartVirtualizationType none() {
        return KickstartFactory.lookupKickstartVirtualizationTypeByLabel(NONE);
    }

    /**
     * Getter for id
     * @return Long to get
    */
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for id
     * @param idIn to set
    */
    protected void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Getter for label
     * @return String to get
    */
    public String getLabel() {
        return this.label;
    }

    /**
     * Setter for label
     * @param labelIn to set
    */
    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    /**
     * Getter for name
     * @return String to get
    */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for name
     * @param nameIn to set
    */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof KickstartVirtualizationType castOther)) {
            return false;
        }
        return new EqualsBuilder().append(this.getId(),
                castOther.getId()).append(this.getLabel(),
                castOther.getLabel()).isEquals();
    }
    /**
     * Returns a i18nized name of the passed in virt type.
     * Mainly used for display purposes
     * @return the i18nized name..
     */
    public String getFormattedName() {
        LocalizationService ls = LocalizationService.getInstance();
        String messageId = "kickstart.jsp.virt-type." + getLabel();
        return ls.getMessage(messageId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.getId()).
            append(this.getLabel()).toHashCode();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public int compareTo(KickstartVirtualizationType arg0) {
        return getLabel().compareTo(arg0.getLabel());
    }

}
