/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.action.config;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.Action;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * ConfigDateFileAction - Class representation of the table rhnActionConfigDateFile.
 *
 */
@Entity
@Table(name = "rhnActionConfigDateFile")
@IdClass(ConfigDateFileActionId.class)
public class ConfigDateFileAction extends BaseDomainHelper implements Serializable {

    @Id
    @Column(name = "file_name")
    private String fileName;

    @Id
    @ManyToOne(targetEntity = Action.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "action_id", nullable = false)
    private Action parentAction;

    @Column(name = "file_type")
    private String fileType;

    /**
     * Getter for fileName
     * @return String to get
    */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * Setter for fileName
     * @param fileNameIn to set
    */
    public void setFileName(String fileNameIn) {
        this.fileName = fileNameIn;
    }

    /**
     * Getter for fileType
     * @return String to get
    */
    public String getFileType() {
        return this.fileType;
    }

    /**
     * Setter for fileType
     * @param fileTypeIn to set
    */
    public void setFileType(String fileTypeIn) {
        this.fileType = fileTypeIn;
    }

    /**
     * Gets the parent Action associated with this ServerAction record
     * @return Returns the parentAction.
     */
    public Action getParentAction() {
        return parentAction;
    }

    /**
     * Sets the parent Action associated with this ServerAction record
     * @param parentActionIn The parentAction to set.
     */
    public void setParentAction(Action parentActionIn) {
        this.parentAction = parentActionIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ConfigDateFileAction castOther)) {
            return false;
        }
        return new EqualsBuilder().append(getParentAction(), castOther.getParentAction())
                                  .append(fileName, castOther.getFileName()).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getParentAction().getId()).append(fileName)
                .toHashCode();
    }

}
