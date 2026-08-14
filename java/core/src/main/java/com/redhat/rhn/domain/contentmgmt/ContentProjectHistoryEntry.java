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

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.legacy.UserImpl;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * Content Project History entry
 */
@Entity
@Table(name = "suseContentProjectHistoryEntry")
public class ContentProjectHistoryEntry implements Serializable {

    @Serial
    private static final long serialVersionUID = -4007161635528460287L;


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_project_history_seq")
    @SequenceGenerator(name = "content_project_history_seq", sequenceName = "suse_ct_prj_hist_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ContentProject contentProject;

    @Column
    private String message;

    @Column
    private Long version;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date created = new Date();

    @ManyToOne(targetEntity = UserImpl.class, fetch = FetchType.LAZY)
    private User user;

    /**
     * Gets the id.
     *
     * @return id
     */
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
     * Gets the contentProject.
     *
     * @return contentProject
     */
    public ContentProject getContentProject() {
        return contentProject;
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
     * Gets the message.
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message.
     *
     * @param messageIn - the message
     */
    public void setMessage(String messageIn) {
        message = messageIn;
    }

    /**
     * Gets the version.
     *
     * @return version
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Sets the version.
     *
     * @param versionIn - the version
     */
    public void setVersion(Long versionIn) {
        version = versionIn;
    }

    /**
     * Gets the created.
     *
     * @return created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * Sets the created.
     *
     * @param createdIn - the created
     */
    public void setCreated(Date createdIn) {
        created = createdIn;
    }

    /**
     * Gets the user.
     *
     * @return user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user.
     *
     * @param userIn - the user
     */
    public void setUser(User userIn) {
        user = userIn;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("version", version)
                .append("contentProject", contentProject == null ? null : contentProject.getLabel())
                .append("created", created)
                .append("user", user)
                .toString();
    }
}
