/*
 * Copyright (c) 2024 SUSE LLC
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
package com.suse.manager.webui.controllers.test;


import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class SimpleTestingResponse implements HttpServletResponse {

    private int status;
    private String contentType;

    @Override
    public void addCookie(Cookie cookie) {
        throw new NotImplementedException("");
    }

    @Override
    public boolean containsHeader(String s) {
        throw new NotImplementedException("");
    }

    @Override
    public String encodeURL(String s) {
        throw new NotImplementedException("");
    }

    @Override
    public String encodeRedirectURL(String s) {
        throw new NotImplementedException("");
    }

    @Override
    public String encodeUrl(String s) {
        throw new NotImplementedException("");
    }

    @Override
    public String encodeRedirectUrl(String s) {
        throw new NotImplementedException("");
    }

    @Override
    public void sendError(int i, String s) throws IOException {
        throw new NotImplementedException("");
    }

    @Override
    public void sendError(int i) throws IOException {
        throw new NotImplementedException("");
    }

    @Override
    public void sendRedirect(String s) throws IOException {
        throw new NotImplementedException("");
    }

    @Override
    public void setDateHeader(String s, long l) {
        throw new NotImplementedException("");
    }

    @Override
    public void addDateHeader(String s, long l) {
        throw new NotImplementedException("");
    }

    @Override
    public void setHeader(String s, String s1) {
        throw new NotImplementedException("");
    }

    @Override
    public void addHeader(String s, String s1) {
        throw new NotImplementedException("");
    }

    @Override
    public void setIntHeader(String s, int i) {
        throw new NotImplementedException("");
    }

    @Override
    public void addIntHeader(String s, int i) {
        throw new NotImplementedException("");
    }

    @Override
    public void setStatus(int i) {
        this.status = i;
    }

    @Override
    public void setStatus(int i, String s) {
        this.status = i;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getHeader(String s) {
        throw new NotImplementedException("");
    }

    @Override
    public Collection<String> getHeaders(String s) {
        throw new NotImplementedException("");
    }

    @Override
    public Collection<String> getHeaderNames() {
        throw new NotImplementedException("");
    }

    @Override
    public String getCharacterEncoding() {
        throw new NotImplementedException("");
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        throw new NotImplementedException("");
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        throw new NotImplementedException("");
    }

    @Override
    public void setCharacterEncoding(String s) {
        throw new NotImplementedException("");
    }

    @Override
    public void setContentLength(int i) {
        throw new NotImplementedException("");
    }

    @Override
    public void setContentLengthLong(long l) {
        throw new NotImplementedException("");
    }

    @Override
    public void setContentType(String s) {
        this.contentType = s;
    }

    @Override
    public void setBufferSize(int i) {
        throw new NotImplementedException("");
    }

    @Override
    public int getBufferSize() {
        throw new NotImplementedException("");
    }

    @Override
    public void flushBuffer() throws IOException {
        throw new NotImplementedException("");
    }

    @Override
    public void resetBuffer() {
        throw new NotImplementedException("");
    }

    @Override
    public boolean isCommitted() {
        throw new NotImplementedException("");
    }

    @Override
    public void reset() {
        throw new NotImplementedException("");
    }

    @Override
    public void setLocale(Locale locale) {
        throw new NotImplementedException("");
    }

    @Override
    public Locale getLocale() {
        throw new NotImplementedException("");
    }
}
