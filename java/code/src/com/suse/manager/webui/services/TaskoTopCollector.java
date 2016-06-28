/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.webui.services;

import com.redhat.rhn.taskomatic.TaskoFactory;

import com.suse.manager.webui.utils.TaskoTopJob;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

/**
 * Collect data for the TaskoTop web UI page
 */
public class TaskoTopCollector {

    /**
     * Gets UI-ready data.
     *
     * @return the data
     */
    public Object getData() {
        List<TaskoTopJob> jobs = TaskoFactory.listUnfinishedRuns().stream()
                .map(t -> TaskoTopJob.generateTaskoTopJobFromTaskoRun(
                        TaskoFactory.lookupRunById(t.getId())))
                .sorted((j1, j2) -> j2.getStartTime().compareTo(j1.getStartTime()))
                .collect(toList());

        Date limitTime = new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5));

        jobs.addAll(TaskoFactory.listRunsNewerThan(limitTime).stream()
                .map(t -> TaskoTopJob.generateTaskoTopJobFromTaskoRun(
                        TaskoFactory.lookupRunById(t.getId())))
                .sorted((j1, j2) -> j2.getStartTime().compareTo(j1.getStartTime()))
                .collect(toList()));

        return jobs;
    }
}
