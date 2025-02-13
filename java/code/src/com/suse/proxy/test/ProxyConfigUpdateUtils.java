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

package com.suse.proxy.test;

import static com.redhat.rhn.common.ExceptionMessage.NOT_INSTANTIABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.RhnError;

import com.suse.proxy.update.ProxyConfigUpdateContext;

import java.util.List;

public class ProxyConfigUpdateUtils {

    private ProxyConfigUpdateUtils() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }

    /**
     * Asserts the expected error messages are present in the context
     *
     * @param expectedErrorMessages the expected error messages array string
     * @param context               the context
     */
    public static void assertExpectedErrors(String[] expectedErrorMessages, ProxyConfigUpdateContext context) {
        assertTrue(context.getErrorReport().hasErrors());
        assertEquals(expectedErrorMessages.length, context.getErrorReport().getErrors().size());

        List<String> actualErrorMessages =
                context.getErrorReport().getErrors().stream().map(RhnError::getMessage).toList();
        assertTrue(actualErrorMessages.containsAll(List.of(expectedErrorMessages)));
    }

}
