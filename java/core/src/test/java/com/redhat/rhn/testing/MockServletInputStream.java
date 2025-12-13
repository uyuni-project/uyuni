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

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

public class MockServletInputStream extends ServletInputStream {

    private byte[] data;
    private int index;

    /**
     * Sets up the input stream to read the provided data
     * @param dataIn the data to be read
     */
    public void setupRead(byte[] dataIn) {
        this.data = dataIn;
    }

    /**
     * Reads the next byte of data from the input stream.
     * @return the next byte of data, or -1 if the end of the stream is reached
     */
    public int read() {
        if (data != null && index < data.length) {
            return data[index++];
        }
        else {
            return -1;
        }
    }

    @Override
    public boolean isFinished() {
        return index >= (data != null ? data.length : 0);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListenerIn) {
        throw new UnsupportedOperationException();
    }
}
