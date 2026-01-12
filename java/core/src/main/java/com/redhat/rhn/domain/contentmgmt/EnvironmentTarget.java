/*
 * Copyright (c) 2019--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.contentmgmt;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * Content Environment Target
 */
@Entity
@Table(name = "suseContentEnvironmentTarget")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class EnvironmentTarget implements Serializable {

    @Serial
    private static final long serialVersionUID = -764569287303965488L;

    private Long id;
    private ContentEnvironment contentEnvironment;
    private Status status;
    private Date builtTime;

    /**
     * Status of the {@link EnvironmentTarget}
     */
    public enum Status {
        NEW("new"),
        BUILDING("building"),
        GENERATING_REPODATA("generating_repodata"),
        BUILT("built"),
        FAILED("failed");

        private final String label;

        Status(String labelIn) {
            this.label = labelIn;
        }

        /**
         * Return the label
         * @return the label
         */
        public String getLabel() {
            return label;
        }
    }

    /**
     * Standard constructor
     */
    protected EnvironmentTarget() {
        status = Status.NEW;
    }

    /**
     * Standard constructor
     *
     * @param contentEnvironmentIn the Environment
     */
    protected EnvironmentTarget(ContentEnvironment contentEnvironmentIn) {
        this();
        this.contentEnvironment = contentEnvironmentIn;
    }

    /**
     * Return the Target as Software Target, if it is one.
     *
     * @return the {@link Optional} of {@link SoftwareEnvironmentTarget}
     */
    public abstract Optional<SoftwareEnvironmentTarget> asSoftwareTarget();

    /**
     * Gets the id.
     *
     * @return id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_prj_env_target_seq")
    @SequenceGenerator(name = "content_prj_env_target_seq", sequenceName = "suse_ct_env_tgt_seq", allocationSize = 1)
    public Long getId() {
        return id;
    }

    /**
     * Sets the id
     *
     * @param idIn the id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Gets the contentEnvironment.
     *
     * @return contentEnvironment
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "env_id")
    public ContentEnvironment getContentEnvironment() {
        return contentEnvironment;
    }

    /**
     * Sets the contentEnvironment.
     *
     * @param contentEnvironmentIn - the contentEnvironment
     */
    public void setContentEnvironment(ContentEnvironment contentEnvironmentIn) {
        contentEnvironment = contentEnvironmentIn;
    }

    /**
     * Gets the status.
     *
     * @return status
     */
    @Column
    @Enumerated(EnumType.STRING)
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param statusIn the status
     */
    public void setStatus(Status statusIn) {
        this.status = statusIn;
    }

    protected ToStringBuilder toStringBuilder() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("contentEnvironment", contentEnvironment)
                .append("status", status);
    }


    /**
     * Gets the time of latest build
     * @return Date of latest build
     */
    @Column(name = "built_time")
    public Date getBuiltTime() {
        return this.builtTime;
    }

    /**
     * Sets the time of latest build
     * @param builtTimeIn New value for build time
     */
    public void setBuiltTime(Date builtTimeIn) {
        this.builtTime = builtTimeIn;
    }

    @Override
    public String toString() {
        return toStringBuilder().toString();
    }
}
