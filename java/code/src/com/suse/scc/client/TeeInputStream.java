/**
 * Copyright (c) 2014--2015 SUSE LLC
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
/**
 * Original code from The Legion Of The Bouncy Castle Inc.
 *
 * The Bouncy Castle License Copyright (c) 2000-2014 The Legion Of The Bouncy Castle
 * Inc. (http://www.bouncycastle.org)
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.suse.scc.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * InputStream proxy that transparently writes a copy of all bytes read from the
 * proxied stream to a given OutputStream.
 *
 * Shamessly copied from the Bouncy Castle implementation, this class should be
 * removed as soon as Commons IO is upgraded to at least 1.4 which has a
 * virtually equivalent implementation.
 */
public class TeeInputStream extends InputStream {

    /** The input. */
    private final InputStream input;

    /** The output. */
    private final OutputStream output;

    /**
     * Creates a TeeInputStream that proxies the given {@link InputStream}
     * and copies all read bytes to the given {@link OutputStream}. The given
     * output stream will be closed when this stream gets closed.
     *
     * @param inputIn input stream to be proxied
     * @param outputIn output stream that will receive a copy of all bytes read
     */
    public TeeInputStream(InputStream inputIn, OutputStream outputIn) {
        this.input = inputIn;
        this.output = outputIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        int i = input.read(buf, off, len);

        if (i > 0) {
            output.write(buf, off, i);
        }

        return i;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        int i = input.read();

        if (i >= 0) {
            output.write(i);
        }

        return i;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.input.close();
        this.output.close();
    }
}
