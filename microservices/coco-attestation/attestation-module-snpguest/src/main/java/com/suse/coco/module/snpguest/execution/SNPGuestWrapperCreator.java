/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.coco.module.snpguest.execution;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class SNPGuestWrapperCreator {
    private static final Logger LOGGER = LogManager.getLogger(SNPGuestWrapperCreator.class);

    private static final String VERSION_NOT_FOUND_ERROR =
            "Unable to get snpguest tool version, assuming version 0.7.1 or below";

    private static final SNPGuestVersion FALLBACK_VERSION = SNPGuestVersion.VER_07_BELOW;

    public enum SNPGuestVersion {
        VER_07_BELOW,
        VER_09_ABOVE
    }

    private static SNPGuestVersion theSnpguestVersion = null;

    private static final Object FIND_VERSION_LOCK = new Object();

    /**
     * Creates a SNPGuestWrapper instance with the right version
     *
     * @return the SNPGuestWrapper
     */
    public static SNPGuestWrapper createSNPGuestWrapper() {
        findVersion();

        switch (theSnpguestVersion) {
            case VER_07_BELOW -> {
                return new SNPGuestWrapperVer07Below();
            }
            case VER_09_ABOVE -> {
                return new SNPGuestWrapperVer09Above();
            }
            default -> {
                //fallback
                return new SNPGuestWrapperVer07Below();
            }
        }
    }

    private SNPGuestWrapperCreator() {
        //Utility classes should not have a public or default constructor.
    }

    /**
     * tries to find the snpguest tool version
     */
    public static void findVersion() {
        synchronized (FIND_VERSION_LOCK) {
            if (null != theSnpguestVersion) {
                return;
            }

            try {
                SNPGuestWrapper snpGuest = new SNPGuestWrapperVer07Below();
                ProcessOutput processOutput = snpGuest.getVersion();

                if (processOutput.getExitCode() != 0) {
                    LOGGER.error(VERSION_NOT_FOUND_ERROR);
                    LOGGER.error("get version has exitCode {}", processOutput.getExitCode());
                    theSnpguestVersion = FALLBACK_VERSION;
                    return;
                }

                //e.g. "snpguest 0.3.2\n"
                Optional<String> versionOutput = processOutput.getStandardOutput()
                        .lines()
                        .filter(s -> s.startsWith("snpguest "))
                        .findFirst();

                if (versionOutput.isEmpty()) {
                    LOGGER.error(VERSION_NOT_FOUND_ERROR);
                    LOGGER.error("get version has output {}", processOutput.getStandardOutput());
                    theSnpguestVersion = FALLBACK_VERSION;
                    return;
                }

                String versionString = versionOutput.get().substring(9);
                if (versionString.compareToIgnoreCase("0.9") >= 0) {
                    LOGGER.info("detected snpguest version 0.9.1 or above");
                    theSnpguestVersion = SNPGuestVersion.VER_09_ABOVE;
                }
                else {
                    LOGGER.info("detected snpguest version 0.7.1 or below");
                    theSnpguestVersion = SNPGuestVersion.VER_07_BELOW;
                }
            }
            catch (ExecutionException eIn) {
                LOGGER.error(VERSION_NOT_FOUND_ERROR);
                LOGGER.error("get version throws an exception {}", eIn.getMessage());
                theSnpguestVersion = FALLBACK_VERSION;
            }
        }
    }
}
