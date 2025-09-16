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

package com.redhat.rhn.common.security.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.security.HMAC;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

/*
 * Test for HMAC
 *
 */
public class HMACTest extends RhnBaseTestCase {

    public void doTestSHA256(String data, String key, String expect) {

        String value = HMAC.sha256(data, key);
        assertEquals(expect, value);
    }

    @Test
    public void testDataKeySHA256() throws Exception {
        doTestSHA256("data", "key", "5031fe3d989c6d1537a013fa6e739da23463fdaec3b70137d828e36ace221bd0");

    }

    @Test
    public void testLongKeySHA256() throws Exception {
        doTestSHA256("data",
      "this is a very long key to see if that breaks the implementation, xxxx",
                   "cfb516736c4f9353b463c4de0d24b35d3c523e75810c6d8cb9320c7ab73a77ce");
    }

}
