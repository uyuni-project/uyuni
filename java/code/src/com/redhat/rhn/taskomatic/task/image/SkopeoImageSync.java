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
package com.redhat.rhn.taskomatic.task.image;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Image sync skopeo bean to call the sync method
 */
public class SkopeoImageSync {

    private Map<String, List<String>> images = new HashMap<>();
    private Map<String, String> imagesRegex = new HashMap<>();
    // TODO should be extracted to bean
    private SkopeoCredential credentials;
    private boolean tlsVerify = true;

    public SkopeoImageSync() {
    }

    public SkopeoImageSync(Map<String, List<String>> imagesIn, Map<String, String> imagesRegexIn, SkopeoCredential credentialsIn, boolean tlsVerifyIn) {
        images = imagesIn;
        imagesRegex = imagesRegexIn;
        credentials = credentialsIn;
        tlsVerify = tlsVerifyIn;
    }

    public Map<String, List<String>> getImages() {
        return images;
    }

    public void setImages(Map<String, List<String>> imagesIn) {
        images = imagesIn;
    }

    public Map<String, String> getImagesRegex() {
        return imagesRegex;
    }

    public void setImagesRegex(Map<String, String> imagesRegexIn) {
        imagesRegex = imagesRegexIn;
    }

    public SkopeoCredential getCredentials() {
        return credentials;
    }

    public void setCredentials(SkopeoCredential credentialsIn) {
        credentials = credentialsIn;
    }

    public boolean isTlsVerify() {
        return tlsVerify;
    }

    public void setTlsVerify(boolean tlsVerifyIn) {
        tlsVerify = tlsVerifyIn;
    }
}
