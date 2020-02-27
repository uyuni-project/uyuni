/**
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.extensions;

import org.pf4j.ExtensionPoint;

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;


/**
 * Interface to implement to provide String packages to the localization service
 */
public interface LocalizationExtensionPoint extends ExtensionPoint {

    /**
     * Get a localized version of a string with the specified locale.
     *
     * @param messageId The key of the message we are fetching
     * @param locale The locale to use when fetching the string
     * @param args arguments for message.
     *
     * @return Translated String
     *
     * @throws MissingResourceException if the messageId can't be found
     */
    public String getMessage(String messageId, Locale locale, Object... args) throws MissingResourceException;

    /**
     * @return the list of message keys the extension knows about
     */
    public Enumeration<String> getKeys();
}
