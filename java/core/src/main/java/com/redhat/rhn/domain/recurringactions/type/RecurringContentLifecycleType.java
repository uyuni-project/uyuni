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
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.redhat.rhn.domain.recurringactions.type;

import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentProject;

import java.util.Optional;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Recurring Action type for CLM build and promote implementations.
 */
@Entity
@Table(name = "suseRecurringContentAction")
public class RecurringContentLifecycleType extends RecurringActionType {

    private ContentProject project;
    private ContentEnvironment environment;
    private ActionType actionType;

    /**
     * Standard constructor
     */
    public RecurringContentLifecycleType() {
    }

    /**
     * Constructor specifying action type
     *
     * @param actionTypeIn the action type
     */
    public RecurringContentLifecycleType(ActionType actionTypeIn) {
        super();
        this.actionType = actionTypeIn;
    }

    /**
     * Constructor for CLM build
     *
     * @param projectIn the content project
     */
    public RecurringContentLifecycleType(ContentProject projectIn) {
        super();
        this.actionType = ActionType.CLM_BUILD;
        this.project = projectIn;
    }

    /**
     * Constructor for CLM promote
     *
     * @param projectIn the content project
     * @param environmentIn the content environment
     */
    public RecurringContentLifecycleType(ContentProject projectIn, ContentEnvironment environmentIn) {
        super();
        this.actionType = ActionType.CLM_PROMOTE;
        this.project = projectIn;
        this.environment = environmentIn;
    }

    @Override
    @Transient
    public ActionType getActionType() {
        if (getRecurringAction() != null && getRecurringAction().getActionType() != null) {
            return getRecurringAction().getActionType();
        }
        if (this.actionType != null) {
            return this.actionType;
        }
        return environment == null ? ActionType.CLM_BUILD : ActionType.CLM_PROMOTE;
    }

    /**
     * Sets the action type
     *
     * @param actionTypeIn the action type
     */
    public void setActionType(ActionType actionTypeIn) {
        this.actionType = actionTypeIn;
    }

    /**
     * Gets the content project
     *
     * @return the content project
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    public ContentProject getProject() {
        return this.project;
    }

    /**
     * Sets the content project
     *
     * @param projectIn the content project
     */
    public void setProject(ContentProject projectIn) {
        this.project = projectIn;
    }

    /**
     * Gets the content environment (null for build actions)
     *
     * @return the content environment
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "env_id")
    public ContentEnvironment getEnvironment() {
        return this.environment;
    }

    /**
     * Sets the content environment
     *
     * @param environmentIn the content environment
     */
    public void setEnvironment(ContentEnvironment environmentIn) {
        this.environment = environmentIn;
    }

    /**
     * @return optional of the content environment
     */
    @Transient
    public Optional<ContentEnvironment> getEnvironmentOpt() {
        return Optional.ofNullable(environment);
    }
}
