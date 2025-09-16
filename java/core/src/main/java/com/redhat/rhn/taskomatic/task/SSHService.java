/*
 * Copyright (c) 2013--2023 SUSE LLC
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

import com.redhat.rhn.taskomatic.task.sshservice.SSHServiceDriver;

/**
 * Provide services for salt ssh clients
 */
public class SSHService extends RhnQueueJob<SSHServiceDriver> {

    public static final String QUEUE_NAME = "ssh_service";

    @Override
    public String getConfigNamespace() {
        return "sshservice";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<SSHServiceDriver> getDriverClass() {
        return SSHServiceDriver.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getQueueName() {
        return QUEUE_NAME;
    }
}
