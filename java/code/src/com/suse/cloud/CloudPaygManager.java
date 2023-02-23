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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Public Cloud Pay-as-you-go management class
 */
public class CloudPaygManager {
    private final Logger LOG = LogManager.getLogger(CloudPaygManager.class);

    private boolean isPaygInstance = false;
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
        detectPaygInstance();
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

    private void detectPaygInstance() {
        isPaygInstance = false;
        //TODO: replace with a real check. registercloudguest is available on all images
        if (isFileExecutable("/usr/sbin/registercloudguest") &&
                fileExists("/usr/share/susemanager/.ispayg")) {
            isPaygInstance = true;
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
     * Check if files exists
     * @param filename a filename to check
     * @return returns true when file exists, otherwise false
     */
    protected boolean fileExists(String filename) {
        return Files.exists(Path.of(filename));
    }
}
