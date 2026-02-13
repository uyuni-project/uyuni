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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for SCAP-related operations
 */
public class ScapUtils {

    private ScapUtils() {
        // Utility class, no instantiation
    }

    /**
     * Builds the oscap command-line parameters string
     *
     * @param xccdfProfileId XCCDF profile ID (required)
     * @param tailoringFileName Tailoring file name (optional)
     * @param tailoringProfileId Tailoring profile ID (optional)
     * @param advancedArgs Advanced arguments (optional)
     * @param fetchRemoteResources Whether to fetch remote resources
     * @return the formatted parameters for oscap command
     */
    public static String buildOscapParameters(String xccdfProfileId, String tailoringFileName,
                                              String tailoringProfileId, String advancedArgs,
                                              boolean fetchRemoteResources) {
        StringBuilder params = new StringBuilder();
        if (StringUtils.isNotEmpty(xccdfProfileId)) {
            params.append("--profile ").append(xccdfProfileId);
        }
        if (StringUtils.isNotEmpty(tailoringFileName)) {
            params.append(" --tailoring-file ").append(tailoringFileName);
            if (StringUtils.isNotEmpty(tailoringProfileId)) {
                params.append(" --tailoring-profile-id ").append(tailoringProfileId);
            }
        }
        if (StringUtils.isNotEmpty(advancedArgs)) {
            params.append(" ").append(advancedArgs);
        }
        if (fetchRemoteResources) {
            params.append(" --fetch-remote-resources");
        }

        return params.toString().trim();
    }
}
