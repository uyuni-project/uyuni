/*
 * Copyright (c) 2014--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.scc;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.product.ChannelTemplate;

import com.suse.scc.model.SCCRepositoryJson;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Type;
import org.hibernate.type.YesNoConverter;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * This is a SUSE repository as parsed from JSON coming in from SCC.
 */
@Entity
@Table(name = "suseSCCRepository")
public class SCCRepository extends BaseDomainHelper {

    private Long id;
    private Long sccId;
    private String name;
    private String distroTarget;
    private String description;
    private String url;
    private boolean autorefresh;
    private boolean signed = true;
    private boolean installerUpdates = false;

    private Set<ChannelTemplate> channelTemplates = new HashSet<>();
    private Set<SCCRepositoryAuth> auth = new HashSet<>();

    /**
     * Default Constructor
     */
    public SCCRepository() { }

    /**
     * Constructor
     * @param j Json SCCRepository object
     */
    public SCCRepository(SCCRepositoryJson j) {
        update(j);
    }

    /**
     * Update the object
     * @param j Json SCCRepository object
     */
    public void update(SCCRepositoryJson j) {
        sccId = j.getSCCId();
        name = j.getName();
        distroTarget = j.getDistroTarget();
        description = j.getDescription();
        url = j.getUrl();
        autorefresh = j.isAutorefresh();
        installerUpdates = j.isInstallerUpdates();
    }
    /**
     * @return the SCC id
     */
    @Column(name = "scc_id")
    public Long getSccId() {
        return sccId;
    }

    /**
     * @param sccIdIn the SCC id to set
     */
    public void setSccId(Long sccIdIn) {
        this.sccId = sccIdIn;
    }

    /**
     * @return the name
     */
    @Column(name = "name")
    public String getName() {
        return name;
    }

    /**
     * @param nameIn the name to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return the distroTarget
     */
    @Column(name = "distro_target")
    public String getDistroTarget() {
        return distroTarget;
    }

    /**
     * @param distroTargetIn the distroTarget to set
     */
    public void setDistroTarget(String distroTargetIn) {
        this.distroTarget = distroTargetIn;
    }

    /**
     * @return the description
     */
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    /**
     * @param descriptionIn the description to set
     */
    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }

    /**
     * @return the url
     */
    @Column(name = "url")
    public String getUrl() {
        return url;
    }

    /**
     * @param urlIn the url to set
     */
    public void setUrl(String urlIn) {
        this.url = urlIn;
    }

    /**
     * @return the autorefresh
     */
    @Column(name = "autorefresh")
    @Convert(converter = YesNoConverter.class)
    public boolean isAutorefresh() {
        return autorefresh;
    }

    /**
     * @param autorefreshIn the autorefresh to set
     */
    public void setAutorefresh(boolean autorefreshIn) {
        this.autorefresh = autorefreshIn;
    }

    /**
     * Gets the id.
     * @return the id
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sccrepository_seq")
    @SequenceGenerator(name = "sccrepository_seq", sequenceName = "suse_sccrepository_id_seq", allocationSize = 1)
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     * @param idIn the new id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * @return the signed
     */
    @Convert(converter = YesNoConverter.class)
    public boolean isSigned() {
        return signed;
    }

    /**
     * @param signedIn the signed to set
     */
    public void setSigned(boolean signedIn) {
        this.signed = signedIn;
    }

    /**
     * @return Return true if this is for installer updates
     */
    @Convert(converter = YesNoConverter.class)
    @Column(name = "installer_updates")
    public boolean isInstallerUpdates() {
        return installerUpdates;
    }

    /**
     * @param installerUpdatesIn set installer updates
     */
    public void setInstallerUpdates(boolean installerUpdatesIn) {
        this.installerUpdates = installerUpdatesIn;
    }

    /**
     * @return Returns the auth.
     */
    @OneToMany(mappedBy = "repo", orphanRemoval = true)
    public Set<SCCRepositoryAuth> getRepositoryAuth() {
        return auth;
    }

    /**
     * @return true if this repository is accessible.
     */
    @Transient
    public boolean isAccessible() {
        return !getRepositoryAuth().isEmpty();
    }

    // local -> rmt if valid -> scc (priority by locality) (groups ordered by id for stability)
    /**
     * @return the best authentication object if there is one for this repository.
     */
    @Transient
    public Optional<SCCRepositoryAuth> getBestAuth() {
        Optional<SCCRepositoryAuth> result = Optional.empty();

        Set<SCCRepositoryAuth> repositoryAuth = getRepositoryAuth();
        if (repositoryAuth.isEmpty()) {
            return result;
        }

        return repositoryAuth.stream().min(new SCCRepositoryAuthComparator());
    }

    /**
     * @param authIn The auth to set.
     */
    public void setRepositoryAuth(Set<SCCRepositoryAuth> authIn) {
        auth = authIn;
    }

    /**
     * @return Returns the products.
     */
    @OneToMany(mappedBy = "repository", fetch = FetchType.LAZY)
    public Set<ChannelTemplate> getChannelTemplates() {
        return channelTemplates;
    }

    /**
     * @param channelTemplatesIn The products to set.
     */
    public void setChannelTemplates(Set<ChannelTemplate> channelTemplatesIn) {
        this.channelTemplates = channelTemplatesIn;
    }

    /**
     * @param templateIn the channel template to add
     */
    public void addChannelTemplate(ChannelTemplate templateIn) {
        templateIn.setRepository(this);
        this.channelTemplates.add(templateIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SCCRepository otherSCCRepository)) {
            return false;
        }
        return new EqualsBuilder()
                .append(getUrl(), otherSCCRepository.getUrl())
                .append(getSccId(), otherSCCRepository.getSccId())
                .append(getName(), otherSCCRepository.getName())
                .append(getDescription(), otherSCCRepository.getDescription())
                .append(isAutorefresh(), otherSCCRepository.isAutorefresh())
                .append(isSigned(), otherSCCRepository.isSigned())
                .append(isInstallerUpdates(), otherSCCRepository.isInstallerUpdates())
            .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getUrl())
                .append(getSccId())
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .append("sccId", getSccId())
        .append("description", getDescription())
        .toString();
    }
}
