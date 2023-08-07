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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

/**
 * Public Cloud Pay-as-you-go management class
 */
public class CloudPaygManager {
    private static final Logger LOG = LogManager.getLogger(CloudPaygManager.class);

    private boolean isPaygInstance;
    private CloudProvider cloudProvider;
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
        cloudProvider = CloudProvider.None;
        if (isFileExecutable("/usr/bin/ec2metadata")) {
            cloudProvider = CloudProvider.AWS;
        }
        else if (isFileExecutable("/usr/bin/azuremetadata")) {
            cloudProvider = CloudProvider.AZURE;
        }
        else if (isFileExecutable("/usr/bin/gcemetadata")) {
            cloudProvider = CloudProvider.GCE;
        }

        isPaygInstance = detectPaygInstance();
    }

    /**
     * @return the CloudProvider
     */
    public CloudProvider getCloudProvider() {
        return cloudProvider;
    }

    /**
     * Only for testing - overwrite the detected provider
     * @param providerIn the provider
     */
    public void setCloudProvider(CloudProvider providerIn) {
        cloudProvider = providerIn;
    }

    /**
     * @return if this is a Pay-as-you-go cloud instance
     */
    public boolean isPaygInstance() {
        return isPaygInstance;
    }

    /**
     * Only for testing - overwrite the detected isPaygInstance
     * @param isPaygInstanceIn is payg instance
     */
    public void setPaygInstance(boolean isPaygInstanceIn) {
        isPaygInstance = isPaygInstanceIn;
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

}
