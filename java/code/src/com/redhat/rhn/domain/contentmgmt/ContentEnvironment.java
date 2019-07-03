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

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.contentmgmt.EnvironmentTarget.Status;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

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
    private Long version = 0L;
    private ContentProject contentProject;
    private List<EnvironmentTarget> targets = new ArrayList<>();
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
     * @param descriptionIn description
     * @param contentProjectIn content project
     */
    public ContentEnvironment(String labelIn, String nameIn, String descriptionIn, ContentProject contentProjectIn) {
        this.label = labelIn;
        this.name = nameIn;
        this.description = descriptionIn;
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
     * Gets the targets.
     *
     * @return targets
     */
    @OneToMany(mappedBy = "contentEnvironment", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    public List<EnvironmentTarget> getTargets() {
        return targets;
    }

    /**
     * Sets the targets.
     *
     * @param targetsIn the targets
     */
    public void setTargets(List<EnvironmentTarget> targetsIn) {
        this.targets = targetsIn;
    }

    /**
     * Add target
     *
     * @param target the target
     */
    public void addTarget(EnvironmentTarget target) {
        target.setContentEnvironment(this);
        targets.add(target);
    }

    /**
     * Remove target
     * @param target the target
     */
    public void removeTarget(EnvironmentTarget target) {
        targets.remove(target);
        target.setContentEnvironment(null);
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

    /**
     * Computes status of the {@link ContentEnvironment} based on the status of its {@link EnvironmentTarget}s
     *
     * If there are no targets or only new targets (waiting for building) -> empty,
     * else if at least 1 target is building -> BUILDING,
     * else if at least 1 target is generating repodata -> GENERATING_REPODATA,
     * else if at least 1 target is failed -> FAILED,
     * otherwise -> BUILT.
     *
     * @return the computed status
     */
    public Optional<Status> computeStatus() {
        Set<Status> statuses = getTargets().stream()
                .map(t -> t.getStatus())
                .collect(Collectors.toSet());

        if (statuses.isEmpty() || statuses.stream().allMatch(s -> s == Status.NEW)) {
            return empty();
        }

        if (statuses.contains(Status.BUILDING)) {
            return of(Status.BUILDING);
        }

        if (statuses.contains(Status.GENERATING_REPODATA)) {
            return of(Status.GENERATING_REPODATA);
        }

        if (statuses.contains(Status.FAILED)) {
            return of(Status.FAILED);
        }

        return of(Status.BUILT);
    }

    /**
     * Computes the built time of the {@link ContentEnvironment} based on the built times of its
     * {@link EnvironmentTarget}
     * Most recent built time of all the environment targets
     *
     * @return the built time
     */
    public Optional<Date> computeBuiltTime() {
        return computeStatus()
                .filter(envStatus -> envStatus == Status.BUILT)
                .map(envStatus -> getTargets().stream()
                        .map(t -> t.getBuiltTime())
                        .filter(value -> value != null)
                        .max(Date::compareTo))
                .orElse(Optional.empty());
    }
}
