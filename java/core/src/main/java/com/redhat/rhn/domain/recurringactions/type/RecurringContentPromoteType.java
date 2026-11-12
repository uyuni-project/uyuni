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

package com.redhat.rhn.domain.recurringactions.type;

import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentProject;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Recurring Action type for CLM promote implementation
 */
@Entity
@Table(name = "suseRecurringContentPromote")
public class RecurringContentPromoteType extends RecurringActionType {

    private ContentProject project;
    private ContentEnvironment environment;

    /**
     * Standard constructor
     */
    public RecurringContentPromoteType() {
    }

    /**
     * Constructor
     *
     * @param projectIn the content project
     * @param environmentIn the content environment
     */
    public RecurringContentPromoteType(ContentProject projectIn, ContentEnvironment environmentIn) {
        super();
        this.project = projectIn;
        this.environment = environmentIn;
    }

    @Override
    @Transient
    public ActionType getActionType() {
        return ActionType.CLM_PROMOTE;
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
     * Gets the content environment
     *
     * @return the content environment
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "env_id", nullable = false)
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
}
