/*
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

// TEST

package com.suse.manager.webui;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * Utility class for internationalization.
 */
public enum Languages {

    /**
     * Singleton instance
     */
    INSTANCE;

    Languages() { }

    /**
     * Singleton implementation
     * @return an instance of this class
     */
    public static Languages getInstance() {
        return INSTANCE;
    }

    /**
     * Translates a string.
     *
     * @param key the key
     * @param args the arguments
     * @return the localized string
     */
    public static String t(String key, Object... args) {
        /* Short-circuit LocalizationService.getMessage() assuming always en_US */
        String escapedKey = key.replaceAll("'", "''");
        return new MessageFormat(escapedKey, Locale.US).format(args);
    }
}
