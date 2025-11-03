/*
 * Copyright (c) 2019--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.contentmgmt;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.org.Org;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import java.util.function.Predicate;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

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
        ERRATUM("erratum"),
        MODULE("module"),
        PTF("ptf");

        private final String label;

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
     * Gets the id.
     *
     * @return id
     */
    @Id
    @GeneratedValue(generator = "content_filter_seq")
    @GenericGenerator(
            name = "content_filter_seq",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "suse_ct_filter_seq"),
                    @Parameter(name = "increment_size", value = "1")
            })
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
     * Sets the criteria, validating it against allowed matcher-field combinations
     *
     * @param criteriaIn - the criteria
     * @throws IllegalArgumentException when the matcher-field combination is not allowed
     */
    public void setCriteria(FilterCriteria criteriaIn) {
        FilterCriteria.validate(getEntityType(), criteriaIn.getMatcher(), criteriaIn.getField());
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

        ContentFilter<?> that = (ContentFilter<?>) o;

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
