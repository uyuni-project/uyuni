/*
 * Copyright (c) 2013 SUSE LLC
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

import com.redhat.rhn.taskomatic.task.sshpush.SSHPushDriver;

/**
 * Call rhn_check on relevant systems via SSH using remote port forwarding.
 */
public class SSHPush extends RhnQueueJob<SSHPushDriver> {

    public static final String QUEUE_NAME = "ssh_push";

    @Override
    public String getConfigNamespace() {
        return "sshpush";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<SSHPushDriver> getDriverClass() {
        return SSHPushDriver.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getQueueName() {
        return QUEUE_NAME;
    }
}
