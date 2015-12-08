/**
 * Copyright (c) 2015 SUSE LLC
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

package com.suse.manager.webui.utils;

import com.redhat.rhn.frontend.taglibs.helpers.RenderUtils;

import org.apache.commons.lang.WordUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility class for Jade views.
 */
public enum ViewHelper {
    /**
     * Singleton instance
     */
    INSTANCE;

    ViewHelper() { }

    /**
     * Singleton implementation
     * @return an instance of this class
     */
    public static ViewHelper getInstance() {
        return INSTANCE;
    }

    /**
     * Capitalizes a string.
     *
     * @param s the string
     * @return the capitalized string
     */
    public String capitalize(String s) {
        return WordUtils.capitalize(s);
    }

    /**
     * Generate the navigation menu defined by the given menu definition.
     *
     * @param request the request object
     * @param menuDefinition the menu definition
     * @return the navigation menu markup as string
     */
    public String renderNavigationMenu(HttpServletRequest request, String menuDefinition) {
        String rendererClass = "com.redhat.rhn.frontend.nav.DialognavRenderer";
        try {
            return RenderUtils.INSTANCE.renderNavigationMenu(
                    request, menuDefinition, rendererClass, 0, 3);
        }
        catch (Exception e) {
            throw new RuntimeException("Error rendering the navigation menu.", e);
        }
    }
}
