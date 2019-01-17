/**
 * Copyright (c) 2018 SUSE LLC
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

package com.redhat.rhn.domain.contentmgmt;

import com.redhat.rhn.domain.BaseDomainHelper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Optional;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import static java.util.Optional.ofNullable;

/**
 * A Content Environment
 */
@Entity
@Table(name = "suseContentEnvironment")
public class ContentEnvironment extends BaseDomainHelper {

    private Long id;
    private String label;
    private String name;
    private String description;
    private Long version;
    private ContentProject contentProject;
    private ContentEnvironment nextEnvironment;
    private ContentEnvironment prevEnvironment;

    /**
     * Standard constructor.
     */
    public ContentEnvironment() {
    }

    /**
     * Standard constructor.
     *
     * @param labelIn label
     * @param nameIn name
     * @param contentProjectIn content project
     */
    public ContentEnvironment(String labelIn, String nameIn, ContentProject contentProjectIn) {
        this.label = labelIn;
        this.name = nameIn;
        this.contentProject = contentProjectIn;
    }

    /**
     * @return the id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_env_seq")
    @SequenceGenerator(name = "content_env_seq", sequenceName = "suse_ct_env_seq", allocationSize = 1)
    public Long getId() {
        return id;
    }

    /**
     * @return the label
     */
    @Column
    public String getLabel() {
        return label;
    }

    /**
     * @return the name
     */
    @Column
    public String getName() {
        return name;
    }

    /**
     * @return the description
     */
    @Column
    public String getDescription() {
        return description;
    }

    /**
     * @return the version
     */
    @Column
    public Long getVersion() {
        return version;
    }

    /**
     * @return the Content Project
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    public ContentProject getContentProject() {
        return contentProject;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * @param labelIn the label to set
     */
    public void setLabel(String labelIn) {
        label = labelIn;
    }

    /**
     * @param nameIn the name to set
     */
    public void setName(String nameIn) {
        name = nameIn;
    }

    /**
     * @param descriptionIn the description to set
     */
    public void setDescription(String descriptionIn) {
        description = descriptionIn;
    }

    /**
     * @param versionIn the version to set
     */
    public void setVersion(Long versionIn) {
        version = versionIn;
    }

    /**
     * @param contentProjectIn the content project to set
     */
    public void setContentProject(ContentProject contentProjectIn) {
        contentProject = contentProjectIn;
    }

    /**
     * @return the nextEnvironment
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "next_env_id")
    protected ContentEnvironment getNextEnvironment() {
        return nextEnvironment;
    }

    /**
     * @return the nextEnvironment
     */
    @OneToOne(mappedBy = "nextEnvironment")
    protected ContentEnvironment getPrevEnvironment() {
        return prevEnvironment;
    }

    /**
     * @return optional of the next environment
     */
    @Transient
    public Optional<ContentEnvironment> getNextEnvironmentOpt() {
        return ofNullable(getNextEnvironment());
    }

    /**
     * @return optional of the previous environment
     */
    @Transient
    public Optional<ContentEnvironment> getPrevEnvironmentOpt() {
        return ofNullable(getPrevEnvironment());
    }

    /**
     * @param nextEnvironmentIn set next environment
     */
    protected void setNextEnvironment(ContentEnvironment nextEnvironmentIn) {
        nextEnvironment = nextEnvironmentIn;
    }

    /**
     * @param prevEnvironmentIn set previous environment
     */
    protected void setPrevEnvironment(ContentEnvironment prevEnvironmentIn) {
        prevEnvironment = prevEnvironmentIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ContentEnvironment)) {
            return false;
        }
        ContentEnvironment otherContentEnvironment = (ContentEnvironment) other;
        return new EqualsBuilder()
            .append(getLabel(), otherContentEnvironment.getLabel())
            .append(getName(), otherContentEnvironment.getName())
            .append(getContentProject(), otherContentEnvironment.getContentProject())
            .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getLabel())
            .append(getName())
            .append(getContentProject())
            .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .append("name", getName())
                .append("project", getContentProject().getName())
                .append("label", getLabel())
                .append("version", getVersion())
                .append("next env id", ofNullable(getNextEnvironment()).map(e -> e.getId()).orElse(null))
                .toString();
    }
}
