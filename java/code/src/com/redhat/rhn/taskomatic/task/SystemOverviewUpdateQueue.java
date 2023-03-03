/*
 * Copyright (c) 2022 SUSE LLC
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

import com.redhat.rhn.taskomatic.task.systems.SystemsOverviewUpdateDriver;

/**
 * Manages the pending system overview updates queue
 *
 */
public class SystemOverviewUpdateQueue extends RhnQueueJob<SystemsOverviewUpdateDriver> {

    @Override
    public String getConfigNamespace() {
        return "system_overview_update_queue";
    }

    @Override
    protected String getQueueName() {
        return "system_overview_update_queue";
    }

    @Override
    protected Class<SystemsOverviewUpdateDriver> getDriverClass() {
        return SystemsOverviewUpdateDriver.class;
    }
}
