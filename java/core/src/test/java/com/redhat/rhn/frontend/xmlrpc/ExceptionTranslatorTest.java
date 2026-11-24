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

package com.redhat.rhn.frontend.xmlrpc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.suse.common.errors.UyuniError;
import com.suse.common.errors.UyuniErrorCategory;
import com.suse.common.errors.UyuniGeneralException;

import org.junit.jupiter.api.Test;

import java.util.List;

import redstone.xmlrpc.XmlRpcFault;

public class ExceptionTranslatorTest {

    @Test
    public void testUyuniErrorsBecomeValidationFault() {
        UyuniGeneralException report = new UyuniGeneralException(List.of(
                new UyuniError(UyuniErrorCategory.ALREADY_EXISTS, "label", "Label in use"),
                new UyuniError(UyuniErrorCategory.INVALID_ARGUMENT, "name", "Name too long")));

        XmlRpcFault fault = ExceptionTranslator.translateException(report);

        assertEquals(2800, fault.getErrorCode());
        assertEquals("Label in use. Name too long", fault.getMessage());
    }

    @Test
    public void testUnknownUyuniErrorBecomesInternalFault() {
        UyuniGeneralException report = new UyuniGeneralException(List.of(new UyuniError("Something failed")));

        XmlRpcFault fault = ExceptionTranslator.translateException(report);

        assertEquals(-1, fault.getErrorCode());
        assertEquals("unhandled internal exception: Something failed", fault.getMessage());
    }
}
