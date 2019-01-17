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
 * Content Project Source base class
 */

@Entity
@Table(name = "suseContentProjectSource")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class ProjectSource {

    private Long id;
    private ContentProject contentProject;

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
     * Sets the contentProject.
     *
     * @param contentProjectIn - the contentProject
     */
    public void setContentProject(ContentProject contentProjectIn) {
        contentProject = contentProjectIn;
    }

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
