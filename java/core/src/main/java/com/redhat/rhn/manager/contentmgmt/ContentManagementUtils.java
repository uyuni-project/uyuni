/*
 * Copyright (c) 2026 SUSE LLC
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

package com.redhat.rhn.manager.contentmgmt;

import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Set;

/**
 * Utility methods for Content Lifecycle Management
 */
public class ContentManagementUtils {

    private static final Set<String> SUPPORTED_DATE_FIELDS = Set.of("issue_date", "build_date");

    private ContentManagementUtils() { }

    /**
     * Check whether a filter criteria holds a date value
     *
     * @param field the filter criteria field
     * @param value the filter criteria value
     * @return true if the field is a date field and the value is not empty
     */
    public static boolean isDateCriteria(String field, String value) {
        return field != null && SUPPORTED_DATE_FIELDS.contains(field) && StringUtils.isNotEmpty(value);
    }

    /**
     * Parse a date filter criteria value
     *
     * @param value the filter criteria value in ISO date time format
     * @throws DateTimeParseException when the value cannot be parsed
     * @return the parsed date
     */
    public static Date parseDateCriteria(String value) throws DateTimeParseException {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return Date.from(offsetDateTime.toInstant());
    }
}
