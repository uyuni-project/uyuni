/*
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.model.maintenance;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.org.Org;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * MaintenanceSchedule - store maintenance schedule objects
 */
@Entity
@Table(name = "suseMaintenanceSchedule")
@NamedQueries
({
    @NamedQuery(name = "MaintenanceSchedule.lookupByUserAndName",
        query = "from com.suse.manager.model.maintenance.MaintenanceSchedule s " +
                "where s.org.id = :orgId and s.name = :name")
})
public class MaintenanceSchedule extends BaseDomainHelper {
    private Long id;
    private Org org;
    private String name;
    private ScheduleType scheduleType;
    private MaintenanceCalendar calendar;

    public enum ScheduleType {
        SINGLE("single"),
        MULTI("multi");

        private String label;

        ScheduleType(String labelIn) {
            this.label = labelIn;
        }

        /**
         * Gets the label.
         *
         * @return label
         */
        public String getLabel() {
            return label;
        }

        /**
         * Looks up ScheduleType by label
         *
         * @param label the label
         * @throws java.lang.IllegalArgumentException if no matching schedule type is found
         * @return the matching schedule type
         */
        public static ScheduleType lookupByLabel(String label) {
            for (ScheduleType value : values()) {
                if (value.label.equals(label)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unsupported label: " + label);
        }
    }

    /**
     * @return the maintenance schedule id
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mtsched_seq")
    @SequenceGenerator(name = "mtsched_seq", sequenceName = "suse_mtsched_id_seq", allocationSize = 1)
    public Long getId() {
        return id;
    }

    /**
     * @return the organization
     */
    @ManyToOne()
    @JoinColumn(name = "org_id", nullable = false)
    public Org getOrg() {
        return org;
    }

    /**
     * @return the schedule name
     */
    @Column(name = "name")
    public String getName() {
        return name;
    }

    /**
     * @return the schedule type
     */
    @Column(name = "sched_type")
    @Enumerated(EnumType.STRING)
    public ScheduleType getScheduleType() {
        return scheduleType;
    }

    /**
     * @return the calendar
     */
    @ManyToOne()
    @JoinColumn(name = "ical_id", nullable = true)
    protected MaintenanceCalendar getCalendar() {
        return calendar;
    }

    /**
     * @return the calendar as optional
     */
    @Transient
    public Optional<MaintenanceCalendar> getCalendarOpt() {
        return Optional.ofNullable(calendar);
    }

    /**
     * Set the id
     * @param idIn the id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Set the org
     * @param orgIn the org
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }

    /**
     * Set the Schedule Name
     * @param nameIn the schedule name
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * Set the Schedule Type
     * @param scheduleTypeIn the schedule type
     */
    public void setScheduleType(ScheduleType scheduleTypeIn) {
        this.scheduleType = scheduleTypeIn;
    }

    /**
     * Set the Calendar
     * @param calendarIn the calendar
     */
    public void setCalendar(MaintenanceCalendar calendarIn) {
        this.calendar = calendarIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MaintenanceSchedule that = (MaintenanceSchedule) o;

        return new EqualsBuilder()
                .append(org, that.org)
                .append(name, that.name)
                .append(scheduleType, that.scheduleType)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(org)
                .append(name)
                .append(scheduleType)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("org", org)
                .append("name", name)
                .append("type", scheduleType)
                .append("ical", calendar)
                .toString();
    }
}
