package com.redhat.rhn.domain.recurringactions;

import com.redhat.rhn.domain.server.MinionServer;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("minion")
public class MinionRecurringAction extends RecurringAction {

    private MinionServer minion;

    public MinionRecurringAction() {
    }

    public MinionRecurringAction(boolean testMode, boolean active, MinionServer minion) {
        super(testMode, active);
        this.minion = minion;
    }

    @Override
    public List<MinionServer> computeMinions() {
        return List.of(minion);
    }

    @Override
    public String computeTaskoScheduleName() {
        // TODO: Refactor for minion/group/org
        return "recurring-action-minion-" + minion.getId();
    }

    @ManyToOne
    @JoinColumn(name = "minion_id")
    public MinionServer getMinion() {
        return minion;
    }

    public void setMinion(MinionServer minion) {
        this.minion = minion;
    }
}
