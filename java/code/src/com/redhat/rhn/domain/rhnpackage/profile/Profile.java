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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.rhnpackage.profile;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.Identifiable;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.org.Org;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Profile
 */
@Entity
@Table(name = "rhnServerProfile")
public class Profile extends BaseDomainHelper implements Identifiable {

    @Id
    @GeneratedValue(generator = "RHN_SERVER_PROFILE_ID_SEQ")
    @GenericGenerator(
            name = "RHN_SERVER_PROFILE_ID_SEQ",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "RHN_SERVER_PROFILE_ID_SEQ"),
                    @Parameter(name = "increment_size", value = "1")
            })
    private Long id;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private String info;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Org org;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_channel")
    private Channel baseChannel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_type_id")
    private ProfileType profileType;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_profile_id")
    private Set<ProfileEntry> packageEntries;

    /**
     * Default constructor
     */
    public Profile() {
    }

    /**
     * Constructs a Profile of the given type.
     * @param type Type of profile desired.
     */
    public Profile(ProfileType type) {
        profileType = type;
    }

    /**
     * @return Returns the baseChannel.
     */
    public Channel getBaseChannel() {
        return baseChannel;
    }

    /**
     * @param b The baseChannel to set.
     */
    public void setBaseChannel(Channel b) {
        this.baseChannel = b;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param d The description to set.
     */
    public void setDescription(String d) {
        this.description = d;
    }

    /**
     * @return Returns the id.
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * @param i The id to set.
     */
    protected void setId(Long i) {
        this.id = i;
    }

    /**
     * @return Returns the info.
     */
    public String getInfo() {
        return info;
    }

    /**
     * @param i The info to set.
     */
    public void setInfo(String i) {
        this.info = i;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param n The name to set.
     */
    public void setName(String n) {
        this.name = n;
    }

    /**
     * @return Returns the org.
     */
    public Org getOrg() {
        return org;
    }

    /**
     * @param o The org to set.
     */
    public void setOrg(Org o) {
        this.org = o;
    }

    /**
     * @return Returns the profileType.
     */
    public ProfileType getProfileType() {
        return profileType;
    }

    /**
     * @param p The profileType to set.
     */
    public void setProfileType(ProfileType p) {
        this.profileType = p;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Profile castOther)) {
            return false;
        }
        return new EqualsBuilder().append(id, castOther.id)
                                  .append(name, castOther.name)
                                  .append(description, castOther.description)
                                  .append(info, castOther.info)
                                  .append(org, castOther.org)
                                  .append(baseChannel, castOther.baseChannel)
                                  .append(profileType, castOther.profileType)
                                  .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .append(name)
                                    .append(description)
                                    .append(info)
                                    .append(org)
                                    .append(baseChannel)
                                    .append(profileType)
                                    .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("name", name).toString();
    }


    /**
     * @return Returns the packageEntries.
     */
    public Set<ProfileEntry> getPackageEntries() {
        return packageEntries;
    }


    /**
     * @param packageEntriesIn The packageEntries to set.
     */
    public void setPackageEntries(Set<ProfileEntry> packageEntriesIn) {
        this.packageEntries = packageEntriesIn;
    }

}
