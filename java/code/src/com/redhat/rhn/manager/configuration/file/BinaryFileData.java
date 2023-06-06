/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.manager.configuration.file;

import com.redhat.rhn.common.util.SHA256Crypt;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.common.Checksum;
import com.redhat.rhn.domain.common.ChecksumFactory;
import com.redhat.rhn.domain.config.ConfigFileType;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.config.ConfigurationFactory;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


/**
 * BinaryFileData
 */
public class BinaryFileData extends ConfigFileData {
    private InputStream contents;
    private long contentSize;

    /**
     *
     * @param data the contents to set
     * @param size the contentSize to set
     */
    public BinaryFileData(InputStream data, long size) {
        super();
        setType(ConfigFileType.file());
        setContents(data);
        setContentSize(size);
        setMacroStart(null);
        setMacroEnd(null);
    }
    /**
     * @return the contents
     */
    public InputStream getContents() {
        return contents;
    }


    /**
     * @param data the contents to set
     */
    public void setContents(InputStream data) {
        this.contents = data;
    }


    /**
     *
     * {@inheritDoc}
     */
    @Override
    public long getContentSize() {
        return contentSize;
    }



    /**
     * @param size the contentSize to set
     */
    public void setContentSize(long size) {
        this.contentSize = size;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public InputStream getContentStream() {
        return getContents();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    protected void validateContents(ValidatorResult result, boolean onCreate) {
        // NO Op

    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public boolean isBinary() {
        return true;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void processRevisedContentFrom(ConfigRevision rev) {
        setContents(new ByteArrayInputStream(rev.
                            getConfigContent().getContents()));
        setContentSize(rev.getConfigContent().getFileSize());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("ConfigFileData", super.toString()).
                        append("Size", getContentSize());
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matchesRevision(ConfigRevision cRevision) {
        if (!super.matchesRevision(cRevision)) {
            return Boolean.FALSE;
        }
        byte[] bContent = ConfigurationFactory.bytesFromStream(getContents(),
                getContentSize());
        Checksum checksum = ChecksumFactory.safeCreate(SHA256Crypt.sha256Hex(bContent), "sha256");
        return checksum.equals(cRevision.getConfigContent().getChecksum());
    }
}
