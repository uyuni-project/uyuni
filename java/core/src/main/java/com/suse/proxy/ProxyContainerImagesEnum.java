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

package com.suse.proxy;

/**
 * Enum for the proxy container images
 * Holds helpful names used to identify the images in the registry
 */
public enum ProxyContainerImagesEnum {
    PROXY_HTTPD("proxy-httpd", "registryHttpdURL", "registryHttpdTag", "httpd_image", "httpd_tag"),
    PROXY_SALT_BROKER(
            "proxy-salt-broker", "registrySaltbrokerURL", "registrySaltbrokerTag" , "saltbroker_image", "saltbroker_tag"
    ),
    PROXY_SQUID("proxy-squid", "registrySquidURL", "registrySquidTag", "squid_image", "squid_tag"),
    PROXY_SSH("proxy-ssh", "registrySshURL", "registrySshTag", "ssh_image", "ssh_tag"),
    PROXY_TFTPD("proxy-tftpd", "registryTftpdURL", "registryTftpdTag", "tftpd_image", "tftpd_tag");

    private final String imageName;
    private final String urlField;
    private final String tagField;
    private final String pillarImageVariableName;
    private final String pillarTagVariableName;

    /**
     * Constructor
     * @param imageNameIn The name of the image, used to identify the image in the registry and also the pillar entry
     * @param urlFieldIn The field name in the form that holds the URL of the image
     * @param tagFieldIn The field name in the form that holds the tag of the image
     * @param pillarImageVariableNameIn The name of the pillar entry that holds the image name (mainly for the sls file)
     * @param pillarTagVariableNameIn The name of the pillar entry that holds the image tag (mainly for the sls file)
     */
    ProxyContainerImagesEnum(
            String imageNameIn,
            String urlFieldIn,
            String tagFieldIn,
            String pillarImageVariableNameIn,
            String pillarTagVariableNameIn
    ) {
        imageName = imageNameIn;
        urlField = urlFieldIn;
        tagField = tagFieldIn;
        pillarImageVariableName = pillarImageVariableNameIn;
        pillarTagVariableName = pillarTagVariableNameIn;
    }

    public String getImageName() {
        return imageName;
    }

    public String getUrlField() {
        return urlField;
    }

    public String getTagField() {
        return tagField;
    }

    public String getPillarImageVariableName() {
        return pillarImageVariableName;
    }

    public String getPillarTagVariableName() {
        return pillarTagVariableName;
    }
}
