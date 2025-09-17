/*
 * Copyright (c) 2023 SUSE LLC
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

package com.redhat.rhn.domain.recurringactions.state;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;

import java.util.Optional;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * Recurring State configuration for internal state implementation
 */

@Entity
@DiscriminatorValue("STATE")
public class RecurringInternalState extends RecurringStateConfig {

    private InternalState internalState;

    /**
     * Standard constructor
     */
    public RecurringInternalState() {
    }

    /**
     * Constructor
     *
     * @param internalStateIn the internal state
     * @param positionIn the position
     */
    public RecurringInternalState(InternalState internalStateIn, Long positionIn) {
        super(positionIn);
        this.internalState = internalStateIn;
    }

    /**
     * Constructor
     *
     * @param stateNameIn the name of the internal state
     * @param positionIn the position
     */
    public RecurringInternalState(String stateNameIn, Long positionIn) {
        super(positionIn);
        Optional<InternalState> state = RecurringActionFactory.lookupInternalStateByName(stateNameIn);
        if (state.isPresent()) {
            this.internalState = state.get();
        }
        else {
            throw new LookupException("State with name: " + stateNameIn + " does not exist!");
        }
    }

    @Override
    @Transient
    public String getStateName() {
        return internalState.getName();
    }

    /**
     * Gets the Internal State
     *
     * @return the Internal State
     */
    @ManyToOne(targetEntity = InternalState.class)
    @JoinColumn(name = "state_id")
    public InternalState getInternalState() {
        return internalState;
    }

    /**
     * Sets the Internal State
     *
     * @param internalStateIn the Internal State
     */
    public void setInternalState(InternalState internalStateIn) {
        this.internalState = internalStateIn;
    }
}
