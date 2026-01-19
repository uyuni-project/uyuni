package com.redhat.rhn.domain.audit;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

/**
 * XccdfRuleFix entity representing remediation information for XCCDF rules.
 * Contains the fix/remediation script or instructions for a specific XCCDF rule.
 */
@Entity
@Table(name = "suseXccdfRuleFix")
public class XccdfRuleFix {
    private Long id;
    private String identifier;
    private String remediation;
    private String benchmarkIdentifier;
    public XccdfRuleFix() {

    }

    public XccdfRuleFix(String benchmarkIdentifierIn, String identifierIn, String fixIn) {
        this.benchmarkIdentifier = benchmarkIdentifierIn;
        this.identifier = identifierIn;
        this.remediation = fixIn;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    @Column(name = "benchmark_identifier")
    public String getBenchmarkIdentifier() {
        return benchmarkIdentifier;
    }

    public void setBenchmarkIdentifier(String benchmarkIdentifier) {
        this.benchmarkIdentifier = benchmarkIdentifier;
    }

    @Column(name = "identifier")
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    @Column(name = "remediation")
    public String getRemediation() {
        return remediation;
    }

    public void setRemediation(String remediation) {
        this.remediation = remediation;
    }


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
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(benchmarkIdentifier)
                .append(identifier)
                .toHashCode();
    }
    @Override
    public String toString() {
        return super.toString();
    }


}