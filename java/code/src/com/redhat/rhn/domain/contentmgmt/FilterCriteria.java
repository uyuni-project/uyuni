/*
 * Copyright (c) 2019 SUSE LLC
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

package com.redhat.rhn.domain.contentmgmt;

import static com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType.ERRATUM;
import static com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType.MODULE;
import static com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType.PACKAGE;
import static com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType.PTF;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.CONTAINS;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.CONTAINS_PKG_EQ_EVR;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.CONTAINS_PKG_GE_EVR;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.CONTAINS_PKG_GT_EVR;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.CONTAINS_PKG_LE_EVR;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.CONTAINS_PKG_LT_EVR;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.CONTAINS_PKG_NAME;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.CONTAINS_PROVIDES_NAME;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.EQUALS;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.GREATER;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.GREATEREQ;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.LOWER;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.LOWEREQ;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.MATCHES;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.MATCHES_PKG_NAME;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.MODULE_NONE;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.PROVIDES_NAME;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.PTF_ALL;
import static com.redhat.rhn.domain.contentmgmt.PtfFilter.FIELD_PTF_ALL;
import static com.redhat.rhn.domain.contentmgmt.PtfFilter.FIELD_PTF_NUMBER;
import static com.redhat.rhn.domain.contentmgmt.PtfFilter.FIELD_PTF_PACKAGE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;


/**
 * The criteria used for matching objects (Package, Errata) in {@link ContentFilter}
 *
 * Consist of 3 fields:
 * - matcher - the matcher type (equals, contains, greater-than...)
 * - field - the field of the examined object (e.g. Package name)
 * - value - the user-defined value for matching (e.g. "libsolv", for package name)
 */
@Embeddable
public class FilterCriteria {

    private Matcher matcher;
    private String field;
    private String value;

    private static final Set<Triple<ContentFilter.EntityType, Matcher, String>> VALID_COMBINATIONS = Set.of(
        Triple.of(PACKAGE, CONTAINS, "name"),
        Triple.of(PACKAGE, LOWER, "nevr"),
        Triple.of(PACKAGE, LOWEREQ, "nevr"),
        Triple.of(PACKAGE, EQUALS, "nevr"),
        Triple.of(PACKAGE, GREATEREQ, "nevr"),
        Triple.of(PACKAGE, GREATER, "nevr"),
        Triple.of(PACKAGE, LOWER, "nevra"),
        Triple.of(PACKAGE, LOWEREQ, "nevra"),
        Triple.of(PACKAGE, EQUALS, "nevra"),
        Triple.of(PACKAGE, GREATEREQ, "nevra"),
        Triple.of(PACKAGE, GREATER, "nevra"),
        Triple.of(PACKAGE, MATCHES, "name"),
        Triple.of(ERRATUM, EQUALS, "advisory_name"),
        Triple.of(ERRATUM, EQUALS, "advisory_type"),
        Triple.of(ERRATUM, EQUALS, "synopsis"),
        Triple.of(ERRATUM, MATCHES, "advisory_name"),
        Triple.of(ERRATUM, MATCHES, "synopsis"),
        Triple.of(ERRATUM, CONTAINS, "synopsis"),
        Triple.of(ERRATUM, CONTAINS, "keyword"),
        Triple.of(ERRATUM, GREATER, "issue_date"),
        Triple.of(ERRATUM, GREATEREQ, "issue_date"),
        Triple.of(ERRATUM, MATCHES_PKG_NAME, "package_name"),
        Triple.of(ERRATUM, CONTAINS_PKG_NAME, "package_name"),
        Triple.of(ERRATUM, CONTAINS_PKG_LT_EVR, "package_nevr"),
        Triple.of(ERRATUM, CONTAINS_PKG_LE_EVR, "package_nevr"),
        Triple.of(ERRATUM, CONTAINS_PKG_EQ_EVR, "package_nevr"),
        Triple.of(ERRATUM, CONTAINS_PKG_GE_EVR, "package_nevr"),
        Triple.of(ERRATUM, CONTAINS_PKG_GT_EVR, "package_nevr"),
        Triple.of(MODULE, EQUALS, "module_stream"),
        Triple.of(MODULE, MODULE_NONE, "module_stream"),
        Triple.of(PACKAGE, PROVIDES_NAME, "provides_name"),
        Triple.of(ERRATUM, CONTAINS_PROVIDES_NAME, "package_provides_name"),
        Triple.of(PTF, PTF_ALL, FIELD_PTF_ALL),
        Triple.of(PTF, LOWER, FIELD_PTF_NUMBER),
        Triple.of(PTF, LOWEREQ, FIELD_PTF_NUMBER),
        Triple.of(PTF, EQUALS, FIELD_PTF_NUMBER),
        Triple.of(PTF, GREATEREQ, FIELD_PTF_NUMBER),
        Triple.of(PTF, GREATER, FIELD_PTF_NUMBER),
        Triple.of(PTF, EQUALS, FIELD_PTF_PACKAGE),
        Triple.of(PTF, MATCHES, FIELD_PTF_PACKAGE),
        Triple.of(PTF, CONTAINS, FIELD_PTF_PACKAGE)
    );

    /**
     * The matcher type
     */
    public enum Matcher {
        CONTAINS("contains"),
        MATCHES_PKG_NAME("matches_pkg_name"),
        CONTAINS_PKG_NAME("contains_pkg_name"),
        CONTAINS_PKG_LT_EVR("contains_pkg_lt_evr"), // <
        CONTAINS_PKG_LE_EVR("contains_pkg_le_evr"), // <=
        CONTAINS_PKG_EQ_EVR("contains_pkg_eq_evr"), // ==
        CONTAINS_PKG_GE_EVR("contains_pkg_ge_evr"), // >=
        CONTAINS_PKG_GT_EVR("contains_pkg_gt_evr"), // >
        LOWER("lower"),
        LOWEREQ("lowereq"),
        EQUALS("equals"),
        GREATER("greater"),
        GREATEREQ("greatereq"),
        MATCHES("matches"),
        PROVIDES_NAME("provides_name"),
        CONTAINS_PROVIDES_NAME("contains_provides_name"),
        MODULE_NONE("module_none"),
        PTF_ALL("ptf_all");

        private final String label;

        Matcher(String labelIn) {
            this.label = labelIn;
        }

        /**
         * Gets the label.
         *
         * @return label
         */
        public String getLabel() {
            return label;
        }

        /**
         * Looks up Matcher by label
         *
         * @param label the label
         * @throws java.lang.IllegalArgumentException if no matching matcher is found
         * @return the matching matcher
         */
        public static Matcher lookupByLabel(String label) {
            for (Matcher value : values()) {
                if (value.label.equals(label)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unsupported label: " + label);
        }
    }

    /**
     * Standard constructor
     */
    public FilterCriteria() {
    }

    /**
     * Standard constructor
     *
     * @param matcherIn the matcher type
     * @param fieldIn the field to match
     * @param valueIn the match value
     */
    public FilterCriteria(Matcher matcherIn, String fieldIn, String valueIn) {
        this.matcher = matcherIn;
        this.field = fieldIn;
        this.value = valueIn;
    }

    /**
     * Validate the matcher-field combination
     *
     * @param entityType the entity type
     * @param matcher the matcher
     * @param field the field
     * @throws IllegalArgumentException when validation does not pass
     */
    public static void validate(ContentFilter.EntityType entityType, Matcher matcher, String field)
            throws IllegalArgumentException {
        if (!VALID_COMBINATIONS.contains(Triple.of(entityType, matcher, field))) {
            throw new IllegalArgumentException(
                    String.format("Invalid criteria combination (entityType: '%s', matcher: '%s', field: '%s')",
                            entityType, matcher, field));
        }
    }

    /**
     * Return a list of available filter criterias
     *
     * @return list of filter criteria
     */
    public static List<Map<String, String>> listFilterCriteria() {
        List<Map<String, String>> result = new LinkedList<>();
        for (Triple<ContentFilter.EntityType, Matcher, String> c : VALID_COMBINATIONS) {
            Map<String, String> criteria = new HashMap<>();
            criteria.put("type", c.getLeft().getLabel());
            criteria.put("matcher", c.getMiddle().getLabel());
            criteria.put("field", c.getRight());
            result.add(criteria);
        }
        return result;
    }

    /**
     * Gets the type.
     *
     * @return type
     */
    @Column(name = "matcher")
    @Enumerated(EnumType.STRING)
    public Matcher getMatcher() {
        return matcher;
    }

    /**
     * Sets the matcher type.
     *
     * @param matcherIn the matcher type
     */
    public void setMatcher(Matcher matcherIn) {
        this.matcher = matcherIn;
    }

    /**
     * Gets the field.
     *
     * @return field
     */
    @Column(name = "field")
    public String getField() {
        return field;
    }

    /**
     * Sets the field.
     *
     * @param fieldIn the field
     */
    public void setField(String fieldIn) {
        this.field = fieldIn;
    }

    /**
     * Gets the value.
     *
     * @return value
     */
    @Column(name = "value")
    public String getValue() {
        return value;
    }

    /**
     * Sets the value.
     *
     * @param valueIn the value
     */
    public void setValue(String valueIn) {
        this.value = valueIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FilterCriteria that = (FilterCriteria) o;

        return new EqualsBuilder()
                .append(matcher, that.matcher)
                .append(field, that.field)
                .append(value, that.value)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(matcher)
                .append(field)
                .append(value)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("matcher", matcher)
                .append("field", field)
                .append("value", value)
                .toString();
    }

}
