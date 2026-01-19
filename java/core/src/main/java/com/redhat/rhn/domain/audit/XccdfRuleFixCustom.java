package com.redhat.rhn.domain.audit;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.legacy.UserImpl;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

import javax.persistence.*;

/**
 * XccdfRuleFixCustom entity representing org-specific custom remediation for XCCDF rules.
 * Links to the global XccdfRuleFix and stores custom bash/salt remediation scripts.
 */
@Entity
@Table(name = "suseXccdfRuleFixCustom")
public class XccdfRuleFixCustom {
    private Long id;
    private XccdfRuleFix ruleFix;
    private Org org;
    private String customRemediationBash;
    private String customRemediationSalt;
    private Date created;
    private Date modified;
    private User createdBy;
    private User modifiedBy;

    public XccdfRuleFixCustom() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_fix_id", nullable = false)
    public XccdfRuleFix getRuleFix() {
        return ruleFix;
    }

    public void setRuleFix(XccdfRuleFix ruleFix) {
        this.ruleFix = ruleFix;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    public Org getOrg() {
        return org;
    }

    public void setOrg(Org org) {
        this.org = org;
    }

    @Column(name = "custom_remediation_bash")
    public String getCustomRemediationBash() {
        return customRemediationBash;
    }

    public void setCustomRemediationBash(String customRemediationBash) {
        this.customRemediationBash = customRemediationBash;
    }

    @Column(name = "custom_remediation_salt")
    public String getCustomRemediationSalt() {
        return customRemediationSalt;
    }

    public void setCustomRemediationSalt(String customRemediationSalt) {
        this.customRemediationSalt = customRemediationSalt;
    }

    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Column(name = "modified")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    @ManyToOne(targetEntity = UserImpl.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    @ManyToOne(targetEntity = UserImpl.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by")
    public User getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(User modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        XccdfRuleFixCustom castOther = (XccdfRuleFixCustom) other;
        return new EqualsBuilder()
                .append(ruleFix, castOther.ruleFix)
                .append(org, castOther.org)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(ruleFix)
                .append(org)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "XccdfRuleFixCustom{" +
                "id=" + id +
                ", org=" + (org != null ? org.getId() : null) +
                ", ruleFix=" + (ruleFix != null ? ruleFix.getId() : null) +
                '}';
    }
}
