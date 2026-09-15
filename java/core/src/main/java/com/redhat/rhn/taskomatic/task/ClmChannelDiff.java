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
 */
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.manager.contentmgmt.ContentManager;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

public class ClmChannelDiff extends RhnJavaJob {

    @Override
    public String getConfigNamespace() {
        return "clm_channel_diff";
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContextIn) throws JobExecutionException {
        ContentManager cm = new ContentManager();
        List<Org> orgs = OrgFactory.lookupAllOrgs();
        for (Org org : orgs) {
            List<ContentProject> clmProjects = ContentProjectFactory.listProjects(org);
            for (ContentProject project : clmProjects) {
                cm.diffProject(project);
            }
        }
    }
}
