/*
 * Copyright (c) 2025 SUSE LLC
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

package com.redhat.rhn.domain.audit;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * XccdfRuleFix entity representing remediation information for XCCDF rules.
 * Contains the fix/remediation script for a specific XCCDF rule.
 */
@Entity
@Table(name = "suseXccdfRuleFix")
public class XccdfRuleFix {

    private Long id;
    private String identifier;
    private String remediation;
    private String benchmarkIdentifier;

    /**
     * Default constructor.
     */
    public XccdfRuleFix() {
        // Default constructor
    }

    /**
     * Constructor for XccdfRuleFix.
     * @param benchmarkIdentifierIn the benchmark identifier
     * @param identifierIn the rule identifier
     * @param fixIn the remediation script
     */
    public XccdfRuleFix(String benchmarkIdentifierIn, String identifierIn, String fixIn) {
        this.benchmarkIdentifier = benchmarkIdentifierIn;
        this.identifier = identifierIn;
        this.remediation = fixIn;
    }

    /**
     * @return the id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return the benchmarkIdentifier
     */
    @Column(name = "benchmark_identifier")
    public String getBenchmarkIdentifier() {
        return benchmarkIdentifier;
    }

    /**
     * @param benchmarkIdentifierIn the benchmarkIdentifier to set
     */
    public void setBenchmarkIdentifier(String benchmarkIdentifierIn) {
        this.benchmarkIdentifier = benchmarkIdentifierIn;
    }

    /**
     * @return the identifier
     */
    @Column(name = "identifier")
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifierIn the identifier to set
     */
    public void setIdentifier(String identifierIn) {
        this.identifier = identifierIn;
    }

    /**
     * @return the remediation
     */
    @Column(name = "remediation")
    public String getRemediation() {
        return remediation;
    }

    /**
     * @param remediationIn the remediation to set
     */
    public void setRemediation(String remediationIn) {
        this.remediation = remediationIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        XccdfRuleFix castOther = (XccdfRuleFix) other;
        return new EqualsBuilder()
                .append(benchmarkIdentifier, castOther.benchmarkIdentifier)
                .append(identifier, castOther.identifier)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(benchmarkIdentifier)
                .append(identifier)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
