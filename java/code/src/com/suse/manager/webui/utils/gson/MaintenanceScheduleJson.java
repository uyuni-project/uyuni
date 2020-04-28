package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.domain.org.Org;

import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;

public class MaintenanceScheduleJson {

    private Long id;
    private Org org;
    private String name;
    private MaintenanceSchedule.ScheduleType scheduleType;
    private MaintenanceCalendar calendar;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Org getOrg() {
        return org;
    }

    public void setOrg(Org org) {
        this.org = org;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MaintenanceSchedule.ScheduleType getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(MaintenanceSchedule.ScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }

    public MaintenanceCalendar getCalendar() {
        return calendar;
    }

    public void setCalendar(MaintenanceCalendar calendar) {
        this.calendar = calendar;
    }
}
