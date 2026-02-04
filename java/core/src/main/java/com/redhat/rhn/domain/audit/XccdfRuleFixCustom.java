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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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

    /**
     * Default constructor for JPA.
     */
    public XccdfRuleFixCustom() {
    }

    /**
     * @return the id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_fix_id", nullable = false)
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
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
    @Column(name = "custom_remediation_bash")
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
    @Column(name = "custom_remediation_salt")
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
    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
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
    @Column(name = "modified")
    @Temporal(TemporalType.TIMESTAMP)
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
    @ManyToOne(targetEntity = UserImpl.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
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
    @ManyToOne(targetEntity = UserImpl.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by")
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
