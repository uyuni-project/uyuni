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

package com.suse.manager.webui.controllers.image.request;

/**
 * Image sync project POST request object
 */
public class ImageSyncProjectCreateRequest {

    private String label;
    private String sourceRegistry;
    private String image;
    private String targetRegistry;

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the source registry
     */
    public String getSourceRegistry() {
        return sourceRegistry;
    }

    /**
     * @return the image
     */
    public String getImage() {
        return image;
    }

    /**
     * @return the target registry
     */
    public String getTargetRegistry() {
        return targetRegistry;
    }
}
