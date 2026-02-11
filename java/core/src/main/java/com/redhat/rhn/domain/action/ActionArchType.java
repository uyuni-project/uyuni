/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.action;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * ActionType
 */
@Entity
@Table(name = "rhnArchTypeActions")
@Immutable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@IdClass(ActionArchTypeId.class)
public class ActionArchType extends BaseDomainHelper implements Serializable {

    @Id
    @Column(name = "arch_type_id")
    private Long archTypeId;
    @Id
    @Column(name = "action_style")
    private String actionStyle;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "action_type_id", updatable = false)
    private ActionType actionType;

    /**
     * Getter for archTypeId
     * @return Long to get
    */
    public Long getArchTypeId() {
        return this.archTypeId;
    }

    /**
     * Setter for archTypeId
     * @param archTypeIdIn to set
    */
    public void setArchTypeId(Long archTypeIdIn) {
        this.archTypeId = archTypeIdIn;
    }

    /**
     * Getter for actionStyle
     * @return String to get
    */
    public String getActionStyle() {
        return this.actionStyle;
    }

    /**
     * Setter for actionStyle
     * @param actionStyleIn to set
    */
    public void setActionStyle(String actionStyleIn) {
        this.actionStyle = actionStyleIn;
    }

    /**
     * @return Returns the actionType.
     */
    public ActionType getActionType() {
        return actionType;
    }
    /**
     * @param actionTypeIn The actionType to set.
     */
    public void setActionType(ActionType actionTypeIn) {
        this.actionType = actionTypeIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ActionArchType castOther)) {
            return false;
        }
        return new EqualsBuilder().append(archTypeId, castOther.archTypeId).append(
                actionStyle, castOther.actionStyle).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(archTypeId).append(actionStyle)
                .toHashCode();
    }

}
