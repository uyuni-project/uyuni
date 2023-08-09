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
import java.util.Date;
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
    private Date cacheTime;
    public enum CloudProvider {
        None,
        AWS,
        AZURE,
        GCE
    }
    /**
     * Constructor
     */
    public CloudPaygManager() {
        cacheTime = new Date(0L);
        hasSCCCredentials = null;
        cloudProvider = CloudProvider.None;
        isPaygInstance = null;
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
     * Only for testing - overwrite the detected provider
     * @param providerIn the provider
     */
    public void setCloudProvider(CloudProvider providerIn) {
        cloudProvider = providerIn;
        cacheTime = new Date();
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
     * Only for testing - overwrite the detected isPaygInstance
     * @param isPaygInstanceIn is payg instance
     */
    public void setPaygInstance(boolean isPaygInstanceIn) {
        isPaygInstance = isPaygInstanceIn;
        cacheTime = new Date();
    }

    /**
     * Only for testing - overwrite the detected hasSCCCredentials
     * @param hasSCCCredentialsIn has scc credentials
     */
    public void setHasSCCCredentials(boolean hasSCCCredentialsIn) {
        hasSCCCredentials = hasSCCCredentialsIn;
        cacheTime = new Date();
    }

    /**
     * Check and perform a refresh of the stored data if needed
     * @param force if true, force a refresh independent of the time
     * @return true when a refresh happened, otherwise false
     */
    public boolean checkRefreshCache(boolean force) {
        long now = new Date().getTime() / 1000L;
        long cache = cacheTime.getTime() / 1000L;
        if (force || (now - cache) > (2 * 60 * 60)) {
            // cached values older than 2 hours - time to refresh
            cloudProvider = detectCloudProvider();
            isPaygInstance = detectPaygInstance();
            hasSCCCredentials = detectHasSCCCredentials();
            cacheTime = new Date();
            return true;
        }
        return false;
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

    private boolean detectHasSCCCredentials() {
        List<Credentials> cl = CredentialsFactory.listSCCCredentials();
        hasSCCCredentials = false;
        if (cl.size() > 0) {
            ContentSyncManager mgr = new ContentSyncManager();
            for (Credentials c : cl) {
                if (mgr.isSCCCredentials(c)) {
                    hasSCCCredentials = true;
                    break;
                }
            }
        }
        return hasSCCCredentials;
    }
    /**
     * Check if SUSE Manager PAYG is operating in a compliant mode
     * @return true if not payg or all components works as they should, otherwise false
     */
    public boolean isCompliant() {
        checkRefreshCache(false);
        if (!isPaygInstance()) {
            return true;
        }
        // we only need to check compliance for SUMA PAYG
        try {
            if (!requestUrl("http://localhost:18888/").equals("online")) {
                LOG.error("Billing Data Service offline");
                return false;
            }
        }
        catch (IOException e) {
            LOG.error("Billing Data Service down", e);
            return false;
        }
        //TODO: Check billing adapter report
        return true;
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
