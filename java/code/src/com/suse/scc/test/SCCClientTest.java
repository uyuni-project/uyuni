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

import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.testing.httpservermock.HttpServerMock;
import com.redhat.rhn.testing.httpservermock.Responder;

import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.client.SCCClientFactory;
import com.suse.scc.model.SCCProduct;
import com.suse.scc.model.SCCSubscription;
import com.suse.scc.model.SCCSystem;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import simple.http.Request;
import simple.http.Response;
import junit.framework.TestCase;

/**
 * Tests for {@link SCCClient} methods.
 */
public class SCCClientTest extends TestCase {

    /**
     * Test for {@link SCCWebClient#listProducts()}.
     */
    public void testListProducts() throws Exception {
        SCCRequester<List<SCCProduct>> requester =
                new SCCRequester<List<SCCProduct>>() {
                    @Override
                    public List<SCCProduct> request(SCCClient scc)
                            throws SCCClientException {
                        return scc.listProducts();
                    }
                };
        List<SCCProduct> products = new HttpServerMock().getResult(
                requester, new SCCServerStub());

        // Assertions
        assertEquals(2, products.size());
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

        // Enabled repositories
        List<Integer> enabledRepos = p.getEnabledRepositories();
        assertEquals(1, enabledRepos.size());
        assertEquals(true, enabledRepos.contains(1150));

        // Extensions
        List<SCCProduct> extensions = p.getExtensions();
        assertEquals(1, extensions.size());
        SCCProduct e = extensions.get(0);
        assertEquals(1145, e.getId());
        assertEquals("SUSE Linux Enterprise Server", e.getName());
        assertEquals("sle-sdk", e.getIdentifier());
        assertEquals("12", e.getVersion());
        assertEquals(null, e.getReleaseType());
        assertEquals("ppc64le", e.getArch());
        assertEquals("SUSE Linux Enterprise Software Development Kit 12 ppc64le",
                e.getFriendlyName());
        assertEquals(null, e.getProductClass());
        assertEquals("cpe:/o:suse:sle-sdk:12.0", e.getCpe());
        assertEquals(true, e.isFree());
        assertEquals(null, e.getDescription());
        assertEquals("https://nu.novell.com/repo/$RCE/SLE10-SDK-SP4-Online/" +
                "sles-10-x86_64.license/", e.getEulaUrl());
        assertEquals(0, e.getExtensions().size());
        assertEquals(1, e.getRepositories().size());

        // Repositories
        List<SCCRepository> repos = p.getRepositories();
        assertEquals(1, repos.size());
        SCCRepository r = repos.get(0);
        assertEquals(1357, r.getSCCId());

        // API v4 support (paging)
        SCCProduct p2 = products.get(1);
        assertEquals(43, p2.getId());
    }

    /**
     * Test for {@link SCCWebClient#listRepositories()}.
     */
    public void testListRepositories() throws Exception {
        SCCRequester<List<SCCRepository>> requester =
                new SCCRequester<List<SCCRepository>>() {
                    @Override
                    public List<SCCRepository> request(SCCClient scc)
                            throws SCCClientException {
                        return scc.listRepositories();
                    }
                };
        List<SCCRepository> repos = new HttpServerMock().getResult(
                requester, new SCCServerStub());

        // Assertions
        assertEquals(2, repos.size());
        SCCRepository r = repos.get(0);
        assertEquals(1358, r.getSCCId());
        assertEquals("SLE10-SDK-SP4-Online", r.getName());
        assertEquals("sles-10-i586", r.getDistroTarget());
        assertEquals("SLE10-SDK-SP4-Online for sles-10-i586", r.getDescription());
        assertEquals("https://nu.novell.com/repo/$RCE/SLE10-SDK-SP4-Online/sles-10-i586", r.getUrl());
        assertEquals(true, r.isAutorefresh());
    }

    /**
     * Test for {@link SCCWebClient#listSubscriptions()}.
     */
    public void testListSubscriptions() throws Exception {
        SCCRequester<List<SCCSubscription>> requester =
                new SCCRequester<List<SCCSubscription>>() {
                    @Override
                    public List<SCCSubscription> request(SCCClient scc)
                            throws SCCClientException {
                        return scc.listSubscriptions();
                    }
                };
        List<SCCSubscription> subs = new HttpServerMock().getResult(
                requester, new SCCServerStub());

        // Assertions
        assertEquals(2, subs.size());
        SCCSubscription s = subs.get(0);
        assertEquals(1, s.getId());
        assertEquals("631dc51f", s.getRegcode());
        assertEquals("Subscription 1", s.getName());
        assertEquals("FULL", s.getType());
        assertEquals("EXPIRED", s.getStatus());
        assertEquals(null, s.getStartsAt());
        assertEquals(DatatypeConverter.parseDateTime("2014-03-14T13:10:21.164Z").getTime(), s.getExpiresAt());
        assertEquals(new Integer(6), s.getSystemLimit());
        assertEquals(new Integer(1), s.getSystemsCount());
        assertEquals(null, s.getVirtualCount());

        // Product classes
        List<String> productClasses = s.getProductClasses();
        assertEquals(1, productClasses.size());
        assertEquals(true, productClasses.contains("SLES"));

        // Systems
        List<SCCSystem> systems = s.getSystems();
        assertEquals(1, systems.size());
        SCCSystem sys = systems.get(0);
        assertEquals(1, sys.getId());
        assertEquals("login1", sys.getLogin());
    }

    // File-based configuration
    private File createTempDir() throws IOException {
        File tmpDir = new File(System.getProperty("java.io.tmpdir") +
                               "/json-smt-scc-" + this.getClass().getCanonicalName());
        this.removeTempDir(tmpDir);
        tmpDir.mkdir();
        return tmpDir;
    }

    private void removeTempDir(File tmpDir) throws IOException {
        if (tmpDir.exists()) {
            FileUtils.forceDelete(tmpDir);
        }
    }

    /**
     * Test for {@link SCCWebClient#listRepositories()} but from the directory.
     * @throws java.lang.Exception
     */
    public void testListRepositoriesFromDirectory() throws Exception {
        File tmpDir = this.createTempDir();
        FileUtils.copyURLToFile(this.getClass().getResource(
                "/com/suse/scc/test/connect/organizations/repositories.json"),
                new File(tmpDir.getAbsolutePath() + "/organizations_repositories.json"));
        try {
            SCCClient scc = SCCClientFactory.getInstance(null, null, null,
                    tmpDir.getAbsolutePath(), null, null);
            List<SCCRepository> repos = scc.listRepositories();

            // Assertions
            assertEquals(1, repos.size());
            SCCRepository r = repos.get(0);
            assertEquals(1358, r.getSCCId());
            assertEquals("SLE10-SDK-SP4-Online", r.getName());
            assertEquals("sles-10-i586", r.getDistroTarget());
            assertEquals("SLE10-SDK-SP4-Online for sles-10-i586", r.getDescription());
            assertEquals("https://nu.novell.com/repo/$RCE/SLE10-SDK-SP4-Online/sles-10-i586", r.getUrl());
            assertEquals(true, r.isAutorefresh());
        } finally {
            this.removeTempDir(tmpDir);
        }
    }

    /**
     * Test for SCC error responses.
     * @throws Exception if things go wrong
     */
    public void testErrorResponse() throws Exception {
        Responder errorResponder = new Responder() {
            @Override
            public void respond(Request requestIn, Response responseIn) {
                responseIn.setCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
                try {
                    responseIn.getPrintStream().close();
                }
                catch (IOException e) {
                    // never happens
                }
            }
        };

        SCCRequester<List<SCCProduct>> requester = new SCCRequester<List<SCCProduct>>() {
            @Override
            public List<SCCProduct> request(SCCClient scc) throws SCCClientException {
                try {
                    scc.listProducts();
                    fail("Did not get an exception, expected error 500");
                    return null;
                }
                catch (SCCClientException e) {
                    assertTrue(e.getMessage().contains("500"));
                    throw e;
                }
            }
        };

        new HttpServerMock().getResult(requester, errorResponder);
    }
}
