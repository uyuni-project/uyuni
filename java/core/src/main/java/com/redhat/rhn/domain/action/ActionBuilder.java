/*
 * Copyright (c) 2026 SUSE LLC
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

package com.redhat.rhn.domain.action;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.user.User;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.Optional;

public class ActionBuilder {
    private static final Logger LOG = LogManager.getLogger(ActionBuilder.class);

    private ActionTypeEnum actionTypeEnum;
    private ActionType actionType;
    private User schedulerUser;
    private Org org;
    private String actionName;
    private Date earliest;

    /**
     * Sets the action type
     * @param actionTypeIn the action type
     * @return the builder
     */
    public ActionBuilder ofType(ActionType actionTypeIn) {
        actionTypeEnum = ActionTypeEnum.of(actionTypeIn).orElse(null);
        actionType = actionTypeIn;
        return this;
    }

    /**
     * Sets the action type enum
     * @param actionTypeEnumIn the action type
     * @return the builder
     */
    public ActionBuilder ofType(ActionTypeEnum actionTypeEnumIn) {
        actionTypeEnum = actionTypeEnumIn;
        actionType = null;
        return this;
    }

    /**
     * Sets the action user
     * @param schedulerUserIn the action user
     * @return the builder
     */
    public ActionBuilder withSchedulerUser(User schedulerUserIn) {
        schedulerUser = schedulerUserIn;
        return this;
    }

    /**
     * Sets the action org
     * @param orgIn the action org
     * @return the builder
     */
    public ActionBuilder withOrg(Org orgIn) {
        org = orgIn;
        return this;
    }

    /**
     * Sets the action name
     * @param actionNameIn the action name
     * @return the builder
     */
    public ActionBuilder withName(String actionNameIn) {
        actionName = actionNameIn;
        return this;
    }

    /**
     * Sets the action earliest time
     * @param earliestIn the action earliest time
     * @return the builder
     */
    public ActionBuilder withEarliest(Date earliestIn) {
        earliest = earliestIn;
        return this;
    }

    /**
     * Builds the action object
     * @return the action object
     */
    public Action build() {
        Action action;
        ActionType buildActionType;
        if (null != actionTypeEnum) {
            try {
                action = actionTypeEnum.createAction();
                buildActionType = (null != actionType) ? actionType :
                        ActionFactory.lookupActionTypeByLabel(actionTypeEnum.getLabel());
                action.setActionType(buildActionType);
            }
            catch (ReflectiveOperationException eIn) {
                LOG.error("Error while creating action of type {}", actionTypeEnum.getLabel(), eIn);
                throw new RuntimeException(eIn);
            }
        }
        else {
            action = new Action();
            buildActionType = null;
        }

        action.setCreated(new Date());
        action.setModified(new Date());
        action.setEarliestAction(Optional.ofNullable(earliest).orElse(new Date()));

        Optional.ofNullable(schedulerUser).ifPresent(action::setSchedulerUser);

        Optional.ofNullable(org)
                .or(() -> Optional.ofNullable(schedulerUser).map(User::getOrg))
                .ifPresent(action::setOrg);

        Optional.ofNullable(actionName)
                .or(() -> Optional.ofNullable(buildActionType).map(ActionType::getName))
                .ifPresent(action::setName);

        //in perl (modules/rhn/RHN/DB/Scheduler.pm) version is given a 2. So that's what I did.
        action.setVersion(2L);
        action.setArchived(0L); //not archived

        return action;
    }
}
