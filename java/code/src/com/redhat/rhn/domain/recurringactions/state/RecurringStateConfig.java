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
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import java.io.Serial;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

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
    @GeneratedValue(generator = "recurring_state_config_seq")
    @GenericGenerator(
            name = "recurring_state_config_seq",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "suse_recurring_state_config_id_seq"),
                    @Parameter(name = "increment_size", value = "1")
            })
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
