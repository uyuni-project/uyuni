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

package com.suse.proxy.update;


import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_REGISTRY;
import static java.lang.String.format;

import com.suse.manager.api.ParseException;
import com.suse.proxy.ProxyContainerImagesEnum;
import com.suse.proxy.ProxyRegistryUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Verifies registry sources exist and are reachable
 */
public class ProxyConfigUpdateRegistryPreConditions implements ProxyConfigUpdateContextHandler {

    private static final Logger LOG = LogManager.getLogger(ProxyConfigUpdateRegistryPreConditions.class);

    @Override
    public void handle(ProxyConfigUpdateContext context) {
        if (!SOURCE_MODE_REGISTRY.equals(context.getRequest().getSourceMode())) {
            return;
        }

        for (ProxyContainerImagesEnum proxyImage : ProxyContainerImagesEnum.values()) {
            if (!context.getRegistryUrls().containsKey(proxyImage)) {
                String noRegistryUrlMessage = format("No registry URL provided for image %s", proxyImage);
                LOG.error(noRegistryUrlMessage);
                context.getErrorReport().register(noRegistryUrlMessage);
                continue;
            }
            try {
                // Testing access by retrieving the tags
                ProxyRegistryUtils.getTags(context.getRegistryUrls().get(proxyImage));
            }
            catch (ParseException parseException) {
                LOG.error("Failed to get tags from registry URL: {}", context.getRegistryUrls().get(proxyImage));
                context.getErrorReport().register(
                        "Failed to get tags from registry URL: " + context.getRegistryUrls().get(proxyImage)
                );
            }
        }

    }

}
