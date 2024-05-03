/*
 * Copyright (c) 2024 SUSE LLC
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

import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.cloud.CloudPaygManager;
import com.suse.cloud.CloudProvider;
import com.suse.cloud.PaygComplainceInfo;
import com.suse.salt.netapi.utils.Xor;

import java.util.Map;

public class TestCloudPaygManagerBuilder {

    private CloudProvider cloudProvider;
    private boolean payg;
    private String billingDataServiceStatus;
    private boolean serviceRunning;
    private boolean packageModified;
    private Xor<TaskomaticApiException, Map<String, Object>> dimensionComputationSchedule;
    private boolean sccCredentials;

    /**
     * Default constructor. Builds a CloudPaygManager that is on BYOS instance, it's by default compliant for PAYG
     * and has SCC Credentials.
     */
    public TestCloudPaygManagerBuilder() {
        withCloudProvider(CloudProvider.None)
            .withByosInstance()
            .withBillingDataServiceStatus("online")
            .withServiceRunning()
            .withOriginalPackages()
            .withDimensionComputationScheduled(Map.of())
            .withSCCCredentials();
    }

    public TestCloudPaygManagerBuilder withCloudProvider(CloudProvider providerIn) {
        cloudProvider = providerIn;
        return this;
    }

    public TestCloudPaygManagerBuilder withPaygInstance() {
        payg = true;

        // If it's payg don't use None as Provider
        if (cloudProvider == CloudProvider.None) {
            cloudProvider = CloudProvider.AWS;
        }

        return this;
    }

    public TestCloudPaygManagerBuilder withByosInstance() {
        payg = false;
        return this;
    }

    public TestCloudPaygManagerBuilder withBillingDataServiceStatus(String status) {
        billingDataServiceStatus = status;
        return this;
    }

    public TestCloudPaygManagerBuilder withoutServiceRunning() {
        serviceRunning = false;
        return this;
    }

    public TestCloudPaygManagerBuilder withServiceRunning() {
        serviceRunning = true;
        return this;
    }

    private TestCloudPaygManagerBuilder withOriginalPackages() {
        packageModified = false;
        return this;
    }

    public TestCloudPaygManagerBuilder withModifiedPackages() {
        packageModified = true;
        return this;
    }

    public TestCloudPaygManagerBuilder withDimensionComputationScheduleMissing() {
        dimensionComputationSchedule = Xor.right(null);
        return this;
    }

    public TestCloudPaygManagerBuilder withDimensionComputationScheduled(Map<String, Object> resultMap) {
        dimensionComputationSchedule = Xor.right(resultMap);
        return this;
    }

    public TestCloudPaygManagerBuilder withDimensionComputationCheckThrowingException() {
        dimensionComputationSchedule = Xor.left(new TaskomaticApiException(new Exception()));
        return this;
    }

    public TestCloudPaygManagerBuilder withSCCCredentials() {
        sccCredentials = true;
        return this;
    }

    public TestCloudPaygManagerBuilder withoutSCCCredentials() {
        sccCredentials = false;
        return this;
    }

    public CloudPaygManager build() {
        ContentSyncManager syncManager = new ContentSyncManager() {
            @Override
            public boolean isSCCCredentials(SCCCredentials c) {
                return sccCredentials;
            }
        };

        TaskomaticApi taskoApi = new TaskomaticApi() {
            @Override
            public Map<String, Object> lookupScheduleByBunchAndLabel(User user, String bunchName, String scheduleLabel)
                throws TaskomaticApiException {
                if (dimensionComputationSchedule.isRight()) {
                    return dimensionComputationSchedule.orElse(null);
                }
                else if (dimensionComputationSchedule.isLeft()) {
                    throw dimensionComputationSchedule.left().orElse(new TaskomaticApiException(new Exception()));
                }

                return null;
            }
        };

        return new CloudPaygManager(taskoApi, syncManager) {
            @Override
            protected String requestUrl(String url) {
                return billingDataServiceStatus;
            }

            @Override
            protected PaygComplainceInfo getInstanceComplianceInfo() {
                return new PaygComplainceInfo(
                    cloudProvider, payg, packageModified, serviceRunning
                );
            }
        };
    }
}
