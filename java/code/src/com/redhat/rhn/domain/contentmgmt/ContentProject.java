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


import static com.redhat.rhn.domain.contentmgmt.ProjectSource.State.DETACHED;
import static com.suse.utils.Opt.consume;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.org.Org;

import com.suse.utils.Opt;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * A Content Project
 */
@Entity
@Table(name = "suseContentProject")
public class ContentProject extends BaseDomainHelper {

    private Long id;
    private Org org;
    private String label;
    private String name;
    private String description;
    private ContentEnvironment firstEnvironment;
    private List<ProjectSource> sources = new ArrayList<>();
    private List<ContentProjectFilter> filters = new ArrayList<>();
    private List<ContentProjectHistoryEntry> historyEntries = new ArrayList<>();

    /**
     * Standard constructor.
     */
    public ContentProject() {
    }

    /**
     * Standard constructor.
     *
     * @param labelIn label
     * @param nameIn name
     * @param descriptionIn description
     * @param orgIn org
     */
    public ContentProject(String labelIn, String nameIn, String descriptionIn, Org orgIn) {
        this.label = labelIn;
        this.name = nameIn;
        this.description = descriptionIn;
        this.org = orgIn;
    }

    /**
     * Gets the id.
     *
     * @return id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_project_seq")
    @SequenceGenerator(name = "content_project_seq", sequenceName = "suse_ct_project_seq", allocationSize = 1)
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param idIn - the id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * Gets the org.
     *
     * @return org
     */
    @ManyToOne
    public Org getOrg() {
        return org;
    }

    /**
     * Sets the org.
     *
     * @param orgIn - the org
     */
    public void setOrg(Org orgIn) {
        org = orgIn;
    }

    /**
     * Gets the label.
     *
     * @return label
     */
    @Column
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label.
     *
     * @param labelIn - the label
     */
    public void setLabel(String labelIn) {
        label = labelIn;
    }

    /**
     * Gets the name.
     *
     * @return name
     */
    @Column
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param nameIn - the name
     */
    public void setName(String nameIn) {
        name = nameIn;
    }

    /**
     * Gets the description.
     *
     * @return description
     */
    @Column
    public String getDescription() {
        return description;
    }

    /**
     * @return the firstEnvironment
     */
    @OneToOne
    @JoinColumn(name = "first_env_id")
    protected ContentEnvironment getFirstEnvironment() {
        return firstEnvironment;
    }

    /**
     * Gets the first environment or empty if none exists
     *
     * @return optional of the first environment
     */
    @Transient
    public Optional<ContentEnvironment> getFirstEnvironmentOpt() {
        return Optional.ofNullable(getFirstEnvironment());
    }

    /**
     * @param firstEnvironmentIn the firstEnvironment to set
     */
    public void setFirstEnvironment(ContentEnvironment firstEnvironmentIn) {
        this.firstEnvironment = firstEnvironmentIn;
    }

    /**
     * Sets the description.
     *
     * @param descriptionIn - the description
     */
    public void setDescription(String descriptionIn) {
        description = descriptionIn;
    }

    /**
     * Gets the sources.
     *
     * @return sources
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "contentProject", orphanRemoval = true)
    @OrderBy("position")
    public List<ProjectSource> getSources() {
        return sources;
    }

    /**
     * Sets the sources.
     *
     * @param sourcesIn - the sources
     */
    public void setSources(List<ProjectSource> sourcesIn) {
        sources = sourcesIn;
        // fix the order
        for (int idx = 0; idx < sources.size(); idx++) {
            sources.get(idx).setPosition(idx);
        }
    }

    /**
     * Adds a source to content project
     *
     * @param source the Source
     * @param position the (optional) position
     * @return true if Source was added, false if the Source had been present already
     */
    public boolean addSource(ProjectSource source, Optional<Integer> position) {
        source.setContentProject(this);
        if (sources.contains(source)) {
            return false;
        }
        consume(position,
                () -> sources.add(source),
                (p) -> sources.add(p, source)
        );

        // fix the order
        for (int idx = position.orElse(sources.size() - 1); idx < sources.size(); idx++) {
            sources.get(idx).setPosition(idx);
        }

        return true;
    }

    /**
     * Removes a source from content project
     *
     * @param source the source
     */
    public void removeSource(ProjectSource source) {
        sources.remove(source);
        source.setContentProject(null);
        // fix the order
        for (int idx = source.getPosition(); idx < sources.size(); idx++) {
            sources.get(idx).setPosition(idx);
        }
    }

    /**
     * Looks up {@link SoftwareProjectSource} "leader" of the {@link ContentProject}
     *
     * When a Project contains at least one {@link SoftwareProjectSource}, one of them has a special "leader" role:
     * After building the Project, the "leader" will be used as a base Channel for Channels from other Project Sources.
     *
     * The "leader" is the first {@link SoftwareProjectSource} in the list of Project sources.
     *
     * @return the leader {@link SoftwareProjectSource}
     */
    public Optional<SoftwareProjectSource> lookupSwSourceLeader() {
        return sources.stream()
                .flatMap(s -> Opt.stream(s.asSoftwareSource()))
                .filter(src -> !src.getState().equals(DETACHED) && src.getChannel().isBaseChannel())
                .filter(src -> src.getChannel().isBaseChannel())
                .findFirst();
    }

    /**
     * Gets the links to Project Filters.
     *
     * @return filters
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "project", orphanRemoval = true)
    public List<ContentProjectFilter> getProjectFilters() {
        return filters;
    }

    /**
     * Get the active (non-detached) Project Filters
     *
     * @return the active Project Filters
     */
    @Transient
    public List<ContentFilter> getActiveFilters() {
        return getProjectFilters().stream()
                .filter(f -> f.getState() != ContentProjectFilter.State.DETACHED)
                .map(f -> f.getFilter())
                .collect(Collectors.toList());
    }

    /**
     * Sets the links to Project Filters.
     *
     * @param filtersIn - the filters
     */
    public void setProjectFilters(List<ContentProjectFilter> filtersIn) {
        filters = filtersIn;
    }

    /**
     * Attach a {@link ContentFilter} to {@link ContentProject}
     *
     * @param filter the filter to attach
     */
    public void attachFilter(ContentFilter filter) {
        ContentProjectFilter projectFilter = new ContentProjectFilter(this, filter);

        int idx = filters.indexOf(projectFilter);
        if (idx != -1) {
            ContentProjectFilter toUpdate = filters.get(idx);
            if (toUpdate.getState() == ContentProjectFilter.State.DETACHED) {
                // When a filter is detached we either transit it to BUILT or EDITED depending on the previous state
                // If the filter was edited after the last built time it transits to EDITED otherwise to BUILT
                ContentProjectFilter.State newState = getFirstEnvironmentOpt()
                        .flatMap(env -> env.computeBuiltTime())
                        .filter(builtTime -> filter.getModified().after(builtTime))
                        .map(builtTime -> ContentProjectFilter.State.EDITED)
                        .orElse(ContentProjectFilter.State.BUILT);
                toUpdate.setState(newState);
            }
        }
        else {
            filters.add(projectFilter);
        }
    }

    /**
     * Detach a {@link ContentFilter} from a {@link ContentProject}
     *
     * @param filter the filter to detach
     */
    public void detachFilter(ContentFilter filter) {
        ContentProjectFilter projectFilter = new ContentProjectFilter(this, filter);

        int idx = filters.indexOf(projectFilter);
        if (idx != -1) {
            ContentProjectFilter toUpdate = filters.get(idx);

            switch (toUpdate.getState()) {
                case BUILT:
                    toUpdate.setState(ContentProjectFilter.State.DETACHED);
                    break;
                case EDITED:
                    toUpdate.setState(ContentProjectFilter.State.DETACHED);
                    break;
                case ATTACHED:
                    filters.remove(idx);
                    break;
                default:
                    // no-op
            }
        }
    }

    /**
     * Gets the historyEntries.
     *
     * @return historyEntries
     */
    @OneToMany(mappedBy = "contentProject")
    public List<ContentProjectHistoryEntry> getHistoryEntries() {
        return historyEntries;
    }

    /**
     * Sets the historyEntries.
     *
     * @param historyEntriesIn - the historyEntries
     */
    public void setHistoryEntries(List<ContentProjectHistoryEntry> historyEntriesIn) {
        historyEntries = historyEntriesIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ContentProject that = (ContentProject) o;

        return new EqualsBuilder()
                .append(label, that.label)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(label)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("label", label)
                .append("org", org)
                .toString();
    }
}
