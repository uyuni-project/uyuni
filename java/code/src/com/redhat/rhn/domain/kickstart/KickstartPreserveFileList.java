/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.domain.kickstart;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.common.FileList;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * kickstartpreservefilelist - Class representation of the table
 * rhnkickstartpreservefilelist.
 */
@Entity
@Table(name = "rhnKickstartPreserveFileList")
@IdClass(KickstartPreserveFileListId.class)
public class KickstartPreserveFileList extends BaseDomainHelper {

    private static final long serialVersionUID = 1L;

    @Id
    @ManyToOne(targetEntity = KickstartData.class)
    @JoinColumn(name = "kickstart_id")
    private KickstartData ksdata;

    @Id
    @ManyToOne(targetEntity = FileList.class)
    @JoinColumn(name = "file_list_id")
    private FileList fileList;

    /**
     * Getter for ksdata
     * @return KickstartData to get
    */
    public KickstartData getksdata() {
        return this.ksdata;
    }

    /**
     * Setter for ksdata
     * @param ksdataIn to set
    */
    public void setKsdata(KickstartData ksdataIn) {
        this.ksdata = ksdataIn;
    }

    /**
     * Getter for fileList
     * @return Long to get
    */
    public FileList getFileList() {
        return this.fileList;
    }

    /**
     * Setter for fileList
     * @param fileListIn to set
    */
    public void setFileList(FileList fileListIn) {
        this.fileList = fileListIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof KickstartPreserveFileList castOther)) {
            return false;
        }
        return new EqualsBuilder().append(ksdata, castOther.ksdata)
                                  .append(fileList, castOther.fileList)
                                  .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(ksdata)
                                    .append(fileList)
                                    .toHashCode();
    }
}
