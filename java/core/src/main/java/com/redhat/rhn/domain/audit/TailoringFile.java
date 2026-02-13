/*
 * Copyright (c) 2025 SUSE LLC
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
package com.redhat.rhn.domain.audit;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.org.Org;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * TailoringFile entity representing a SCAP tailoring file.
 */
@Entity
@Table(name = "suseScapTailoringFile")
public class TailoringFile extends BaseDomainHelper {

    private Long id;
    private String name;
    private String fileName;
    private String description;
    private Org org;

    /**
     * Default constructor.
     */
    public TailoringFile() {
    }

    /**
     * Constructor for TailoringFile.
     * @param nameIn the name of the tailoring file
     * @param fileNameIn the physical filename
     */
    public TailoringFile(String nameIn, String fileNameIn) {
        this.name = nameIn;
        this.fileName = fileNameIn;
    }

    /**
     * @return the id
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * @return the name
     */
    @Column(name = "name")
    public String getName() {
        return name;
    }

    /**
     * @param nameIn the name to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return the fileName
     */
    @Column(name = "file_name")
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileNameIn the fileName to set
     */
    public void setFileName(String fileNameIn) {
        this.fileName = fileNameIn;
    }

    /**
     * Gets the display filename (original filename without org ID and name prefix).
     * This method extracts the original filename from the unique filename format:
     * {orgId}_{sanitizedName}_{originalFilename} → {originalFilename}
     *
     * @return the original filename for display purposes
     */
    @Transient
    public String getDisplayFileName() {
        if (fileName == null) {
            return null;
        }
        // Find the second underscore (after orgId_sanitizedName_)
        int firstUnderscore = fileName.indexOf('_');
        if (firstUnderscore == -1) {
            return fileName; // Fallback: return as-is if format doesn't match
        }

        int secondUnderscore = fileName.indexOf('_', firstUnderscore + 1);
        if (secondUnderscore == -1) {
            return fileName; // Fallback: return as-is if format doesn't match
        }
        // Return everything after the second underscore
        return fileName.substring(secondUnderscore + 1);
    }

    /**
     * @return the description
     */
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    /**
     * @param descriptionIn the description to set
     */
    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }

    /**
     * @return the organization
     */
    @ManyToOne
    @JoinColumn(name = "org_id")
    public Org getOrg() {
        return org;
    }

    /**
     * @param orgIn the org to set
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        TailoringFile castOther = (TailoringFile) other;
        return new EqualsBuilder()
                .append(name, castOther.name)
                .append(org, castOther.org)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(org)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString();
    }
}

