package com.suse.manager.webui.utils.gson;

import java.util.List;

public class MaintenanceWindowJson {

    private Long scheduleId;
    private String scheduleName;
    private String scheduleType;

    private Long calendarId;
    private String calendarName;
    private String calendarUrl;
    private String calendarData;

    List<String> scheduleNames;

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public Long getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(Long calendarId) {
        this.calendarId = calendarId;
    }

    public String getCalendarName() {
        return calendarName;
    }

    public void setCalendarName(String calendarName) {
        this.calendarName = calendarName;
    }

    public String getCalendarData() {
        return calendarData;
    }

    public void setCalendarData(String calendarData) {
        this.calendarData = calendarData;
    }

    public String getCalendarUrl() {
        return calendarUrl;
    }

    public void setCalendarUrl(String calendarUrl) {
        this.calendarUrl = calendarUrl;
    }

    public List<String> getScheduleNames() {
        return scheduleNames;
    }

    public void setScheduleNames(List<String> scheduleNames) {
        this.scheduleNames = scheduleNames;
    }
}
