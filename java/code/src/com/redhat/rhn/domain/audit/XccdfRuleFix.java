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
 */
package com.redhat.rhn.domain.audit;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;


@Entity
@Table(name = "suseXccdfRuleFix")
@SqlResultSetMapping(
        name = "ScapPolicyDetailMapping",
        classes = @ConstructorResult(
                targetClass = ScapPolicyDetailJson.class,
                columns = {
                        @ColumnResult(name = "scapActionId", type = Long.class),
                        @ColumnResult(name = "scapPolicyId", type = Long.class),
                        @ColumnResult(name = "serverId", type = Long.class),
                        @ColumnResult(name = "serverName", type = String.class),
                        @ColumnResult(name = "pickupTime", type = java.time.LocalDateTime.class),  // pickup_time as LocalDateTime
                        @ColumnResult(name = "Passed", type = Long.class),
                        @ColumnResult(name = "Failed", type = Long.class),
                        @ColumnResult(name = "Other", type = Long.class)
                }
        )
)
public class XccdfRuleFix {
    private Long id;
    private String identifier;
    private String remediation;
    private String benchMarkId;

    /**
     * Constructor
     */
    public XccdfRuleFix() {

    }

    /**
     * Constructor
     * @param benchMarkIdIn
     * @param identifierIn
     * @param fixIn
     */
    public XccdfRuleFix(String benchMarkIdIn, String identifierIn, String fixIn) {
        this.benchMarkId = benchMarkIdIn;
        this.identifier = identifierIn;
        this.remediation = fixIn;
    }

    public void setId(Long idIn) {
        this.id = idIn;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    @Column(name = "benchmark_id")
    public String getBenchMarkId() {
        return benchMarkId;
    }

    public void setBenchMarkId(String benchMarkIdIn) {
        this.benchMarkId = benchMarkIdIn;
    }

    @Column(name = "identifier")
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifierIn) {
        this.identifier = identifierIn;
    }
    @Column(name = "remediation")
    public String getRemediation() {
        return remediation;
    }

    public void setRemediation(String remediationIn) {
        this.remediation = remediationIn;
    }

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
                .append(benchMarkId, castOther.benchMarkId)
                .append(identifier, castOther.identifier)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(benchMarkId)
                .append(identifier)
                .toHashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
