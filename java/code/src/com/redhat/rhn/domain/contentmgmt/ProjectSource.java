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
     * Publish content project.
     */
    public abstract void publish();

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

    @Override
    public String toString() {
        return toStringBuilder().toString();
    }
}
