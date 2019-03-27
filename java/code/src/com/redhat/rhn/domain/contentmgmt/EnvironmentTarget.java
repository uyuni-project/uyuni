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

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Optional;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
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
 * Content Environment Target
 */
@Entity
@Table(name = "suseContentEnvironmentTarget")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class EnvironmentTarget {

    private Long id;
    private ContentEnvironment contentEnvironment;

    /**
     * Standard constructor
     */
    public EnvironmentTarget() {
    }

    /**
     * Standard constructor
     *
     * @param contentEnvironmentIn the Environment
     */
    public EnvironmentTarget(ContentEnvironment contentEnvironmentIn) {
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

    protected ToStringBuilder toStringBuilder() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("contentEnvironment", contentEnvironment);
    }

    @Override
    public String toString() {
        return toStringBuilder().toString();
    }
}
