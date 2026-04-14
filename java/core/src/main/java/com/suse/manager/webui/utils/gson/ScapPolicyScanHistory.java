/*
 * Copyright (c) 2025 SUSE LLC
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
        // No-op constructor
    }

    /**
     * Constructor for SQL result mapping
     * @param xidIn test result ID
     * @param sidIn server ID
     * @param serverNameIn server name
     * @param completedIn completion timestamp
     * @param startTimeIn scan start time
     * @param endTimeIn scan end time
     * @param profileIn XCCDF profile identifier
     * @param scapActionIdIn SCAP action ID
     */
    public ScapPolicyScanHistory(Long xidIn, Long sidIn, String serverNameIn,
                                LocalDateTime completedIn, LocalDateTime startTimeIn,
                                LocalDateTime endTimeIn, String profileIn, Long scapActionIdIn) {
        this.xid = xidIn;
        this.sid = sidIn;
        this.serverName = serverNameIn;
        this.completed = completedIn;
        this.startTime = startTimeIn;
        this.endTime = endTimeIn;
        this.profile = profileIn;
        this.scapActionId = scapActionIdIn;
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
     * @param passIn pass count
     */
    public void setPass(Long passIn) {
        this.pass = passIn;
    }

    /**
     * @return pass count
     */
    public Long getPass() {
        return pass;
    }

    /**
     * Set fail count
     * @param failIn fail count
     */
    public void setFail(Long failIn) {
        this.fail = failIn;
    }

    /**
     * @return fail count
     */
    public Long getFail() {
        return fail;
    }

    /**
     * Set other count
     * @param otherIn other count
     */
    public void setOther(Long otherIn) {
        this.other = otherIn;
    }

    /**
     * @return other count
     */
    public Long getOther() {
        return other;
    }

    /**
     * Set test result ID
     * @param xidIn test result ID
     */
    public void setXid(Long xidIn) {
        this.xid = xidIn;
    }

    /**
     * Set server ID
     * @param sidIn server ID
     */
    public void setSid(Long sidIn) {
        this.sid = sidIn;
    }

    /**
     * Set server name
     * @param serverNameIn server name
     */
    public void setServerName(String serverNameIn) {
        this.serverName = serverNameIn;
    }

    /**
     * Set completion timestamp
     * @param completedIn completion timestamp
     */
    public void setCompleted(LocalDateTime completedIn) {
        this.completed = completedIn;
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
     * @param startTimeIn scan start time
     */
    public void setStartTime(LocalDateTime startTimeIn) {
        this.startTime = startTimeIn;
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
     * @param endTimeIn scan end time
     */
    public void setEndTime(LocalDateTime endTimeIn) {
        this.endTime = endTimeIn;
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
     * @param profileIn XCCDF profile identifier
     */
    public void setProfile(String profileIn) {
        this.profile = profileIn;
    }

    /**
     * Set SCAP action ID
     * @param scapActionIdIn SCAP action ID
     */
    public void setScapActionId(Long scapActionIdIn) {
        this.scapActionId = scapActionIdIn;
    }
}
