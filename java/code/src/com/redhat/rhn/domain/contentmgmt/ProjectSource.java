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

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Content Project Source base class
 */

@Entity
@Table(name = "suseContentProjectSource")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class ProjectSource {

    private Long id;
    private State state;
    private ContentProject contentProject;

    /**
     * State of the Source
     */
    public enum State {
        ATTACHED,
        DETACHED,
        BUILT
    }

    /**
     * Utility enum for the ProjectSource types
     */
    public enum Type {
        SW_CHANNEL("software", SoftwareProjectSource.class);

        /* the label for converting from string */
        private final String label;
        /* the source class for Hibernate Queries */
        private final Class sourceClass;

        /**
         * Constructor
         *
         * @param labelIn the label
         * @param sourceClassIn the source class
         */
        Type(String labelIn, Class sourceClassIn) {
            this.label = labelIn;
            this.sourceClass = sourceClassIn;
        }

        /**
         * Get the string label
         *
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * Get the corresponding ProjectSource class
         *
         * @return the source class
         */
        public Class getSourceClass() {
            return sourceClass;
        }

        /**
         * Looks up Type by label
         *
         * @param label the label
         * @throws java.lang.IllegalArgumentException if no matching type is found
         * @return the matching type
         */
        public static Type lookupByLabel(String label) {
            for (Type value : values()) {
                if (value.label.equals(label)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unsupported label: " + label);
        }

        /**
         * Looks up Type by source class
         *
         * @param sourceClass source class
         * @throws java.lang.IllegalArgumentException if no matching type is found
         * @return the matching type
         */
        public static Type lookupBySourceClass(Class sourceClass) {
            for (Type value : values()) {
                if (value.sourceClass.equals(sourceClass)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unsupported class: " + sourceClass);
        }
    }

    /**
     * Standard constructor
     */
    public ProjectSource() {
        state = State.ATTACHED;
    }

    /**
     * Standard constructor
     * @param project the ContentProject
     */
    public ProjectSource(ContentProject project) {
        this();
        this.contentProject = project;
    }

    /**
     * Gets the id.
     *
     * @return id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_prj_src_seq")
    @SequenceGenerator(name = "content_prj_src_seq", sequenceName = "suse_ct_prj_src_seq", allocationSize = 1)
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
     * Gets the state.
     *
     * @return state
     */
    @Enumerated(EnumType.STRING)
    @Column
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
     * Sets the contentProject.
     *
     * @param contentProjectIn - the contentProject
     */
    public void setContentProject(ContentProject contentProjectIn) {
        contentProject = contentProjectIn;
    }

    /**
     * Gets the Source as Software Source if it's one.
     *
     * @return the Optional of SoftwareProjectSource
     */
    public abstract Optional<SoftwareProjectSource> asSoftwareSource();

    /**
     * Gets the contentProject.
     *
     * @return contentProject
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    public ContentProject getContentProject() {
        return contentProject;
    }

    protected ToStringBuilder toStringBuilder() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("contentProject", contentProject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringBuilder().toString();
    }
}
