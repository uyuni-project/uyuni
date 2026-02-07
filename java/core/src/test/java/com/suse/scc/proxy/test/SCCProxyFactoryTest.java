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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.scc.proxy.test;

import com.redhat.rhn.testing.RhnBaseTestCase;

import com.suse.scc.proxy.SCCProxyFactory;
import com.suse.scc.proxy.SccProxyStatus;

import org.junit.jupiter.api.Test;

public class SCCProxyFactoryTest extends RhnBaseTestCase {

    @Test
    public void generatedCoverageTestLookupByStatus() {
        // this test has been generated programmatically to test SCCProxyFactory.lookupByStatus
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        SCCProxyFactory testObject = new SCCProxyFactory();
        SccProxyStatus arg0 = SccProxyStatus.SCC_CREATION_PENDING;
        testObject.lookupByStatus(arg0);
    }
}
