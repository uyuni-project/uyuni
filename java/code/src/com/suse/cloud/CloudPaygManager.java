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
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.satellite.SystemCommandExecutor;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Public Cloud Pay-as-you-go management class
 */
public class CloudPaygManager {
    private static final Logger LOG = LogManager.getLogger(CloudPaygManager.class);

    private Boolean isPaygInstance;
    private Boolean hasSCCCredentials;
    private CloudProvider cloudProvider;
    private boolean isCompliant;
    private Instant cacheTime;
    public enum CloudProvider {
        None,
        AWS,
        AZURE,
        GCE
    }

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
        cloudProvider = CloudProvider.None;
        isPaygInstance = null;
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
        cloudProvider = CloudProvider.None;
        isPaygInstance = null;
        isCompliant = false;
    }

    /**
     * @return if this is a Pay-as-you-go cloud instance
     */
    public boolean isPaygInstance() {
        checkRefreshCache(false);
        if (isPaygInstance == null) {
            isPaygInstance = detectPaygInstance();
        }
        return isPaygInstance;
    }

    /**
     * @return the CloudProvider
     */
    public CloudProvider getCloudProvider() {
        checkRefreshCache(false);
        if (cloudProvider == CloudProvider.None) {
            cloudProvider = detectCloudProvider();
        }
        return cloudProvider;
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
        // if isCompliant is false, we re-detect
        if (force || !isCompliant || Duration.between(cacheTime, Instant.now()).toHours() >= 1) {
            // cached values older than 1 hour - time to refresh
            cloudProvider = detectCloudProvider();
            isPaygInstance = detectPaygInstance();
            hasSCCCredentials = detectHasSCCCredentials();
            isCompliant = detectIsCompliant();
            cacheTime = Instant.now();
            return true;
        }
        return false;
    }

    /**
     * Only for testing - overwrite the detected provider
     * @param providerIn the provider
     */
    public void setCloudProvider(CloudProvider providerIn) {
        cloudProvider = providerIn;
        cacheTime = Instant.now();
    }

    /**
     * Only for testing - overwrite the detected isPaygInstance
     * @param isPaygInstanceIn is payg instance
     */
    public void setPaygInstance(boolean isPaygInstanceIn) {
        isPaygInstance = isPaygInstanceIn;
        cacheTime = Instant.now();
    }

    /**
     * Only for testing - overwrite the detected hasSCCCredentials
     * @param hasSCCCredentialsIn has scc credentials
     */
    public void setHasSCCCredentials(boolean hasSCCCredentialsIn) {
        hasSCCCredentials = hasSCCCredentialsIn;
        cacheTime = Instant.now();
    }

    /**
     * Only for testing - overwrite the detected isCompliant
     * @param isCompliantIn is system compliant
     */
    public void setCompliant(boolean isCompliantIn) {
        isCompliant = isCompliantIn;
        cacheTime = Instant.now();
    }

    private CloudProvider detectCloudProvider() {
        if (isFileExecutable("/usr/bin/ec2metadata")) {
            return CloudProvider.AWS;
        }
        else if (isFileExecutable("/usr/bin/azuremetadata")) {
            return CloudProvider.AZURE;
        }
        else if (isFileExecutable("/usr/bin/gcemetadata")) {
            return CloudProvider.GCE;
        }
        return CloudProvider.None;
    }

    private boolean detectPaygInstance() {
        if (!isFileExecutable("/usr/bin/instance-flavor-check")) {
            return false;
        }

        try {
            return "PAYG".equals(getInstanceType());
        }
        catch (ExecutionException ex) {
            LOG.error("Unable to identify the instance type. Fallback to BYOS.", ex);
            return false;
        }
    }

    private boolean detectHasSCCCredentials() {
        List<Credentials> cl = CredentialsFactory.listSCCCredentials();
        hasSCCCredentials = false;
        if (cl.size() > 0) {
            for (Credentials c : cl) {
                if (mgr.isSCCCredentials(c)) {
                    hasSCCCredentials = true;
                    break;
                }
            }
        }
        return hasSCCCredentials;
    }

    private boolean detectIsCompliant() {
        // do not use isPaygInstance() to prevent a loop
        if (BooleanUtils.isFalse(isPaygInstance)) {
            return true;
        }

        // Check if the payg-dimension-computation job is active
        try {
            if (tapi.lookupScheduleByBunchAndLabel(null, "payg-dimension-computation-bunch",
                    "payg-dimension-computation-default") == null) {
                LOG.error("payg-dimension-computation job is not active");
                return false;
            }
        }
        catch (TaskomaticApiException e) {
            LOG.error("Unable to check payg-dimension-computation job is active");
            LOG.info(e.getMessage(), e);
            return false;
        }

        // files of this package should not be modified
        String[] cmd = {"/usr/bin/rpm", "-V", "billing-data-service"};
        SystemCommandExecutor scexec = new SystemCommandExecutor();
        int retcode = scexec.execute(cmd);
        if (retcode != 0) {
            // 5 means checksum changed / file is modified. Example "S.5....T.  /path/to/file"
            if (scexec.getLastCommandOutput().lines().anyMatch(l -> l.charAt(2) == '5')) {
                LOG.error("Billing Data Service has modifications");
                LOG.info(scexec.getLastCommandOutput());
                return false;
            }
        }

        // we only need to check compliance for SUMA PAYG
        try {
            if (!requestUrl("http://localhost:18888/").equals("online")) {
                LOG.error("Billing Data Service offline");
                return false;
            }
        }
        catch (IOException e) {
            LOG.error("Billing Data Service down");
            LOG.info(e.getMessage(), e);
            return false;
        }
        //TODO: Check billing adapter report
        return true;
    }

    /**
     * Check if file exists and is executable
     * @param filename a filename to check
     * @return returns true when file exists and is executable
     */
    protected boolean isFileExecutable(String filename) {
        return Files.isExecutable(Path.of(filename));
    }

    /**
     * Executes the script to check the instance type and returns it.
     * @return PAYG or BYOS depending on the instance type.
     * @throws ExecutionException when the script is not successfully executed
     */
    protected String getInstanceType() throws ExecutionException {
        try {
            Process proc = Runtime.getRuntime().exec("/usr/bin/sudo /usr/bin/instance-flavor-check");
            proc.waitFor();

            try (InputStream inputStream = proc.getInputStream()) {
                String type = StringUtils.trim(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
                LOG.debug("Script execution returned {} with exit code {}", type, proc.exitValue());
                return type;
            }
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();

            throw new ExecutionException("Interrupted while checking the instance type", ex);
        }
        catch (Exception ex) {
            throw new ExecutionException("Unexpected Error while checking the instance type", ex);
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
