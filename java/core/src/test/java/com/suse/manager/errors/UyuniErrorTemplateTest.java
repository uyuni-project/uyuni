/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.suse.common.errors.UyuniError;
import com.suse.common.errors.UyuniErrorCategory;

import org.junit.jupiter.api.Test;

public class UyuniErrorTemplateTest {

    @Test
    public void testRequired() {
        UyuniError error = UyuniErrorTemplate.REQUIRED.error("name");

        assertEquals(UyuniErrorCategory.INVALID_ARGUMENT, error.getCategory());
        assertEquals("name", error.getArgumentKey());
        assertEquals("name is required", error.getMessage());
    }

    @Test
    public void testIllegalValue() {
        UyuniError error = UyuniErrorTemplate.ILLEGAL_VALUE.error("port", "abc");

        assertEquals(UyuniErrorCategory.INVALID_ARGUMENT, error.getCategory());
        assertEquals("Invalid value for port: abc", error.getMessage());
    }

    @Test
    public void testNotFound() {
        UyuniError error = UyuniErrorTemplate.NOT_FOUND.error("serverId", 1000010001L);

        assertEquals(UyuniErrorCategory.NOT_FOUND, error.getCategory());
        assertEquals("serverId", error.getArgumentKey());
        assertEquals("serverId 1000010001 not found", error.getMessage());
    }

    @Test
    public void testAlreadyExists() {
        UyuniError error = UyuniErrorTemplate.ALREADY_EXISTS.error("label", "my-channel");

        assertEquals(UyuniErrorCategory.ALREADY_EXISTS, error.getCategory());
        assertEquals("label my-channel already exists", error.getMessage());
    }

    @Test
    public void testInUse() {
        UyuniError error = UyuniErrorTemplate.IN_USE.error("channel", "my-channel");

        assertEquals(UyuniErrorCategory.FAILED_PRECONDITION, error.getCategory());
        assertEquals("channel my-channel is still in use", error.getMessage());
    }

    @Test
    public void testDomainTemplateWithExistingMessage() {
        UyuniErrorTemplate labelInUse = new UyuniErrorTemplate(UyuniErrorCategory.ALREADY_EXISTS,
                "edit.channel.invalidchannellabel.labelinuse");

        UyuniError error = labelInUse.error("label", "my-channel");

        assertEquals(UyuniErrorCategory.ALREADY_EXISTS, error.getCategory());
        assertEquals("label", error.getArgumentKey());
        assertEquals("The channel label 'my-channel' is already in use, please enter a different name",
                error.getMessage());
    }
}
