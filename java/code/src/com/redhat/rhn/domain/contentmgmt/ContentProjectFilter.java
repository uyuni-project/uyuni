/**
 * Copyright (c) 2019 SUSE LLC
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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Link between {@link ContentProject} and {@link ContentFilter}
 */
@Entity
@Table(name = "suseContentFilterProject")
public class ContentProjectFilter {

    private Long id;
    private State state;
    private ContentProject project;
    private ContentFilter filter;

    /**
     * State of the Source
     */
    public enum State {
        ATTACHED,
        DETACHED,
        BUILT
    }

    /**
     * Standard constructor
     */
    public ContentProjectFilter() {
        this.state = State.ATTACHED;
    }

    /**
     * Standard constructor
     *
     * @param projectIn the {@link ContentProject}
     * @param filterIn the {@link ContentFilter}
     */
    public ContentProjectFilter(ContentProject projectIn, ContentFilter filterIn) {
        this();
        this.project = projectIn;
        this.filter = filterIn;
    }

    /**
     * Gets the id.
     *
     * @return id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_project_filter_seq")
    @SequenceGenerator(name = "content_project_filter_seq", sequenceName = "suse_ct_f_p_seq", allocationSize = 1)
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param idIn the id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Gets the state.
     *
     * @return state
     */
    @Enumerated(EnumType.STRING)
    public State getState() {
        return state;
    }

    /**
     * Sets the state.
     *
     * @param stateIn the state
     */
    public void setState(State stateIn) {
        this.state = stateIn;
    }

    /**
     * Gets the project.
     *
     * @return project
     */
    @ManyToOne
    @JoinColumn(name = "project_id")
    public ContentProject getProject() {
        return project;
    }

    /**
     * Sets the project.
     *
     * @param projectIn the project
     */
    public void setProject(ContentProject projectIn) {
        this.project = projectIn;
    }

    /**
     * Gets the filter.
     *
     * @return filter
     */
    @ManyToOne
    @JoinColumn(name = "filter_id")
    public ContentFilter getFilter() {
        return filter;
    }

    /**
     * Sets the filter.
     *
     * @param filterIn the filter
     */
    public void setFilter(ContentFilter filterIn) {
        this.filter = filterIn;
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

        ContentProjectFilter that = (ContentProjectFilter) o;

        return new EqualsBuilder()
                .append(project, that.project)
                .append(filter, that.filter)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(project)
                .append(filter)
                .toHashCode();
    }
}
