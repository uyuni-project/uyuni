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
package com.suse.scc.test;

import com.redhat.rhn.testing.httpservermock.HttpServerMock;

import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.client.SCCConfig;
import com.suse.scc.model.SCCProduct;

import java.util.List;

import junit.framework.TestCase;

/**
 * Tests for {@link SCCClient} methods.
 */
public class SCCClientTest extends TestCase {

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Use HTTP when running those tests
        SCCConfig.getInstance().put(SCCConfig.SCHEMA, "http://");
    }

    public void testListProducts() throws Exception {
        SCCRequester<List<SCCProduct>> requester =
                new SCCRequester<List<SCCProduct>>() {
                    @Override
                    public List<SCCProduct> request(SCCClient sccClient)
                            throws SCCClientException {
                        return sccClient.listProducts();
                    }
                };
        String filename = "products.json";
        List<SCCProduct> products =
                new HttpServerMock().getResult(requester, new SCCServerStub(filename));

        // Assertions
        assertEquals(1, products.size());
        SCCProduct p = products.get(0);
        assertEquals(42, p.getId());
        assertEquals("SUSE Linux Enterprise Server", p.getName());
        assertEquals("SUSE_SLES", p.getIdentifier());
        assertEquals("11", p.getVersion());
        assertEquals("GA", p.getReleaseType());
        assertEquals("x86_64", p.getArch());
        assertEquals("SUSE Linux Enterprise Server 11 x86_64", p.getFriendlyName());
        assertEquals("7261", p.getProductClass());
        assertEquals("cpe:/o:suse:sled-addon:12.0", p.getCpe());
        assertEquals(false, p.isFree());
        assertEquals(null, p.getDescription());
        assertEquals("https://nu.novell.com/SUSE:/Products:/SLE-12/images/repo/" +
                "SLE-12-Server-POOL-x86_64-Media.license/", p.getEulaUrl());
        // TODO: Verify extensions and repositories
    }
}
