/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.schedule;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ActionedSystem;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;

/**
 * InProgressSystemsSetupAction
 */
public class InProgressSystemsSetupAction extends ActionSystemsSetupAction {

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataResult<ActionedSystem> getDataResult(User user, Action action, PageControl pc) {
        return ActionManager.inProgressSystems(user, action, pc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RhnSetDecl getSetDecl() {
        return RhnSetDecl.ACTIONS_UNSCHEDULE;
    }

}
