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
package com.redhat.rhn.domain.action.supportdata;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.Labeled;

import java.util.Arrays;

public enum UploadGeoType implements Labeled {
    EU,
    US;

    @Override
    public String getLabel() {
        return this.name().toLowerCase();
    }

    public String getDescription() {
        return LocalizationService.getInstance().getMessage("supportdata.uploadGeoType." + this.name().toLowerCase());
    }

    /**
     * Retrieve a UploadGeoType by its label
     * @param label the label
     * @return the UploadGeoType instance identified by the given label
     * @throws IllegalArgumentException when the label does not match any known UploadGeoType
     */
    public static UploadGeoType byLabel(String label) {
        return Arrays.stream(UploadGeoType.values())
            .filter(uploadGeo -> uploadGeo.getLabel().equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No UploadGeoType found with label " + label));
    }
}
