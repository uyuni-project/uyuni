/*
 * Copyright (c) 2014 SUSE LLC
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
package com.redhat.rhn.domain.scc.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.common.ManagerInfoFactory;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Tests for {@link SCCCachingFactory}.
 */
public class SCCCachingFactoryTest extends BaseTestCaseWithUser {

    /**
     * Test if initially an empty list is returned.
     */
    @Test
    public void testRepositoriesEmpty() {
        List<SCCRepository> repos = SCCCachingFactory.lookupRepositories();
        assertTrue(repos.isEmpty());
    }

    /**
     * Test repository insertion and lookup.
     */
    @Test
    public void testRepositoriesInsertAndLookup() {
        SCCRepository repo0 = createTestRepo(0L);
        SCCRepository repo1 = createTestRepo(1L);
        SCCRepository repo2 = createTestRepo(2L);

        SCCCachingFactory.saveRepository(repo0);
        SCCCachingFactory.saveRepository(repo1);
        SCCCachingFactory.saveRepository(repo2);

        List<SCCRepository> repos = SCCCachingFactory.lookupRepositories();
        assertFalse(repos.isEmpty());
        assertEquals(repo0, repos.get(0));
        assertEquals(repo1, repos.get(1));
        assertEquals(repo2, repos.get(2));
    }

    /**
     * Test refreshNeeded().
     */
    @Test
    public void testRefreshNeeded() throws InterruptedException {
        for (SCCCredentials c : CredentialsFactory.listSCCCredentials()) {
            CredentialsFactory.removeCredentials(c);
        }
        SCCCredentials creds = CredentialsFactory.createSCCCredentials(TestUtils.randomString(),
            TestUtils.randomString());

        creds.setModified(new Date(System.currentTimeMillis()));
        HibernateFactory.getSession().save(creds);

        HibernateFactory.getSession().flush();

        TimeUnit.SECONDS.sleep(1);
        ManagerInfoFactory.setLastMgrSyncRefresh();
        Optional<Date> lastRefreshDate = ManagerInfoFactory.getLastMgrSyncRefresh();

        // no products synced - this should prevent setting the date
        assertTrue(lastRefreshDate.isEmpty(), "Last refresh date is unexpectedly set");

        SUSEProductTestUtils.createVendorSUSEProducts();
        ManagerInfoFactory.setLastMgrSyncRefresh();
        lastRefreshDate = ManagerInfoFactory.getLastMgrSyncRefresh();
        assertTrue(lastRefreshDate.isPresent(), "Last refresh date is unexpectedly empty");

        // Repos are newer than credentials -> no refresh
        assertFalse(SCCCachingFactory.refreshNeeded(lastRefreshDate));

        TimeUnit.SECONDS.sleep(1);

        // Newer credentials -> refresh
        creds.setModified(new Date(System.currentTimeMillis()));
        HibernateFactory.getSession().save(creds);

        HibernateFactory.getSession().flush();
        assertTrue(SCCCachingFactory.refreshNeeded(lastRefreshDate));
    }

    @Test
    public void testListReposForRootProduct() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(user, null, true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        Set<SCCRepository> repos = SCCCachingFactory.lookupRepositoriesByRootProductNameVersionArchForPayg(
                "sles", "12", "x86_64").collect(Collectors.toSet());
        List<String> repoNames = repos.stream().map(SCCRepository::getName).collect(Collectors.toList());
        assertContains(repoNames, "SUSE-PackageHub-12-Pool");
        assertContains(repoNames, "SLE-Module-Web-Scripting12-Pool");
        assertContains(repoNames, "SLES12-Updates");
        assertContains(repoNames, "SLE-Manager-Tools12-Pool");
    }
    /**
     * Repo for testing setting random strings and a given ID.
     * @return repository
     */
    private SCCRepository createTestRepo(Long id) {
        SCCRepository repo = new SCCRepository();
        repo.setSccId(id);
        repo.setDescription(TestUtils.randomString());
        repo.setDistroTarget(TestUtils.randomString());
        repo.setName(TestUtils.randomString());
        repo.setUrl(TestUtils.randomString());
        repo.setAutorefresh(true);
        return repo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        SCCCachingFactory.clearRepositories();
    }
}
