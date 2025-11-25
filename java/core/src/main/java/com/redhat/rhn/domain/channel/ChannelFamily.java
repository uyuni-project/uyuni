/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.domain.channel;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.org.Org;

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
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * ChannelFamily
 */
@Entity
@Table(name = "rhnChannelFamily")
public class ChannelFamily extends BaseDomainHelper {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RHN_CHANNEL_FAMILY_ID_SEQ")
    @SequenceGenerator(name = "RHN_CHANNEL_FAMILY_ID_SEQ", sequenceName = "RHN_CHANNEL_FAMILY_ID_SEQ",
            allocationSize = 1)
    private Long id;

    @Column
    private String name;

    @Column
    private String label;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Org org;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "rhnChannelFamilyMembers",
            joinColumns = @JoinColumn(name = "channel_family_id"),
            inverseJoinColumns = @JoinColumn(name = "channel_id"))
    private Set<Channel> channels = new HashSet<>();

    @OneToMany(mappedBy = "channelFamily", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private Set<PrivateChannelFamily> privateChannelFamilies = new HashSet<>();

    @OneToOne(mappedBy = "channelFamily", fetch = FetchType.LAZY)
    private PublicChannelFamily publicChannelFamily;

    /**
     * Default constructor.
     */
    public ChannelFamily() {
    }

    /**
     * Constructor that create the family with the specified label. Meant for unit testing only.
     * @param labelIn the label
     */
    public ChannelFamily(String labelIn) {
        this.label = labelIn;
    }

    /**
     * @return Returns the channels.
     */
    public Set<Channel> getChannels() {
        return this.channels;
    }

    /**
     * @param channelsIn The channels to set.
     */
    public void setChannels(Set<Channel> channelsIn) {
        this.channels = channelsIn;
    }
    /**
     * @return Returns the org.
     */
    public Org getOrg() {
        return org;
    }
    /**
     * @param orgIn The org to set.
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @param nameIn The name to set.
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }
    /**
     * @param labelIn The label to set.
     */
    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }
    /**
     * @param idIn The id to set.
     */
    protected void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return true if this channel family is a public channel family
     */
    public boolean isPublic() {
        return getPublicChannelFamily() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ChannelFamily castOther)) {
            return false;
        }
        return new EqualsBuilder().append(id, castOther.getId())
                                  .append(label, castOther.getLabel())
                                  .append(name, castOther.getName())
                                  .append(org, castOther.getOrg())
                                  .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .append(label)
                                    .append(name)
                                    .append(org)
                                    .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("name", name)
            .append("label", label).toString();
    }

    /**
     * @return Returns the privateChannelFamilies.
     */
    public Set<PrivateChannelFamily> getPrivateChannelFamilies() {
        return privateChannelFamilies;
    }


    /**
     * @param privateChannelFamiliesIn The privateChannelFamilies to set.
     */
    protected void setPrivateChannelFamilies(
            Set<PrivateChannelFamily> privateChannelFamiliesIn) {
        this.privateChannelFamilies = privateChannelFamiliesIn;
    }

    /**
     * Setter
     * @param pcfIn to set
     */
    public void addPrivateChannelFamily(PrivateChannelFamily pcfIn) {
        if (this.privateChannelFamilies == null) {
            this.privateChannelFamilies = new HashSet<>();
        }
        this.privateChannelFamilies.add(pcfIn);
    }

    /**
     * Gets the public channel family.
     * @return the public channel family
     */
    public PublicChannelFamily getPublicChannelFamily() {
        return publicChannelFamily;
    }

    /**
     * Sets the public channel family.
     * @param publicChannelFamilyIn the new public channel family
     */
    public void setPublicChannelFamily(PublicChannelFamily publicChannelFamilyIn) {
        publicChannelFamily = publicChannelFamilyIn;
    }
}

