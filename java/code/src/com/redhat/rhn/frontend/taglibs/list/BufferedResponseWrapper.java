/*
 * Copyright (c) 2009--2017 Red Hat, Inc.
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

package com.redhat.rhn.frontend.taglibs.list;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Buffers servlet output
 */
class BufferedResponseWrapper extends HttpServletResponseWrapper {

   private BufferedServletOutputStream out = new BufferedServletOutputStream();
   private PrintWriter writer = new PrintWriter(out);

   /**
    * ${@inheritDoc}
    */
    BufferedResponseWrapper(HttpServletResponse target) {
        super(target);
    }


    /**
     * ${@inheritDoc}
     */
    @Override
    public ServletOutputStream getOutputStream() {
        return out;
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public PrintWriter getWriter() {
        return writer;
    }

    /**
     * ${@inheritDoc}
     */
    public void flush() throws IOException {
        writer.flush();
        out.flush();
    }

    /**
     * Gets buffered content from underlying output stream
     * @return string
     */
    public String getBufferedOutput() {
        return out.getBufferedContent();
    }
}
