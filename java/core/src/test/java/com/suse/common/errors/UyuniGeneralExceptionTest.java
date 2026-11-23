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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.common.errors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.List;

public class UyuniGeneralExceptionTest {

    @Test
    public void testGetErrors() {
        final String dummyError1 = "Error 1";
        final String dummyError2 = "Error 2";
        String[] expectedMessages = {dummyError1, dummyError2};

        UyuniError error1 = new UyuniError(dummyError1);
        UyuniError error2 = new UyuniError(dummyError2);

        List<UyuniError> errors = List.of(error1, error2);
        UyuniGeneralException exception = new UyuniGeneralException(errors);

        assertEquals(errors, exception.getErrors());
        assertArrayEquals(expectedMessages, exception.getErrorMessages());
    }

    @Test
    public void testGetCategorySameCategory() {
        UyuniGeneralException exception = new UyuniGeneralException(List.of(
                new UyuniError(UyuniErrorCategory.NOT_FOUND, "serverId", "Server 1 not found"),
                new UyuniError(UyuniErrorCategory.NOT_FOUND, "channelId", "Channel 2 not found")));

        assertEquals(UyuniErrorCategory.NOT_FOUND, exception.getCategory());
    }

    @Test
    public void testGetCategoryMixedCategories() {
        UyuniGeneralException exception = new UyuniGeneralException(List.of(
                new UyuniError(UyuniErrorCategory.NOT_FOUND, "serverId", "Server 1 not found"),
                new UyuniError(UyuniErrorCategory.ALREADY_EXISTS, "label", "Label in use")));

        assertEquals(UyuniErrorCategory.INVALID_ARGUMENT, exception.getCategory());
    }

    @Test
    public void testGetCategoryWithUnknown() {
        UyuniGeneralException exception = new UyuniGeneralException(List.of(
                new UyuniError(UyuniErrorCategory.NOT_FOUND, "serverId", "Server 1 not found"),
                new UyuniError("Something failed")));

        assertEquals(UyuniErrorCategory.UNKNOWN, exception.getCategory());
    }
}
