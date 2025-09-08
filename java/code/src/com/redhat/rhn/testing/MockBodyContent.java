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

import com.mockobjects.util.AssertMo;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;

public class MockBodyContent extends BodyContent {
    private JspWriter jspWriter;
    private Writer writer;
    private String text;

    public MockBodyContent(String textIn) {
        super(null);
        text = textIn;
    }

    public MockBodyContent() {
        super(null);
    }

    public int getBufferSize() {
        notImplemented();
        return super.getBufferSize();
    }

    public void write(int c) throws IOException {
        notImplemented();
        super.write(c);
    }

    public void flush() throws IOException {
        notImplemented();
        super.flush();
    }

    public boolean isAutoFlush() {
        notImplemented();
        return super.isAutoFlush();
    }

    public void write(char cbuf[]) throws IOException {
        notImplemented();
        super.write(cbuf);
    }

    public boolean equals(Object obj) {
        notImplemented();
        return super.equals(obj);
    }

    public void clearBody() {
        notImplemented();
        super.clearBody();
    }

    public void write(String str) throws IOException {
        notImplemented();
        super.write(str);
    }

    public void setupGetEnclosingWriter(JspWriter aJspWriter) {
        jspWriter = aJspWriter;
    }

    public JspWriter getEnclosingWriter() {
        return jspWriter;
    }

    private void notImplemented() {
        AssertMo.notImplemented(MockBodyContent.class.getName());
    }

    public Reader getReader() {
        notImplemented();
        return null;
    }

    public void newLine() throws IOException {
        notImplemented();
    }

    public void write(char cbuf[], int off, int len) throws IOException {
        notImplemented();
    }

    public void print(boolean b) throws IOException {
        notImplemented();
    }


    public void writeOut(Writer aWriter) throws IOException {
        writer = aWriter;
    }

    public void print(char c) throws IOException {
        notImplemented();
    }

    public void print(int i) throws IOException {
        notImplemented();
    }

    public void print(long l) throws IOException {
        notImplemented();
    }

    public void print(float v) throws IOException {
        notImplemented();
    }

    public void print(double v) throws IOException {
        notImplemented();
    }

    public void print(char[] chars) throws IOException {
        notImplemented();
    }

    public void print(String s) throws IOException {
        notImplemented();
    }

    public void print(Object o) throws IOException {
        notImplemented();
    }

    public void println() throws IOException {
        notImplemented();
    }

    public void println(boolean b) throws IOException {
        notImplemented();
    }

    public void println(char c) throws IOException {
        notImplemented();
    }

    public void println(int i) throws IOException {
        notImplemented();
    }

    public void println(long l) throws IOException {
        notImplemented();
    }

    public void println(float v) throws IOException {
        notImplemented();
    }

    public void println(double v) throws IOException {
        notImplemented();
    }

    public void println(char[] chars) throws IOException {
        notImplemented();
    }

    public void println(String s) throws IOException {
        notImplemented();
    }

    public void println(Object o) throws IOException {
        notImplemented();
    }

    public void clear() throws IOException {
        notImplemented();
    }

    public void clearBuffer() throws IOException {
        notImplemented();
    }

    public void close() throws IOException {
        notImplemented();
    }

    public int getRemaining() {
        notImplemented();
        return 0;
    }

    public void write(String str, int off, int len) throws IOException {
        notImplemented();
        super.write(str, off, len);
    }

    public String getString() {
        return text;
    }

    public String getText() {
        return text;
    }

    public void setText(String textIn) {
        text = textIn;
    }
}
