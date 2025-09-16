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
package com.redhat.rhn.domain.image;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.org.Org;

/**
 * Utils related to OS Image Store paths and URIs
 */
public class OSImageStoreUtils {

    private static String osImageStorePath = "/srv/www/os-images/";
    private static String osImageWWWDirectory = "os-images";

    private OSImageStoreUtils() { }


    /**
     * Returns a OS Image Store Path for an Org
     *
     * @param org the org associated with the Image Store
     * @return the full local path
     */
    public static String getOSImageStorePathForOrg(Org org) {
        return osImageStorePath + org.getId() + "/";
    }

    /**
     * Returns a OS Image Store Path for an Image
     *
     * @param image the OS image
     * @return the full local path
     */
    public static String getOSImageStorePathForImage(ImageInfo image) {
        if (!image.getStore().getStoreType().equals(ImageStoreFactory.TYPE_OS_IMAGE)) {
            throw new IllegalArgumentException("Image store is not OS Image Store");
        }
        return getOSImageStorePathForOrg(image.getOrg());
    }

    /**
     * Returns the OS Image Store Path base local path
     *
     * @return the full local path
     */
    public static String getOsImageStorePath() {
        return osImageStorePath;
    }

    /**
     * Returns the OS Image Store URI
     *
     * @return the full URI
     */
    public static String getOSImageStoreURI() {
        String suseManagerHostname = ConfigDefaults.get().getHostname();
        return "https://" + suseManagerHostname + "/" + osImageWWWDirectory + "/";
    }

    /**
     * Returns a OS Image Store URI for an Org
     *
     * @param org the org associated with the Image Store
     * @return the full URI for the Org
     */
    public static String getOSImageStoreURIForOrg(Org org) {
        return getOSImageStoreURI() + org.getId() + "/";
    }

    /**
     * Returns an OS Image Store URI relative to the server's domain name for an Org
     *
     * @param org the org associated with the Image Store
     * @return the relative URI for the Org
     */
    public static String getOSImageStoreRelativeURI(Org org) {
        return "/" + osImageWWWDirectory + "/" + org.getId() + "/";
    }

    /**
     * Returns an OS Image File local path
     *
     * @param file the image file
     * @return the local path
     */
    public static String getOSImageFilePath(ImageFile file) {
        if (file.isExternal()) {
            throw new IllegalArgumentException("External file has no local path");
        }
        return getOSImageStorePathForOrg(file.getImageInfo().getOrg()) + file.getFile();
    }

    /**
     * Returns an OS Image File URI
     *
     * @param file the image file
     * @return the URI
     */
    public static String getOSImageFileURI(ImageFile file) {
        if (file.isExternal()) {
            return file.getFile();
        }
        return getOSImageStoreURIForOrg(file.getImageInfo().getOrg()) + file.getFile();
    }

    /**
     * Returns a Delta Image local path
     *
     * @param delta the delta image info
     * @return the local path
     */
    public static String getDeltaImageFilePath(DeltaImageInfo delta) {
        return getOSImageStorePathForImage(delta.getSourceImageInfo()) + delta.getFile();
    }

}
