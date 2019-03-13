/**
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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.common.ManagerInfoFactory;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import java.util.Date;
import java.util.List;

/**
 * Tests for {@link SCCCachingFactory}.
 */
public class SCCCachingFactoryTest extends RhnBaseTestCase {

    /**
     * Test if initially an empty list is returned.
     */
    public void testRepositoriesEmpty() {
        List<SCCRepository> repos = SCCCachingFactory.lookupRepositories();
        assertTrue(repos.isEmpty());
    }

    /**
     * Test repository insertion and lookup.
     */
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
    public void testRefreshNeeded() {
        for (Credentials c : CredentialsFactory.lookupSCCCredentials()) {
            CredentialsFactory.removeCredentials(c);
        }
        Credentials creds = CredentialsFactory.createSCCCredentials();
        creds.setUsername(TestUtils.randomString());
        creds.setPassword(TestUtils.randomString());
        creds.setModified(new Date(System.currentTimeMillis()));
        HibernateFactory.getSession().save(creds);

        ManagerInfoFactory.setLastMgrSyncRefresh();
        // Repos are newer than credentials -> no refresh
        assertFalse(SCCCachingFactory.refreshNeeded());

        // Newer credentials -> refresh
        creds.setModified(new Date(System.currentTimeMillis()));
        HibernateFactory.getSession().save(creds);
        assertTrue(SCCCachingFactory.refreshNeeded());
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
    protected void setUp() throws Exception {
        super.setUp();
        SCCCachingFactory.clearRepositories();
    }
}
