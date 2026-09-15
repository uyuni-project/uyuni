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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.common.util.download;

import org.apache.struts.actions.DownloadAction.StreamInfo;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


/**
 * ByteArrayStreamInfo
 * Used by CsvDownloadAction and DownloadFile for downloading text files
 * @author jmatthews
 */
public class ByteArrayStreamInfo implements StreamInfo {

    protected String contentType;
    protected byte[] bytes;

    /**
     * Constructor
     * @param conType ContentType
     * @param data byte array of data
     */
    public ByteArrayStreamInfo(String conType, byte[] data) {
        this.contentType = conType;
        this.bytes = data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContentType() {
        return contentType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(bytes);
    }
}
