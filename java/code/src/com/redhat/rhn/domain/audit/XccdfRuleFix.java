package com.redhat.rhn.domain.audit;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.suse.manager.webui.utils.gson.ScapPolicyDetailJson;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

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
    public XccdfRuleFix() {

    }

    public XccdfRuleFix(String benchMarkIdIn, String identifierIn, String fixIn) {
        this.benchMarkId = benchMarkIdIn;
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

    @Column(name = "benchmark_id")
    public String getBenchMarkId() {
        return benchMarkId;
    }

    public void setBenchMarkId(String benchMarkId) {
        this.benchMarkId = benchMarkId;
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