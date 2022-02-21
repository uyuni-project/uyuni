/*
 * Copyright (c) 2017 SUSE LLC
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

package com.suse.manager.webui.utils.gson;

import java.util.Map;

/**
 * Image profile POST request object
 */
public class ImageProfileCreateRequest {

    private String label;
    private String path;
    private String kiwiOptions;
    private String imageType;
    private String imageStore;
    private String activationKey;
    private Map<String, String> customData;

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @return the Kiwi options
     */
    public String getKiwiOptions() {
        return kiwiOptions;
    }

    /**
     * @return the image type
     */
    public String getImageType() {
        return imageType;
    }

    /**
     * @return the store label
     */
    public String getImageStore() {
        return imageStore;
    }

    /**
     * @return the activation key
     */
    public String getActivationKey() {
        return activationKey;
    }

    /**
     * @return the custom data
     */
    public Map<String, String> getCustomData() {
        return customData;
    }
}
