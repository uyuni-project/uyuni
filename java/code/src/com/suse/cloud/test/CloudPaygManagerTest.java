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
package com.suse.cloud.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.cloud.CloudPaygManager;
import com.suse.cloud.CloudProvider;

import org.junit.jupiter.api.Test;

import java.util.Map;

public class CloudPaygManagerTest extends BaseTestCaseWithUser {

    @Test
    public void testCloudProvider() {
        CloudPaygManager cpm = new TestCloudPaygManagerBuilder().build();
        CloudProvider prv = cpm.getCloudProvider();
        assertEquals(CloudProvider.None, prv);

        CloudPaygManager cpmAWS =  new TestCloudPaygManagerBuilder().withCloudProvider(CloudProvider.AWS).build();
        assertEquals(CloudProvider.AWS, cpmAWS.getCloudProvider());

        CloudPaygManager cpmAzure = new TestCloudPaygManagerBuilder().withCloudProvider(CloudProvider.AZURE).build();
        assertEquals(CloudProvider.AZURE, cpmAzure.getCloudProvider());

        CloudPaygManager cpmGce = new TestCloudPaygManagerBuilder().withCloudProvider(CloudProvider.GCE).build();
        assertEquals(CloudProvider.GCE, cpmGce.getCloudProvider());
    }

    @Test
    public void testIsPayg() {
        CloudPaygManager cpm = new TestCloudPaygManagerBuilder()
            .withPaygInstance()
            .build();

        assertTrue(cpm.isPaygInstance(), "Expecting a PAYG instance");
    }

    @Test
    public void testRefresh() {
        CloudPaygManager cpm = new TestCloudPaygManagerBuilder()
            .withDimensionComputationScheduled(Map.of())
            .withPaygInstance()
            .withBillingDataServiceStatus("online")
            .build();

        assertTrue(cpm.checkRefreshCache(false), "Not refreshed");

        assertTrue(cpm.isPaygInstance(), "Not a PAYG instance");
        assertFalse(cpm.hasSCCCredentials(), "Has SCC credentials");
        assertEquals(CloudProvider.AWS, cpm.getCloudProvider());
        assertTrue(cpm.isCompliant(), "Is not compliant");
    }

    @Test
    public void testRefreshIsCompliant() {
        // test 1 - no schedule available
        CloudPaygManager cpm = new TestCloudPaygManagerBuilder()
            .withPaygInstance()
            .withDimensionComputationScheduleMissing()
            .build();

        assertTrue(cpm.checkRefreshCache(false), "Not refreshed");
        assertFalse(cpm.isCompliant(), "Unexpected: Is compliant");


        // test 2 - taskomatic api throw exception
        cpm =  new TestCloudPaygManagerBuilder()
            .withPaygInstance()
            .withDimensionComputationCheckThrowingException()
            .build();

        assertTrue(cpm.checkRefreshCache(false), "Not refreshed");
        assertFalse(cpm.isCompliant(), "Unexpected: Is compliant");

        // test 3 - active schedule exists
        cpm = new TestCloudPaygManagerBuilder()
            .withPaygInstance()
            .withDimensionComputationScheduled(Map.of())
            .build();

        assertTrue(cpm.isCompliant(), "Unexpected: Is not compliant");
        // test cache is used
        assertFalse(cpm.checkRefreshCache(false), "Unexpected: refresh happened");

        // test 4 - billing-data-service is down
        cpm = new TestCloudPaygManagerBuilder()
            .withPaygInstance()
            .withBillingDataServiceStatus("error")
            .build();

        assertTrue(cpm.checkRefreshCache(true), "Not refreshed");
        assertFalse(cpm.isCompliant(), "Unexpected: Is compliant");

        // test 5 - packages are modified
        cpm = new TestCloudPaygManagerBuilder()
            .withPaygInstance()
            .withModifiedPackages()
            .build();

        assertTrue(cpm.checkRefreshCache(false), "Not refreshed");
        assertFalse(cpm.isCompliant(), "Unexpected: Is compliant");

        // test 6 - billing adapter is down
        cpm = new TestCloudPaygManagerBuilder()
            .withPaygInstance()
            .withBillingAdapterDown()
            .build();

        assertTrue(cpm.checkRefreshCache(false), "Not refreshed");
        assertFalse(cpm.isCompliant(), "Unexpected: Is compliant");

        // test 7 - billing adapter report errors
        cpm = new TestCloudPaygManagerBuilder()
            .withPaygInstance()
            .withBillingAdapterRunning(false)
            .build();

        assertTrue(cpm.checkRefreshCache(false), "Not refreshed");
        assertFalse(cpm.isCompliant(), "Unexpected: Is compliant");

        cpm = new TestCloudPaygManagerBuilder()
            .withPaygInstance()
            .build();

        assertTrue(cpm.checkRefreshCache(false), "Not refreshed");
        assertTrue(cpm.isCompliant(), "Unexpected: Is not compliant");
    }

    @Test
    public void testHasSCCCredentials() {

        // test 1 - no credentials available
        CloudPaygManager cpm = new TestCloudPaygManagerBuilder().build();
        assertFalse(cpm.hasSCCCredentials(), "Has SCC credentials");

        // test 2 - credentials available identifies as SCC credentials
        cpm = new TestCloudPaygManagerBuilder()
            .withSCCCredentials()
            .build();

        SCCCredentials credentials = CredentialsFactory.createSCCCredentials("dummy", "dummy");
        credentials.setUrl("dummy");
        CredentialsFactory.storeCredentials(credentials);

        assertTrue(cpm.checkRefreshCache(true), "Not refreshed");
        assertTrue(cpm.hasSCCCredentials(), "Has no SCC credentials");

        CredentialsFactory.removeCredentials(credentials);

        // test 3 - no credentials available
        cpm = new TestCloudPaygManagerBuilder()
            .withSCCCredentials()
            .build();

        assertTrue(cpm.checkRefreshCache(true), "Not refreshed");
        assertFalse(cpm.hasSCCCredentials(), "Has SCC credentials");

        // test 4 - credentials available not identified as SCC credentials
        cpm = new TestCloudPaygManagerBuilder()
            .withoutSCCCredentials()
            .build();

        credentials = CredentialsFactory.createSCCCredentials("mf_user", "dummy");
        CredentialsFactory.storeCredentials(credentials);

        assertTrue(cpm.checkRefreshCache(true), "Not refreshed");
        assertFalse(cpm.hasSCCCredentials(), "Has SCC credentials");
    }
}
