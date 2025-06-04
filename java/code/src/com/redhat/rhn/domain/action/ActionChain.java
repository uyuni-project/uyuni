/*
 * Copyright (c) 2014 SUSE LLC
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

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.legacy.UserImpl;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Type;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * POJO for a rhnActionChain row.
 * @author Silvio Moioli {@literal <smoioli@suse.de>}
 */
@Entity
@Table(name = "rhnActionChain")
public class ActionChain extends BaseDomainHelper {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "actionchain_seq")
    @SequenceGenerator(name = "actionchain_seq", sequenceName = "rhn_actionchain_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "dispatched", nullable = false)
    @Type(type = "yes_no")
    private boolean dispatched;


    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserImpl user;

    @OneToMany(mappedBy = "actionChain", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ActionChainEntry> entries;

    /**
     * Default constructor.
     */
    public ActionChain() {
        entries = new HashSet<>();
    }

    /**
     * Gets the id.
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     * @param idIn the new id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * Gets the label.
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label.
     * @param labelIn the new label
     */
    public void setLabel(String labelIn) {
        label = labelIn;
    }

    /**
     * Gets the user.
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user.
     * @param userIn the new user
     */
    public void setUser(User userIn) {
        if (userIn instanceof UserImpl userImpl) {
            user = userImpl;
        }
        else {
            user = null;
        }
    }

    /**
     * @return true if the actionchain was already dispatched.
     */
    public boolean isDispatched() {
        return dispatched;
    }

    /**
     * @return if all actions associated with this action chain are done
     * (either completed or failed)
     */
    public boolean isDone() {
        // check if all Actions has been executed (there must be an entry in ServerActions)
        // and all the ServerActions are Done
        return !getEntries().isEmpty() &&
                getEntries().stream().noneMatch(ace -> ace.getAction().getServerActions().isEmpty()) &&
                getEntries().stream().flatMap(ace -> ace.getAction().getServerActions().stream())
                .allMatch(sa -> sa.getStatus().isDone());
    }

    /**
     * Sets if the actionchain was already dispatched.
     * @param dispatchedIn
     */
    public void setDispatched(boolean dispatchedIn) {
        this.dispatched = dispatchedIn;
    }

    /**
     * Gets the entries.
     *
     * @return the entries
     */
    public Set<ActionChainEntry> getEntries() {
        return entries;
    }

    /**
     * Gets the earliest action date in related actions.
     *
     * @return the entries
     */
    public Date getEarliestAction() {
        return entries.stream().map(ActionChainEntry::getAction)
                .map(Action::getEarliestAction).min(Date::compareTo).get();
    }

    /**
     * Sets the entries.
     *
     * @param entriesIn the new entries
     */
    public void setEntries(Set<ActionChainEntry> entriesIn) {
        entries = entriesIn;
    }

    /**
     * Gets the creation date in localized form
     * @return the localized date
     */
    public String getLocalizedCreated() {
        return LocalizationService.getInstance().formatShortDate(getCreated());
    }

    /**
     * Gets the creation date in localized form
     * @return the localized date
     */
    public String getLocalizedModified() {
        return LocalizationService.getInstance().formatShortDate(getModified());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ActionChain otherActionChain)) {
            return false;
        }
        return new EqualsBuilder()
            .append(getLabel(), otherActionChain.getLabel())
            .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getLabel())
            .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .append("id", getId())
        .append("label", getLabel())
        .toString();
    }
}
