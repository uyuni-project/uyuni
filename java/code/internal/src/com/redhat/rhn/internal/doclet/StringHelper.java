/*
 * Copyright (c) 2024 SUSE LLC
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
package com.redhat.rhn.internal.doclet;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * String helper to be passed in the velocity context for use in templates
 */
public class StringHelper {

    /**
     * Convert value into a CamelCase string
     *
     * @param value the string to convert
     * @return the string with only digits and letters and an upper case first letter on each word.
     */
    public String toCamelCase(String value) {
        return Arrays.stream(value.split("[^a-zA-Z0-9]"))
                .map(word -> StringUtils.capitalize(word))
                .collect(Collectors.joining(""));
    }
}
