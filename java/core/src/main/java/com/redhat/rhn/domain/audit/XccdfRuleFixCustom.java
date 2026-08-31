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

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.legacy.UserImpl;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * XccdfRuleFixCustom entity representing org-specific custom remediation for XCCDF rules.
 * Links to the global XccdfRuleFix and stores custom bash/salt remediation scripts.
 */
@Entity
@Table(name = "suseXccdfRuleFixCustom")
public class XccdfRuleFixCustom {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_fix_id", nullable = false)
    private XccdfRuleFix ruleFix;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Org org;

    @Column(name = "custom_remediation_bash")
    private String customRemediationBash;

    @Column(name = "custom_remediation_salt")
    private String customRemediationSalt;

    @Column(name = "created")
    private Date created;

    @Column(name = "modified")
    private Date modified;

    @ManyToOne(targetEntity = UserImpl.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(targetEntity = UserImpl.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by")
    private User modifiedBy;

    /**
     * Default constructor for JPA.
     */
    public XccdfRuleFixCustom() {
        // Default constructor for JPA.
    }

    /**
     * @return the id
     */
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
     * @return the ruleFix
     */
    public XccdfRuleFix getRuleFix() {
        return ruleFix;
    }

    /**
     * @param ruleFixIn the ruleFix to set
     */
    public void setRuleFix(XccdfRuleFix ruleFixIn) {
        this.ruleFix = ruleFixIn;
    }

    /**
     * @return the org
     */
    public Org getOrg() {
        return org;
    }

    /**
     * @param orgIn the org to set
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }

    /**
     * @return the customRemediationBash
     */
    public String getCustomRemediationBash() {
        return customRemediationBash;
    }

    /**
     * @param customRemediationBashIn the customRemediationBash to set
     */
    public void setCustomRemediationBash(String customRemediationBashIn) {
        this.customRemediationBash = customRemediationBashIn;
    }

    /**
     * @return the customRemediationSalt
     */
    public String getCustomRemediationSalt() {
        return customRemediationSalt;
    }

    /**
     * @param customRemediationSaltIn the customRemediationSalt to set
     */
    public void setCustomRemediationSalt(String customRemediationSaltIn) {
        this.customRemediationSalt = customRemediationSaltIn;
    }

    /**
     * @return the created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @param createdIn the created to set
     */
    public void setCreated(Date createdIn) {
        this.created = createdIn;
    }

    /**
     * @return the modified
     */
    public Date getModified() {
        return modified;
    }

    /**
     * @param modifiedIn the modified to set
     */
    public void setModified(Date modifiedIn) {
        this.modified = modifiedIn;
    }

    /**
     * @return the createdBy
     */
    public User getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdByIn the createdBy to set
     */
    public void setCreatedBy(User createdByIn) {
        this.createdBy = createdByIn;
    }

    /**
     * @return the modifiedBy
     */
    public User getModifiedBy() {
        return modifiedBy;
    }

    /**
     * @param modifiedByIn the modifiedBy to set
     */
    public void setModifiedBy(User modifiedByIn) {
        this.modifiedBy = modifiedByIn;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(ruleFix)
                .append(org)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "XccdfRuleFixCustom{" +
                "id=" + id +
                ", org=" + (org != null ? org.getId() : null) +
                ", ruleFix=" + (ruleFix != null ? ruleFix.getId() : null) +
                '}';
    }
}
