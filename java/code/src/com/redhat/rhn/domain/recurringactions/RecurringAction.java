package com.redhat.rhn.domain.recurringactions;

import com.redhat.rhn.domain.server.MinionServer;

import org.hibernate.annotations.Type;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "suseRecurringAction") // TODO: Drop the suse prefix?
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "target_type")
public abstract class RecurringAction {

    private long id;

    private boolean testMode;

    private boolean active;

    public abstract List<MinionServer> computeMinions();

    public abstract String computeTaskoScheduleName();

    public RecurringAction() { }

    public RecurringAction(boolean testMode, boolean active) {
        this.testMode = testMode;
        this.active = active;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recurring_action_seq")
    @SequenceGenerator(name = "recurring_action_seq", sequenceName = "suse_recurring_action_id_seq", allocationSize = 1)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "test_mode")
    @Type(type = "yes_no")
    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    @Column
    @Type(type = "yes_no")
    public boolean isActive() { // TODO: Set schema type to boolean
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
