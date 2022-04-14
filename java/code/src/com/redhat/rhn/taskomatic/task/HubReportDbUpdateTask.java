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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class HubReportDbUpdateTask extends RhnQueueJob {

    private Logger log = LogManager.getLogger(getClass());

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected Class getDriverClass() {
        return HubReportDbUpdateDriver.class;
    }

    @Override
    protected String getQueueName() {
        return "report_db_hub_update";
    }

    @Override
    public String getConfigNamespace() {
        return "report_db_hub_update";
    }
}
