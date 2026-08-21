/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.server;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Transactional-system-specific data for Salt minions running on transactional
 * distributions i.e. openSUSE MicroOS, openSUSE Leap Micro, and SUSE Linux Micro.
 */
@Entity
@Table(name = "suseMinionTransactionalInfo")
public class MinionTransactionalInfo implements Serializable {

    @Id
    @Column(name = "minion_server_id")
    private Long minionServerId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "minion_server_id")
    private MinionServer minionServer;

    @Column(name = "active_snapshot")
    private Long activeSnapshot;

    @Column(name = "default_snapshot")
    private Long defaultSnapshot;

    // JSON array of snapshot detail objects.
    // Each entry: {"number": N, "active": bool, "default": bool, "description": "...", "date": "..."}
    @Column(name = "snapshot_details")
    private String snapshotDetails;

    @Column(name = "snapshot_updated")
    private Date snapshotUpdated;

    /**
     * Default constructor required by Hibernate.
     */
    public MinionTransactionalInfo() {
    }

    /**
     * @param minionServerIn the owning minion server
     */
    public MinionTransactionalInfo(MinionServer minionServerIn) {
        this.minionServer = minionServerIn;
    }

    /**
     * @return the number of the currently active (booted) Btrfs snapshot, or null
     */
    public Long getActiveSnapshot() {
        return activeSnapshot;
    }

    /**
     * @param activeSnapshotIn the active snapshot number to set
     */
    public void setActiveSnapshot(Long activeSnapshotIn) {
        this.activeSnapshot = activeSnapshotIn;
    }

    /**
     * @return the number of the default (next-boot) Btrfs snapshot, or null
     */
    public Long getDefaultSnapshot() {
        return defaultSnapshot;
    }

    /**
     * @param defaultSnapshotIn the default snapshot number to set
     */
    public void setDefaultSnapshot(Long defaultSnapshotIn) {
        this.defaultSnapshot = defaultSnapshotIn;
    }

    /**
     * @return raw JSON string of snapshot details array, or null
     */
    public String getSnapshotDetails() {
        return snapshotDetails;
    }

    /**
     * @param snapshotDetailsIn JSON array string of snapshot detail objects, or null to clear
     */
    public void setSnapshotDetails(String snapshotDetailsIn) {
        this.snapshotDetails = snapshotDetailsIn;
    }

    /**
     * @return when the snapshot information was last updated, or null
     */
    public Date getSnapshotUpdated() {
        return snapshotUpdated;
    }

    /**
     * @param snapshotUpdatedIn when the snapshot information was last updated
     */
    public void setSnapshotUpdated(Date snapshotUpdatedIn) {
        this.snapshotUpdated = snapshotUpdatedIn;
    }

}
