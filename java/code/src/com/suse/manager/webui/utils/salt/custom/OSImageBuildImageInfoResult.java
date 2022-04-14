/*
 * Copyright (c) 2018 SUSE LLC
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
package com.suse.manager.webui.utils.salt.custom;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The resulting image info from kiwi-image-build
 */
public class OSImageBuildImageInfoResult {

    private OSImageInspectSlsResult.Bundle bundle;
    private List<OSImageInspectSlsResult.Bundle> bundles;
    private OSImageInspectSlsResult.Image image;
    private OSImageInspectSlsResult.BootImage boot_image;

    /**
     * @return the bundle info
     */
    public List<OSImageInspectSlsResult.Bundle> getBundles() {
        if (bundles != null) {
            return bundles;
        }
        else if (bundle != null) {
            return Collections.singletonList(bundle);
        }
        else {
            return Collections.emptyList();
        }
    }

    /**
     * @return the image info
     */
    public OSImageInspectSlsResult.Image getImage() {
        return image;
    }

    /**
     * @return the boot image info
     */
    public Optional<OSImageInspectSlsResult.BootImage> getBootImage() {
        return Optional.ofNullable(boot_image);
    }
}
