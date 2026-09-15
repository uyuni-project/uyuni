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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.frontend.taglibs.list;

import com.redhat.rhn.frontend.servlets.LegacyServletOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Buffers servlet output rather than streaming it to the client
 *
 */
class BufferedServletOutputStream extends LegacyServletOutputStream {

    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    /**
     * ${@inheritDoc}
     */
    @Override
    public void write(int b) {
        buffer.write(b);
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        buffer.flush();
    }

    /**
     * Gets buffered content as UTF-8 encoded string
     * @return String
     */
    public String getBufferedContent() {
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
