/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.ldap;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResultEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads multi-valued LDAP attributes that Active Directory may return with a
 * {@code ;range=} option (for example {@code memberOf;range=0-1499}). Continues requesting the
 * next range until the directory reports the final page ({@code range=N-*}).
 */
public final class LdapRangedAttributes {

    private static final Pattern RANGE_OPTION = Pattern.compile("range=(\\d+)-(\\d+|\\*)",
            Pattern.CASE_INSENSITIVE);
    private static final int MAX_RANGE_PAGES = 100;

    private LdapRangedAttributes() {
    }

    /**
     * Collects every value of {@code attributeName} on {@code entryDn}, following AD range
     * pages when present.
     *
     * @param pool pooled service connection
     * @param entryDn DN of the entry to read
     * @param attributeName base attribute name (without options), e.g. {@code memberOf}
     * @return all attribute values in page order, or an empty list when the attribute is absent
     * @throws LDAPException if a directory read fails
     */
    public static List<String> readAllValues(LDAPConnectionPool pool, String entryDn, String attributeName)
            throws LDAPException {
        List<String> values = new ArrayList<>();
        String requestAttribute = attributeName;
        for (int page = 0; page < MAX_RANGE_PAGES; page++) {
            SearchResultEntry entry = pool.getEntry(entryDn, requestAttribute);
            if (entry == null) {
                return values;
            }
            Optional<Attribute> attribute = findByBaseName(entry, attributeName);
            if (attribute.isEmpty()) {
                return values;
            }
            Attribute attr = attribute.get();
            String[] pageValues = attr.getValues();
            if (pageValues != null) {
                for (String value : pageValues) {
                    if (value != null && !value.isBlank()) {
                        values.add(value);
                    }
                }
            }
            Optional<Range> range = parseRange(attr.getOptions());
            if (range.isEmpty() || range.get().complete()) {
                return values;
            }
            requestAttribute = attributeName + ";range=" + (range.get().end() + 1) + "-*";
        }
        return values;
    }

    /**
     * Finds the first attribute on {@code entry} whose base name matches {@code attributeName},
     * ignoring range (and other) options.
     *
     * @param entry directory entry
     * @param attributeName base attribute name
     * @return matching attribute, if any
     */
    static Optional<Attribute> findByBaseName(Entry entry, String attributeName) {
        if (entry == null || attributeName == null) {
            return Optional.empty();
        }
        for (Attribute attribute : entry.getAttributes()) {
            if (attributeName.equalsIgnoreCase(attribute.getBaseName())) {
                return Optional.of(attribute);
            }
        }
        return Optional.empty();
    }

    /**
     * Parses an AD {@code range=start-end} or {@code range=start-*} option from an attribute's
     * option set.
     *
     * @param options attribute options
     * @return parsed range, or empty when no range option is present
     */
    static Optional<Range> parseRange(Set<String> options) {
        if (options == null || options.isEmpty()) {
            return Optional.empty();
        }
        for (String option : options) {
            Matcher matcher = RANGE_OPTION.matcher(option);
            if (matcher.matches()) {
                int start = Integer.parseInt(matcher.group(1));
                String endToken = matcher.group(2);
                if ("*".equals(endToken)) {
                    return Optional.of(new Range(start, Integer.MAX_VALUE, true));
                }
                return Optional.of(new Range(start, Integer.parseInt(endToken), false));
            }
        }
        return Optional.empty();
    }

    /**
     * AD range page descriptor.
     *
     * @param start inclusive start index
     * @param end inclusive end index, or {@link Integer#MAX_VALUE} for a terminal {@code *} page
     * @param complete {@code true} when this is the final page ({@code range=N-*})
     */
    record Range(int start, int end, boolean complete) {
    }

}
