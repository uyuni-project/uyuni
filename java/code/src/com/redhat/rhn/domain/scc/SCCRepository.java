/**
 * Copyright (c) 2014--2018 SUSE LLC
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
package com.redhat.rhn.domain.scc;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.product.SUSEProductSCCRepository;
import com.redhat.rhn.manager.content.ContentSyncManager;

import com.suse.scc.model.SCCRepositoryJson;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Type;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * This is a SUSE repository as parsed from JSON coming in from SCC.
 */
@Entity
@Table(name = "suseSCCRepository")
@NamedQueries
({
    @NamedQuery(name = "SCCRepository.lookupByChannelFamily",
                query = "select r from SCCRepository r " +
                        " join r.products pr " +
                        " join pr.product p " +
                        " join p.channelFamily cf " +
                        "where cf.label = :channelFamily")
})
public class SCCRepository extends BaseDomainHelper {

    private Long id;
    private Long sccId;
    private String name;
    private String distroTarget;
    private String description;
    private String url;
    private boolean autorefresh;
    private boolean signed = true;

    private Set<SUSEProductSCCRepository> products = new HashSet<>();
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
    @Type(type = "yes_no")
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
    @SequenceGenerator(name = "sccrepository_seq", sequenceName = "suse_sccrepository_id_seq",
                       allocationSize = 1)
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
    @Type(type = "yes_no")
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
     * @return Returns the auth.
     */
    @OneToMany(mappedBy = "repository")
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

    /**
     * @return the best authentication object if there is one for this repository.
     */
    @Transient
    public Optional<SCCRepositoryAuth> getBestAuth() {
        Optional<SCCRepositoryAuth> result = Optional.empty();
        for (SCCRepositoryAuth a : getRepositoryAuth()) {
            if (Config.get().getString(ContentSyncManager.RESOURCE_PATH, null) != null) {
                if (!a.getOptionalCredentials().isPresent()) {
                    return Optional.of(a);
                }
            }
            else {
                if (a.getOptionalCredentials().orElse(new Credentials()).getUrl() != null) {
                    return Optional.of(a);
                }
            }
            result = Optional.of(a);
        }
        return result;
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
    @OneToMany(mappedBy = "repository")
    public Set<SUSEProductSCCRepository> getProducts() {
        return products;
    }

    /**
     * @param productsIn The products to set.
     */
    public void setProducts(Set<SUSEProductSCCRepository> productsIn) {
        this.products = productsIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SCCRepository)) {
            return false;
        }
        SCCRepository otherSCCRepository = (SCCRepository) other;
        return new EqualsBuilder()
            .append(getUrl(), otherSCCRepository.getUrl())
            .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getUrl())
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
