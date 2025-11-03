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
package com.redhat.rhn.domain.user;

import com.redhat.rhn.domain.user.legacy.UserImpl;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * StateChange
 */
@Entity
@Table(name = "rhnWebContactChangeLog")
public class StateChange implements Comparable<StateChange>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RHN_WCON_CHLOG_SEQ")
    @SequenceGenerator(name = "RHN_WCON_CHLOG_SEQ", sequenceName = "RHN_WCON_DISABLED_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "date_completed")
    private Date date = new Date();

    @ManyToOne(targetEntity = UserImpl.class)
    @JoinColumn(name = "web_contact_id", nullable = false)
    private User user;

    @ManyToOne(targetEntity = UserImpl.class)
    @JoinColumn(name = "web_contact_from_id", nullable = false)
    private User changedBy;

    @ManyToOne
    @JoinColumn(name = "change_state_id", nullable = false)
    private State state;


    /**
     * @return Returns the date.
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param d The date to set.
     */
    public void setDate(Date d) {
        this.date = d;
    }

    /**
     * @return Returns the changedBy.
     */
    public User getChangedBy() {
        return changedBy;
    }

    /**
     * @param d The changedBy to set.
     */
    public void setChangedBy(User d) {
        this.changedBy = d;
    }

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param i The id to set.
     */
    public void setId(Long i) {
        this.id = i;
    }

    /**
     * @return Returns the state.
     */
    public State getState() {
        return state;
    }

    /**
     * @param s The state to set.
     */
    public void setState(State s) {
        this.state = s;
    }

    /**
     * @return Returns the user.
     */
    public User getUser() {
        return user;
    }

    /**
     * @param u The user to set.
     */
    public void setUser(User u) {
        this.user = u;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StateChange that)) {
            return false;
        }
        EqualsBuilder builder = new EqualsBuilder();

        builder.append(this.getId(), that.getId());
        builder.append(this.getDate(), that.getDate());
        builder.append(this.getState(), that.getState());
        builder.append(this.getChangedBy(), that.getChangedBy());

        builder.append(this.getUser(), that.getUser());

        return builder.isEquals();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(this.getId());
        builder.append(this.getDate());
        builder.append(this.getState());
        builder.append(this.getUser());
        builder.append(this.getChangedBy());
        return builder.toHashCode();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(StateChange rhs) {
        CompareToBuilder builder = new CompareToBuilder();
        builder.append(getDate(), rhs.getDate());
        builder.append(getId(), rhs.getId());
        builder.append(this.getState(), rhs.getState());
        builder.append(this.getUser(), rhs.getUser());
        builder.append(this.getChangedBy(), rhs.getChangedBy());
        return builder.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
