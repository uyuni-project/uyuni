/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.domain.action.script;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFormatter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;

/**
 * ScriptAction
 */
@Entity
@DiscriminatorValue("-30")
public class ScriptAction extends Action {

    @OneToOne(mappedBy = "parentAction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ScriptActionDetails scriptActionDetails;

    /**
     * @return Returns the scriptActionDetails.
     */
    public ScriptActionDetails getScriptActionDetails() {
        return scriptActionDetails;
    }
    /**
     * @param scriptActionDetailsIn The scriptActionDetails to set.
     */
    public void setScriptActionDetails(ScriptActionDetails scriptActionDetailsIn) {
        if (scriptActionDetailsIn != null) {
            scriptActionDetailsIn.setParentAction(this);
        }
        scriptActionDetails = scriptActionDetailsIn;
    }


    /**
     * Get the Formatter for this class but in this case we use
     * ScriptActionFormatter.
     *
     * {@inheritDoc}
     */
    @Override
    public ActionFormatter getFormatter() {
        if (formatter == null) {
            formatter = new ScriptActionFormatter(this);
        }
        return formatter;
    }

}
