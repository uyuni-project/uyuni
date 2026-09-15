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
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.redhat.rhn.domain.kickstart;

import com.redhat.rhn.domain.common.FileList;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;

public class KickstartPreserveFileListId implements Serializable {

    @Serial
    private static final long serialVersionUID = 8426626801295726357L;

    private KickstartData ksdata;
    private FileList fileList;

    /**
     * Constructor
     */
    public KickstartPreserveFileListId() {
    }

    /**
     * Constructor
     *
     * @param ksdataIn   the input ksdata
     * @param fileListIn the input fileList
     */
    public KickstartPreserveFileListId(KickstartData ksdataIn, FileList fileListIn) {
        ksdata = ksdataIn;
        fileList = fileListIn;
    }

    public KickstartData getKsdata() {
        return ksdata;
    }

    public void setKsdata(KickstartData ksdataIn) {
        ksdata = ksdataIn;
    }

    public FileList getFileList() {
        return fileList;
    }

    public void setFileList(FileList fileListIn) {
        fileList = fileListIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof KickstartPreserveFileListId that)) {
            return false;
        }

        return new EqualsBuilder()
                .append(ksdata, that.ksdata)
                .append(fileList, that.fileList)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(ksdata)
                .append(fileList)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "KickstartPreserveFileListId{" +
                "ksdata=" + ksdata +
                ", fileList=" + fileList +
                '}';
    }
}
