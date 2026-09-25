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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.taskomatic.task;

import com.suse.manager.errata.advisorymap.ErrataAdvisoryMapManager;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.IOException;

/**
 * Update SUSE errata advisory map to retrieve announcement ids and advisor URLs
 * */
public class ErrataAdvisoryMapSync extends RhnJavaJob {

    /**
     * default constructor
     */
    public ErrataAdvisoryMapSync() {
        this(new ErrataAdvisoryMapManager());
    }

    /**
     * constructor
     *
     * @param advisoryMapManagerIn an instance of ErrataAdvisoryMapManager.
     */
    public ErrataAdvisoryMapSync(ErrataAdvisoryMapManager advisoryMapManagerIn) {
        this.advisoryMapManager = advisoryMapManagerIn;
    }

    private final ErrataAdvisoryMapManager advisoryMapManager;


    @Override
    public String getConfigNamespace() {
        return "errata_advisory_map_sync";
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Updates SUSE errata advisory map");

        try {
            advisoryMapManager.syncErrataAdvisoryMap();
        }
        catch (IOException eIn) {
            log.info("Error while updating SUSE errata advisory map", eIn);
        }

        log.info("Done Updates SUSE errata advisory map");
    }
}
