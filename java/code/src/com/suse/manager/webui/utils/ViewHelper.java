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

import org.apache.commons.lang.WordUtils;

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
}
