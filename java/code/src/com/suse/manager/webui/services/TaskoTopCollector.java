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
import com.redhat.rhn.taskomatic.TaskoRun;

import com.suse.manager.webui.utils.TaskoTopJob;

import java.util.List;
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
                .map(t -> {
                    TaskoRun r = TaskoFactory.lookupRunById(t.getId());
                    return new TaskoTopJob(
                        r.getId(),
                        r.getTemplate().getTask().getName(),
                        r.getStartTime(),
                        r.getEndTime(),
                        TaskoFactory.lookupScheduleById(r.getScheduleId()).getData());
                })
                .sorted((j1, j2) -> -j1.getStartTime().compareTo(j2.getStartTime()))
                .collect(toList());
        return jobs;
    }
}
