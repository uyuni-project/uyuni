/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.scc.test.registration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.domain.scc.SCCRegCacheItem;

import com.suse.scc.registration.SCCSystemRegistrationContext;
import com.suse.scc.registration.SCCSystemRegistrationSystemDataAcquisitor;

import org.junit.jupiter.api.Test;

public class SCCSystemRegistrationSystemDataAcquisitorTest extends AbstractSCCSystemRegistrationTest {

    /**
     * Tests when no systems are provided.
     * In this case no systems should be added to context.getPendingRegistrationSystems().
     */
    @Test
    public void testSuccessSCCSystemRegistrationSystemDataAcquisitorWhenNoSystemsProvided() throws Exception {
        // setup
        this.setupSystems(0, 0);
        final SCCSystemRegistrationContext context = new SCCSystemRegistrationContext(null, getTestSystems(), null);

        // pre-conditions
        assertEquals(0, context.getItems().size());
        assertEquals(0, context.getPendingRegistrationSystems().size());
        assertEquals(0, context.getItemsBySccSystemId().size());

        // execution
        new SCCSystemRegistrationSystemDataAcquisitor().handle(context);

        // assertions
        assertEquals(0, context.getPendingRegistrationSystems().size());
        assertEquals(0, context.getItemsBySccSystemId().size());
    }

    /**
     * Test success when 20 systems are provided and 5 of them are PayG.
     * In this case 15 systems should be added to context.getPendingRegistrationSystems() and 5 to
     * context.getPaygSystems().
     * At this point all systems should be marked as requiring registration.
     */
    @Test
    public void testSuccessSCCSystemRegistrationSystemDataAcquisitor() throws Exception {
        // setup
        this.setupSystems(15, 5);
        final SCCSystemRegistrationContext context = new SCCSystemRegistrationContext(null, getTestSystems(), null);

        // pre-conditions
        assertEquals(20, context.getItems().size());
        assertEquals(20, context.getItems().stream().filter(SCCRegCacheItem::isSccRegistrationRequired).count());
        assertEquals(0, context.getPendingRegistrationSystems().size());
        assertEquals(0, context.getItemsBySccSystemId().size());
        assertEquals(0, context.getPaygSystems().size());

        // execution
        new SCCSystemRegistrationSystemDataAcquisitor().handle(context);

        // assertions
        assertEquals(20, context.getItems().stream().filter(SCCRegCacheItem::isSccRegistrationRequired).count());
        assertEquals(15, context.getPendingRegistrationSystems().size());
        assertEquals(context.getPendingRegistrationSystems().keySet(), context.getItemsBySccSystemId().keySet());
        assertEquals(5, context.getPaygSystems().size());
    }

}
