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
package com.redhat.rhn.domain.image;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.token.Token;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Optional;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * ImageProfile
 */
@Entity
@Table(name = "suseImageProfile")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "image_type")
public abstract class ImageProfile extends BaseDomainHelper {

    public static final String TYPE_DOCKERFILE = "dockerfile";
    public static final String TYPE_KIWI = "kiwi";

    /** The profileId. */
    private Long profileId;
    private String label;
    private Org org;
    private Token token;
    private ImageStore targetStore;
    private Set<ProfileCustomDataValue> customDataValues;

    /**
     * @return the profileId
     */
    @Id
    @Column(name = "profile_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "imgprof_seq")
    @SequenceGenerator(name = "imgprof_seq", sequenceName = "suse_imgprof_prid_seq",
                       allocationSize = 1)
    public Long getProfileId() {
        return profileId;
    }

    /**
     * @return the label
     */
    @Column(name = "label")
    public String getLabel() {
        return label;
    }

    /**
     * @return the org
     */
    @ManyToOne
    public Org getOrg() {
        return org;
    }

    /**
     * @return the token
     */
    @ManyToOne
    public Token getToken() {
        return token;
    }

    /**
     * @return the targetStore
     */
    @ManyToOne
    @JoinColumn(name = "target_store_id")
    public ImageStore getTargetStore() {
        return targetStore;
    }

    /**
     * @return the custom data values
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "profile", cascade = CascadeType.ALL)
    public Set<ProfileCustomDataValue> getCustomDataValues() {
        return customDataValues;
    }
    /**
     * @param profileIdIn the profileId to set
     */
    public void setProfileId(Long profileIdIn) {
        this.profileId = profileIdIn;
    }

    /**
     * @param labelIn the label to set
     */
    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    /**
     * @param orgIn the org to set
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }

    /**
     * @param tokenIn the token to set
     */
    public void setToken(Token tokenIn) {
        this.token = tokenIn;
    }

    /**
     * @param storeIn the targetStore to set
     */
    public void setTargetStore(ImageStore storeIn) {
        this.targetStore = storeIn;
    }

    /**
     * @param customDataValuesIn the custom data values
     */
    public void setCustomDataValues(Set<ProfileCustomDataValue> customDataValuesIn) {
        this.customDataValues = customDataValuesIn;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object other) {
        if (!(other instanceof ImageProfile)) {
            return false;
        }
        ImageProfile castOther = (ImageProfile) other;
        return new EqualsBuilder().append(label, castOther.label)
                                  .append(org, castOther.org)
                                  .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder().append(label)
                                    .append(org)
                                    .toHashCode();
    }

    /**
     * Always returns an empty <code>Optional</code>. @see com.redhat.rhn.domain.image.DockerfileProfile
     *
     * @return an empty <code>Optional</code>
     */
    public Optional<DockerfileProfile> asDockerfileProfile() {
        return Optional.empty();
    }

    /**
     * Always returns an empty <code>Optional</code>. @see com.redhat.rhn.domain.image.KiwiProfile
     *
     * @return an empty <code>Optional</code>
     */
    public Optional<KiwiProfile> asKiwiProfile() {
        return Optional.empty();
    }

    /**
     * Gets the image type value as specified in {@code @DiscriminatorValue} annotation
     * @return the image type
     */
    @Transient
    public String getImageType() {
        return this.getClass().getAnnotation(DiscriminatorValue.class).value();
    }
}
