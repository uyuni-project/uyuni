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

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.org.Org;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.function.Predicate;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Content Filter
 *
 * @param <T> the entity being filtered
 */
@Entity
@Table(name = "suseContentFilter")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class ContentFilter<T> extends BaseDomainHelper implements Predicate<T> {

    private Long id;
    private Org org;
    private String name;
    private Rule rule;
    private FilterCriteria criteria;

    /**
     * Entity type that is dealt with by filter.
     */
    public enum EntityType {
        PACKAGE("package"),
        ERRATUM("erratum");

        private String label;

        EntityType(String labelIn) {
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
         * Looks up Entity type by label
         *
         * @param label the label
         * @throws java.lang.IllegalArgumentException if no matching Entity type is found
         * @return the matching Entity type
         */
        public static EntityType lookupByLabel(String label) {
            for (EntityType value : values()) {
                if (value.label.equals(label)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unsupported label: " + label);
        }
    }

    /**
     * Type of the filter
     */
    public enum Rule {
        ALLOW("allow"),
        DENY("deny");

        private final String label;

        Rule(String typeIn) {
            this.label = typeIn;
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
         * Looks up Rule by label
         *
         * @param label the label
         * @throws java.lang.IllegalArgumentException if no matching Rule is found
         * @return the matching Rule
         */
        public static Rule lookupByLabel(String label) {
            for (Rule value : values()) {
                if (value.label.equals(label)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unsupported label: " + label);
        }
    }

    /**
     * Get {@link EntityType} of this object
     * @return the {@link EntityType}
     */
    @Transient
    public abstract EntityType getEntityType();

    /**
     * Test whether an object passes the filter
     *
     * @param t the object
     * @return true if the object passes the filter
     */
    public abstract boolean testInternal(T t);

    @Override
    public boolean test(T t) {
        boolean result = testInternal(t);
        // we want to invert the test if Rule type is DENY
        return rule == Rule.ALLOW ? result : !result;
    }

    /**
     * Gets the id.
     *
     * @return id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_filter_seq")
    @SequenceGenerator(name = "content_filter_seq", sequenceName = "suse_ct_filter_seq", allocationSize = 1)
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param idIn - the id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * Gets the org.
     *
     * @return org
     */
    @ManyToOne
    public Org getOrg() {
        return org;
    }

    /**
     * Sets the org.
     *
     * @param orgIn - the org
     */
    public void setOrg(Org orgIn) {
        org = orgIn;
    }

    /**
     * Gets the name.
     *
     * @return name
     */
    @Column
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param nameIn - the name
     */
    public void setName(String nameIn) {
        name = nameIn;
    }

    /**
     * Gets the rule.
     *
     * @return rule
     */
    @Enumerated(EnumType.STRING)
    public Rule getRule() {
        return rule;
    }

    /**
     * Sets the rule.
     *
     * @param ruleIn the rule
     */
    public void setRule(Rule ruleIn) {
        this.rule = ruleIn;
    }

    /**
     * Gets the criteria.
     *
     * @return criteria
     */
    @Embedded
    public FilterCriteria getCriteria() {
        return criteria;
    }

    /**
     * Sets the criteria.
     *
     * @param criteriaIn - the criteria
     */
    public void setCriteria(FilterCriteria criteriaIn) {
        criteria = criteriaIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ContentFilter that = (ContentFilter) o;

        return new EqualsBuilder()
                .append(org, that.org)
                .append(name, that.name)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(org)
                .append(name)
                .toHashCode();
    }
}
