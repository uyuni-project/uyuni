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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.jsp.JspWriter;

public class MockJspWriter extends JspWriter {
    private StringWriter stringWriter = new StringWriter();
    private PrintWriter printWriter = new PrintWriter(stringWriter);

    /**
     * Default constructor
     */
    public MockJspWriter() {
        super(0, true);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        stringWriter.write(cbuf, off, len);
    }


    @Override
    public void newLine() throws IOException {
        stringWriter.write(System.lineSeparator());
    }

    @Override
    public void print(boolean bIn) throws IOException {
        stringWriter.write(String.valueOf(bIn));
    }

    @Override
    public void print(char cIn) throws IOException {
        stringWriter.write(cIn);
    }

    @Override
    public void print(int iIn) throws IOException {
        stringWriter.write(String.valueOf(iIn));
    }

    @Override
    public void print(long lIn) throws IOException {
        stringWriter.write(String.valueOf(lIn));
    }

    @Override
    public void print(float vIn) throws IOException {
        stringWriter.write(String.valueOf(vIn));
    }

    @Override
    public void print(double vIn) throws IOException {
        stringWriter.write(String.valueOf(vIn));
    }

    @Override
    public void print(char[] charsIn) throws IOException {
        if (charsIn != null) {
            stringWriter.write(charsIn);
        }
    }

    @Override
    public void print(String sIn) throws IOException {
        if (sIn != null) {
            stringWriter.write(sIn);
        }
    }

    @Override
    public void print(Object oIn) throws IOException {
        if (oIn != null) {
            stringWriter.write(oIn.toString());
        }
    }

    @Override
    public void println() throws IOException {
        stringWriter.write(System.lineSeparator());
    }

    @Override
    public void println(boolean bIn) throws IOException {
        stringWriter.write(String.valueOf(bIn));
        stringWriter.write(System.lineSeparator());
    }

    @Override
    public void println(char cIn) throws IOException {
        stringWriter.write(cIn);
        stringWriter.write(System.lineSeparator());
    }

    @Override
    public void println(int iIn) throws IOException {
        stringWriter.write(String.valueOf(iIn));
        stringWriter.write(System.lineSeparator());
    }

    @Override
    public void println(long lIn) throws IOException {
        stringWriter.write(String.valueOf(lIn));
        stringWriter.write(System.lineSeparator());
    }

    @Override
    public void println(float vIn) throws IOException {
        stringWriter.write(String.valueOf(vIn));
        stringWriter.write(System.lineSeparator());
    }

    @Override
    public void println(double vIn) throws IOException {
        stringWriter.write(String.valueOf(vIn));
        stringWriter.write(System.lineSeparator());
    }

    @Override
    public void println(char[] charsIn) throws IOException {
        if (charsIn != null) {
            stringWriter.write(charsIn);
        }
        stringWriter.write(System.lineSeparator());
    }

    @Override
    public void println(String sIn) throws IOException {
        if (sIn != null) {
            stringWriter.write(sIn);
        }
        stringWriter.write(System.lineSeparator());
    }

    @Override
    public void println(Object oIn) throws IOException {
        if (oIn != null) {
            stringWriter.write(oIn.toString());
        }
        stringWriter.write(System.lineSeparator());
    }

    @Override
    public void clear() throws IOException {
        // Clear both the buffer and the internal string writer
        StringBuffer buffer = stringWriter.getBuffer();
        buffer.setLength(0);
    }

    @Override
    public void clearBuffer() throws IOException {
        // Clear just the buffer contents - in JSP context this typically means clear without throwing exception
        StringBuffer buffer = stringWriter.getBuffer();
        if (!buffer.isEmpty()) {
            buffer.setLength(0);
        }
    }

    @Override
    public void flush() throws IOException {
        // For a mock implementation, flushing is a no-op as we're writing to memory
        printWriter.flush();
    }

    @Override
    public void close() throws IOException {
        // Close the underlying writers
        printWriter.close();
        stringWriter.close();
    }

    @Override
    public int getRemaining() {
        return 0;
    }

    /**
     * Get the content written to this writer as a string.
     * This is useful for testing JSP output.
     * @return the content written so far
     */
    public String getContent() {
        printWriter.flush();
        return stringWriter.toString();
    }

    /**
     * Reset the writer, clearing all content.
     * This is a convenience method for testing.
     */
    public void reset() {
        printWriter.flush();
        StringBuffer buffer = stringWriter.getBuffer();
        buffer.setLength(0);
    }
}

