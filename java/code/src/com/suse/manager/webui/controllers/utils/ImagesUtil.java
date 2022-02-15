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

package com.suse.manager.webui.controllers.utils;

import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;

import com.suse.manager.gatherer.GathererRunner;
import com.suse.manager.model.gatherer.GathererModule;

import java.util.Map;

/**
 * Utilities for images.
 */
public class ImagesUtil {

    private ImagesUtil() { }

    /**
     * @return true if the gathering runtime information about images
     * is enabled.
     */
    public static boolean isImageRuntimeInfoEnabled() {
        Map<String, GathererModule> modules = new GathererRunner().listModules();
        return modules.keySet().stream()
                .filter(VirtualHostManagerFactory.KUBERNETES::equalsIgnoreCase)
                .findFirst().isPresent();
    }

}
