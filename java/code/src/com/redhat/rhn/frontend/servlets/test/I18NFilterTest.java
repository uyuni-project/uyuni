/*
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
package com.redhat.rhn.frontend.servlets.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.frontend.servlets.SetCharacterEncodingFilter;

import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * AuthFilterTest
 */
public class I18NFilterTest extends BaseFilterTst {

    private static final String UTF8 = "UTF-8";

    /** Test the CheckCharSet functionality
     * @throws Exception if something fails
     */
    @Test
    public void testCheckCharset() throws Exception {

        SetCharacterEncodingFilter filter = new SetCharacterEncodingFilter();

        try {
            filter.doFilter(request, response, chain);
        }
        catch (IOException ioe) {
            // This should never happen ..
            throw new Exception("doFilter() failed ..");
        }

        assertEquals(request.getCharacterEncoding(), UTF8);
        assertEquals(response.getCharacterEncoding(), UTF8);
    }

}
