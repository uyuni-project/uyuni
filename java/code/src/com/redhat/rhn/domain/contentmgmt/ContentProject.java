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
import com.redhat.rhn.domain.org.Org;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import javax.persistence.OrderColumn;
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
    private List<ContentFilter> filters = new ArrayList<>();
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
    }

    /**
     * Adds a source to content project
     *
     * @param source the source
     */
    public void addSource(ProjectSource source) {
        source.setContentProject(this);
        sources.add(source);
    }

    /**
     * Removes a source from content project
     *
     * @param source the source
     */
    public void removeSource(ProjectSource source) {
        sources.remove(source);
    }

    /**
     * Gets the filters.
     *
     * @return filters
     */
    @ManyToMany
    @JoinTable(
            name = "suseContentFilterProject",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "filter_id")
    )
    @OrderColumn(name = "position")
    public List<ContentFilter> getFilters() {
        return filters;
    }

    /**
     * Sets the filters.
     *
     * @param filtersIn - the filters
     */
    public void setFilters(List<ContentFilter> filtersIn) {
        filters = filtersIn;
    }

    /**
     * Adds a filter.
     *
     * @param contentFilter the content filter
     */
    public void addFilter(ContentFilter contentFilter) {
        if (!org.equals(contentFilter.getOrg())) {
            throw new ContentManagementException("Filter organization does not match Content Project");
        }
        filters.add(contentFilter);
    }

    /**
     * Removes filter.
     *
     * @param contentFilter - the filter to remove
     * @return true if the filter was contained in the collection
     */
    public boolean removeFilter(ContentFilter contentFilter) {
        if (!org.equals(contentFilter.getOrg())) {
            throw new ContentManagementException("Filter organization does not match Content Project");
        }
        return filters.remove(contentFilter);
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
