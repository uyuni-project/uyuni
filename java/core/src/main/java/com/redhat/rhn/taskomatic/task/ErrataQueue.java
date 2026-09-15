/*
 * Copyright (c) 2009--2011 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.taskomatic.task.errata.ErrataQueueDriver;

/**
 * Manages the pending errata queue
 *
 */
public class ErrataQueue extends RhnQueueJob<ErrataQueueDriver> {

    public static final String DISPLAY_NAME = "errata_queue";

    @Override
    public String getConfigNamespace() {
        return "errata_queue";
    }

    @Override
    protected String getQueueName() {
        return DISPLAY_NAME;
    }

    @Override
    protected Class<ErrataQueueDriver> getDriverClass() {
        return ErrataQueueDriver.class;
    }
}
