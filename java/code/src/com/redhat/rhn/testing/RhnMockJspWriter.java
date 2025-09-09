/*
 * Copyright (c) 2011--2025 SUSE LLC
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

/**
 * RhnMockJspWriter
 */
public class RhnMockJspWriter extends JspWriter {
    private StringWriter stringWriter = new StringWriter();
    private PrintWriter printWriter = new PrintWriter(stringWriter);

    /**
     * Default constructor
     */
    public RhnMockJspWriter() {
        super(0, true);
    }

    @Override
    public void write(char[] buf, int off, int len) throws IOException {
        printWriter.write(buf, off, len);
    }


    @Override
    public void newLine() throws IOException {
        printWriter.write(System.lineSeparator());
    }

    @Override
    public void print(boolean bIn) throws IOException {
        printWriter.write(String.valueOf(bIn));
    }

    @Override
    public void print(char cIn) throws IOException {
        printWriter.write(cIn);
    }

    @Override
    public void print(int iIn) throws IOException {
        printWriter.write(String.valueOf(iIn));
    }

    @Override
    public void print(long lIn) throws IOException {
        printWriter.write(String.valueOf(lIn));
    }

    @Override
    public void print(float vIn) throws IOException {
        printWriter.write(String.valueOf(vIn));
    }

    @Override
    public void print(double vIn) throws IOException {
        printWriter.print(String.valueOf(vIn));
    }

    @Override
    public void print(char[] charsIn) throws IOException {
        if (charsIn != null) {
            printWriter.write(charsIn);
        }
    }

    @Override
    public void print(String sIn) throws IOException {
        if (sIn != null) {
            printWriter.write(sIn);
        }
    }

    @Override
    public void print(Object oIn) throws IOException {
        if (oIn != null) {
            printWriter.write(oIn.toString());
        }
    }

    @Override
    public void println() throws IOException {
        printWriter.write(System.lineSeparator());
    }

    @Override
    public void println(boolean bIn) throws IOException {
        printWriter.write(String.valueOf(bIn));
        printWriter.write(System.lineSeparator());
    }

    @Override
    public void println(char cIn) throws IOException {
        printWriter.write(cIn);
        printWriter.write(System.lineSeparator());
    }

    @Override
    public void println(int iIn) throws IOException {
        printWriter.write(String.valueOf(iIn));
        printWriter.write(System.lineSeparator());
    }

    @Override
    public void println(long lIn) throws IOException {
        printWriter.write(String.valueOf(lIn));
        printWriter.write(System.lineSeparator());
    }

    @Override
    public void println(float vIn) throws IOException {
        printWriter.write(String.valueOf(vIn));
        printWriter.write(System.lineSeparator());
    }

    @Override
    public void println(double vIn) throws IOException {
        printWriter.write(String.valueOf(vIn));
        printWriter.write(System.lineSeparator());
    }

    @Override
    public void println(char[] charsIn) throws IOException {
        if (charsIn != null) {
            printWriter.write(charsIn);
        }
        printWriter.write(System.lineSeparator());
    }

    @Override
    public void println(String sIn) {
        if (sIn != null) {
            printWriter.write(sIn);
        }
        printWriter.write(System.lineSeparator());
    }

    @Override
    public void println(Object oIn) {
        printWriter.print(oIn);
    }

    @Override
    public void clear() throws IOException {
        // Clear both the buffer and the internal string writer
        StringBuffer buffer = stringWriter.getBuffer();
        buffer.setLength(0);
    }

    @Override
    public void clearBuffer() throws IOException {
        // Clear the internal string writer
        StringBuffer buffer = stringWriter.getBuffer();
        buffer.setLength(0);
    }

    @Override
    public void flush() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        printWriter.close();
        stringWriter.close();
    }

    @Override
    public int getRemaining() {
        return 0;
    }

    @Override
    public String toString() {
        return stringWriter.toString();
    }
    /**
     * Resets the writer to its initial state.
     */
    public void reset() {
        printWriter.flush();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }
}

