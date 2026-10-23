/*
 * Copyright (c) 2026 SUSE LLC
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
package com.redhat.rhn.domain.image;

import com.redhat.rhn.domain.org.Org;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * File extension of a KIWI build artefact which is allowed to be collected. Entries without organization are
 * the defaults of the instance, an entry of an organization overrides the default for the same extension.
 */
@Entity
@Table(name = "suseImageFileType")
public class ImageFileType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "org_id")
    private Org org;
    private String extension;
    private String comment;
    private boolean enabled = true;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the organization this entry applies to, null for the default of the whole instance
     */
    public Org getOrg() {
        return org;
    }

    /**
     * @param orgIn the organization this entry applies to, null for the default of the whole instance
     */
    public void setOrg(Org orgIn) {
        org = orgIn;
    }

    /**
     * @return the file extension, including the leading dot
     */
    public String getExtension() {
        return extension;
    }

    /**
     * @param extensionIn the file extension, including the leading dot
     */
    public void setExtension(String extensionIn) {
        extension = extensionIn;
    }

    /**
     * @return the description of the file type
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param commentIn the description of the file type
     */
    public void setComment(String commentIn) {
        comment = commentIn;
    }

    /**
     * @return true if files with this extension are collected
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabledIn true if files with this extension should be collected
     */
    public void setEnabled(boolean enabledIn) {
        enabled = enabledIn;
    }
}
