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

package com.redhat.rhn.testing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class MockServletOutputStream extends ServletOutputStream {
    private ByteArrayOutputStream buffer;

    /**
     * Default constructor
     */
    public MockServletOutputStream() {
        super();
        setupClearContents();
    }


    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListenerIn) {
    }

    /**
     * @see java.lang.Object#toString()
     * @return the contents as a string
     */
    public String toString() {
        return getContents();
    }

    /**
     * Writes a specified byte to the output stream.
     * @param b   the {@code byte}.
     * @throws IOException
     */
    public void write(int b) throws IOException {
        buffer.write(b);
    }

    /**
     * Clears the current contents of the output stream.
     */
    public void setupClearContents() {
        buffer = new ByteArrayOutputStream();
    }

    public String getContents() {
        return buffer.toString();
    }

}
