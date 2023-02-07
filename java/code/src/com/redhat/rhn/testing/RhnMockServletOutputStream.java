/*
 * Copyright (c) 2009--2016 Red Hat, Inc.
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
package com.redhat.rhn.testing;

import com.redhat.rhn.frontend.servlets.LegacyServletOutputStream;

import java.io.IOException;

/**
 * RhnMockServletOutputStream - simple mock of an output stream
 */
public class RhnMockServletOutputStream extends LegacyServletOutputStream {
    private StringBuffer contents;

    /**
     * Default no arg constructor
     */
    public RhnMockServletOutputStream() {
        contents = new StringBuffer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void println(String s) throws IOException {
        super.println(s);
        contents.append(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] b) {
        contents.append(new String(b));
    }

    /**
     * Get what has been written to the outputstream
     * @return String contents
     */
    public String getContents() {
        return contents.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int b) {
        contents.append(b + "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] b, int off, int len) {
        String bytes = new String(b);
        contents.append(bytes.toCharArray(), off, len);
    }

}
