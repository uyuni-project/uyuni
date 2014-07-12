/**
 * Copyright (c) 2012--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.systems.sdc;

import java.util.List;

import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.system.SystemManager;

/**
 * SingleSnapshotTagsAction
 * @version $Rev$
 */
public class SingleSnapshotTagsAction extends SnapshotBaseAction {

    /** {@inheritDoc} */
    public List getResult(RequestContext context) {
        Long sid = context.getRequiredParam(RequestContext.SID);
        Long ssid = context.getRequiredParam(SNAPSHOT_ID);
        return SystemManager.snapshotTagsForSystemAndSnapshot(sid, ssid, null);
    }

}
