package com.suse.manager.webui.utils.gson;

public class MaintenanceScheduleJson {

    private Long scheduleId;
    private String scheduleName;
    private String scheduleType;

    private Long calendarId;
    private String calendarName;
    private String calendarUrl;
    private String calendarData;
    private boolean calendarAdded;
    private String calendarType;

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

    public boolean isCalendarAdded() {
        return calendarAdded;
    }

    public void setCalendarAdded(boolean calendarAdded) {
        this.calendarAdded = calendarAdded;
    }

    public String getCalendarType() {
        return calendarType;
    }

    public void setCalendarType(String calendarType) {
        this.calendarType = calendarType;
    }
}
