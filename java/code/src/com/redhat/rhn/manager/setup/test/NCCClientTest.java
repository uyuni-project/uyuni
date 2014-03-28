/**
 * Copyright (c) 2014 SUSE
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
package com.redhat.rhn.manager.setup.test;

import com.redhat.rhn.manager.setup.MirrorCredentialsDto;
import com.redhat.rhn.manager.setup.NCCClient;
import com.redhat.rhn.manager.setup.NCCException;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.httpservermock.HttpServerMock;

import com.suse.manager.model.ncc.Subscription;

import java.util.Date;
import java.util.List;

/**
 * Tests for {@link NCCClient}.
 */
public class NCCClientTest extends RhnBaseTestCase {

    public void testDownloadSubscriptions() throws Exception {
        final MirrorCredentialsDto cred = new MirrorCredentialsDto("fpp@doamin.com", "lala", "ala");

        NCCRequester<List<Subscription>> requester = new NCCRequester<List<Subscription>>() {
            public List<Subscription> request(NCCClient nccClient) throws NCCException {
                return nccClient.downloadSubscriptions(cred);
            }
        };
       List<Subscription> subs = new HttpServerMock().getResult(requester, new NCCServerStub());
        System.out.println(cred);
        assertEquals(1, subs.size());
        Subscription s = subs.get(0);

        assertEquals("1", s.getSubid());
        assertEquals("1234", s.getRegcode());
        assertEquals("subname0", s.getSubname());
        assertEquals("Gold", s.getType());
        assertEquals("Turbo", s.getSubstatus());
        assertEquals(new Date(1333231200000L), s.getStartDate());
        assertEquals(new Date(1427839200000L), s.getEndDate());
        assertEquals(3, s.getDuration());
        assertEquals("Blade", s.getProductClass());
        assertEquals("Blade", s.getServerClass());
        assertEquals(10, s.getNodecount());
        assertEquals(2, s.getConsumed());
        assertEquals(3, s.getConsumedVirtual());
    }

}
