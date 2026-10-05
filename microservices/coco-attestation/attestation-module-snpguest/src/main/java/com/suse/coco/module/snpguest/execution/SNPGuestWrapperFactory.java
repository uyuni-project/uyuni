/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.coco.module.snpguest.execution;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SNPGuestWrapperFactory {
    private static final Logger LOGGER = LogManager.getLogger(SNPGuestWrapperFactory.class);

    private static final SNPGuestVersion FALLBACK_VERSION = SNPGuestVersion.VER_07_BELOW;

    public enum SNPGuestVersion {
        VER_07_BELOW,
        VER_09_ABOVE
    }

    private static SNPGuestVersion theSnpguestVersion = null;

    private static Runtime runtime = Runtime.getRuntime();

    /**
     * Method to specify a runtime. For unit testing.
     * @param runtimeIn the runtime used to execute processes
     */
    public static void setRuntime(Runtime runtimeIn) {
        runtime = runtimeIn;
    }

    /**
     * Creates a SNPGuestWrapper instance with the right version
     *
     * @return the SNPGuestWrapper
     */
    public static AbstractSNPGuestWrapper createSNPGuestWrapper() {
        findVersion();

        return switch (theSnpguestVersion) {
            case VER_07_BELOW -> new SNPGuestWrapperVer07Below();
            case VER_09_ABOVE -> new SNPGuestWrapperVer09Above();
        };
    }

    private SNPGuestWrapperFactory() {
        //Utility classes should not have a public or default constructor.
    }

    private static SNPGuestVersion notFoundSetFallback() {
        LOGGER.error("Unable to get snpguest tool version, assuming version 0.7.1 or below");
        theSnpguestVersion = FALLBACK_VERSION;
        return theSnpguestVersion;
    }

    /**
     * tries to find the snpguest tool version
     * @return the snpguest version
     */
    public static synchronized SNPGuestVersion findVersion() {
        if (null != theSnpguestVersion) {
            return theSnpguestVersion;
        }

        int exitCode = -1;
        String versionString = "";
        try {
            // /usr/bin/rpm -q --queryformat '%{VERSION}\n' snpguest

            String[] versionCommand = {"/usr/bin/rpm", "-q", "--queryformat", "%{VERSION}", "snpguest"};
            Process process = runtime.exec(versionCommand);
            exitCode = process.waitFor();

            try (BufferedReader inErr = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                versionString = inErr.readLine();
            }
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOGGER.error("get version throws an exception {}", ex.getMessage());
            return notFoundSetFallback();
        }
        catch (Exception ex) {
            LOGGER.error("get version throws an exception {}", ex.getMessage());
            return notFoundSetFallback();
        }

        if (exitCode != 0) {
            LOGGER.error("get version has exitCode {}", exitCode);
            return notFoundSetFallback();
        }

        if ((null == versionString) || versionString.isEmpty()) {
            LOGGER.error("get version has output {}", versionString);
            return notFoundSetFallback();
        }

        if (versionString.compareToIgnoreCase("0.9") >= 0) {
            LOGGER.info("detected snpguest version 0.9.1 or above {}", versionString);
            theSnpguestVersion = SNPGuestVersion.VER_09_ABOVE;
        }
        else {
            LOGGER.info("detected snpguest version 0.7.1 or below {}", versionString);
            theSnpguestVersion = SNPGuestVersion.VER_07_BELOW;
        }

        return theSnpguestVersion;
    }
}
