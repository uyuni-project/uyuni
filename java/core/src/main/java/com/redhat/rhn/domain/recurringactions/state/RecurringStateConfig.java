/*
 * Copyright (c) 2023--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.recurringactions.state;

import com.redhat.rhn.domain.recurringactions.type.RecurringState;

import org.hibernate.annotations.DiscriminatorFormula;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Recurring State Configuration base class
 */

@Entity
@Table(name = "suseRecurringStateConfig")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula("CASE WHEN state_id IS NOT NULL THEN 'STATE' ELSE 'CONFCHAN' END")
public abstract class RecurringStateConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1772543788360679832L;

    private Long id;
    private Long position;
    private RecurringState recurringState;

    /**
     * Standard constructor
     */
    protected RecurringStateConfig() {
    }

    /**
     * Constructor
     *
     * @param positionIn the position
     */
    protected RecurringStateConfig(Long positionIn) {
        this.position = positionIn;
    }

    /**
     * Returns the name of the state
     *
     * @return the state name
     */
    @Transient
    public abstract String getStateName();

    /**
     * Gets the id
     *
     * @return the id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recurring_state_config_seq")
    @SequenceGenerator(name = "recurring_state_config_seq", sequenceName = "suse_recurring_state_config_id_seq",
            allocationSize = 1)
    public Long getId() {
        return id;
    }

    /**
     * Sets the id
     *
     * @param idIn the id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Gets the position
     *
     * @return the position
     */
    @Column(name = "position")
    public Long getPosition() {
        return position;
    }

    /**
     * Sets the position
     *
     * @param positionIn the position
     */
    public void setPosition(Long positionIn) {
        this.position = positionIn;
    }

    /**
     * Gets the Recurring State object
     *
     * @return the Recurring State object
     */
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = RecurringState.class)
    @JoinColumn(name = "rec_id")
    public RecurringState getRecurringState() {
        return recurringState;
    }

    /**
     * Sets the Recurring State object
     *
     * @param recurringStateIn the Recurring State object
     */
    public void setRecurringState(RecurringState recurringStateIn) {
        this.recurringState = recurringStateIn;
    }
}
