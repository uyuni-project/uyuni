package com.suse.manager.webui.utils.gson;


import java.time.LocalDateTime;

/**
 * DTO for SCAP policy scan history entries
 */
public class ScapPolicyScanHistory {
    private Long xid;              // Test result ID
    private Long sid;              // Server ID
    private String serverName;
    private LocalDateTime completed;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String profile;
    private Long scapActionId;
    
    // Rule result counts (from SQL aggregation)
    private Long pass = 0L;
    private Long fail = 0L;
    private Long other = 0L;
    
    /**
     * No-argument constructor required by database query framework
     */
    public ScapPolicyScanHistory() {
        // No-op constructor for framework
    }
    
    /**
     * Constructor for SQL result mapping
     * @param xid test result ID
     * @param sid server ID
     * @param serverName server name
     * @param completed completion timestamp
     * @param startTime scan start time
     * @param endTime scan end time
     * @param profile XCCDF profile identifier
     * @param scapActionId SCAP action ID
     */
    public ScapPolicyScanHistory(Long xid, Long sid, String serverName,
                                LocalDateTime completed, LocalDateTime startTime,
                                LocalDateTime endTime, String profile, Long scapActionId) {
        this.xid = xid;
        this.sid = sid;
        this.serverName = serverName;
        this.completed = completed;
        this.startTime = startTime;
        this.endTime = endTime;
        this.profile = profile;
        this.scapActionId = scapActionId;
    }
    
    /**
     * @return test result ID
     */
    public Long getXid() {
        return xid;
    }
    
    /**
     * @return server ID
     */
    public Long getSid() {
        return sid;
    }
    
    /**
     * @return server name
     */
    public String getServerName() {
        return serverName;
    }
    
    /**
     * @return completion timestamp
     */
    public LocalDateTime getCompleted() {
        return completed;
    }
    
    /**
     * @return scan start time
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    /**
     * @return scan end time
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    /**
     * @return XCCDF profile identifier
     */
    public String getProfile() {
        return profile;
    }
    
    /**
     * @return SCAP action ID
     */
    public Long getScapActionId() {
        return scapActionId;
    }
    
    /**
     * Set pass count
     * @param pass pass count
     */
    public void setPass(Long pass) {
        this.pass = pass;
    }
    
    /**
     * Set fail count
     * @param fail fail count
     */
    public void setFail(Long fail) {
        this.fail = fail;
    }
    
    /**
     * Set other count
     * @param other other count
     */
    public void setOther(Long other) {
        this.other = other;
    }
    
    /**
     * Set test result ID
     * @param xid test result ID
     */
    public void setXid(Long xid) {
        this.xid = xid;
    }
    
    /**
     * Set server ID
     * @param sid server ID
     */
    public void setSid(Long sid) {
        this.sid = sid;
    }
    
    /**
     * Set server name
     * @param serverName server name
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    
    /**
     * Set completion timestamp
     * @param completed completion timestamp
     */
    public void setCompleted(LocalDateTime completed) {
        this.completed = completed;
    }
    
    /**
     * Set completion timestamp from Timestamp (for database framework)
     * @param timestamp completion timestamp
     */
    public void setCompleted(java.sql.Timestamp timestamp) {
        this.completed = timestamp != null ? timestamp.toLocalDateTime() : null;
    }
    
    /**
     * Set scan start time
     * @param startTime scan start time
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    /**
     * Set scan start time from Timestamp (for database framework)
     * @param timestamp scan start time
     */
    public void setStartTime(java.sql.Timestamp timestamp) {
        this.startTime = timestamp != null ? timestamp.toLocalDateTime() : null;
    }
    
    /**
     * Set scan end time
     * @param endTime scan end time
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    /**
     * Set scan end time from Timestamp (for database framework)
     * @param timestamp scan end time
     */
    public void setEndTime(java.sql.Timestamp timestamp) {
        this.endTime = timestamp != null ? timestamp.toLocalDateTime() : null;
    }
    
    /**
     * Set XCCDF profile identifier
     * @param profile XCCDF profile identifier
     */
    public void setProfile(String profile) {
        this.profile = profile;
    }
    
    /**
     * Set SCAP action ID
     * @param scapActionId SCAP action ID
     */
    public void setScapActionId(Long scapActionId) {
        this.scapActionId = scapActionId;
    }
}
