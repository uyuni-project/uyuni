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
import java.io.Reader;
import java.io.Writer;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;

public class MockBodyContent extends BodyContent {
    private JspWriter jspWriter;
    private Writer writer;
    private String text;

    /**
     * Constructor with initial text content.
     * @param textIn the initial text content
     */
    public MockBodyContent(String textIn) {
        super(null);
        text = textIn;
    }

    /**
     * Default constructor.
     */
    public MockBodyContent() {
        super(null);
    }

    /**
     * Gets the buffer size.
     * @return the buffer size
     */
    public int getBufferSize() {
        notImplemented();
        return super.getBufferSize();
    }

    /**
     * Writes a single character.
     * @param c the character to write
     * @throws IOException if an I/O error occurs
     */
    public void write(int c) throws IOException {
        notImplemented();
        super.write(c);
    }

    /**
     * Flushes the stream.
     * @throws IOException if an I/O error occurs
     */
    public void flush() throws IOException {
        notImplemented();
        super.flush();
    }

    /**
     * Checks if auto flush is enabled.
     * @return true if auto flush is enabled
     */
    public boolean isAutoFlush() {
        notImplemented();
        return super.isAutoFlush();
    }

    /**
     * Writes an array of characters.
     * @param cbuf the character array to write
     * @throws IOException if an I/O error occurs
     */
    public void write(char []cbuf) throws IOException {
        notImplemented();
        super.write(cbuf);
    }

    /**
     * Compares this object with another for equality.
     * @param obj the object to compare with
     * @return true if objects are equal
     */
    public boolean equals(Object obj) {
        notImplemented();
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        notImplemented();
        return super.hashCode();
    }

    /**
     * Clears the body content.
     */
    public void clearBody() {
        notImplemented();
        super.clearBody();
    }

    /**
     * Writes a string.
     * @param str the string to write
     * @throws IOException if an I/O error occurs
     */
    public void write(String str) throws IOException {
        notImplemented();
        super.write(str);
    }

    /**
     * Sets up the enclosing writer.
     * @param jspWriterIn the JSP writer to set as enclosing writer
     */
    public void setupGetEnclosingWriter(JspWriter jspWriterIn) {
        jspWriter = jspWriterIn;
    }

    /**
     * Gets the enclosing writer.
     * @return the enclosing JSP writer
     */
    public JspWriter getEnclosingWriter() {
        return jspWriter;
    }

    private void notImplemented() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets a reader for the content.
     * @return null (not implemented)
     */
    public Reader getReader() {
        notImplemented();
        return null;
    }

    /**
     * Writes a line separator.
     * @throws IOException if an I/O error occurs
     */
    public void newLine() throws IOException {
        notImplemented();
    }

    /**
     * Writes a portion of a character array.
     * @param cbuf the character array
     * @param off the start offset
     * @param len the number of characters to write
     * @throws IOException if an I/O error occurs
     */
    public void write(char []cbuf, int off, int len) throws IOException {
        notImplemented();
    }

    /**
     * Prints a boolean value.
     * @param b the boolean value to print
     * @throws IOException if an I/O error occurs
     */
    public void print(boolean b) throws IOException {
        notImplemented();
    }


    /**
     * Writes the content to the specified writer.
     * @param writerIn the writer to write to
     * @throws IOException if an I/O error occurs
     */
    public void writeOut(Writer writerIn) throws IOException {
        writer = writerIn;
    }

    /**
     * Prints a character.
     * @param c the character to print
     * @throws IOException if an I/O error occurs
     */
    public void print(char c) throws IOException {
        notImplemented();
    }

    /**
     * Prints an integer.
     * @param i the integer to print
     * @throws IOException if an I/O error occurs
     */
    public void print(int i) throws IOException {
        notImplemented();
    }

    /**
     * Prints a long value.
     * @param l the long value to print
     * @throws IOException if an I/O error occurs
     */
    public void print(long l) throws IOException {
        notImplemented();
    }

    /**
     * Prints a float value.
     * @param v the float value to print
     * @throws IOException if an I/O error occurs
     */
    public void print(float v) throws IOException {
        notImplemented();
    }

    /**
     * Prints a double value.
     * @param v the double value to print
     * @throws IOException if an I/O error occurs
     */
    public void print(double v) throws IOException {
        notImplemented();
    }

    /**
     * Prints a character array.
     * @param chars the character array to print
     * @throws IOException if an I/O error occurs
     */
    public void print(char[] chars) throws IOException {
        notImplemented();
    }

    /**
     * Prints a string.
     * @param s the string to print
     * @throws IOException if an I/O error occurs
     */
    public void print(String s) throws IOException {
        notImplemented();
    }

    /**
     * Prints an object.
     * @param o the object to print
     * @throws IOException if an I/O error occurs
     */
    public void print(Object o) throws IOException {
        notImplemented();
    }

    /**
     * Prints a line separator.
     * @throws IOException if an I/O error occurs
     */
    public void println() throws IOException {
        notImplemented();
    }

    /**
     * Prints a boolean value followed by a line separator.
     * @param b the boolean value to print
     * @throws IOException if an I/O error occurs
     */
    public void println(boolean b) throws IOException {
        notImplemented();
    }

    /**
     * Prints a character followed by a line separator.
     * @param c the character to print
     * @throws IOException if an I/O error occurs
     */
    public void println(char c) throws IOException {
        notImplemented();
    }

    /**
     * Prints an integer followed by a line separator.
     * @param i the integer to print
     * @throws IOException if an I/O error occurs
     */
    public void println(int i) throws IOException {
        notImplemented();
    }

    /**
     * Prints a long value followed by a line separator.
     * @param l the long value to print
     * @throws IOException if an I/O error occurs
     */
    public void println(long l) throws IOException {
        notImplemented();
    }

    /**
     * Prints a float value followed by a line separator.
     * @param v the float value to print
     * @throws IOException if an I/O error occurs
     */
    public void println(float v) throws IOException {
        notImplemented();
    }

    /**
     * Prints a double value followed by a line separator.
     * @param v the double value to print
     * @throws IOException if an I/O error occurs
     */
    public void println(double v) throws IOException {
        notImplemented();
    }

    /**
     * Prints a character array followed by a line separator.
     * @param chars the character array to print
     * @throws IOException if an I/O error occurs
     */
    public void println(char[] chars) throws IOException {
        notImplemented();
    }

    /**
     * Prints a string followed by a line separator.
     * @param s the string to print
     * @throws IOException if an I/O error occurs
     */
    public void println(String s) throws IOException {
        notImplemented();
    }

    /**
     * Prints an object followed by a line separator.
     * @param o the object to print
     * @throws IOException if an I/O error occurs
     */
    public void println(Object o) throws IOException {
        notImplemented();
    }

    /**
     * Clears the stream.
     * @throws IOException if an I/O error occurs
     */
    public void clear() throws IOException {
        notImplemented();
    }

    /**
     * Clears the buffer.
     * @throws IOException if an I/O error occurs
     */
    public void clearBuffer() throws IOException {
        notImplemented();
    }

    /**
     * Closes the stream.
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
        notImplemented();
    }

    /**
     * Gets the remaining buffer space.
     * @return the remaining buffer space
     */
    public int getRemaining() {
        notImplemented();
        return 0;
    }

    /**
     * Writes a portion of a string.
     * @param str the string to write
     * @param off the start offset
     * @param len the number of characters to write
     * @throws IOException if an I/O error occurs
     */
    public void write(String str, int off, int len) throws IOException {
        notImplemented();
        super.write(str, off, len);
    }

    /**
     * Gets the string content.
     * @return the string content
     */
    public String getString() {
        return text;
    }

    /**
     * Gets the text content.
     * @return the text content
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text content.
     * @param textIn the text content to set
     */
    public void setText(String textIn) {
        text = textIn;
    }
}
