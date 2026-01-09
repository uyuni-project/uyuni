/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.frontend.servlets.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.frontend.servlets.SetCharacterEncodingFilter;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;

/**
 * AuthFilterTest
 */
public class I18NFilterTest extends BaseFilterTest {

    /**
     * Test the CheckCharSet functionality
     *
     * @throws ServletException if something fails
     * @throws IOException if something fails
     */
    @Test
    public void testCheckCharset() throws ServletException, IOException {
        context.checking(new Expectations() {{
            oneOf(chain).doFilter(request, response);
        }});

        new SetCharacterEncodingFilter().doFilter(request, response, chain);

        assertEquals(request.getCharacterEncoding(), StandardCharsets.UTF_8.name());
        assertEquals(response.getCharacterEncoding(), StandardCharsets.UTF_8.name());
    }

}
