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
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.cloud.CloudPaygManager;
import com.suse.cloud.CspBillingAdapterStatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CloudPaygManagerTest extends BaseTestCaseWithUser {

    private static class ContentSyncManagerTestHelper extends ContentSyncManager {
        private boolean isSCCCredentialsResult = true;
        public void setIsSCCCredentialsResult(boolean resultIn) {
            isSCCCredentialsResult = resultIn;
        }

        @Override
        public boolean isSCCCredentials(SCCCredentials c) {
            return isSCCCredentialsResult;
        }
    }
    private static class TaskomaticApiTestHelper extends TaskomaticApi {
        private Optional<Map<String, String>> result;
        private Optional<TaskomaticApiException> exception;

        public void setNull() {
            this.result = Optional.empty();
            this.exception = Optional.empty();
        }

        public void setException(TaskomaticApiException exIn) {
            this.exception = Optional.of(exIn);
            this.result = Optional.empty();
        }

        public void setResult(Map<String, String> resultIn) {
            this.result = Optional.of(resultIn);
            this.exception = Optional.empty();
        }

        public Map lookupScheduleByBunchAndLabel(User user, String bunchName, String scheduleLabel)
                throws TaskomaticApiException  {
            if (result.isPresent()) {
                return result.get();
            }
            else if (exception.isPresent()) {
                throw exception.get();
            }
            else {
                return null;
            }
        }
    }

    public static class CloudPaygManagerTestHelper extends CloudPaygManager {

        private String iType = "BYOS";
        private String reqResult = "";
        private boolean serviceRunning = true;
        private boolean packageModified = false;

        public CloudPaygManagerTestHelper() throws IOException, ClassNotFoundException {
            super();
            setCspBillingAdapterStatusFile("csp-config.ok.json");
        }
        public CloudPaygManagerTestHelper(TaskomaticApi tapiIn, ContentSyncManager syncManagerIn)
                throws IOException, ClassNotFoundException {
            super(tapiIn, syncManagerIn);
            setCspBillingAdapterStatusFile("csp-config.ok.json");
        }

        @Override
        protected boolean isFileExecutable(String filename) {
            return filename.equals("/usr/bin/instance-flavor-check") ||
                    filename.equals("/usr/bin/ec2metadata");
        }

        public void setInstanceType(String iTypeIn) {
            iType = iTypeIn;
        }

        public void setRequestResult(String reqResultIn) {
            reqResult = reqResultIn;
        }

        public void setServiceRunning(boolean isRunning) {
            serviceRunning = isRunning;
        }

        public void setPackageModified(boolean pkgModifiedIn) {
            packageModified = pkgModifiedIn;
        }

        protected String getInstanceType() {
            return iType;
        }
        @Override
        protected String requestUrl(String url) {
            return reqResult;
        }
        @Override
        protected boolean isServiceRunning(String serviceIn) {
            return serviceRunning;
        }
        @Override
        protected boolean hasPackageModifications(String pkg) {
            return packageModified;
        }
        private void setCspBillingAdapterStatusFile(String testFileName) throws IOException, ClassNotFoundException {
            String jarPath = "/com/suse/cloud/test/data/";
            cspBillingAdapterConfig = new File(TestUtils.findTestData(jarPath + testFileName).getPath()).toPath();
        }
    }

    @Test
    public void testCloudProvider() {
        CloudPaygManager cpm = new CloudPaygManager();
        CloudPaygManager.CloudProvider prv = cpm.getCloudProvider();
        assertEquals(CloudPaygManager.CloudProvider.None, prv);

        CloudPaygManager cpmAWS = new CloudPaygManager() {
            @Override
            protected boolean isFileExecutable(String filename) {
                return filename.equals("/usr/bin/ec2metadata");
            }
        };
        assertEquals(CloudPaygManager.CloudProvider.AWS, cpmAWS.getCloudProvider());

        CloudPaygManager cpmAzure = new CloudPaygManager() {
            @Override
            protected boolean isFileExecutable(String filename) {
                return filename.equals("/usr/bin/azuremetadata");
            }
        };
        assertEquals(CloudPaygManager.CloudProvider.AZURE, cpmAzure.getCloudProvider());

        CloudPaygManager cpmGce = new CloudPaygManager() {
            @Override
            protected boolean isFileExecutable(String filename) {
                return filename.equals("/usr/bin/gcemetadata");
            }
        };
        assertEquals(CloudPaygManager.CloudProvider.GCE, cpmGce.getCloudProvider());
    }

    @Test
    public void testIsPayg() throws IOException, ClassNotFoundException {
        CloudPaygManagerTestHelper cpm = new CloudPaygManagerTestHelper();
        cpm.setInstanceType("PAYG");
        assertTrue(cpm.isPaygInstance(), "Expecting a PAYG instance");
    }

    @Test
    public void testRefresh() throws IOException, ClassNotFoundException {
        TaskomaticApiTestHelper tapi = new TaskomaticApiTestHelper();
        tapi.setResult(new HashMap<>());
        CloudPaygManagerTestHelper cpm = new CloudPaygManagerTestHelper(tapi, new ContentSyncManager());
        cpm.setInstanceType("PAYG");
        cpm.setRequestResult("online");

        assertTrue(cpm.checkRefreshCache(false), "Not refreshed");

        assertTrue(cpm.isPaygInstance(), "Not a PAYG instance");
        assertFalse(cpm.hasSCCCredentials(), "Has SCC credentials");
        assertEquals(CloudPaygManager.CloudProvider.AWS, cpm.getCloudProvider());
        assertTrue(cpm.isCompliant(), "Is not compliant");
    }

    @Test
    public void testRefreshIsCompliant() throws IOException, ClassNotFoundException {
        TaskomaticApiTestHelper tapi = new TaskomaticApiTestHelper();
        tapi.setNull();

        CloudPaygManagerTestHelper cpm = new CloudPaygManagerTestHelper(tapi, new ContentSyncManager());
        cpm.setInstanceType("PAYG");
        cpm.setRequestResult("online");
        cpm.setServiceRunning(true);
        cpm.setPackageModified(false);

        // test 1 - no schedule available
        assertTrue(cpm.checkRefreshCache(false), "Not refreshed");
        assertFalse(cpm.isCompliant(), "Unexpected: Is compliant");


        // test 2 - taskomatic api throw exception
        tapi.setException(new TaskomaticApiException(new Exception("failed")));
        assertTrue(cpm.checkRefreshCache(false), "Not refreshed");
        assertFalse(cpm.isCompliant(), "Unexpected: Is compliant");

        // test 3 - active schedule exists
        tapi.setResult(new HashMap<String, String>());
        assertTrue(cpm.isCompliant(), "Unexpected: Is not compliant");
        // test cache is used
        assertFalse(cpm.checkRefreshCache(false), "Unexpected: refresh happened");

        // test 4 - billing-data-service is down
        cpm.setRequestResult("error");
        assertTrue(cpm.checkRefreshCache(true), "Not refreshed");
        assertFalse(cpm.isCompliant(), "Unexpected: Is compliant");

        // test 5 - packages are modified
        cpm.setRequestResult("online");
        cpm.setPackageModified(true);
        assertTrue(cpm.checkRefreshCache(false), "Not refreshed");
        assertFalse(cpm.isCompliant(), "Unexpected: Is compliant");

        // test 6 - billing adapter is down
        cpm.setPackageModified(false);
        cpm.setServiceRunning(false);
        assertTrue(cpm.checkRefreshCache(false), "Not refreshed");
        assertFalse(cpm.isCompliant(), "Unexpected: Is compliant");

        cpm.setServiceRunning(true);
        assertTrue(cpm.checkRefreshCache(false), "Not refreshed");
        assertTrue(cpm.isCompliant(), "Unexpected: Is not compliant");

        // test 7 - billing adapter report errors
        cpm.setCspBillingAdapterStatusFile("csp-config.nonet.json");
        assertTrue(cpm.checkRefreshCache(true), "Not refreshed");
        assertFalse(cpm.isCompliant(), "Unexpected: Is compliant");

    }

    @Test
    public void testHasSCCCredentials() {

        ContentSyncManagerTestHelper csm = new ContentSyncManagerTestHelper();
        CloudPaygManager cpm = new CloudPaygManager(new TaskomaticApi(), csm);

        // test 1 - no credentials available
        assertFalse(cpm.hasSCCCredentials(), "Has SCC credentials");

        // test 2 - credentials available identifies as SCC credentials
        csm.setIsSCCCredentialsResult(true);
        SCCCredentials credentials = CredentialsFactory.createSCCCredentials("dummy", "dummy");
        credentials.setUrl("dummy");
        CredentialsFactory.storeCredentials(credentials);

        assertTrue(cpm.checkRefreshCache(true), "Not refreshed");
        assertTrue(cpm.hasSCCCredentials(), "Has no SCC credentials");

        CredentialsFactory.removeCredentials(credentials);

        // test 3 - no credentials available
        assertTrue(cpm.checkRefreshCache(true), "Not refreshed");
        assertFalse(cpm.hasSCCCredentials(), "Has SCC credentials");

        // test 4 - credentials available not identified as SCC credentials
        csm.setIsSCCCredentialsResult(false);
        credentials = CredentialsFactory.createSCCCredentials("mf_user", "dummy");
        CredentialsFactory.storeCredentials(credentials);

        assertTrue(cpm.checkRefreshCache(true), "Not refreshed");
        assertFalse(cpm.hasSCCCredentials(), "Has SCC credentials");
    }

    @Test
    public void testCspBillingAdapterStatusParsing() throws IOException, ClassNotFoundException {
        Gson gson = new GsonBuilder().setDateFormat(CspBillingAdapterStatus.DATE_FORMAT).create();
        String jarPath = "/com/suse/cloud/test/data/";
        CspBillingAdapterStatus cspStatus = gson.fromJson(Files.readString(
                new File(TestUtils.findTestData(jarPath + "csp-config.ok.json").getPath()).toPath()),
                    CspBillingAdapterStatus.class);
        assertTrue(cspStatus.isBillingApiAccessOk(), "Unexpected error: status should be ok");
        assertTrue(cspStatus.getErrors().isEmpty(), "Unexpected errors found");
        assertEquals(1692881466L, cspStatus.getTimestamp().toInstant().getEpochSecond());

        cspStatus = gson.fromJson(Files.readString(
                        new File(TestUtils.findTestData(jarPath + "csp-config.nocreds.json").getPath()).toPath()),
                CspBillingAdapterStatus.class);
        assertFalse(cspStatus.isBillingApiAccessOk(), "Unexpected error: status should be false");
        assertContains(cspStatus.getErrors(),
                "Failed to meter bill dimension managed_systems: Unable to locate credentials");
        assertContains(cspStatus.getErrors(),
                "Failed to meter bill dimension monitoring: Unable to locate credentials");
        assertEquals(1692708633L, cspStatus.getTimestamp().toInstant().getEpochSecond());

    }
}
