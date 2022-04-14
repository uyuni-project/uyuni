/*
 * Copyright (c) 2022 SUSE LLC
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

package com.suse.manager.saltboot;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SaltbootVersion comparator
 * <p>Saltboot version is in format ImageName-ImageVersion
 *   ImageVersion follows Kiwi semver version with added revision number:
 *   X.Y.Z-R where X,Y,Z and R are unsigned integers
 *   ImageName is ignored in this comparator.</p>
 *  <p><i>Sorts for newest version</i></p>
 */
public class SaltbootVersionCompare implements Comparator<String> {

    /**
     * SaltbootVersion comparator
     * <p>Saltboot version is in format ImageName-ImageVersion
     *   ImageVersion follows Kiwi semver version with added revision number:
     *   X.Y.Z-R where X,Y,Z and R are unsigned integers
     *   ImageName is ignored in this comparator.</p>
     *  <p><i>Sorts for newest version</i></p>
     * @param image1
     * @param image2
     * @return 1 if image2 is newer than image1, -1 if image1 is newer than image2, 0 if they are the same version
     */
    public int compare(String image1, String image2) {
        // Validate version format
        if (image1 == null || image2 == null) {
            throw new IllegalArgumentException("Versions can not be null");
        }
        Pattern regex = Pattern.compile("-(\\d+\\.\\d+\\.\\d+)-(\\d+)$");

        // Saltboot (Kiwi + revision) version is x.x.x-x where x is uint
        Matcher match1 = regex.matcher(image1);
        Matcher match2 = regex.matcher(image2);
        if (!match1.find() || !match2.find()) {
            throw new IllegalArgumentException("Invalid version format");
        }

        String version1 = match1.group(1);
        String version2 = match2.group(1);
        Integer rev1 = Integer.parseInt(match1.group(2));
        Integer rev2 = Integer.parseInt(match2.group(2));
        if (version1.equals(version2)) {
            // revision difference
            if (rev1 > rev2) {
                return -1;
            }
            if (rev1 < rev2) {
                return 1;
            }
            return 0;
        }
        String[] thisParts = version1.split("\\.");
        String[] thatParts = version2.split("\\.");
        int length = Math.max(thisParts.length, thatParts.length);
        for (int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ?
                    Integer.parseInt(thisParts[i]) : 0;
            int thatPart = i < thatParts.length ?
                    Integer.parseInt(thatParts[i]) : 0;
            if (thisPart > thatPart) {
                return -1;
            }
            if (thisPart < thatPart) {
                return 1;
            }
        }
        return 0;
    }
}
