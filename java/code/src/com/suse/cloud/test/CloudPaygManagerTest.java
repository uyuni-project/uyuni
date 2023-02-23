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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.cloud.CloudPaygManager;

import org.junit.jupiter.api.Test;

public class CloudPaygManagerTest extends BaseTestCaseWithUser {

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
    public void testIsPayg() {
        CloudPaygManager cpm = new CloudPaygManager() {
            @Override
            protected boolean isFileExecutable(String filename) {
                return filename.equals("/usr/sbin/registercloudguest") ||
                        filename.equals("/usr/bin/ec2metadata");
            }

            @Override
            protected boolean fileExists(String filename) {
                return filename.equals("/usr/share/susemanager/.ispayg");
            }
        };
        assertTrue(cpm.isPaygInstance(), "Expecting a PAYG instance");
    }
}
