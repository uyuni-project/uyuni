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
package com.suse.cloud;

import com.redhat.rhn.common.util.http.HttpClientAdapter;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * Public Cloud PAYG management class
 */
public class CloudPaygManager {
    private static final Logger LOG = LogManager.getLogger(CloudPaygManager.class);

    private static final Path PAYG_COMPLIANCE_INFO_JSON = Path.of("/var/cache/rhn/payg_compliance.json");
    private static final int VALIDITY_MINUTES = 11;

    private final Gson gson = new GsonBuilder().create();

    private boolean isCompliant;
    private PaygComplainceInfo complainceInfo;
    private Boolean hasSCCCredentials;
    private Instant cacheTime;

    private final TaskomaticApi tapi;
    private final ContentSyncManager mgr;

    /**
     * Constructor
     */
    public CloudPaygManager() {
        tapi = new TaskomaticApi();
        mgr = new ContentSyncManager(null, this);
        cacheTime = Instant.MIN;
        hasSCCCredentials = null;
        complainceInfo = null;
        isCompliant = false;
    }

    /**
     * Constructor
     * @param tapiIn the taskomatic api object
     * @param syncManagerIn the content sync manager object
     */
    public CloudPaygManager(TaskomaticApi tapiIn, ContentSyncManager syncManagerIn) {
        tapi = tapiIn;
        mgr = syncManagerIn;
        cacheTime = Instant.MIN;
        hasSCCCredentials = null;
        complainceInfo = null;
        isCompliant = false;
    }

    /**
     * @return if this is a PAYG cloud instance
     */
    public boolean isPaygInstance() {
        checkRefreshCache(false);
        return complainceInfo.isPaygInstance();
    }

    /**
     * @return the CloudProvider
     */
    public CloudProvider getCloudProvider() {
        checkRefreshCache(false);
        return complainceInfo.getCloudProvider();
    }

    /**
     * @return return true when we have SCC credentials
     */
    public boolean hasSCCCredentials() {
        checkRefreshCache(false);
        if (hasSCCCredentials == null) {
            hasSCCCredentials = detectHasSCCCredentials();
        }
        return hasSCCCredentials;
    }

    /**
     * Check if SUSE Manager PAYG is operating in a compliant mode
     * @return true if not payg or all components works as they should, otherwise false
     */
    public boolean isCompliant() {
        checkRefreshCache(false);
        return isCompliant;
    }

    /**
     * Check and perform a refresh of the stored data if needed
     * @param force if true, force a refresh independent of the time
     * @return true when a refresh happened, otherwise false
     */
    public boolean checkRefreshCache(boolean force) {
        if (complainceInfo != null &&
                !complainceInfo.isPaygInstance() &&
                complainceInfo.getCloudProvider() == CloudProvider.None &&
                Files.isReadable(PAYG_COMPLIANCE_INFO_JSON)) {
            force = true;
        }
        // if isCompliant is false, we re-detect
        if (force || !isCompliant || Duration.between(cacheTime, Instant.now()).toHours() >= 1) {
            // cached values older than 1 hour - time to refresh
            hasSCCCredentials = detectHasSCCCredentials();
            isCompliant = detectIsCompliant();
            cacheTime = Instant.now();
            return true;
        }
        return false;
    }

    private boolean detectHasSCCCredentials() {
        return CredentialsFactory.listSCCCredentials().stream()
                .anyMatch(mgr::isSCCCredentials);
    }

    private boolean detectIsCompliant() {
        // Parse the compliance info from the external json file, if available
        complainceInfo = getInstanceComplianceInfo();

        // Verify the information is correctly updated
        if (Duration.between(complainceInfo.getTimestamp(), Instant.now()).toMinutes() > VALIDITY_MINUTES) {
            LOG.error("The instance compliance info is not up-to-date.");
            return false;
        }

        // If it's not payg, it's always compliant
        if (!complainceInfo.isPaygInstance()) {
            return true;
        }

        // If the external compliance tool report it's not ok, it cannot be compliant
        if (!complainceInfo.isCompliant()) {
            LOG.error("The instance has been reported as non-compliant.");
            if (complainceInfo.isAnyPackageModified()) {
                LOG.error("Some packages have been modified and cannot be trusted.");
            }

            if (!complainceInfo.isBillingAdapterRunning()) {
                LOG.error("The CSP Billing Adapter service is not active.");
            }
            else if (!complainceInfo.isBillingAdapterHealthy()) {
                LOG.error("The CSP Billing Adapter service has errors.");
            }

            if (!complainceInfo.isMeteringAccessible()) {
                LOG.error("Billing Data Service is not accessible.");
            }

            return false;
        }

        // Check if the payg-dimension-computation job is active
        try {
            if (tapi.lookupScheduleByBunchAndLabel(null, "payg-dimension-computation-bunch",
                    "payg-dimension-computation-default") == null) {
                LOG.error("payg-dimension-computation job is not active.");
                return false;
            }
        }
        catch (TaskomaticApiException e) {
            LOG.error("Unable to check if payg-dimension-computation job is active.");
            LOG.info(e.getMessage(), e);
            return false;
        }

        // Check if the billing data service is up and running
        try {
            if (!requestUrl("http://localhost:18888/").equals("online")) {
                LOG.error("Billing Data Service is not active.");
                return false;
            }
        }
        catch (IOException e) {
            LOG.error("Unable to contact the Billing Data Service.", e);
            return false;
        }

        return true;
    }

    protected PaygComplainceInfo getInstanceComplianceInfo() {
        // If no compliance info is available, it means the instance is BYOS
        if (!Files.isReadable(PAYG_COMPLIANCE_INFO_JSON)) {
            return new PaygComplainceInfo();
        }

        try (BufferedReader reader = Files.newBufferedReader(PAYG_COMPLIANCE_INFO_JSON)) {
            return gson.fromJson(reader, PaygComplainceInfo.class);
        }
        catch (Exception ex) {
            LOG.error("Unable to parse compliance file. Assume instance is BYOS");
            return new PaygComplainceInfo();
        }
    }

    protected String requestUrl(String url) throws IOException {
        HttpClientAdapter httpClient = new HttpClientAdapter();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse httpResponse = httpClient.executeRequest(httpGet);
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return new String(httpResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
        }
        else {
            throw new IOException("error downloading " + url + " status code " +
                   httpResponse.getStatusLine().getStatusCode());
        }
    }
}
