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
package com.redhat.rhn.manager.setup.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.test.ChannelFamilyTest;
import com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncSource;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.setup.MirrorCredentialsDto;
import com.redhat.rhn.manager.setup.MirrorCredentialsManager;
import com.redhat.rhn.manager.setup.MirrorCredentialsNotUniqueException;
import com.redhat.rhn.manager.setup.SubscriptionDto;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;
import com.redhat.rhn.testing.TestUtils;

import com.suse.cloud.CloudPaygManager;
import com.suse.cloud.test.TestCloudPaygManagerBuilder;
import com.suse.scc.model.SCCSubscriptionJson;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

/**
 * Tests for {@link MirrorCredentialsManager}.
 */
public class MirrorCredentialsManagerTest extends RhnMockStrutsTestCase {

    // Manager class instance
    private MirrorCredentialsManager credsManager;

    /**
     * Test findMirrorCredentials().
     */
    @Test
    public void testFindAllMirrorCreds() {
        MirrorCredentialsDto creds0 = storeTestCredentials();
        MirrorCredentialsDto creds1 = storeTestCredentials();
        MirrorCredentialsDto creds2 = storeTestCredentials();
        List<MirrorCredentialsDto> creds = credsManager.findMirrorCredentials();
        assertTrue(creds.size() >= 3);
        assertTrue(creds.contains(creds0));
        assertTrue(creds.contains(creds1));
        assertTrue(creds.contains(creds2));
    }

    /**
     * Test findMirrorCredentials() sort order.
     */
    @Test
    public void testFindMirrorCredentialsSortOrder() {
        // Store some credentials
        storeTestCredentials();
        storeTestCredentials();
        MirrorCredentialsDto primaryCreds = storeTestCredentials();
        storeTestCredentials();

        // Make one of them the primary
        credsManager.makePrimaryCredentials(primaryCreds.getId());
        List<MirrorCredentialsDto> creds = credsManager.findMirrorCredentials();

        // Remember the ID of the last iteration
        long lastId = -1;
        for (MirrorCredentialsDto c : creds) {
            // Primary should be first
            if (creds.indexOf(c) == 0) {
                assertEquals(primaryCreds, c);
            }
            // After that: ascending IDs
            else if (creds.indexOf(c) >= 2) {
                assertTrue(c.getId() > lastId);
            }
            lastId = c.getId();
        }
    }

    /**
     * Test findMirrorCredentials(long).
     */
    @Test
    public void testFindMirrorCredsById() {
        MirrorCredentialsDto creds0 = storeTestCredentials();
        MirrorCredentialsDto creds1 = storeTestCredentials();
        MirrorCredentialsDto creds2 = storeTestCredentials();
        assertEquals(creds0, credsManager.findMirrorCredentials(creds0.getId()));
        assertEquals(creds1, credsManager.findMirrorCredentials(creds1.getId()));
        assertEquals(creds2, credsManager.findMirrorCredentials(creds2.getId()));
    }

    /**
     * Test deleteMirrorCredentials().
     */
    @Test
    public void testDeleteCredentials() {
        MirrorCredentialsDto creds0 = storeTestCredentials();
        MirrorCredentialsDto creds1 = storeTestCredentials();
        int size = credsManager.findMirrorCredentials().size();
        assertTrue(size >= 2);
        credsManager.deleteMirrorCredentials(creds0.getId(), request);
        List<MirrorCredentialsDto> creds = credsManager.findMirrorCredentials();
        assertEquals(size - 1, creds.size());
        assertFalse(creds.contains(creds0));
        assertTrue(creds.contains(creds1));
    }

    /**
     * Test makePrimaryCredentials()
     */
    @Test
    public void testMakePrimaryCredentials() {
        MirrorCredentialsDto creds0 = storeTestCredentials();
        MirrorCredentialsDto creds1 = storeTestCredentials();
        MirrorCredentialsDto creds2 = storeTestCredentials();

        credsManager.makePrimaryCredentials(creds0.getId());
        assertTrue(credsManager.findMirrorCredentials(creds0.getId()).isPrimary());
        assertFalse(credsManager.findMirrorCredentials(creds1.getId()).isPrimary());
        assertFalse(credsManager.findMirrorCredentials(creds2.getId()).isPrimary());

        credsManager.makePrimaryCredentials(creds1.getId());
        assertFalse(credsManager.findMirrorCredentials(creds0.getId()).isPrimary());
        assertTrue(credsManager.findMirrorCredentials(creds1.getId()).isPrimary());
        assertFalse(credsManager.findMirrorCredentials(creds2.getId()).isPrimary());

        credsManager.makePrimaryCredentials(creds2.getId());
        assertFalse(credsManager.findMirrorCredentials(creds0.getId()).isPrimary());
        assertFalse(credsManager.findMirrorCredentials(creds1.getId()).isPrimary());
        assertTrue(credsManager.findMirrorCredentials(creds2.getId()).isPrimary());
    }

    @Test
    public void throwsExceptionIfCredentialsWithSameUsernameAlreadyExists() {
        MirrorCredentialsDto creds = storeTestCredentials();

        Exception ex = assertThrows(MirrorCredentialsNotUniqueException.class,
            () -> storeTestCredentials(creds.getUser(), creds.getPassword()));

        assertEquals("Username already exists", ex.getMessage());
    }

    @Test
    public void canRetrieveListOfSubscriptions() {
        MirrorCredentialsDto creds = storeTestCredentials();

        ChannelFamilyTest.ensureChannelFamilyExists(user, "MODULE", "SUSE Linux Enterprise Modules");
        ChannelFamilyTest.ensureChannelFamilyExists(user, "SLE-M-T", "SUSE Manager Tools");
        ChannelFamilyTest.ensureChannelFamilyExists(user, "SMS", "SUSE Manager Server");
        ChannelFamilyTest.ensureChannelFamilyExists(user, "SMP", "SUSE Manager Proxy");

        // Mock the content sync manager to return a known set of subscriptions
        CloudPaygManager cloudPaygManager = new TestCloudPaygManagerBuilder().build();
        ContentSyncManager contentSyncManager = new ContentSyncManager() {
            @Override
            public List<SCCSubscriptionJson> updateSubscriptions(ContentSyncSource source) throws ContentSyncException {
                return List.of(
                    new SCCSubscriptionJson("One", "ACTIVE", "2023-10-03T10:15:00.00Z", "2026-10-03T10:15:00.00Z",
                        List.of("MODULE")
                    ),
                    new SCCSubscriptionJson("two", "EXPIRED", "2022-10-03T08:10:23.00Z", "2023-10-03T17:15:30.00Z",
                        List.of("SLE-M-T")
                    ),
                    new SCCSubscriptionJson("three", "ACTIVE", "2023-06-20T00:00:00.00Z", "2025-06-20T00:00:00.00Z",
                        List.of()
                    ),
                    new SCCSubscriptionJson("four", "ACTIVE", "2020-01-01T12:30:00.00Z", "2030-01-01T12:30:00.00Z",
                        List.of("SMS", "SMP", "SMQ") // SMQ does not exist, code should use the label as is
                    )
                );
            }
        };

        credsManager = new MirrorCredentialsManager(cloudPaygManager, contentSyncManager);

        List<SubscriptionDto> subscriptions = credsManager.getSubscriptions(creds, request, true);

        assertNotNull(subscriptions);
        assertEquals(2, subscriptions.size()); // Only one and four should be included

        assertEquals(subscriptions.get(0).getName(), "SUSE Linux Enterprise Modules");
        assertEquals(
            Date.from(LocalDateTime.of(2023, 10, 3, 10, 15, 0, 0).atOffset(ZoneOffset.UTC).toInstant()),
            subscriptions.get(0).getStartDate()
        );
        assertEquals(
            Date.from(LocalDateTime.of(2026, 10, 3, 10, 15, 0, 0).atOffset(ZoneOffset.UTC).toInstant()),
            subscriptions.get(0).getEndDate()
        );

        assertEquals(subscriptions.get(1).getName(), "SUSE Manager Server OR SUSE Manager Proxy OR SMQ");
        assertEquals(
            Date.from(LocalDateTime.of(2020, 1, 1, 12, 30, 0, 0).atOffset(ZoneOffset.UTC).toInstant()),
            subscriptions.get(1).getStartDate()
        );
        assertEquals(
            Date.from(LocalDateTime.of(2030, 1, 1, 12, 30, 0, 0).atOffset(ZoneOffset.UTC).toInstant()),
            subscriptions.get(1).getEndDate()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        credsManager = new MirrorCredentialsManager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();

        // Tear down the manager class instance
        credsManager = null;
    }

    /**
     * Store test credentials.
     * @return a DTO representing the created credentials
     */
    private MirrorCredentialsDto storeTestCredentials() throws ContentSyncException {
        return storeTestCredentials("testuser-" + TestUtils.randomString(), "testpass-" + TestUtils.randomString());
    }

    /**
     * Store test credentials for a given id.
     *
     * @param userIn the username
     * @param passwordIn the password

     * @return a DTO representing the created credentials
     */
    private MirrorCredentialsDto storeTestCredentials(String userIn, String passwordIn) throws ContentSyncException {
        MirrorCredentialsDto creds = new MirrorCredentialsDto();
        creds.setUser(userIn);
        creds.setPassword(passwordIn);
        long dbId = credsManager.storeMirrorCredentials(creds, request);
        creds.setId(dbId);
        return creds;
    }
}
