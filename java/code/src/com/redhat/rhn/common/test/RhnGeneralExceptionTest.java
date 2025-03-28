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

package com.redhat.rhn.common.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.RhnError;
import com.redhat.rhn.common.RhnGeneralException;

import org.junit.jupiter.api.Test;

import java.util.List;

public class RhnGeneralExceptionTest {

    @Test
    public void testGetErrors() {
        final String dummyError1 = "Error 1";
        final String dummyError2 = "Error 2";
        String[] expectedMessages = {dummyError1, dummyError2};

        RhnError error1 = new RhnError(dummyError1);
        RhnError error2 = new RhnError(dummyError2);

        List<RhnError> errors = List.of(error1, error2);
        RhnGeneralException exception = new RhnGeneralException(errors);

        assertEquals(errors, exception.getErrors());
        assertArrayEquals(expectedMessages, exception.getErrorMessages());
    }
}
