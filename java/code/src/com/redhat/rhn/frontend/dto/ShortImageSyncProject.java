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
package com.redhat.rhn.frontend.dto;

import com.redhat.rhn.domain.image.ImageSyncProject;

/**
 * Minimal Image Sync Project Info
 */
public class ShortImageSyncProject extends ImageSyncProject {
    /**
     * Copy Constructor
     * @param pIn project
     */
    public ShortImageSyncProject(ImageSyncProject pIn) {
        super(pIn.getName(), pIn.getOrg(), pIn.getSrcStore(), pIn.getDestinationImageStore(), pIn.isScoped());
        setId(pIn.getId());
    }
}
