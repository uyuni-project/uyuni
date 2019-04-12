/**
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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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

    /**
     * The matcher type
     */
    public enum Matcher {
        CONTAINS("contains");

        private String label;

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
