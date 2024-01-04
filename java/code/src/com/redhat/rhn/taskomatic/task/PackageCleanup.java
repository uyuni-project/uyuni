/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.domain.org.OrgFactory;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Cleans up orphaned packages
 *
 */
public class PackageCleanup extends RhnJavaJob {

    @Override
    public String getConfigNamespace() {
        return "package_cleanup";
    }

    // this task is executed every 10 minutes. A daily job should be executed between
    // these two times.
    private static final LocalTime DAILY_JOB_START = LocalTime.parse("20:49:00");
    private static final LocalTime DAILY_JOB_END = DAILY_JOB_START.plusMinutes(10);

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext ctx) throws JobExecutionException {
        try {
            String pkgDir = Config.get().getString("web.mount_point");

            // Retrieve list of orpahned packages
            List<Row> candidates = findCandidates();

            // Bail if no work to do
            if (candidates == null || candidates.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("No orphaned packages found");
                }
            }
            else if (log.isDebugEnabled()) {
                log.debug("Found {} orphaned packages", candidates.size());
            }

            // Delete them from the filesystem
            for (Row row : candidates) {
                String path = (String) row.get("path");
                if (log.isDebugEnabled()) {
                    log.debug("Deleting package {}", path);
                }
                if (path == null) {
                    continue;
                }
                deletePackage(pkgDir, path);
            }

            // Reset the queue (table)
            resetQueue();

            // Change org for orphan vendor packages that a user can delete them
            changeOrgForOrphanVendorPackages();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    /*
     * Sometimes reposync disassociates vendor packages (org_id = 0) from
     * channels.
     * These orphans are then hard to work with in uyuni (nobody has
     * permissions to view/delete them). We workaround this issue by
     * assigning such packages to the default organization, so that they can
     * be deleted using the existing orphan-deleting procedure.
     *
     * Sometimes packages move from 1 channel to another. If the org change
     * between the reposync of these two channels, the package gets downloaded
     * again and exists twice. To prevent this we want this cleanup running
     * only 1 time per day outside of a full reposync which happens typically
     * during the night.
     */
    private void changeOrgForOrphanVendorPackages() {
        LocalTime now = LocalTime.now();
        if (now.isAfter(DAILY_JOB_START) && now.isBefore(DAILY_JOB_END)) {
            WriteMode update = ModeFactory.getWriteMode(TaskConstants.MODE_NAME,
                    TaskConstants.TASK_QUERY_PKGCLEANUP_ORPHAN_VENDOR_PKG_CHANGE_ORG);
            int updates = update.executeUpdate(Map.of("org_id", OrgFactory.getSatelliteOrg().getId()));
            if (updates > 0) {
                log.info("Found {} orphan vendor packages and moved them to the default organization.", updates);
            }
        }
    }

    private void resetQueue() {
        WriteMode update = ModeFactory.getWriteMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_PKGCLEANUP_RESET_QUEUE);
        update.executeUpdate(Collections.emptyMap());
    }

    private void deletePackage(String pkgDir, String path) {
        File f = new File(pkgDir, path);
        if (f.exists() && f.canWrite() && !f.isDirectory()) {
            f.delete();
            if (log.isDebugEnabled()) {
                log.debug("Deleting {}", f.getAbsoluteFile());
            }

            // Remove parents but only within path, and keep two top directories
            File pathFile = new File(path);
            File parent;
            do {
                pathFile = pathFile.getParentFile();
                if (pathFile == null ||
                        pathFile.getParentFile() == null ||
                            pathFile.getParentFile().getParentFile() == null) {
                    break;
                }
                parent = new File(pkgDir, pathFile.getPath());
            }
            while (parent.delete());
        }
        else {
            log.error("{} not found", f.getAbsoluteFile());
        }
    }

    private List<Row> findCandidates() {
        SelectMode query = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_PKGCLEANUP_FIND_CANDIDATES);
        return query.execute(Collections.emptyMap());
    }
}
